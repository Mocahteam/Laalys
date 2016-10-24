package fr.lip6.mocah.laalys.labeling;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.labeling.constants.Labels;
import fr.lip6.mocah.laalys.petrinet.IMarking;
import fr.lip6.mocah.laalys.petrinet.IPathIntersection;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.constants.ActionSource;
import fr.lip6.mocah.laalys.traces.constants.ActionType;

public class Labeling_V9 implements ILabeling {

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
	 * Reseau de petri artificiel
	 * C'est le RdP filtre dans lequel on va injecter comme marquage initial le marquage
	 * courant du RdP complet pour pouvoir calculer un graph de couverture/accessibilite
	 * artificiel
	 */
	private IPetriNet artificialRdp = null;
	
	/**
	 * Liste des réseaux de Petri artificiels déjà calculés. Evite d'avoir à les recalculer
	 * si nécessaire
	 */
	private ArrayList<IPetriNet> artificialRdpList = null;
	 
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
	 * marquage du RdP filtree avant franchissement de la transition
	 */
	private IMarking MF = null;
	/**
	 * marquage du RdP complet avant franchissement de la transition
	 */
	private IMarking MC = null;
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
	
	
	public Labeling_V9( Logger logger, boolean logAll ) 
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
		logger.log(Level.INFO, "debut de la labellisation des actions" );
		this.traces = traces;
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
		analyseTransitionCase0_1();
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
		this.artificialRdp = null;
		this.artificialRdpList = new ArrayList<>();
		completeMarkingSeen = new ArrayList<>();
		completeMarkingSeen.add(new PathState(
				null,
				this.completeRdp.getCurrentMarkings().clone(),
				PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp).clone()));
		this.traces.reset();
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
		return filteredRdp;
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
		this.MF = this.filteredRdp.getCurrentMarkings().clone();
		this.MC = this.completeRdp.getCurrentMarkings().clone();
		this.MC_subset = PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp);
		this.MpC = null;
		this.MpC_subset = null;
		
		//récupération des transitions dans les différents Rdp
		this.currentCompleteTransition = this.completeRdp.getTransitionById( action.getAction() );
		if (currentCompleteTransition == null){
			if ( this.logAll ) logger.log(Level.SEVERE, "impossible d'effectuer l'analyse de l'action \"" + action.getAction() + "\" car aucune équivalence n'est présente dans le réseau de Pétri Complet" );
			return;
		}
		this.currentFilteredTransition = this.filteredRdp.getTransitionById( action.getAction() );
		this.currentAction = action;
		if ( this.logAll ) logger.log(Level.INFO, "debut de la labellisation de l'action : " + this.currentAction.getAction() );
		
		// Lancement de l'analyse de l'action courante.
		analyseTransitionCase0();
		
		if ( this.logAll ) logger.log(Level.INFO, "l'action \"" +  this.currentAction.getAction() + "\" a ete labellisee \"" +  this.currentAction.getLabels() + "\"");
	}
	
	private void analyseTransitionCase0() throws Exception
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 0 :");
		
		// On regarde si la transition est un Try
		if (!isTryAction()) {
			if ( this.logAll ) logger.log(Level.INFO, "L'action n'est pas un Try  => sens(t, MC)");
			//on joue l'action dans le rdp complet => génération de M'C
			this.completeRdp.changeStatePetriNet( this.currentCompleteTransition );
			// enregistre le marquage M' du Rdp Complet
			this.MpC = this.completeRdp.getCurrentMarkings().clone();
			// enregistre le marquage M' du Rdp Complet adapté pour le Rdp Filtré
			this.MpC_subset = PetriNet.extractSubMarkings(this.filteredRdp, this.completeRdp);
			//si ce n'est pas une transition systeme
			if ( !this.systemTransitions.contains(this.currentAction.getAction()) ){
				if ( this.logAll ) logger.log(Level.INFO, "Action non système");
				//on passe dans le cas 1 (cas general)
				analyseTransitionCase1();
			}
			else
			{
				if ( this.logAll ) logger.log(Level.INFO, "Action système");
				// on joue la transition dans le Rdp Filtré si elle est présente dans RdpF et si elle est sensibilisée
				computeMpF_IfPossible();
				// Aucun label n'est à définir dans ce cas (on ne labellise pas les transitions système)
				// En revanche on vérifie si le marquage généré par la transition système ne produit pas un marquage déjà vue
				// Si tel est le cas, on propage ce label sur la dernière action non système jouée (on considère que c'est elle
				// qui a entreiné le déclanchement de l'action système => A voir si cette hypothèse est robuste...)
				if (isMarkingSeen(MpC)) {
					if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> propagation déjà vu sur dernière action non système jouée");
					// recherche en sans inverse la première action non système
					boolean found = false;
					for (int i = completeMarkingSeen.size() - 1 ; i >= 0 && !found ; i--) {
						ITrace tr = completeMarkingSeen.get(i).action;
						if (!systemTransitions.contains(tr.getAction())) {
							// ajout du label DEJA_VU s'il n'y est pas déjà
							if (!tr.getLabels().contains(Labels.DEJA_VU))
								tr.addLabel(Labels.DEJA_VU);
							found = true; // arrêt de la boucle
						}
					}
				}
				// On n'oublie pas d'enregistrer cet état comme parcouru par le joueur
				completeMarkingSeen.add(new PathState(this.currentAction, MpC.clone(), MpC_subset.clone()));
			}
		} else {
			if ( this.logAll ) logger.log(Level.INFO, "L'action est un Try => !sens(t, MC)");
			//on regarde si la transition est presente dans le  rdp filtre
			if ( this.currentFilteredTransition != null )
			{
				if ( this.logAll ) logger.log(Level.INFO, "t € RdpF");
				// On teste si la transition a pu être déclenchée => amont_t(t, GF, MF)
				if (this.filteredRdp.isPreviouslyEnabled(this.currentFilteredTransition)) {
					if ( this.logAll ) logger.log(Level.INFO, "amont_t(t, GF, MF)");
					// On teste si la transition pourra être déclenchée => v(t, GF, MF, lSys)
					if (this.filteredRdp.isQuasiAliveFromMarking(this.currentFilteredTransition, this.MF, this.systemTransitions)) {
						if ( this.logAll ) logger.log(Level.INFO, "v(t, GF, MF, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Intrusion");
						currentAction.addLabel( Labels.INTRUSION );
					}
					else {
						if ( this.logAll ) logger.log(Level.INFO, "!v(t, GF, MF, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Trop tard");
						currentAction.addLabel( Labels.TROP_TARD );
					}
				}
				else {
					if ( this.logAll ) logger.log(Level.INFO, "!amont_t(t, GF, MF)");
					// On teste si la transition pourra être déclenchée => v(t, GF, MF, lSys)
					if (this.filteredRdp.isQuasiAliveFromMarking(this.currentFilteredTransition, this.MF, this.systemTransitions)) {
						if ( this.logAll ) logger.log(Level.INFO, "v(t, GF, MF, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Trop tôt");
						currentAction.addLabel( Labels.TROP_TOT );
					}
					else {
						// Vis-à-vis du marquage courant, on n'a jamais pu jouer cette transition et on ne pourrapas le faire. Mais
						// comme t ϵ RdpF elle est forcément accéssible sinon l'expert n'aurait pas pu la jouer. Donc c'est qu'elle
						// est sur une autre branche.
						if ( this.logAll ) logger.log(Level.INFO, "!v(t, GF, MF, lSys)");
						if ( this.logAll ) logger.log(Level.INFO, "\t=> Autre branche de résolution");
						currentAction.addLabel( Labels.AUTRE_BRANCHE_DE_RESOLUTION );
					}
				}
			}
			else
			{
				if ( this.logAll ) logger.log(Level.INFO, "!t € RdpF");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> Erronée");
				currentAction.addLabel( Labels.ERRONEE );
			}
		}
	}
		
	private void analyseTransitionCase0_1() throws Exception
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 0.1 :");
		// si dernière transition de la trace c fn
		if ( this.expertEndTransitions.contains(this.currentAction.getAction()))
		{
			if ( this.logAll ) logger.log(Level.INFO, "dernière transition de la trace c fn");
			// On n'a rien à faire, on est dans le cas idéal où la dernière transition de la trace est une transition de fin qui
			// a généré un marquage connu dans GF
		} else { // sinon  <=> dernière transition de la trace !c fn
			if ( this.logAll ) logger.log(Level.INFO, "dernière transition de la trace !c fn");
			// si M'C == null
			if (this.MpC_subset == null) {
				if ( this.logAll ) logger.log(Level.INFO, "M'C == null");
				// On est dans le cas où la dernière transition de la trace était une action refusée par le jeu, M'C == null. On
				// cherche donc à identifier les transtitions manquantes pour atteindre la fin du niveau
				storeBadChoices();
			} else { // sinon  <=> M'C != null
				if ( this.logAll ) logger.log(Level.INFO, "M'C != null");
				// si M'C c GF
				if (this.filteredRdp.contains(this.MpC_subset)) {
					if ( this.logAll ) logger.log(Level.INFO, "M'C c GF");
					checkBadChoicesBeing(this.filteredRdp);
				} else { // sinon  <=> M'C !c GF
					if ( this.logAll ) logger.log(Level.INFO, "M'C !c GF");
					// si M'C c GA
					if (this.artificialRdp.contains(this.MpC_subset)) {
						if ( this.logAll ) logger.log(Level.INFO, "M'C c GA");
						checkBadChoicesBeing(this.artificialRdp);
					} else { // sinon  <=> M'C !c GA
						if ( this.logAll ) logger.log(Level.INFO, "M'C !c GA");
						// On est dans le cas où la dernière transition de la trace est une action non experte qui place le jeu dans un état
						// hors des espaces filtrés (GF et GA). On remonte donc le parcours du joueur jusqu'à trouver un marquage à partir
						// duquel on peut redescendre jusqu'à la fin du niveau.
						storeBadChoices();
					}
				}
			}
		}
	}
	
	private void checkBadChoicesBeing(IPetriNet RdpW) throws Exception {
		//si une des transitions "fin de niveau" peut être franchie a partir du marquage du rdp complet
		//dans le rdp de travail
		//<=> V(fn, GW, M'C, lSys)
		if ( isExpertEndsReachable( RdpW, MpC_subset ) )
		{
			if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW, M'C, lSys)");
			// On est dans le cas où la dernière transition de la trace n'est pas une fin de niveau, on cherche donc à
			// identifier les transtitions manquantes pour atteindre la fin du niveau
			storeMissingTransitionFromMarking(RdpW, MpC_subset);
		} else {
			if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW, M'C, lSys)");
			// On est dans le cas où la dernière transition de la trace place le joueur dans un puits. On remonte donc le
			// parcours du joueur jusqu'à trouver un marquage à partir du quel on peut redescendre jusqu'à la fin du niveau.
			storeBadChoices();
		}
	}

	// Pré-requis : l'action courante n'est pas un Try et n'est pas une transition système
	private void analyseTransitionCase1() throws Exception 
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 1 : Cas général");
		// si t c fn
		if (this.expertEndTransitions.contains( this.currentAction.getAction() )) {
			if ( this.logAll ) logger.log(Level.INFO, "t c fn");
			// on joue la transition dans le Rdp Filtré si elle est présente dans RdpF et si elle est sensibilisée
			computeMpF_IfPossible();
			if ( this.logAll ) logger.log(Level.INFO, "\t=> correcte");
			this.currentAction.addLabel( Labels.CORRECTE );
		} else { // sinon <=> t !c fn
			if ( this.logAll ) logger.log(Level.INFO, "t !c fn");
			// si MC inclus dans GF
			if (this.filteredRdp.contains(MC_subset)) {
				if ( this.logAll ) logger.log(Level.INFO, "MC inclus dans GF");
				// si M'C inclus dans GF
				if (this.filteredRdp.contains(MpC_subset)) {
					if ( this.logAll ) logger.log(Level.INFO, "M'C inclus dans GF");
					analyseTransitionCase2(filteredRdp);
				} else { // sinon <=> M'C non inclus dans GF
					if ( this.logAll ) logger.log(Level.INFO, "M'C NON inclus dans GF");
					// on joue la transition dans le Rdp Filtré si elle est présente dans RdpF et si elle est sensibilisée
					computeMpF_IfPossible();
					if ( this.logAll ) logger.log(Level.INFO, "\t=> erronée");
					this.currentAction.addLabel( Labels.ERRONEE );
				}
			} else { // sinon <=> MC non inclus dans GF
				if ( this.logAll ) logger.log(Level.INFO, "MC NON inclus dans GF");
				// si M'C inclus dans GF
				if (this.filteredRdp.contains(MpC_subset)) {
					if ( this.logAll ) logger.log(Level.INFO, "M'C inclus dans GF");
					// si v(fn, GF, M'C, lSys)
					if ( isExpertEndsReachable( this.filteredRdp, MpC_subset ) ) {
						if ( this.logAll ) logger.log(Level.INFO, "v(fn, GF, M'C, lSys)");
						analyseTransitionCase3 (filteredRdp, getShortestPathsToTransitions( this.filteredRdp, MpC_subset, expertEndTransitions ));
					} else { // sinon <=> !v(fn, GF, M'C, lSys)
						if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GF, M'C, lSys)");
						analyseTransitionCase5 (filteredRdp);
					}
					//on propage le marquage complet dans le rdp filtré
					if ( this.logAll ) logger.log(Level.INFO, "\ton propage le marquage complet dans le rdp filtré");
					this.filteredRdp.setCurrentMarkings(MpC_subset);
				} else { // sinon <=> M'C non inclus dans GF
					if ( this.logAll ) logger.log(Level.INFO, "M'C NON inclus dans GF");
					analyseTransitionCase4 ();
				}
			}
		}
		
		// on vérifie si le joueur a déjà rencontré M'C
		if (isMarkingSeen(MpC)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> déjà vu");
			this.currentAction.addLabel(Labels.DEJA_VU);
		}
		completeMarkingSeen.add(new PathState(this.currentAction, MpC.clone(), MpC_subset.clone()));
	}
	
	// Pré-requis : MC == MW && M'C inclus GW
	private void analyseTransitionCase2(IPetriNet RdpW) throws Exception 
	{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 2 : Dans espace filtré");
		// si v(fn, GW, MC, lSys)
		if ( isExpertEndsReachable( RdpW, MC_subset ) ) {
			if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW, MC, lSys)");
			// si v(fn, GW, M'C, lSys)
			if ( isExpertEndsReachable( RdpW, MpC_subset ) ) {
				if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW, M'C, lSys)");
				//on recupere la liste des transitions a franchir pour atteindre la fin de niveau
				//pour M'C ...
				ArrayList<IPathIntersection> shortestPaths_MpC = getShortestPathsToTransitions( RdpW, MpC_subset, expertEndTransitions );
				//... et pour MC
				ArrayList<IPathIntersection> shortestPaths_MC = getShortestPathsToTransitions( RdpW, MC_subset, expertEndTransitions );
				// si longueur pcc(M'C, GW, fn, lSys) < longueur pcc(MC, GW, fn, lSys)
				if (shortestPaths_MpC.get(0).getDistance() < shortestPaths_MC.get(0).getDistance()) {
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) < longueur pcc(MC, GW, fn, lSys)");
					analyseTransitionCase2_1 (RdpW);
				} else { // sinon <=> longueur pcc(M'C, GW, fn, lSys) >= longueur pcc(MC, GW, fn, lSys)
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) >= longueur pcc(MC, GW, fn, lSys)");
					// si longueur pcc(M'C, GW, fn, lSys) == longueur pcc(MC, GW, fn, lSys)
					if (shortestPaths_MpC.get(0).getDistance() == shortestPaths_MC.get(0).getDistance()) {
						if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) == longueur pcc(MC, GW, fn, lSys)");
						analyseTransitionCase2_2 (RdpW, shortestPaths_MpC, shortestPaths_MC);
					} else { // sinon <=> longueur pcc(M'C, GW, fn, lSys) > longueur pcc(MC, GW, fn, lSys)
						if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) > longueur pcc(MC, GW, fn, lSys)");
						analyseTransitionCase3 (RdpW, shortestPaths_MpC);
					}
				}
			} else { // sinon <=> !v(fn, GW, MC, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "\t=> erronée");
				this.currentAction.addLabel( Labels.ERRONEE );
			}
		} else { // sinon <=> !v(fn, GW, MC, lSys)
			if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW, MC, lSys)");
			// si v(fn, GW, M'C, lSys)
			if ( isExpertEndsReachable( RdpW, MpC_subset ) ) {
				if ( this.logAll ) logger.log(Level.INFO, "v(fn, GW, M'C, lSys)");
				analyseTransitionCase3 (RdpW, getShortestPathsToTransitions( RdpW, MpC_subset, expertEndTransitions ));
			} else { // sinon <=> !v(fn, GW, M'C, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GW, M'C, lSys)");
				analyseTransitionCase5 (RdpW);
			}
		}
		
		// Post traitement
		// si RdpW est RdpF : Propager M'C dans MF
		if (RdpW == this.filteredRdp) {
			if ( this.logAll ) logger.log(Level.INFO, "RdpW est RdpF avec MiF == MiC");
			//on propage le marquage complet dans le rdp filtré
			if ( this.logAll ) logger.log(Level.INFO, "\ton propage le marquage complet dans le rdp filtré");
			this.filteredRdp.setCurrentMarkings(MpC_subset);
		} else { // sinon <=> RdpW n'est pas RdpF => c'est donc RdpA
			if ( this.logAll ) logger.log(Level.INFO, "RdpW est RdpF avec MiF != MiC");
			// on joue la transition dans le Rdp Filtré si elle est présente dans RdpF et si elle est sensibilisée
			computeMpF_IfPossible();
		}
	}
	
	private void analyseTransitionCase2_1(IPetriNet RdpW) throws Exception {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 2.1 : Vers solution");
		// si t € RdpW
		if ( RdpW.getTransitionById(this.currentAction.getAction()) != null ) {
			if ( this.logAll ) logger.log(Level.INFO, "t € RdpF");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> correcte");
			this.currentAction.addLabel( Labels.CORRECTE );
		} else { // sinon <=> t !€ RdpW
			// si succ(M'C, MW, GW)
			if (RdpW.isSuccessorMarking(MpC_subset)) {
				if ( this.logAll ) logger.log(Level.INFO, "succ(M'C, MW, GW)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> équivalente");
				this.currentAction.addLabel( Labels.EQUIVALENTE );
			} else { // sinon <=> !succ(M'C, MW, GW)
				if ( this.logAll ) logger.log(Level.INFO, "!succ(M'C, MW, GW)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> saut avant");
				this.currentAction.addLabel( Labels.SAUT_AVANT );
			}
		}
	}
	
	private void analyseTransitionCase2_2(IPetriNet RdpW, ArrayList<IPathIntersection> shortestPath_MpC, ArrayList<IPathIntersection> shortestPath_MC) {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 2.2 : Stagnation");
		// si M'C filtré == MC filtré
		if (MpC_subset.isEqualTo(MC_subset)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C filtré == MC filtré");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> inutile");
			this.currentAction.addLabel( Labels.INUTILE );
		} else { // sinon <=> M'C filtré != MC filtré
			// Vérifier si pcc(M'C, GW, fn, lSys) == pcc(MW, GW, fn, lSys)
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
				if ( this.logAll ) logger.log(Level.INFO, "pcc(M'C, GW, fn, lSys) == pcc(MW, GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> inutile");
				this.currentAction.addLabel( Labels.INUTILE );
			} else { // sinon <=> pcc(M'C, GW, fn, lSys) != pcc(MW, GW, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "pcc(M'C, GW, fn, lSys) != pcc(MW, GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> non optimale");
				this.currentAction.addLabel( Labels.NON_OPTIMALE );
			}
		}
	}
	
	private void analyseTransitionCase3(IPetriNet RdpW, ArrayList<IPathIntersection> shortestPath_MpC) throws Exception {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 3 : Historique");
		
		// calcul de la distance à la solution du meilleur état par lequel est passé le joueur
		int minDist = Integer.MAX_VALUE;
		for (PathState markSeen : completeMarkingSeen) {
			IMarking currentMark = markSeen.submark;
			// on calcule les chemins possible pour atteindre la fin du niveau à partir du marquage courrant
			ArrayList<IPathIntersection> paths = getShortestPathsToTransitions(RdpW, currentMark, expertEndTransitions);
			// il est possible qu'il soit impossible d'atteindre la fin du niveau à partir de ce marquage courant
			if (paths.size() > 0) {
				minDist = Math.min(minDist, paths.get(0).getDistance());
			}
		}
		// si M'C inclus dans l'historique du joueur
		if (isMarkingSeen(MpC)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C c historique joueur");
			// si longueur pcc(M'C, GW, fn, lSys) == min longueurs pcc(historique joueur, GW, fn, lSys)
			if (shortestPath_MpC.get(0).getDistance() == minDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) == min longueurs pcc(historique joueur, GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> rattrapage");
				this.currentAction.addLabel( Labels.RATTRAPAGE );
			} else { // sinon <=> longueur pcc(M'C, GW, fn, lSys) != min longueurs pcc(historique joueur, GW, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) != min longueurs pcc(historique joueur, GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> retour en arrière");
				this.currentAction.addLabel( Labels.RETOUR_ARRIERE );
			}
		} else { // sinon <=> M'C NON inclus dans l'historique du joueur
			if ( this.logAll ) logger.log(Level.INFO, "M'C !c historique joueur");
			// si longueur pcc(M'C, GW, fn, lSys) < min longueurs pcc(historique joueur, GW, fn, lSys)
			if (shortestPath_MpC.get(0).getDistance() < minDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) < min longueurs pcc(historique joueur, GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> saut avant");
				this.currentAction.addLabel( Labels.SAUT_AVANT );
			} else { // sinon <=> longueur pcc(M'C, GW, fn, lSys) >= min longueurs pcc(historique joueur, GW, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) >= min longueurs pcc(historique joueur, GW, fn, lSys)");
				// si longueur pcc(M'C, GW, fn, lSys) == min longueurs pcc(historique joueur, GW, fn, lSys)
				if (shortestPath_MpC.get(0).getDistance() == minDist) {
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) == min longueurs pcc(historique joueur, GW, fn, lSys)");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> rattrapage");
					this.currentAction.addLabel( Labels.RATTRAPAGE );
				} else { // sinon <=> longueur pcc(M'C, GW, fn, lSys) > min longueurs pcc(historique joueur, GW, fn, lSys)
					if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(M'C, GW, fn, lSys) > min longueurs pcc(historique joueur, GW, fn, lSys)");
					if ( this.logAll ) logger.log(Level.INFO, "\t=> non optimale");
					this.currentAction.addLabel( Labels.NON_OPTIMALE );
				}
			}
		}
	}
	
	/**
	 * Vérifie si dans l'ensemble des RdP artificiels déjà construit il en existe un contenant
	 * dans son graphe le marquage "mark" passé en paramètre.
	 * @param	mark: marquage a rechercher dans l'ensemble des RdP artificiels déjà construits
	 * @return le RdP artificiel contenant le marquage "mark" ou null sinon
	 */
	private IPetriNet getKnownArtificialRdp (IMarking mark) {
		for (IPetriNet rdpA : artificialRdpList)
			if (rdpA.contains(mark))
				return rdpA;
		return null;
	}
	
	private void analyseTransitionCase4() throws Exception {
		if ( this.logAll ) logger.log(Level.INFO, "CAS 4 : Hors espace filtré depuis état initial");
		// PRE TRAITEMENT
		// si le réseau artificiel n'existe pas ou que le marquage MC n'est pas inclus dans un GA déjà construit
		// il faut alors générer un nouveau GA
		this.artificialRdp = getKnownArtificialRdp(MC_subset);
		if ( this.artificialRdp == null)
		{
			if ( this.logAll ) logger.log(Level.INFO, "Necessité de reconstruction du graphe du Rdp artificiel");
			//on met a jours le graph artificiel avec les mêmes propriétés que le Rdp filtré
			this.artificialRdp = new PetriNet(true, this.filteredRdp.getKindOfGraph(), this.filteredRdp.getGlobalStrategy());
			this.artificialRdp.setPlaces( this.filteredRdp.getPlaces() );
			this.artificialRdp.setTransitions( this.filteredRdp.getTransitions() );
			this.artificialRdp.setCurrentMarkings(MC_subset);
			
			//on restore les marquage MC et MF dans leurs réseaux respectifs car l'analyse de l'action sera reprise depuis le debut
			this.filteredRdp.setCurrentMarkings( this.MF );
			this.completeRdp.setCurrentMarkings( this.MC );
			
			this.artificialRdp.initialization();
			this.artificialRdpList.add(this.artificialRdp);
		}
		
		// si M'C c GA
		if (this.artificialRdp.contains(MpC_subset)) {
			if ( this.logAll ) logger.log(Level.INFO, "M'C c GA");
			analyseTransitionCase2(this.artificialRdp);
		} else { // sinon <=> M'C !c GA
			if ( this.logAll ) logger.log(Level.INFO, "M'C !c GA");
			// si v(fn, GA, MA, lSys)
			if (isExpertEndsReachable(this.artificialRdp, this.artificialRdp.getCurrentMarkings())) {
				if ( this.logAll ) logger.log(Level.INFO, "v(fn, GA, MA, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> erronée");
				this.currentAction.addLabel( Labels.ERRONEE );
			} else {
				if ( this.logAll ) logger.log(Level.INFO, "!v(fn, GA, MA, lSys)");
				analyseTransitionCase5(this.artificialRdp);
			}
			// on joue la transition dans le Rdp Filtré si elle est présente dans RdpF et si elle est sensibilisée
			computeMpF_IfPossible();
		}
	}
	
	private void analyseTransitionCase5(IPetriNet RdpW) throws Exception{
		if ( this.logAll ) logger.log(Level.INFO, "CAS 5 : Dans puits");
		// calcul de la distance la plus courte pour atteindre la fin du niveau à partir du (ou des)
		// marquage(s) le(s) plus proche(s) dans RdpW de M'C
		ArrayList<IMarking> nearestMarkings = getNearestMarkingsThatBringsToExpertEnds(RdpW, MpC_subset);
		int mpcMinDist = Integer.MAX_VALUE;
		ArrayList<IPathIntersection> paths;
		// parcours de tous les marquages les plus proches de M'C
		for (IMarking m : nearestMarkings) {
			// pour chacun d'eux, calcul du plus court chemin
			paths = getShortestPathsToTransitions(RdpW, m, expertEndTransitions);
			if (paths.size() > 0)
				mpcMinDist = Math.min(mpcMinDist, paths.get(0).getDistance());
		}
		
		// Idem pour MC, calcul de la distance la plus courte pour atteindre la fin du niveau à partir du (ou des)
		// marquage(s) le(s) plus proche(s) dans RdpW de MC
		nearestMarkings = getNearestMarkingsThatBringsToExpertEnds(RdpW, MC_subset);
		int mcMinDist = Integer.MAX_VALUE;
		// parcours de tous les marquages les plus proches de MC
		for (IMarking m : nearestMarkings) {
			// pour chacun d'eux, calcul du plus court chemin
			paths = getShortestPathsToTransitions(RdpW, m, expertEndTransitions);
			if (paths.size() > 0)
				mcMinDist = Math.min(mcMinDist, paths.get(0).getDistance());
		}
		
		// si longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) < longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)
		if (mpcMinDist < mcMinDist) {
			if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) < longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)");
			if ( this.logAll ) logger.log(Level.INFO, "\t=> rapprochement");
			this.currentAction.addLabel( Labels.RAPPROCHEMENT );
		} else { // sinon <=> longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) >= longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)
			if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) >= longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)");
			// si longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) == longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)
			if (mpcMinDist == mcMinDist) {
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) == longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> stagnation");
				this.currentAction.addLabel( Labels.STAGNATION );
			} else { // sinon <=> longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) > longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)
				if ( this.logAll ) logger.log(Level.INFO, "longueur pcc(ppm(M'C, GW, fn), GW, fn, lSys) > longueur pcc(ppm(MC, GW, fn), GW, fn, lSys)");
				if ( this.logAll ) logger.log(Level.INFO, "\t=> éloignement");
				this.currentAction.addLabel( Labels.ELOIGNEMENT );
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
		
	private void computeMpF_IfPossible() {
		//si t € rdpF && sens( t, rdpF )
		if ( this.currentFilteredTransition != null && this.filteredRdp.enabledTransition( this.currentFilteredTransition ) )
		{
			if ( this.logAll ) logger.log(Level.INFO, "t € rdpF && sens( t, rdpF ) => calcul de M'F");
			// On calcule M'F
			this.filteredRdp.changeStatePetriNet(this.currentFilteredTransition);
		}
	}
	
	/**
	 * @inheritDoc
	 * @param	exportPath
	 * @throws Exception 
	 */
	public void export(String exportPath) throws Exception {
		PetriNet.exportToGraphml(exportPath, this.completeRdp, this.filteredRdp, this.artificialRdpList, this.completeMarkingSeen, this.expertEndTransitions);
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
	
	// Permet d'enregistrer les mauvais choix fait par le joueur. Le paramètre "recovery" sert dans le cas où l'analyse a été intérompue pour
	// cause de nécessité de reconstruire le graphe du Rdp Artificiel. Dans ce cas, la fonction reprend là où elle s'était arrêtée.
	private void storeBadChoices () throws Exception {
		// compteur pour parcourir à l'envers les marquages traversés par le joueur
		int badChoiceCpt = this.completeMarkingSeen.size() - 1;
		
		IPetriNet rdpFounded = null;
		IMarking markFounded = null;
		IMarking previousMarking_subset;
		do {
			// récupération du marquage à traiter
			PathState previousMarkingSeen = this.completeMarkingSeen.get(badChoiceCpt);
			// extraction du sous-marquage compatible avec le Rdp Filtré
			previousMarking_subset = previousMarkingSeen.submark;
			// Vérifier si le précédent marquage parcouru par le joueur est inclus dans GF
			if (this.filteredRdp.contains(previousMarking_subset)){
				// Vérifier si la fin du niveau est atteignable
				if (isExpertEndsReachable( this.filteredRdp, previousMarking_subset ) ){
					rdpFounded = this.filteredRdp;
					markFounded = previousMarking_subset;
				}
			} else {
				// On tente de travailler sur le Rdp artificiel
				if (this.artificialRdp == null || !this.artificialRdp.contains(previousMarking_subset)){
					// On tente de reconstruire le graphe du Rdp Artificiel à partir du marquage en cours d'analyse
					if ( this.logAll ) logger.log(Level.INFO, "Construction du graphe du Rdp artificiel pour poursuivre l'analyse");
					//on met a jours le graph artificiel avec les mêmes propriétés que le Rdp filtré
					if (this.artificialRdp == null){
						this.artificialRdp = new PetriNet(true, this.filteredRdp.getKindOfGraph(), this.filteredRdp.getGlobalStrategy());
						this.artificialRdp.setPlaces( this.filteredRdp.getPlaces() );
						this.artificialRdp.setTransitions( this.filteredRdp.getTransitions() );
					}
					this.artificialRdp.setCurrentMarkings(previousMarking_subset);
					this.artificialRdp.initialization();
					this.artificialRdpList.add(this.artificialRdp);
				}
				// Vérifier si la fin du niveau est atteignable
				if (isExpertEndsReachable( this.artificialRdp, previousMarking_subset ) ){
					rdpFounded = this.artificialRdp;
					markFounded = previousMarking_subset;
				}
			}
			if (rdpFounded == null) {
				// Noter la transition correspondante comme "mauvais choix"
				ITrace prevAction = previousMarkingSeen.action;
				prevAction.addLabel(Labels.MAUVAIS_CHOIX);
				// On continue à remonter les marquages traversés par le joueur
				badChoiceCpt--;
			}
		} while (rdpFounded == null);
		
		// Parmis les marquages par lesquels le joueur est passé dans le Rdp Complet, on en a trouvé un présent dans GW
		// et depuis lequel on peut atteindre la fin du niveau. On enregistre donc toutes ces transitions comme
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
				nearestMarkings.addAll(pn.getNearestMarkings(startingMarking, tr));
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

}
