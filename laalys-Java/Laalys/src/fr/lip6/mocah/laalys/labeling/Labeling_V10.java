package fr.lip6.mocah.laalys.labeling;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.labeling.constants.Labels;
import fr.lip6.mocah.laalys.petrinet.IMarking;
import fr.lip6.mocah.laalys.petrinet.IPathIntersection;
import fr.lip6.mocah.laalys.petrinet.IPathLink;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.constants.ActionSource;
import fr.lip6.mocah.laalys.traces.constants.ActionType;

public class Labeling_V10 implements ILabeling {
	
	/**
	 * looger, on l'utilise afin de pouvoir garder une trace de ce qui a ete
	 * fait par l'application. Utile pour le debug.
	 * Deux fonctions sont a utiliser :
	 * 		logger.log( message ) pour enregistrer ce qu'il c'est passe
	 * 		logger.error( message ) pour enregister un message d'erreur
	 */
	private Logger logger = null;
	
	/**
	 * est ce que l'on veut logger toutes les etpaes de la labellisation
	 */
	public boolean logAll = false;
	
	/**
	 * Réseau de Pétri complet :
	 * C'est le Réseau de Pétri représentant le niveau du jeu, c'est à dire que
	 * chaque action accepté par le jeu correspond à une transition 
	 * immédiatement franchissable (à condition que toutes les transitions
	 * correspondantes aux actions nécessaires pour réaliser cette dernière 
	 * dans le jeu aient été franchie dans le RdP). On peut donc grace à ce RdP
	 * complet connaitre l'état du monde exact qui découle de l'ensemble des 
	 * actions du joueur.
	 */
	private IPetriNet completeRdp = null;
	
	/**
	 * Réseau de Pétri filtré :
	 * c'est le Réseau de Pétri complet dans lequel on n'a gardé que les transitions
	 * (joueur et systeme) qui ont été joués par l'expert dans L'ENSEMBLE des 
	 * solutions qu'il considère comme experte
	 */
	private IPetriNet filteredRdp = null;
	
	/**
	 * Reseau de petri de travail 1
	 * On garanti que le graphe de couverture/accessibilite de ce RdP contient le
	 * marquage MC.
	 */
	private IPetriNet workingRdp1 = null;
	
	/**
	 * Reseau de petri de travail 2
	 * On garanti que le graphe de couverture/accessibilite de ce RdP contient le
	 * marquage M'C.
	 */
	private IPetriNet workingRdp2 = null;
	
	/**
	 * Liste des réseaux de Petri de travail déjà calculés. Evite d'avoir à les recalculer
	 * si nécessaire
	 */
	private ArrayList<IPetriNet> workingRdpList = null;
	 
	/**
	 * ensemble des traces a analyser
	 */
	private ITraces traces = null;
	
	/**
	 * les specificitees des RdP :
	 * 	transitions fin de niveaux
	 * 	transitions systemes
	 */
	private IFeatures _specif = null;
	
	/**
	 * listes de toutes les transitions systemes
	 */
	private ArrayList<String> systemTransitions = new ArrayList<>();
	/**
	 * liste de toutes les transitions fin de niveau
	 */
	private ArrayList<String> expertEndTransitions = new ArrayList<>();
	
	/**
	 * L'idée ici est de stocker que le marquage "mark" (et son équivalent filtré "subMark") a été obtenu à l'aide de l'action "action"
	 */
	private ArrayList<PathState> completeMarkingSeen = null;
	
	/**
	 * marquage complet avant franchissement de la transition adapté au RdP filtré
	 */
	private IMarking MC_subset = null;
	/**
	 * marquage du RdP complet apres franchissement de la transition
	 */
	private IMarking MpC = null;
	/**
	 * marquage complet apres franchissement de la transition adapté au RdP filtré
	 */
	private IMarking MpC_subset = null;
	/**
	 * l'action en cours de labellisation
	 */
	private ITrace currentAction = null;
	/**
	 * la transition correspondant a l'action dans le RdP complet
	 */
	private ITransition currentCompleteTransition = null;
	/**
	 * la transition correspondant a l'action dans le RdP filtre
	 */
	private ITransition currentFilteredTransition = null;
	
	
	public Labeling_V10( Logger logger, boolean logAll ) 
	{
		this.logger = logger;
		this.logAll = logAll;
	}
	
	
	/**
	 * @inheritDoc
	 * @param	traces
	 * @throws Exception 
	 */
	public void label( ITraces traces ) throws Exception
	{
		if (this.logAll) logger.log(Level.INFO, "debut de la labellisation des actions" );
		this.traces = traces;
		this.traces.reset();
		this.reset();
		
		//verification qu'il y a bien un RdP complet
		if ( this.completeRdp == null )
		{
			logger.log(Level.SEVERE, "impossible d'effectuer l'analyse des traces sans reseau complet" );
			// Lever une exception pour indiquer que la labelisation a échouée
			throw new Exception ("Full Petri Net is required");
		}
		//verification qu'il y a bien un RdP filtre
		if ( this.filteredRdp == null )
		{
			logger.log(Level.SEVERE, "impossible d'effectuer l'analyse des traces sans reseau filtree" );
			// Lever une exception pour indiquer que la labelisation a échouée
			throw new Exception ("Filtered Petri Net is required");
		}
		//vertification qu'il y bien au moins une action de fin
		if (this.expertEndTransitions.isEmpty()) {
			logger.log(Level.SEVERE, "impossible d'effectuer l'analyse des traces sans au moins une fin experte définie" );
			// Lever une exception pour indiquer que la labelisation a échouée
			throw new Exception ("At least one expert end is required");
		}
		
		//on lance la labellisation de la trace
		for (ITrace trace : this.traces.getTraces())
		{
			labelAction( trace );
		}
		// terminer l'évaluation des actions manquantes
		analyseTransitionEndStep();
	}
	
