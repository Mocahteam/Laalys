package fr.lip6.mocah.laalys.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Note pour comparaison avec Tina :
 *  - Tina ne prend pas en compte les arcs inhibiteurs dans le calcul de son graphe de couverture, les arcs inhibiteurs sont
 * retir�s du RdP (voir option -C http://projects.laas.fr/tina/manuals/tina.html) => Ceci a pour effet avec notre algorithme
 * de r�duire dans certains cas le nombre d'�tat possible.
 *  - Nous calculons pour le r�seau un nombre minimal de jetons requis avant d'ajouter des w (n�cessaire pour prendre en compte
 * les arcs inhibiteurs et read dans le calcul du graphe de couverture) => Ceci a pour effet avec notre algorithme d'augmenter
 * le nombre d'�tat produit dans le cas ou ce nombre de jeton minimal est > 1.
 * @author Mathieu Muratet, Cl�ment Rouanet
 */
public class CoverabilityGraph extends AccessibleGraph {
	private String globalStrategy;

	private int minimalWeightToAddOmega = 1; // voir gros commentaire computeAccessibleGraph
	//private var minimalWeightsToAddOmega:ArrayList.<int>; // voir gros commentaire computeAccessibleGraph
	
	public static final String STRATEGY_FIRST = "FIRST";
	public static final String STRATEGY_AND = "AND";
	public static final String STRATEGY_OR = "OR";
	
	public static final String TYPE = "COVERABILITY_GRAPH";
	
	/**
	 * Permet de d�finir un graphe de couverture � partir d'un marquage initial "initialMarking", un ensemble de place
	 * "pl", un ensemble de transition "tr" et une strategie d'analyse du graphe "strategy". Ce dernier param�tre strategy
	 * est utilis� pour les fonctions visant � rechercher l'acc�s � une propri�t� (transition ou marquage) � partir d'un
	 * marquage donn� (cas de "isPreviouslyEnabled", "isSubsequentlyEnabled", "isMarkingAccessible" et
	 * "getShortestPathsToTransition"). En effet contrairement au graphe d'accessibilit� qui enregistre de mani�re
	 * exaustive tous les marquages accessibles, le graphe de couverture quand � lui approxime un ensemble de marquage �
	 * l'aide des om�gas. Il n'est donc plus possible de d�terminer si un marquage particulier peut �tre produit avec un tel
	 * r�seau de Petri, on ne peut que rechercher une approximation de ce marquage. Une des cons�quences est qu'il est 
	 * possible de trouver dans le graphe de couverture plusieurs marquages �quivalents au marquage recherch�. Dans ce cas
	 * on recherche les marquages les plus proches (les �tats du monde qui approximent le mieux le marquage recherch�), par
	 * plus proche on entend les marquages �quivalents qui contiennent le moins de omega.
	 * Dans le cas o� plusieurs marquages englobent le marquage recherch� dans le graphe de couverture, la propri�t� "globalStrategy"
	 * est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => l'acc�s � la propri�t� recherch�e doit �tre valide pour le premier marquage englobant le marquage recherch�
	 *    CoverabilityGraph.STRATEGY_AND => l'acc�s � la propri�t� recherch�e doit �tre valide pour tous marquages englobants le marquage recherch�
	 *    CoverabilityGraph.STRATEGY_OR => l'acc�s � la propri�t� recherch�e doit �tre valide pour au moins un marquage englobant le marquage recherch�
	 * 
	 * @see #isPreviouslyEnabled()
	 * @see #isSubsequentlyEnabled()
	 * @see #isMarkingAccessible()
	 * @see #getShortestPathsToTransition()
	 */
	public CoverabilityGraph(IMarking initialMark, ArrayList<IPlaceInfo> pl, ArrayList<ITransition> tr, String strategy)
	{
		super(initialMark, pl, tr);
		minimalWeightToAddOmega = 1;
		//minimalWeightsToAddOmega = new ArrayList.<int>();
		this.globalStrategy = strategy;
	}
	
	/**
	 * V�rifie si le marquage simul� � partir du marquage courant est un marquage qui couvre strictement un marquage
	 * d�j� connu en amont du marquage courant. Si tel est le cas place les omega en cons�quence
	 * @param currentMarking repr�sente le marquage en cours de traitement
	 * @param simulateMarking repr�sente un marquage simul� � partir de currentMarking
	 */
	protected IMarking addOmegas(IMarking currentMarking, IMarking simulateMarking)
	{
		IMarking newMarking = simulateMarking.clone();
		IMarking coveredParent = findCoveredParent(simulateMarking, currentMarking);
		if (coveredParent != null){
			// Parcours de chaque place pour d�terminer les om�gas
			for (int k = 0 ; k < this.places.size() ; k++)
			{
				// Si le poid du marquage simul� de la k-ieme place est sup�rieur au poid de la k-i�me place du marquage
				// parent et qu'il est aussi sup�rieur au poid minimal attendu
				if (simulateMarking.getTokenAt(k) > coveredParent.getTokenAt(k) && simulateMarking.getTokenAt(k) >= minimalWeightToAddOmega/*minimalWeightsToAddOmega[k]*/)
				{
					// Attribuer la valeur uint.MAX_VALUE � cette place pour repr�senter le om�ga
					newMarking.setTokenAt(k, Integer.MAX_VALUE);
				}
				// si le test pr�c�dent est faux, on ne touche pas au marquage
			}
		}
		return newMarking;
	}
	
	@Override 
	public void computeGraph()
	{
		// pour chacune des places du RdP on va calculer le poid minimum avant d'ajouter Omega
		// cela permet de "forcer" tous les marquages intermediaires a etre present dans le graphe de couverture.
		// Cette astuce est indipensable pour prendre en comptes des arcs READ et/ou INHIBITEUR car si on factorise tout de
		// suite les marquages avec l'ajout de Omega il peut y avoir des imprecisions dans le graph qui peuvent mener � des
		// impressisions lors du parcours du graphe. Par exemple, s'il y a un arc READ de poids 2 et un arc INHIBITEUR de poids
		// 3 sortant d'une place alors il ne faut ajouter le Omega que � partir du moment ou le poid de la place est superieur
		// a 3. Car sinon comment savoir que la transition li�e � l'arc READ n'est activ�e qu'� partir d'un marquage de poids
		// 2 et que la transition li�e � l'arc INHIBITEUR n'est plus active � partir du marquage de poid 4. (m�me cas de figure
		// si le poid de l'arc READ est supp�rieur � celui de l'arc INHIBITEUR.
		// Une premi�re version de l'algo permettait de d�finir un poid minimale pour chaque place. L'id�e �tait de factoriser
		// au maximum le graphe de couverture tout en permettant localement de le d�velopper pour prendre en compte une contrainte
		// li�e � un arc. De cette mani�re on obtient un graphe factoris� par endroits et d�velopp� par ailleurs ce qui pose
		// probl�me pour suivre les actions du joueur jusqu'� ce que la contrainte soit atteinte. Cette nouvelle version d�fini
		// un poid minimal pour tout le r�seau. Ca entraine une factorisation moindre du graphe de couverture mais nous garanti
		// que l'on puisse suivre finement les actions du joueur jusqu'� ce que la contrainte maximale soit atteinte. 
		minimalWeightToAddOmega = 1;
		for (int k = 0 ; k < this.places.size() ; k++ )
		{
			//minimalWeightsToAddOmega.push(1);
			for (IArc arcOut : this.places.get(k).getArcsOut())
			{
				minimalWeightToAddOmega = Math.max(minimalWeightToAddOmega, arcOut.getWeight());
				//minimalWeightsToAddOmega[k] = Math.max(minimalWeightsToAddOmega[k], arcOut.getWeight());
			}
		}

		// init time
		while (toExplore.size() != 0)
		{
			IMarking mark = toExplore.remove(toExplore.size() - 1);
			// synchronisation du r�seau de p�tri sur le marquage en cours de traitement
			pn.setCurrentMarkings(mark);
			String code = mark.getCode();
			ShadowMarking refMarkingObj = refMarkingByMarkingCode.get(code);
			if (refMarkingObj == null || refMarkingObj.shadow)
			{
				// store this marking
				int refMarking;
				if (refMarkingObj == null)
				{
					// calcul du nouvel indice de ce marquage
					refMarking = markingByRefMarking.size();
					// ajout de ce nouveau marquage aux marquages connus
					markingByRefMarking.add(mark);
					// ajout d'un canevas pour enregistrer les marquages accessibles � partir de ce marquage
					accessibleMarkingsByRefMarking.add(new AccessibleMarkings(refMarking));
					// enregistrement dans le dictionnaire de la r�f�rence de ce marquage pour pouvoir
					// le retrouver � partir de son code
					refMarkingByMarkingCode.put(code, new ShadowMarking(refMarking, true));
					refMarkingObj = refMarkingByMarkingCode.get(code);
				}
				else
					refMarking = refMarkingObj.id;
				// try to fire out transitions
				for (ITransition tr : transitions)
				{
					if (!pn.changeStatePetriNet(tr))
						continue;
					
					IMarking newMark = pn.getCurrentMarkings();
					String newCode = newMark.getCode();
					int child;
					// avant de cherche � ajouter des om�gas, v�rifier si le marquage produit
					// est bien inconnu
					ShadowMarking newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					if (newRefMarkingObj == null){
						// calculer le nouveau marquage avec �ventuellement des om�gas int�gr�s
						newMark = addOmegas(mark, pn.getCurrentMarkings());
						newCode = newMark.getCode();
						newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					}
					// le marquage a peut avoir �t� modifi� par la fonction addOmega, on teste donc
					// � nouveau notre connaissance de ce marquage
					if (newRefMarkingObj == null)
					{
						// enregistrer ce nouveau marquage � la liste des marquages � traiter
						toExplore.add(newMark);
						// calcul du nouvel indice de ce marquage
						child = markingByRefMarking.size();
						// ajout de ce nouveau marquage aux marquages connus
						markingByRefMarking.add(newMark);
						accessibleMarkingsByRefMarking.add(new AccessibleMarkings(child));
						// enregistrement dans le dictionnaire de la r�f�rence de ce marquage pour pouvoir
						// le retrouver � partir de son code
						refMarkingByMarkingCode.put(newCode, new ShadowMarking(child, true));
					}
					else
					{
						// marquage simul� possiblement mis � jour par addOmegas est d�j� connu,
						// on note son identifiant
						child = newRefMarkingObj.id;
					}
					// add this new marking as an outMarking of the current marking
					accessibleMarkingsByRefMarking.get(refMarking).addOutMarking(new IndirectMarking(tr, child));
					// add this current marking as an inMarking of the new marking
					accessibleMarkingsByRefMarking.get(child).addInMarking(new IndirectMarking(tr, refMarking));
					
					// repositionnement du r�seau de p�tri sur le marquage en cours d'analyse
					pn.setCurrentMarkings(mark);
				}
				refMarkingObj.shadow = false;
			}
		}
		//this.print();
	}
	