	/**
	 * remet les RdP dans leurs etats initiaux 
	 * afin de pouvoir re analyser la/les traces depuis le debut
	 * @throws Exception 
	 */
	public void reset() throws Exception
	{
		this.completeRdp.setCurrentMarkings( this.completeRdp.getInitialMarkings() );
		this.filteredRdp.setCurrentMarkings( this.filteredRdp.getInitialMarkings() );
		this.workingRdp1 = null;
		this.workingRdp2 = null;
		this.workingRdpList = new ArrayList<>();
		completeMarkingSeen = new ArrayList<>();
		completeMarkingSeen.add(new PathState(
				null,
				this.completeRdp.getCurrentMarkings().clone(),
				PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp).clone(),
				this.filteredRdp));
	}
	
	/*
	 * GETTER AND SETTER
	 */
	
	public IPetriNet getCompletePN() 
	{
		return completeRdp;
	}
	
	public void setCompletePN(IPetriNet value) 
	{
		completeRdp = value;
	}
	
	public IPetriNet getFilteredPN() 
	{
		if (this.workingRdp1 != null)
			return this.workingRdp1;
		else
			return this.filteredRdp;
	}
	
	public void setFilteredPN(IPetriNet value) 
	{
		filteredRdp = value;
	}
	
	public IFeatures getFeatures() 
	{
		return _specif;
	}
	
	public void setFeatures(IFeatures value) 
	{
		_specif = value;
		this.systemTransitions = _specif.getSystemTransitions();
		this.expertEndTransitions   = _specif.getEndLevelTransitions();
	}
	
	/**
	 * realise la labellisation de l'action
	 * @param	action
	 * @throws Exception 
	 */
	public void labelAction( ITrace action ) throws Exception
	{
		//on initialise les labels
		action.freeLabels();
		
		// enregistre les marquages courants
		this.MC_subset = PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp);
		if (this.workingRdp1 == null){
			this.workingRdp1 = this.filteredRdp;
			this.workingRdpList.add(this.filteredRdp);
		}
		this.MpC = null;
		this.MpC_subset = null;
		this.workingRdp2 = null;
		
		//recuperation des transitions dans les differents Rdp
		this.currentCompleteTransition = this.completeRdp.getTransitionById( action.getAction() );
		if (currentCompleteTransition == null){
			if ( this.logAll ) logger.log(Level.SEVERE, "impossible d'effectuer l'analyse de l'action \"" + action.getAction() + "\" car aucune équivalence n'est présente dans le réseau de Pétri Complet" );
			return;
		}
		this.currentFilteredTransition = this.filteredRdp.getTransitionById( action.getAction() );
		this.currentAction = action;
		if ( this.logAll ) logger.log(Level.INFO, "debut de la labellisation de l'action : " + this.currentAction.getAction() );
		
		// Lancement de l'analyse de l'action courante.
		// On regarde si la transition est un Try
		if (!isTryAction()) {
			if ( this.logAll ) logger.log(Level.INFO, "L'action n'est pas un Try  => sens(t, MC)");
			analyseTransitionCase1();
		} else {
			if ( this.logAll ) logger.log(Level.INFO, "L'action est un Try => !sens(t, MC)");
			analyseTransitionCase2();
		}
		
		if ( this.logAll ) logger.log(Level.INFO, "l'action \"" +  this.currentAction.getAction() + "\" a ete labellisee \"" +  this.currentAction.getLabels() + "\"\n");
	}
	
	// Cas 1 : cas des actions acceptées par le jeu
	private void analyseTransitionCase1() throws Exception
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1 : cas des actions acceptées par le jeu");
		//on joue l'action dans le rdp complet => generation de M'C
		this.completeRdp.changeStatePetriNet( this.currentCompleteTransition );
		// enregistre le marquage M' du Rdp Complet
		this.MpC = this.completeRdp.getCurrentMarkings().clone();
		// enregistre le marquage M' du Rdp Complet adapte pour le Rdp Filtre
		this.MpC_subset = PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp);
		//si ce n'est pas une transition systeme
		if ( !this.systemTransitions.contains(this.currentAction.getAction()) && !this.currentAction.getOrigin().equals(ActionType.SYSTEM) ){
			if ( this.logAll ) logger.log(Level.INFO, "Action non systeme");
			//on passe dans le cas 1_1 (cas des actions du joueur (non système) acceptées par le jeu)
			analyseTransitionCase1_1();
		}
		else
		{
			if ( this.logAll ) logger.log(Level.INFO, "Action systeme");
			// on joue la transition dans le Rdp de travail si elle est presente dans RdpF et si elle est sensibilisee dans RdpW1
			//si t € RdpW1 && sens( t, RdpW1 )
			if ( this.currentFilteredTransition != null && this.workingRdp1.enabledTransition( this.currentFilteredTransition ) )
			{
				if ( this.logAll ) logger.log(Level.INFO, "t € RdpW1 && sens( t, RdpW1 ) => calcul de M'W1");
				// On calcule M'W1
				this.workingRdp1.changeStatePetriNet(this.currentFilteredTransition);
			}
			// Aucun label n'est a definir dans ce cas (on ne labellise pas les transitions systeme)
			// En revanche on verifie si le marquage genere par la transition systeme ne produit pas un marquage deja vue
			// Si tel est le cas, on propage ce label sur la derniere action non systeme jouee (on considere que c'est elle
			// qui a entreine le declanchement de l'action systeme => A voir si cette hypothese est robuste...)
			if (isMarkingSeen(MpC)) {
				if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> propagation deja vu sur derniere action non systeme jouee");
				// recherche en sans inverse la premiere action non systeme
				boolean found = false;
				for (int i = completeMarkingSeen.size() - 1 ; i >= 0 && !found ; i--) {
					ITrace tr = completeMarkingSeen.get(i).action;
					if (tr != null && !systemTransitions.contains(tr.getAction())) {
						// ajout du label DEJA_VU s'il n'y est pas deja
						if (!tr.getLabels().contains(Labels.DEJA_VU))
							tr.addLabel(Labels.DEJA_VU);
						found = true; // arret de la boucle
					}
				}
			}
			// On n'oublie pas d'enregistrer cet etat comme parcouru par le joueur si cette action est incluse dans RdpW1
			if ( this.workingRdp1.getTransitionById(this.currentAction.getAction()) != null )
				completeMarkingSeen.add(new PathState(this.currentAction, this.MpC.clone(), this.MpC_subset.clone(), this.workingRdp1));
		}
	}
	
	// Cas 2 : cas des actions refusées par le jeu
	private void analyseTransitionCase2() throws Exception
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 2 : cas des actions refusées par le jeu");
		// Normalement on ne devrait jamais avoir un transition systeme non sensibilisee
		// mais on verifie quand meme au cas ou
		if ( !this.systemTransitions.contains(this.currentAction.getAction()) && !this.currentAction.getOrigin().equals(ActionType.SYSTEM) ){
			if ( this.logAll ) logger.log(Level.INFO, "Action non systeme");
			//on regarde si la transition est presente dans le  rdp filtre
			if ( this.currentFilteredTransition != null )
			{
				if ( this.logAll ) logger.log(Level.INFO, "t € RdpF");
				// On teste si la transition a pu etre declenchee => amont_t(t, GW1, MC)
				if (this.workingRdp1.isPreviouslyEnabled(this.currentFilteredTransition)) {
					if ( this.logAll ) logger.log(Level.INFO, "amont_t(t, GW1, MC)");
					// On teste si la transition pourra etre declenchee => v(t, GW1, MC, lSys)
					if (this.workingRdp1.isQuasiAliveFromMarking(this.currentFilteredTransition, this.MC_subset, this.systemTransitions)) {
						if ( this.logAll ) logger.log(Level.INFO, "v(t, GW1, MC, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Intrusion");
						currentAction.addLabel( Labels.INTRUSION );
					}
					else {
						if ( this.logAll ) logger.log(Level.INFO, "!v(t, GW1, MC, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Trop tard");
						currentAction.addLabel( Labels.TROP_TARD );
					}
				}
				else {
					if ( this.logAll ) logger.log(Level.INFO, "!amont_t(t, GW1, MC)");
					// On teste si la transition pourra etre declenchee => v(t, GW1, MC, lSys)
					if (this.workingRdp1.isQuasiAliveFromMarking(this.currentFilteredTransition, this.MC_subset, this.systemTransitions)) {
						if ( this.logAll ) logger.log(Level.INFO, "v(t, GW1, MC, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Trop tôt");
						currentAction.addLabel( Labels.TROP_TOT );
					}
					else {
						// Vis-a-vis du marquage courant, on n'a jamais pu jouer cette transition et on ne pourrapas le faire. Mais
						// comme t ϵ RdpF elle est forcement accessible sinon l'expert n'aurait pas pu la jouer. Donc c'est qu'elle
						// est sur une autre branche.
						if ( this.logAll ) logger.log(Level.INFO, "!v(t, GW1, MC, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Autre branche de resolution");
						currentAction.addLabel( Labels.AUTRE_BRANCHE_DE_RESOLUTION );
					}
				}
			}
			else
			{
				if ( this.logAll ) logger.log(Level.INFO, "!t € RdpF");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> Erronee");
				currentAction.addLabel( Labels.ERRONEE );
			}
		} else {
			// celà ne devrait pas arriver, problème dans la trace ???
			if ( this.logAll ) logger.log(Level.INFO, "Action système, Attention le cas d'une transition système non sensibilisée ne devrait pas arriver... Vérifier la trace ???");
			// De toute façon on ne labelise pas les transition systèmes donc on ne fait rien
		}
	}
	
	// Cas 1_1 : Cas des actions du joueur (non système) acceptées par le jeu
	private void analyseTransitionCase1_1() throws Exception 
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1_1 : Cas des actions du joueur (non système) acceptées par le jeu");
		
		// PRE TRAITEMENT : Rechercher un RdpW déjà calculé incluant M'C ou en générer un si non trouvé
		this.workingRdp2 = getKnownWorkingRdp(MpC_subset);
		if (this.workingRdp2 == null){
			if ( this.logAll ) logger.log(Level.INFO, "Necessité de reconstruction d'un nouveau graphe du Rdp filtré à partir de M'C");
			// on construit un nouveau Rdp sur la base du filtré, on définit son marquage initial M'C et on calcule son graphe
			this.workingRdp2 = new PetriNet(true, this.filteredRdp.getKindOfGraph(), this.filteredRdp.getGlobalStrategy());
			this.workingRdp2.setPlaces( this.filteredRdp.getPlaces() );
			this.workingRdp2.setTransitions( this.filteredRdp.getTransitions() );
			this.workingRdp2.setCurrentMarkings(this.MpC_subset);
			this.workingRdp2.initialization();
			// On ajoute ce nouveau Rdp de travail
			this.workingRdpList.add(this.workingRdp2);
		}
		// Positionnement du RdpW sur M'C
		this.workingRdp2.setCurrentMarkings(MpC_subset);
		
		// si t c fn
		if (this.expertEndTransitions.contains( this.currentAction.getAction() )) {
			if ( this.logAll ) logger.log(Level.INFO, "t c fn");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> correcte");
			this.currentAction.addLabel( Labels.CORRECTE );
		} else { // sinon <=> t !c fn
			if ( this.logAll ) logger.log(Level.INFO, "t !c fn");
			// si v(fn, GW1, MC, lSys)
			if ( isExpertEndsReachable( this.workingRdp1, MC_subset ) ) {
				if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW1, MC, lSys)");
				// si v(fn, GW2, M'C, lSys)
				if ( isExpertEndsReachable( this.workingRdp2, MpC_subset ) ) {
					if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW2, M'C, lSys)");
					//on recupere la liste des transitions a franchir pour atteindre la fin de niveau
					//pour M'C ...
					ArrayList<IPathIntersection> shortestPaths_MpC = getShortestPathsToTransitions( this.workingRdp2, MpC_subset, expertEndTransitions );
					//... et pour MC
					ArrayList<IPathIntersection> shortestPaths_MC = getShortestPathsToTransitions( this.workingRdp1, MC_subset, expertEndTransitions );
					// si longueur pcc(M'C, GW2, fn, lSys) < longueur pcc(MC, GW1, fn, lSys)
					if (shortestPaths_MpC.get(0).getDistance() < shortestPaths_MC.get(0).getDistance()) {
						if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) < longueur pcc(MC, GW1, fn, lSys)");
						analyseTransitionCase1_2();
					}
					// si longueur pcc(M'C, GW2, fn, lSys) == longueur pcc(MC, GW1, fn, lSys)
					else if (shortestPaths_MpC.get(0).getDistance() == shortestPaths_MC.get(0).getDistance()) {
						if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) == longueur pcc(MC, GW1, fn, lSys)");
						analyseTransitionCase1_3(shortestPaths_MpC, shortestPaths_MC);
					}
					// => longueur pcc(M'C, GW2, fn, lSys) > longueur pcc(MC, GW1, fn, lSys)
					else {
						if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) > longueur pcc(MC, GW1, fn, lSys)");
						analyseTransitionCase1_4(shortestPaths_MpC);
					}
				} else { // sinon <=> !v(fn, GW2, M'C, lSys)
					if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW2, M'C, lSys)");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> erronée");
					this.currentAction.addLabel( Labels.ERRONEE );
				}
			} else { // sinon <=> !v(fn, GW1, MC, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW1, MC, lSys)");
				// si v(fn, GW2, M'C, lSys)
				if ( isExpertEndsReachable( this.workingRdp2, MpC_subset ) ) {
					if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW2, M'C, lSys)");
					//on recupere la liste des transitions a franchir pour atteindre la fin de niveau à partir de M'C
					ArrayList<IPathIntersection> shortestPaths_MpC = getShortestPathsToTransitions( this.workingRdp2, MpC_subset, expertEndTransitions );
					analyseTransitionCase1_4(shortestPaths_MpC);
				} else { // sinon <=> !v(fn, GW2, M'C, lSys)
					if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW2, M'C, lSys)");
					analyseTransitionCase1_5();
				}
			}
		}
		
		// POST TRAITEMENT : on vérifie si le joueur a déjà rencontré M'C
		if (isMarkingSeen(MpC)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> déjà vu");
			this.currentAction.addLabel(Labels.DEJA_VU);
		}
		// ajout à l'historique
		completeMarkingSeen.add(new PathState(this.currentAction, this.MpC.clone(), this.MpC_subset.clone(), this.workingRdp2));
		// On passe le RdpW2 dans le RdpW1 car M'C sera MC pour la prochaine trace
		this.workingRdp1 = this.workingRdp2;
	}
	
	// Cas 1_2 : "Vers solution"
	private void analyseTransitionCase1_2() throws Exception {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1_2 : Vers solution");
		// si t € RdpW1
		if ( this.workingRdp1.getTransitionById(this.currentAction.getAction()) != null ) {
			if ( this.logAll ) logger.log(Level.INFO, "t € RdpW1");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> correcte");
			this.currentAction.addLabel( Labels.CORRECTE );
		} else { // sinon <=> t !€ RdpW1
			if ( this.logAll ) logger.log(Level.INFO, "t !€ RdpW1");
			// si succ(M'C, MC, GW1)
			if (this.workingRdp1.isSuccessorMarking(MpC_subset)) {
				if ( this.logAll ) logger.log(Level.INFO, "succ(M'C, MC, GW1)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> équivalente");
				this.currentAction.addLabel( Labels.EQUIVALENTE );
			} else { // sinon <=> !succ(M'C, MC, GW1)
				if ( this.logAll ) logger.log(Level.INFO, "!succ(M'C, MC, GW1)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> saut avant");
				this.currentAction.addLabel( Labels.SAUT_AVANT );
			}
		}
	}
	
	// Cas 1_3 : "Stagnation"
	private void analyseTransitionCase1_3(ArrayList<IPathIntersection> shortestPath_MpC, ArrayList<IPathIntersection> shortestPath_MC) {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1_3 : Stagnation");
		// si M'C filtré == MC filtré
		if (MpC_subset.isEqualTo(MC_subset)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C filtré == MC filtré");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> inutile");
			this.currentAction.addLabel( Labels.INUTILE );
		} else { // sinon <=> M'C filtré != MC filtré
			// Vérifier si pcc(M'C, GW2, fn, lSys) == pcc(MC, GW1, fn, lSys)
			boolean equal = true;
			// Si le nombre de chemin est différent ou que l'un des deux n'a pas de chemin (pas de chemin qui mène
			// vers la destination), on considère que les deux ensembles de chemin ne sont pas égaux
			if (shortestPath_MpC.size() != shortestPath_MC.size() || shortestPath_MC.size() == 0 || shortestPath_MpC.size() == 0)
				equal = false;
			else {
				// On parcours toutes les intersections de MpC et on la recherche dans les intersections de MC
				for (int i = 0 ; i < shortestPath_MpC.size() && equal ; i++) {
					equal = false;
					IPathIntersection currentPath_MpC = shortestPath_MpC.get(i);
					for (int j = 0 ; j < shortestPath_MC.size() && !equal ; j++) {
						equal = currentPath_MpC.isEqualWith(shortestPath_MC.get(j));
					}
				}
			}
			if (equal) {
				if ( this.logAll ) logger.log(Level.INFO, "pcc(M'C, GW2, fn, lSys) == pcc(MC, GW1, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> inutile");
				this.currentAction.addLabel( Labels.INUTILE );
			} else { // sinon <=> pcc(M'C, GW2, fn, lSys) != pcc(MC, GW1, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "pcc(M'C, GW2, fn, lSys) != pcc(MC, GW1, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> non optimale");
				this.currentAction.addLabel( Labels.NON_OPTIMALE );
			}
		}
	}
	
	// Cas 1_4 "Historique"
	private void analyseTransitionCase1_4(ArrayList<IPathIntersection> shortestPath_MpC) throws Exception {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1_4 : Historique");
		
		// calcul de la distance à la solution du meilleur état par lequel est passé le joueur
		int minDist = Integer.MAX_VALUE;
		for (PathState markSeen : completeMarkingSeen) {
			IMarking MH = markSeen.submark;
			IPetriNet RdpH = markSeen.rdp;
			// on calcule les chemins possible pour atteindre la fin du niveau à partir du marquage courrant
			ArrayList<IPathIntersection> paths = getShortestPathsToTransitions(RdpH, MH, expertEndTransitions);
			// il est possible qu'il soit impossible d'atteindre la fin du niveau à partir de ce marquage courant
			if (paths.size() > 0) {
				minDist = Math.min(minDist, paths.get(0).getDistance());
			}
		}
		// si M'C inclus dans l'historique du joueur
		if (isMarkingSeen(MpC)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
			// si longueur pcc(M'C, GW2, fn, lSys) == min longueurs pcc(MH, GH, fn, lSys)
			if (shortestPath_MpC.get(0).getDistance() == minDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) == min longueurs pcc(MH, GH, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> rattrapage");
				this.currentAction.addLabel( Labels.RATTRAPAGE );
			} else { // sinon <=> longueur pcc(M'C, GW2, fn, lSys) != min longueurs pcc(MH, GH, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) != min longueurs pcc(MH, GH, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> retour en arrière");
				this.currentAction.addLabel( Labels.RETOUR_ARRIERE );
			}
		} else { // sinon <=> M'C NON inclus dans l'historique du joueur
			if ( this.logAll ) logger.log(Level.INFO, "M'C !c historique joueur");
			// si longueur pcc(M'C, GW2, fn, lSys) < min longueurs pcc(MH, GH, fn, lSys)
			if (shortestPath_MpC.get(0).getDistance() < minDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) < min longueurs pcc(MH, GH, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> saut avant");
				this.currentAction.addLabel( Labels.SAUT_AVANT );
			} else { // sinon <=> longueur pcc(M'C, GW2, fn, lSys) >= min longueurs pcc(MH, GH, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) >= min longueurs pcc(MH, GH, fn, lSys)");
				// si longueur pcc(M'C, GW2, fn, lSys) == min longueurs pcc(MH, GH, fn, lSys)
				if (shortestPath_MpC.get(0).getDistance() == minDist) {
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) == min longueurs pcc(MH, GH, fn, lSys)");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> rattrapage");
					this.currentAction.addLabel( Labels.RATTRAPAGE );
				} else { // sinon <=> longueur pcc(M'C, GW2, fn, lSys) > min longueurs pcc(MH, GH, fn, lSys)
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW2, fn, lSys) > min longueurs pcc(MH, GH, fn, lSys)");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> non optimale");
					this.currentAction.addLabel( Labels.NON_OPTIMALE );
				}
			}
		}
	}
	
	/**
	 * Vérifie si dans l'ensemble des RdP de travail déjà construit il en existe un contenant
	 * dans son graphe le marquage "mark" passé en paramètre.
	 * @param	mark: marquage a rechercher dans l'ensemble des RdP de travail déjà construits
	 * @return le RdP artificiel contenant le marquage "mark" ou null sinon
	 */
	private IPetriNet getKnownWorkingRdp (IMarking mark) {
		for (IPetriNet rdpW : this.workingRdpList)
			if (rdpW.contains(mark))
				return rdpW;
		return null;
	}
	
	// Cas 1_5 "Tendance"
	private void analyseTransitionCase1_5() throws Exception{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1_5 : Tendance");
		// calcul de la distance la plus courte pour atteindre la fin du niveau à partir du (ou des)
		// marquage(s) le(s) plus proche(s) dans RdpF de M'C
		ArrayList<IMarking> nearestMarkings = getNearestMarkingsThatBringsToExpertEnds(this.filteredRdp, this.MpC_subset);
		int mpcMinDist = Integer.MAX_VALUE;
		ArrayList<IPathIntersection> paths;
		// parcours de tous les marquages les plus proches de M'C
		for (IMarking m : nearestMarkings) {
			// pour chacun d'eux, calcul du plus court chemin
			paths = getShortestPathsToTransitions(this.filteredRdp, m, this.expertEndTransitions);
			if (paths.size() > 0)
				mpcMinDist = Math.min(mpcMinDist, paths.get(0).getDistance());
		}
		// Idem pour MC, calcul de la distance la plus courte pour atteindre la fin du niveau à partir du (ou des)
		// marquage(s) le(s) plus proche(s) dans RdpF de MC
		nearestMarkings = getNearestMarkingsThatBringsToExpertEnds(this.filteredRdp, this.MC_subset);
		int mcMinDist = Integer.MAX_VALUE;
		// parcours de tous les marquages les plus proches de MC
		for (IMarking m : nearestMarkings) {
			// pour chacun d'eux, calcul du plus court chemin
			paths = getShortestPathsToTransitions(this.filteredRdp, m, this.expertEndTransitions);
			if (paths.size() > 0)
				mcMinDist = Math.min(paths.get(0).getDistance(), mcMinDist);
		}
		
		// si longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) < longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)
		if (mpcMinDist < mcMinDist) {
			if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) < longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> rapprochement");
			this.currentAction.addLabel( Labels.RAPPROCHEMENT );
		} else { // sinon <=> longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) >= longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)
			if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) >= longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)");
			// si longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) == longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)
			if (mpcMinDist == mcMinDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) == longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> stagnation");
				this.currentAction.addLabel( Labels.STAGNATION );
			} else { // sinon <=> longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) > longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GF, fn), GF, fn, lSys) > longueur pcc(ppm(MC, GF, fn), GF, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> éloignement");
				this.currentAction.addLabel( Labels.ELOIGNEMENT );
			}
		}
	}
	
	// Cas "EndStep" : gestion des actions manquantes en fin de niveau
	private void analyseTransitionEndStep() throws Exception
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS \"EndStep\" :");
		// si dernière transition de la trace c fn
		if ( this.expertEndTransitions.contains(this.currentAction.getAction()))
		{
			if ( this.logAll ) logger.log(Level.INFO, "dernière transition de la trace c fn");
			// On n'a rien à faire, on est dans le cas idéal où la dernière transition de la trace est une
			// transition de fin qui a généré un marquage connu dans GF
		} else { // sinon  <=> dernière transition de la trace !c fn
			if ( this.logAll ) logger.log(Level.INFO, "dernière transition de la trace !c fn");
			// si M'C == null
			if (this.MpC_subset == null) {
				if ( this.logAll ) logger.log(Level.INFO, "M'C == null");
				// On est dans le cas où la dernière transition de la trace était une action refusée par le
				// jeu, M'C == null. On cherche donc à identifier les transtitions manquantes pour atteindre
				// la fin du niveau.
				storeBadChoices();
			} else { // sinon  <=> M'C != null
				if ( this.logAll ) logger.log(Level.INFO, "M'C != null");
				// si v(fn, GW2, M'C, lSys)
				if (isExpertEndsReachable( this.workingRdp2, this.MpC_subset )){
					if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW2, M'C, lSys)");
					// On est dans le cas où la dernière transition de la trace n'est pas une fin de niveau,
					// on cherche donc à identifier les transtitions manquantes pour atteindre la fin du niveau
					storeMissingTransitionFromMarking(this.workingRdp2, this.MpC_subset);
				} else {
					if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW2, M'C, lSys)");
					// On est dans le cas où la dernière transition de la trace place le joueur dans un puits.
					// On remonte donc le parcours du joueur jusqu'à trouver un marquage à partir du quel on
					// peut redescendre jusqu'à la fin du niveau.
					storeBadChoices();
				}
			}
		}
	}

	private boolean isTryAction () {
		//on verifie si l'action est un try ou non
		if ( this.currentAction.getIsTry() == null )
		{
			this.currentAction.setIsTry(!this.completeRdp.enabledTransition( this.currentCompleteTransition ));
		}
		return this.currentAction.getIsTry().booleanValue();
	}
	
	/**
	 * @inheritDoc
	 * @param	exportPath
	 * @throws Exception 
	 */
	public void export(String exportPath) throws Exception {
		PetriNet.exportToGraphml(exportPath, this.completeRdp, this.filteredRdp, this.workingRdpList, this.completeMarkingSeen, this.expertEndTransitions);
	}
		
	private boolean isExpertEndsReachable(IPetriNet pn, IMarking startingMarking) throws Exception
	{
		// Récupération des transitions de fin de l'expert
		for (String trId: expertEndTransitions) {
			ITransition tr = pn.getTransitionById(trId);
			if (tr != null){
				if (pn.isQuasiAliveFromMarking(tr, startingMarking, systemTransitions))
					return true;
			}
		}
		return false;
	}
	
	// Permet d'enregistrer les mauvais choix fait par le joueur.
	private void storeBadChoices () throws Exception {
		// compteur pour parcourir à l'envers les marquages traversés par le joueur
		int badChoiceCpt = this.completeMarkingSeen.size() - 1;
		
		IPetriNet rdpFounded = null;
		IMarking markFounded = null;
		do {
			// récupération du marquage à traiter
			PathState previousMarkingSeen = this.completeMarkingSeen.get(badChoiceCpt);
			// Vérifier si la fin du niveau est atteignable
			if (isExpertEndsReachable (previousMarkingSeen.rdp, previousMarkingSeen.submark)){
				rdpFounded = previousMarkingSeen.rdp;
				markFounded = previousMarkingSeen.submark;
			}
			if (rdpFounded == null) {
				// Noter la transition correspondante comme "mauvais choix"
				ITrace prevAction = previousMarkingSeen.action;
				prevAction.addLabel(Labels.MAUVAIS_CHOIX);
				// On continue à remonter les marquages traversés par le joueur
				badChoiceCpt--;
			}
		} while (rdpFounded == null);
		
		// Parmis les marquages par lesquels le joueur est passé dans le Rdp Complet, on en a trouvé depuis
		// lequel on peut atteindre la fin du niveau. On enregistre donc toutes ces transitions comme
		// manquantes.
		storeMissingTransitionFromMarking(rdpFounded, markFounded);
	}
	
	private void storeMissingTransitionFromMarking (IPetriNet rdpW, IMarking startingMarking) throws Exception {
		// calcul des plus courts chemins possibles
		ArrayList<IPathIntersection> pcc = getShortestPathsToTransitions( rdpW, startingMarking, expertEndTransitions );
		// Extraire au moins un chemin parmis tous les chemins possibles
		if (pcc.size() > 0) {
			// De façon tout à fait arbitraire on prend la première intersection. Pour rappel quelle que soit l'intersection choisie, on est sur d'être à la même
			// distance de la transition ciblée. La combinatoire étant très importante, calculer de manière exaustive tous les chemins possibles peut prendre
			// beaucoup de temps. Dans notre cas on souhaite seulement identifier une combinaison possible. On prendra donc la première.
			ArrayList<ITransition> missingTr = new ArrayList<>();
			IPathIntersection currentIntersection = pcc.get(0);
			while (currentIntersection != null && currentIntersection.getLinks().size() > 0){
				// enregistrer la transition manquante
				missingTr.add(currentIntersection.getLinks().get(0).getLink());
				// passer à l'intersection suivante
				currentIntersection = currentIntersection.getLinks().get(0).getNextIntersection();
			}
			// On élimine les éventuelles transitions systèmes
			for (int i = missingTr.size()-1 ; i >= 0 ; i--) {
				if (this.systemTransitions.contains( missingTr.get(i).getId() ))
					missingTr.remove(i);
			}
			// On enregistre chacune des transitions restantes comme manquante
			for (ITransition tr : missingTr) {
				ITrace missing = new Trace(tr.getId(), ActionSource.LABELISATION, ActionType.UNKNOW, false);
				missing.addLabel( Labels.MANQUANTE );
				
				traces.getTraces().add(missing);
			}
		}
	}
		
	private boolean isMarkingSeen(IMarking marking) {
		for (int i = 0 ; i < completeMarkingSeen.size() ; i++) {
			if (marking.isEqualTo(completeMarkingSeen.get(i).mark)){
				return true;
			}
		}
		return false;
	}
		
	private ArrayList<IMarking> getNearestMarkingsThatBringsToExpertEnds ( IPetriNet pn, IMarking startingMarking ) throws Exception {
		ArrayList<IMarking> nearestMarkings = new ArrayList<>();
		for (String trId : expertEndTransitions) {
			ITransition tr = pn.getTransitionById(trId);
			if (tr != null)
				nearestMarkings.addAll(pn.getNearestMarkings(startingMarking, tr, this.systemTransitions));
		}
		return nearestMarkings;
	}
		
	private ArrayList<IPathIntersection> getShortestPathsToTransitions( IPetriNet pn, IMarking startingMarking, ArrayList<String> targetTransitions ) throws Exception
	{
		ArrayList<IPathIntersection> result = new ArrayList<>();
		// Récupération des transitions correspondants au mot clé (il peut y en avoir plusieurs)
		// Dans ce cas là, on ne conserve que les arbres les plus courts
		int minDist = Integer.MAX_VALUE;
		for (String trId : targetTransitions) {
			ITransition tr = pn.getTransitionById(trId);
			if (tr != null){
				ArrayList<IPathIntersection> paths = pn.getShortestPathsToTransition(startingMarking, tr, this.systemTransitions);
				// Si path contient au moins un élément, on peut se contenter de ne regarder que le premier car tous les chemins ont la 
				// même distance.
				if (paths.size() > 0 && paths.get(0).getDistance() <= minDist) {
					if (paths.get(0).getDistance() < minDist) {
						minDist = paths.get(0).getDistance();
						result = new ArrayList<>();
					}
					result.addAll(paths);
				}
			}
		}
		return result;
	}


	@Override
	public String getNextBetterActionsToReach(String targetActionName, int maxActions) throws Exception {
		IPetriNet workingRdp = this.workingRdp1;
		if (workingRdp == null) workingRdp = this.filteredRdp;
		ArrayList<String> targets = this.expertEndTransitions;
		if (!targetActionName.equals("end")){
			targets = new ArrayList<>();
			targets.add(targetActionName);
		}
		ArrayList<IPathIntersection> shortestPaths_MC = getShortestPathsToTransitions( workingRdp, PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp), targets );
		if (shortestPaths_MC.size() == 0)
			return "";
		else{
			String betterActions = "";
			int cpt = 0;
			IPathLink currentLink = shortestPaths_MC.get(0).getLinks().get(0);
			while (cpt < maxActions){
				if (cpt > 0)
					betterActions = betterActions+"\t";
				betterActions = betterActions+currentLink.getLink().getName();
				if (currentLink.getNextIntersection() != null){
					currentLink = currentLink.getNextIntersection().getLinks().get(0);
					cpt++;
				} else
					cpt = maxActions; //stop loop
			}
			
			return betterActions;
		}
	}

}