	/**
	 * V�rifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), on recherche un marquage 
	 * englobant.
	 */
	@Override 
	public boolean contains (IMarking mark)
	{
		for (IMarking m : markingByRefMarking)
		{
			if (m.isEquivalentTo(mark, minimalWeightToAddOmega))
				return true;
		}
		return false;
	}
	
	/**
	 * Remonte l'arborescence du graphe de couverture pour identifier un parent en amont de "startingMarking" recouvert
	 * strictement par "startingMarking". Retourne null si aucun parent n'est recouvert strictement par "startingMarking".
	 * A noter qu'il n'est pas n�cessaire d'identifier tous les marquages recouvert, il suffit d'en trouver un (propri�t�
	 * valid�e par Isabelle Mounier (�quipe MOVE - LIP6)).
	 */
	protected IMarking findCoveredParent (IMarking startingMarking, IMarking currentMarking) {
		HashSet<String> seen = new HashSet<String>();
		HashSet<IMarking> toSee = new HashSet<IMarking>();
		toSee.add(currentMarking);
		// On travaille tant qu'il reste des marquages � traiter
		while (!toSee.isEmpty()){
			// On r�cup�re le prochain marquage � traiter
			IMarking workingMarking = toSee.iterator().next();
			// On le supprime des marquages � traiter
			toSee.remove(workingMarking);
			// Et on r�cup�re son code
			String currentCode = workingMarking.getCode();
			// V�rifier si on n'a pas d�j� trait� ce marquage
			if (!seen.contains(currentCode)) {
				// enregistrer ce marquage comme trait�
				seen.add(currentCode);
				// v�rifier si ce parent est recouvert strictement par notre marquage de d�part ("startingMarking")
				if (startingMarking.strictlyCover(workingMarking)){
					// on a trouv� un parent recouvert strictement par notre marquage de d�part : c'est le crit�re d'ajout des om�gas
					// on retourne ce parent
					return workingMarking;
					// On stoppe ici la recherche car on consid�re que ce parent qui est couvert strictement s'il est dans le graphe
					// c'est qu'il a lui m�me �t� analys� vis � vis de ses parents, il est donc inutile de refaire le travail (propri�t�
					// des graphe de couverture valid� par Isabelle Mounier (�quipe MOVE - LIP6)
				} else {
					// on remonte l'arborescence pour tenter de trouver un marquage que "startingMarking" recouvre strictement
					ArrayList<IIndirectMarking> inMarkings = accessibleMarkingsByRefMarking.get(refMarkingByMarkingCode.get(currentCode).id).getInMarkings();
					// on parcours maintenant chaque parent
					for (IIndirectMarking indirectMarking : inMarkings) {
						// on r�cup�re le parent courant
						toSee.add(markingByRefMarking.get(indirectMarking.getRefMarking()));
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Renvoie la reference du marquage �gual � "mark" s'il existe dans le graphe ou les r�ferences des marquages les plus proches.
	 * Par plus proche on entend les marquages �quivalents qui contiennent le moins de omega.
	 */
	protected ArrayList<Integer> getRefClosestEquivalentMarkings(IMarking mark)
	{
		ArrayList<Integer> equivalentMarkings = getRefEquivalentMarkings(mark);
		
		ArrayList<Integer> closestMarks = new ArrayList<Integer>();
		int nbOmega = Integer.MAX_VALUE;
		
		// on parcours tous les marquages �quivalents et on va retourner celui qui est le plus proche
		// � savoir celui qui a le moins de om�ga (le moins approximant parmi les �quivalents)
		for (Integer refMark : equivalentMarkings)
		{
			IMarking marking = markingByRefMarking.get(refMark);
			// on compte le nombre de om�ga du marquage courrant
			int currentNbOmega = 0;
			for (int i = 0 ; i < marking.getLength() ; i++)
			{
				if (marking.getTokenAt(i) == Integer.MAX_VALUE)
					currentNbOmega++;
			}
			// Evaluation du nombre de Om�ga par rapport aux pr�c�dents
			if (currentNbOmega <= nbOmega)
			{
				if (currentNbOmega <= nbOmega)
				{
					// si le nombre de om�ga est strictement plus petit que le ou les pr�c�dents, on enregistre ce nouveau marquage
					// et son nombre de om�ga
					closestMarks = new ArrayList<Integer>();
					nbOmega = currentNbOmega;
				}
				// On enregistre cette nouvelle r�f�rence de marquage
				closestMarks.add(refMark);
			}
		}
		return closestMarks;
	}
	
	/**
	 * Renvoie la reference du marquage �gual � "mark" s'il existe dans le graphe ou la liste de tous les marquages (leurs ref) qui sont
	 * equivalents � "mark" dans le graphe ("mark" peut ne pas �tre pr�sent dans le cas d'un graphe de couverture mais �tre approxim� par un
	 * autre marquage contenant des om�gas).
	 * Deux marquages sont �quivalents si les poids de leurs places sont identiques except� pour les places contenant des om�gas
	 */
	protected ArrayList<Integer> getRefEquivalentMarkings(IMarking mark)
	{
		ArrayList<Integer> equivalentMarkings = new ArrayList<Integer>();
		// On tente de le trouver direct dans le dictionnaire
		if (refMarkingByMarkingCode.containsKey(mark.getCode()))
		{
			equivalentMarkings.add(refMarkingByMarkingCode.get(mark.getCode()).id);
		}
		else
		{
			//on parcours tous les marquage du graph
			for (IMarking m : this.getAllMarkings())
			{
				//si le marquage m est equivalent au marquage mark alors on l'ajoute au marquage equivalent
				if (mark.isEquivalentTo(m, minimalWeightToAddOmega))
					equivalentMarkings.add(refMarkingByMarkingCode.get(m.getCode()).id);
			}
		}
		return equivalentMarkings;
	}
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "fromRef" au(x) marquage(s)
	 * permettant de d�clancher la transition "to". Chaque noeud des graphes retourn�s contiennent leur distance � la fin du chemin.
	 * Si "from" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris � la place (par plus proche on entend les marquages �quivalents qui contiennent le moins de omega).
	 * Dans le cas o� plusieurs marquages englobent le marquage "from" dans le graphe de couverture, la propri�t�
	 * "globalStrategy" est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => un seul arbre est construit � partir du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => les arbres sont contruits seulement si pour tout marquage englobant "from", la transition "to"
	 * est accessible. Si tel n'est pas le cas, aucun arbre n'est renvoy� (vecteur vide) ; Si tel est le cas, seuls les arbres les plus
	 * petits (plus courts chemins) partant des marquages englobant "from" seront renvoy�s.
	 *    CoverabilityGraph.STRATEGY_OR => les arbres sont contruits pour tout marquage englobant "from". Dans ce cas, seuls les arbres les plus
	 * petits (plus courts chemins) partant des marquages englobant "from" seront renvoy�s.
	 * D'autre part, si un marquage d'un graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 * @throws Exception 
	 */
	@Override 
	public ArrayList<IPathIntersection> getShortestPathsToTransition(IMarking from, ITransition to, ArrayList<String> systemTransition) throws Exception
	{
		if (systemTransition == null)
			systemTransition = new ArrayList<String>();
		ArrayList<IPathIntersection> roads = new ArrayList<IPathIntersection>();
		if (processStrategy(new Process_isSubsequentlyEnabled(), from, to, systemTransition, new HashSet<Integer>()))
		{
			// On r�cup�re l'ensemble des r�f�rences de marquage qui correspondent � "from"
			ArrayList<Integer> refMarkings = getRefClosestEquivalentMarkings(from);
			// On lance l'analyse sur chaque r�f�rence de marquage en fonction de la strat�gie
			if (refMarkings.size() == 1 || globalStrategy.equals(STRATEGY_FIRST))
				roads.add(getShortestPaths_rec(refMarkings.get(0), to, new HashMap<Integer, MarkingSeen>(), systemTransition));
			else
			{
				int minDist = Integer.MAX_VALUE;
				for (int ref : refMarkings)
				{
					IPathIntersection path = getShortestPaths_rec(ref, to, new HashMap<Integer, MarkingSeen>(), systemTransition);
					if (path.getDistance() <= minDist)
					{
						if (path.getDistance() < minDist)
						{
							minDist = path.getDistance();
							roads = new ArrayList<IPathIntersection>();
						}
						roads.add(path);
					}
				}
			}
		}
		
		//System.out.println ("Longueur des chemins les plus courts : " + roads.size());
		//System.out.println ("Affichage des intersections possibles :");
		//roads.print();
		
		return roads;
	}
	
	@Override 
	public String getType()
	{
		return CoverabilityGraph.TYPE;
	}
	
	/**
	 * V�rifie si le marquage "mark" est accessible dans le graphe � partir de "from".
	 * Si "from" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris � la place (par plus proche on entend les marquages �quivalents qui contiennent le moins de omega).
	 * Dans le cas o� plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propri�t�
	 * "globalStrategy" est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => le marquage "mark" doit �tre accessible � partir du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => le marquage "mark" doit �tre accessible � partir de chaque marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_OR => le marquage "mark" doit �tre accessible � partir d'au moins un marquage englobant "from"
	 * @throws Exception 
	 */
	@Override 
	public boolean isMarkingAccessible(IMarking mark, IMarking from) throws Exception
	{
		return processStrategy(new Process_isMarkingAccessible(), from, mark, new HashSet<String>());
	}
	
	/**
	 * V�rifie si la transition "t" est pr�sente en amont de "startingMarking" dans le graphe.
	 * Si "startingMarking" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), les marquages les plus proches
	 * seront pris � la place (par plus proche on entend les marquages �quivalents qui contiennent le moins de omega).
	 * Dans le cas o� plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propri�t� "globalStrategy"
	 * est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => la transition "t" doit �tre pr�sente en amont du premier marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_AND => la transition "t" doit �tre pr�sente en amont de chaque marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_OR => la transition "t" doit �tre pr�sente en amont d'au moins un marquage englobant "startingMarking"
	 * @throws Exception 
	 */
	@Override
	public boolean isPreviouslyEnabled (ITransition t, IMarking startingMarking) throws Exception
	{
		return processStrategy(new Process_isPreviouslyEnabled(), startingMarking, t, new HashSet<Integer>());
	}
	
	/**
	 * V�rifie si la transition "t" est pr�sente en aval de "startingMarking" dans le graphe.
	 * Si "startingMarking" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris � la place (par plus proche on entend les marquages �quivalents qui contiennent le moins de omega).
	 * Dans le cas o� plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propri�t� "globalStrategy"
	 * est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => la transition "t" doit �tre pr�sente en aval du premier marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_AND => la transition "t" doit �tre pr�sente en aval de chaque marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_OR => la transition "t" doit �tre pr�sente en aval d'au moins un marquage englobant "startingMarking"
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	@Override
	public boolean isSubsequentlyEnabled(ITransition t, IMarking startingMarking, ArrayList<String> systemTransition) throws Exception
	{
		if (systemTransition == null)
			systemTransition = new ArrayList<String>();
		return processStrategy(new Process_isSubsequentlyEnabled(), startingMarking, t, systemTransition, new HashSet<Integer>());
	}
	
	/**
	 * V�rifie si le marquage "to" est un successeur imm�diat du marquage "from".
	 * Si "from" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris � la place (par plus proche on entend les marquages �quivalents qui contiennent le moins de omega).
	 * Dans le cas o� plusieurs marquages englobent le marquage "from" dans le graphe de couverture, la propri�t� "globalStrategy"
	 * est utilis�e pour d�finir la strat�gie � employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => le marquage "to" doit �tre un successeur immediat du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => la marquage "to" doit �tre un successeur immediat de chaque marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_OR => la marquage "to" doit �tre un successeur immediat d'au moins un marquage englobant "from"
	 * @throws Exception 
	 */
	@Override 
	public boolean isSuccessorMarking(IMarking from, IMarking to) throws Exception
	{
		return processStrategy(new Process_isSuccessorMarking(), from, to);
	}
	
	/**
	 * Cette interface permet de r�aliser en Java un passage de fonction en param�tre d'une autre fonction
	 * Elle d�finie la fonction "execute" qui prend en param�tre la r�f�rence d'un marquage ("refMarking")
	 * et un tableau "args" contenant les donn�es de travail propres aux fonctions qui l'impl�menteront
	 * @author mmuratet
	 *
	 */
	protected interface Command{
		public boolean execute(int refMarking, Object... args) throws Exception;
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif � "isMarkingAccessible"
	 * en param�tre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en param�tre la r�f�rence du marquage source
	 * ("parentRef") et un tableau "args" contenant les donn�es de travail propres � cette fonction � savoir
	 * un objet de type IMarking pour le marquage � rechercher et un objet de type HashSet<String> pour les
	 * marquages d�j� analys�s.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isMarkingAccessible implements Command{
		public boolean execute(int parentRef, Object... args)
		{
			// R�cup�ration des param�tres dans l'argument "args"
			if (args.length != 2 || !(args[0] instanceof IMarking) || !(args[1] instanceof HashSet<?>))
				throw new Error("\"isMarkingAcessible_rec\" waiting two fields in args parameter : an \"IMarking\" and a \"HashSet<String>\"");
			IMarking child = (IMarking) args[0];
			HashSet<String> seen = (HashSet<String>) args[1];
			
			return isMarkingAccessible_rec(parentRef, child, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif � "isPreviouslyEnabled"
	 * en param�tre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en param�tre la r�f�rence du marquage source
	 * ("refMarking") et un tableau "args" contenant les donn�es de travail propres � cette fonction � savoir
	 * un objet de type ITransition pour la transition � rechercher et un objet de type HashSet<Integer> pour
	 * les marquages d�j� analys�s.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isPreviouslyEnabled implements Command{
		public boolean execute(int refMarking, Object... args)
		{
			// R�cup�ration des param�tres dans l'argument "args"
			if (args.length != 2 || !(args[0] instanceof ITransition) || !(args[1] instanceof HashSet<?>))
				throw new Error("\"isPreviouslyEnabled_rec\" waiting two fields in args parameter : an \"ITransition\" and a \"HashSet<Integer>\"");
			ITransition t = (ITransition) args[0];
			HashSet<Integer> seen = (HashSet<Integer>) args[1];
			
			return isPreviouslyEnabled_rec(refMarking, t, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif � "isSubsequentlyEnabled"
	 * en param�tre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en param�tre la r�f�rence du marquage source ("refMarking")
	 * et un tableau "args" contenant les donn�es de travail propres � cette fonction � savoir un objet de type ITransition
	 * pour la transition � rechercher, un objet de type ArrayList<String> pour la liste des transitions syst�mes et
	 * un objet de type HashSet<Integer> pour les marquages d�j� analys�s.
	 * Si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le troisi�me param�tre de "args") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isSubsequentlyEnabled implements Command{
		public boolean execute(int refMarking, Object... args)
		{
			// R�cup�ration des param�tres dans l'argument "args"
			if (args.length != 3 || !(args[0] instanceof ITransition) || !(args[1] instanceof ArrayList<?>) || !(args[2] instanceof HashSet<?>))
				throw new Error("\"isSubsequentlyEnabled_rec\" waiting three fields in args parameter : an \"ITransition\", a\"ArrayList<String>\" and a \"HashSet<Integer>\"");
			ITransition t = (ITransition) args[0];
			ArrayList<String> systemTransition = (ArrayList<String>) args[1];
			HashSet<Integer> seen = (HashSet<Integer>) args[2];
			
			return isSubsequentlyEnabled_rec(refMarking, t, systemTransition, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif � "isSuccessorMarking"
	 * en param�tre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en param�tre la r�f�rence du marquage source
	 * ("parentRef") et un tableau "args" contenant les donn�es de travail propres � ce traitement � savoir
	 * un objet de type IMarking pour le marquage � rechercher et un objet de type ArrayList<String> pour la
	 * liste des transitions syst�mes.
	 * @throws Exception 
	 */
	protected class Process_isSuccessorMarking implements Command{
		public boolean execute (int parentRef, Object... args) throws Exception{
			// R�cup�ration des param�tres dans l'argument "args"
			if (args.length != 1 || !(args[0] instanceof IMarking))
				throw new Error("\"isSuccessorMarking_process\" waiting one field in args parameter : an \"IMarking\"");
			IMarking child = (IMarking) args[0];
			
			// Acc�s � la classe englobante et appel de la fonction de sa classe m�re
			return CoverabilityGraph.super.isSuccessorMarking(markingByRefMarking.get(parentRef), child);
		}
	}
	
	/** Fonction permettant de prendre en compte la strat�gie 
	 * @throws Exception */
	protected boolean processStrategy(Command workingFunction, IMarking startingMarking, Object... args) throws Exception
	{
		// On r�cup�re l'ensemble des r�f�rences de marquage qui correspondent � "startingMarking"
		ArrayList<Integer> refMarkings = getRefClosestEquivalentMarkings(startingMarking);
		// On lance l'analyse sur chaque r�f�rence de marquage en fonction de la strat�gie
		if (refMarkings.size() == 1 || globalStrategy.equals(STRATEGY_FIRST)){
			return workingFunction.execute(refMarkings.get(0), args);
		}
		else
		{
			for (int ref : refMarkings)
			{
				boolean localRes = workingFunction.execute(ref, args);
				if (globalStrategy.equals(STRATEGY_OR))
				{
					if (localRes) return true;
				}
				else
				{ // strategy == STRATEGY_AND
					if (!localRes) return false;
				}
			}
			if (globalStrategy.equals(STRATEGY_OR))
				return false;
			else // STRATEGY_AND
				return true;
		}
	}
}
