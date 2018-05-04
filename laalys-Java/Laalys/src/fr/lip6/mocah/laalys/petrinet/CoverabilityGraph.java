package fr.lip6.mocah.laalys.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Note pour comparaison avec Tina :
 *  - Tina ne prend pas en compte les arcs inhibiteurs dans le calcul de son graphe de couverture, les arcs inhibiteurs sont
 * retirés du RdP (voir option -C http://projects.laas.fr/tina/manuals/tina.html) => Ceci a pour effet avec notre algorithme
 * de réduire dans certains cas le nombre d'état possible.
 *  - Nous calculons pour le réseau un nombre minimal de jetons requis avant d'ajouter des w (nécessaire pour prendre en compte
 * les arcs inhibiteurs et read dans le calcul du graphe de couverture) => Ceci a pour effet avec notre algorithme d'augmenter
 * le nombre d'état produit dans le cas ou ce nombre de jeton minimal est > 1.
 * @author Mathieu Muratet, Clément Rouanet
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
	 * Permet de définir un graphe de couverture à partir d'un marquage initial "initialMarking", un ensemble de place
	 * "pl", un ensemble de transition "tr" et une strategie d'analyse du graphe "strategy". Ce dernier paramètre strategy
	 * est utilisé pour les fonctions visant à rechercher l'accès à une propriété (transition ou marquage) à partir d'un
	 * marquage donné (cas de "isPreviouslyEnabled", "isSubsequentlyEnabled", "isMarkingAccessible" et
	 * "getShortestPathsToTransition"). En effet contrairement au graphe d'accessibilité qui enregistre de manière
	 * exaustive tous les marquages accessibles, le graphe de couverture quand à lui approxime un ensemble de marquage à
	 * l'aide des omégas. Il n'est donc plus possible de déterminer si un marquage particulier peut être produit avec un tel
	 * réseau de Petri, on ne peut que rechercher une approximation de ce marquage. Une des conséquences est qu'il est 
	 * possible de trouver dans le graphe de couverture plusieurs marquages équivalents au marquage recherché. Dans ce cas
	 * on recherche les marquages les plus proches (les états du monde qui approximent le mieux le marquage recherché), par
	 * plus proche on entend les marquages équivalents qui contiennent le moins de omega.
	 * Dans le cas où plusieurs marquages englobent le marquage recherché dans le graphe de couverture, la propriété "globalStrategy"
	 * est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => l'accès à la propriété recherchée doit être valide pour le premier marquage englobant le marquage recherché
	 *    CoverabilityGraph.STRATEGY_AND => l'accès à la propriété recherchée doit être valide pour tous marquages englobants le marquage recherché
	 *    CoverabilityGraph.STRATEGY_OR => l'accès à la propriété recherchée doit être valide pour au moins un marquage englobant le marquage recherché
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
	 * Vérifie si le marquage simulé à partir du marquage courant est un marquage qui couvre strictement un marquage
	 * déjà connu en amont du marquage courant. Si tel est le cas place les omega en conséquence
	 * @param currentMarking représente le marquage en cours de traitement
	 * @param simulateMarking représente un marquage simulé à partir de currentMarking
	 */
	protected IMarking addOmegas(IMarking currentMarking, IMarking simulateMarking)
	{
		IMarking newMarking = simulateMarking.clone();
		IMarking coveredParent = findCoveredParent(simulateMarking, currentMarking);
		if (coveredParent != null){
			// Parcours de chaque place pour déterminer les omégas
			for (int k = 0 ; k < this.places.size() ; k++)
			{
				// Si le poid du marquage simulé de la k-ieme place est supérieur au poid de la k-ième place du marquage
				// parent et qu'il est aussi supérieur au poid minimal attendu
				if (simulateMarking.getTokenAt(k) > coveredParent.getTokenAt(k) && simulateMarking.getTokenAt(k) >= minimalWeightToAddOmega/*minimalWeightsToAddOmega[k]*/)
				{
					// Attribuer la valeur uint.MAX_VALUE à cette place pour représenter le oméga
					newMarking.setTokenAt(k, Integer.MAX_VALUE);
				}
				// si le test précédent est faux, on ne touche pas au marquage
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
		// suite les marquages avec l'ajout de Omega il peut y avoir des imprecisions dans le graph qui peuvent mener à des
		// impressisions lors du parcours du graphe. Par exemple, s'il y a un arc READ de poids 2 et un arc INHIBITEUR de poids
		// 3 sortant d'une place alors il ne faut ajouter le Omega que à partir du moment ou le poid de la place est superieur
		// a 3. Car sinon comment savoir que la transition liée à l'arc READ n'est activée qu'à partir d'un marquage de poids
		// 2 et que la transition liée à l'arc INHIBITEUR n'est plus active à partir du marquage de poid 4. (même cas de figure
		// si le poid de l'arc READ est suppérieur à celui de l'arc INHIBITEUR.
		// Une première version de l'algo permettait de définir un poid minimale pour chaque place. L'idée était de factoriser
		// au maximum le graphe de couverture tout en permettant localement de le développer pour prendre en compte une contrainte
		// liée à un arc. De cette manière on obtient un graphe factorisé par endroits et développé par ailleurs ce qui pose
		// problème pour suivre les actions du joueur jusqu'à ce que la contrainte soit atteinte. Cette nouvelle version défini
		// un poid minimal pour tout le réseau. Ca entraine une factorisation moindre du graphe de couverture mais nous garanti
		// que l'on puisse suivre finement les actions du joueur jusqu'à ce que la contrainte maximale soit atteinte. 
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
			// synchronisation du réseau de pétri sur le marquage en cours de traitement
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
					// ajout d'un canevas pour enregistrer les marquages accessibles à partir de ce marquage
					accessibleMarkingsByRefMarking.add(new AccessibleMarkings(refMarking));
					// enregistrement dans le dictionnaire de la référence de ce marquage pour pouvoir
					// le retrouver à partir de son code
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
					// avant de cherche à ajouter des omégas, vérifier si le marquage produit
					// est bien inconnu
					ShadowMarking newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					if (newRefMarkingObj == null){
						// calculer le nouveau marquage avec éventuellement des omégas intégrés
						newMark = addOmegas(mark, pn.getCurrentMarkings());
						newCode = newMark.getCode();
						newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					}
					// le marquage a peut avoir été modifié par la fonction addOmega, on teste donc
					// à nouveau notre connaissance de ce marquage
					if (newRefMarkingObj == null)
					{
						// enregistrer ce nouveau marquage à la liste des marquages à traiter
						toExplore.add(newMark);
						// calcul du nouvel indice de ce marquage
						child = markingByRefMarking.size();
						// ajout de ce nouveau marquage aux marquages connus
						markingByRefMarking.add(newMark);
						accessibleMarkingsByRefMarking.add(new AccessibleMarkings(child));
						// enregistrement dans le dictionnaire de la référence de ce marquage pour pouvoir
						// le retrouver à partir de son code
						refMarkingByMarkingCode.put(newCode, new ShadowMarking(child, true));
					}
					else
					{
						// marquage simulé possiblement mis à jour par addOmegas est déjà connu,
						// on note son identifiant
						child = newRefMarkingObj.id;
					}
					// add this new marking as an outMarking of the current marking
					accessibleMarkingsByRefMarking.get(refMarking).addOutMarking(new IndirectMarking(tr, child));
					// add this current marking as an inMarking of the new marking
					accessibleMarkingsByRefMarking.get(child).addInMarking(new IndirectMarking(tr, refMarking));
					
					// repositionnement du réseau de pétri sur le marquage en cours d'analyse
					pn.setCurrentMarkings(mark);
				}
				refMarkingObj.shadow = false;
			}
		}
		//this.print();
	}
	
	/**
	 * Vérifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement présent dans le graphe (cas du graphe de couverture), on recherche un marquage 
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
	 * A noter qu'il n'est pas nécessaire d'identifier tous les marquages recouvert, il suffit d'en trouver un (propriété
	 * validée par Isabelle Mounier (équipe MOVE - LIP6)).
	 */
	protected IMarking findCoveredParent (IMarking startingMarking, IMarking currentMarking) {
		HashSet<String> seen = new HashSet<String>();
		HashSet<IMarking> toSee = new HashSet<IMarking>();
		toSee.add(currentMarking);
		// On travaille tant qu'il reste des marquages à traiter
		while (!toSee.isEmpty()){
			// On récupère le prochain marquage à traiter
			IMarking workingMarking = toSee.iterator().next();
			// On le supprime des marquages à traiter
			toSee.remove(workingMarking);
			// Et on récupère son code
			String currentCode = workingMarking.getCode();
			// Vérifier si on n'a pas déjà traité ce marquage
			if (!seen.contains(currentCode)) {
				// enregistrer ce marquage comme traité
				seen.add(currentCode);
				// vérifier si ce parent est recouvert strictement par notre marquage de départ ("startingMarking")
				if (startingMarking.strictlyCover(workingMarking)){
					// on a trouvé un parent recouvert strictement par notre marquage de départ : c'est le critère d'ajout des omégas
					// on retourne ce parent
					return workingMarking;
					// On stoppe ici la recherche car on considère que ce parent qui est couvert strictement s'il est dans le graphe
					// c'est qu'il a lui même été analysé vis à vis de ses parents, il est donc inutile de refaire le travail (propriété
					// des graphe de couverture validé par Isabelle Mounier (équipe MOVE - LIP6)
				} else {
					// on remonte l'arborescence pour tenter de trouver un marquage que "startingMarking" recouvre strictement
					ArrayList<IIndirectMarking> inMarkings = accessibleMarkingsByRefMarking.get(refMarkingByMarkingCode.get(currentCode).id).getInMarkings();
					// on parcours maintenant chaque parent
					for (IIndirectMarking indirectMarking : inMarkings) {
						// on récupère le parent courant
						toSee.add(markingByRefMarking.get(indirectMarking.getRefMarking()));
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Renvoie la reference du marquage égual à "mark" s'il existe dans le graphe ou les réferences des marquages les plus proches.
	 * Par plus proche on entend les marquages équivalents qui contiennent le moins de omega.
	 */
	protected ArrayList<Integer> getRefClosestEquivalentMarkings(IMarking mark)
	{
		ArrayList<Integer> equivalentMarkings = getRefEquivalentMarkings(mark);
		
		ArrayList<Integer> closestMarks = new ArrayList<Integer>();
		int nbOmega = Integer.MAX_VALUE;
		
		// on parcours tous les marquages équivalents et on va retourner celui qui est le plus proche
		// à savoir celui qui a le moins de oméga (le moins approximant parmi les équivalents)
		for (Integer refMark : equivalentMarkings)
		{
			IMarking marking = markingByRefMarking.get(refMark);
			// on compte le nombre de oméga du marquage courrant
			int currentNbOmega = 0;
			for (int i = 0 ; i < marking.getLength() ; i++)
			{
				if (marking.getTokenAt(i) == Integer.MAX_VALUE)
					currentNbOmega++;
			}
			// Evaluation du nombre de Oméga par rapport aux précédents
			if (currentNbOmega <= nbOmega)
			{
				if (currentNbOmega <= nbOmega)
				{
					// si le nombre de oméga est strictement plus petit que le ou les précédents, on enregistre ce nouveau marquage
					// et son nombre de oméga
					closestMarks = new ArrayList<Integer>();
					nbOmega = currentNbOmega;
				}
				// On enregistre cette nouvelle référence de marquage
				closestMarks.add(refMark);
			}
		}
		return closestMarks;
	}
	
	/**
	 * Renvoie la reference du marquage égual à "mark" s'il existe dans le graphe ou la liste de tous les marquages (leurs ref) qui sont
	 * equivalents à "mark" dans le graphe ("mark" peut ne pas être présent dans le cas d'un graphe de couverture mais être approximé par un
	 * autre marquage contenant des omégas).
	 * Deux marquages sont équivalents si les poids de leurs places sont identiques excepté pour les places contenant des omégas
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
	 * permettant de déclancher la transition "to". Chaque noeud des graphes retournés contiennent leur distance à la fin du chemin.
	 * Si "from" n'est pas exactement présent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris à la place (par plus proche on entend les marquages équivalents qui contiennent le moins de omega).
	 * Dans le cas où plusieurs marquages englobent le marquage "from" dans le graphe de couverture, la propriété
	 * "globalStrategy" est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => un seul arbre est construit à partir du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => les arbres sont contruits seulement si pour tout marquage englobant "from", la transition "to"
	 * est accessible. Si tel n'est pas le cas, aucun arbre n'est renvoyé (vecteur vide) ; Si tel est le cas, seuls les arbres les plus
	 * petits (plus courts chemins) partant des marquages englobant "from" seront renvoyés.
	 *    CoverabilityGraph.STRATEGY_OR => les arbres sont contruits pour tout marquage englobant "from". Dans ce cas, seuls les arbres les plus
	 * petits (plus courts chemins) partant des marquages englobant "from" seront renvoyés.
	 * D'autre part, si un marquage d'un graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes. Dans ces cas là les transitions systèmes sont considérées avec un poids
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
			// On récupère l'ensemble des références de marquage qui correspondent à "from"
			ArrayList<Integer> refMarkings = getRefClosestEquivalentMarkings(from);
			// On lance l'analyse sur chaque référence de marquage en fonction de la stratégie
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
	 * Vérifie si le marquage "mark" est accessible dans le graphe à partir de "from".
	 * Si "from" n'est pas exactement présent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris à la place (par plus proche on entend les marquages équivalents qui contiennent le moins de omega).
	 * Dans le cas où plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propriété
	 * "globalStrategy" est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => le marquage "mark" doit être accessible à partir du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => le marquage "mark" doit être accessible à partir de chaque marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_OR => le marquage "mark" doit être accessible à partir d'au moins un marquage englobant "from"
	 * @throws Exception 
	 */
	@Override 
	public boolean isMarkingAccessible(IMarking mark, IMarking from) throws Exception
	{
		return processStrategy(new Process_isMarkingAccessible(), from, mark, new HashSet<String>());
	}
	
	/**
	 * Vérifie si la transition "t" est présente en amont de "startingMarking" dans le graphe.
	 * Si "startingMarking" n'est pas exactement présent dans le graphe (cas du graphe de couverture), les marquages les plus proches
	 * seront pris à la place (par plus proche on entend les marquages équivalents qui contiennent le moins de omega).
	 * Dans le cas où plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propriété "globalStrategy"
	 * est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => la transition "t" doit être présente en amont du premier marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_AND => la transition "t" doit être présente en amont de chaque marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_OR => la transition "t" doit être présente en amont d'au moins un marquage englobant "startingMarking"
	 * @throws Exception 
	 */
	@Override
	public boolean isPreviouslyEnabled (ITransition t, IMarking startingMarking) throws Exception
	{
		return processStrategy(new Process_isPreviouslyEnabled(), startingMarking, t, new HashSet<Integer>());
	}
	
	/**
	 * Vérifie si la transition "t" est présente en aval de "startingMarking" dans le graphe.
	 * Si "startingMarking" n'est pas exactement présent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris à la place (par plus proche on entend les marquages équivalents qui contiennent le moins de omega).
	 * Dans le cas où plusieurs marquages englobent le marquage "startingMarking" dans le graphe de couverture, la propriété "globalStrategy"
	 * est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => la transition "t" doit être présente en aval du premier marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_AND => la transition "t" doit être présente en aval de chaque marquage englobant "startingMarking"
	 *    CoverabilityGraph.STRATEGY_OR => la transition "t" doit être présente en aval d'au moins un marquage englobant "startingMarking"
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
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
	 * Vérifie si le marquage "to" est un successeur immédiat du marquage "from".
	 * Si "from" n'est pas exactement présent dans le graphe (cas du graphe de couverture), les marquages les
	 * plus proche seront pris à la place (par plus proche on entend les marquages équivalents qui contiennent le moins de omega).
	 * Dans le cas où plusieurs marquages englobent le marquage "from" dans le graphe de couverture, la propriété "globalStrategy"
	 * est utilisée pour définir la stratégie à employer :
	 *    CoverabilityGraph.STRATEGY_FIRST => le marquage "to" doit être un successeur immediat du premier marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_AND => la marquage "to" doit être un successeur immediat de chaque marquage englobant "from"
	 *    CoverabilityGraph.STRATEGY_OR => la marquage "to" doit être un successeur immediat d'au moins un marquage englobant "from"
	 * @throws Exception 
	 */
	@Override 
	public boolean isSuccessorMarking(IMarking from, IMarking to) throws Exception
	{
		return processStrategy(new Process_isSuccessorMarking(), from, to);
	}
	
	/**
	 * Cette interface permet de réaliser en Java un passage de fonction en paramètre d'une autre fonction
	 * Elle définie la fonction "execute" qui prend en paramètre la référence d'un marquage ("refMarking")
	 * et un tableau "args" contenant les données de travail propres aux fonctions qui l'implémenteront
	 * @author mmuratet
	 *
	 */
	protected interface Command{
		public boolean execute(int refMarking, Object... args) throws Exception;
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif à "isMarkingAccessible"
	 * en paramètre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en paramètre la référence du marquage source
	 * ("parentRef") et un tableau "args" contenant les données de travail propres à cette fonction à savoir
	 * un objet de type IMarking pour le marquage à rechercher et un objet de type HashSet<String> pour les
	 * marquages déjà analysés.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isMarkingAccessible implements Command{
		public boolean execute(int parentRef, Object... args)
		{
			// Récupération des paramètres dans l'argument "args"
			if (args.length != 2 || !(args[0] instanceof IMarking) || !(args[1] instanceof HashSet<?>))
				throw new Error("\"isMarkingAcessible_rec\" waiting two fields in args parameter : an \"IMarking\" and a \"HashSet<String>\"");
			IMarking child = (IMarking) args[0];
			HashSet<String> seen = (HashSet<String>) args[1];
			
			return isMarkingAccessible_rec(parentRef, child, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif à "isPreviouslyEnabled"
	 * en paramètre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en paramètre la référence du marquage source
	 * ("refMarking") et un tableau "args" contenant les données de travail propres à cette fonction à savoir
	 * un objet de type ITransition pour la transition à rechercher et un objet de type HashSet<Integer> pour
	 * les marquages déjà analysés.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isPreviouslyEnabled implements Command{
		public boolean execute(int refMarking, Object... args)
		{
			// Récupération des paramètres dans l'argument "args"
			if (args.length != 2 || !(args[0] instanceof ITransition) || !(args[1] instanceof HashSet<?>))
				throw new Error("\"isPreviouslyEnabled_rec\" waiting two fields in args parameter : an \"ITransition\" and a \"HashSet<Integer>\"");
			ITransition t = (ITransition) args[0];
			HashSet<Integer> seen = (HashSet<Integer>) args[1];
			
			return isPreviouslyEnabled_rec(refMarking, t, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif à "isSubsequentlyEnabled"
	 * en paramètre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en paramètre la référence du marquage source ("refMarking")
	 * et un tableau "args" contenant les données de travail propres à cette fonction à savoir un objet de type ITransition
	 * pour la transition à rechercher, un objet de type ArrayList<String> pour la liste des transitions systèmes et
	 * un objet de type HashSet<Integer> pour les marquages déjà analysés.
	 * Si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le troisième paramètre de "args") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes.
	 */
	@SuppressWarnings("unchecked")
	protected class Process_isSubsequentlyEnabled implements Command{
		public boolean execute(int refMarking, Object... args)
		{
			// Récupération des paramètres dans l'argument "args"
			if (args.length != 3 || !(args[0] instanceof ITransition) || !(args[1] instanceof ArrayList<?>) || !(args[2] instanceof HashSet<?>))
				throw new Error("\"isSubsequentlyEnabled_rec\" waiting three fields in args parameter : an \"ITransition\", a\"ArrayList<String>\" and a \"HashSet<Integer>\"");
			ITransition t = (ITransition) args[0];
			ArrayList<String> systemTransition = (ArrayList<String>) args[1];
			HashSet<Integer> seen = (HashSet<Integer>) args[2];
			
			return isSubsequentlyEnabled_rec(refMarking, t, systemTransition, seen);
		}
	}
	
	/**
	 * Cette classe implemente l'interface Command pour passer le traitement relatif à "isSuccessorMarking"
	 * en paramètre d'une autre fonction.
	 * Cette classe implemente la fonction "execute" qui prend en paramètre la référence du marquage source
	 * ("parentRef") et un tableau "args" contenant les données de travail propres à ce traitement à savoir
	 * un objet de type IMarking pour le marquage à rechercher et un objet de type ArrayList<String> pour la
	 * liste des transitions systèmes.
	 * @throws Exception 
	 */
	protected class Process_isSuccessorMarking implements Command{
		public boolean execute (int parentRef, Object... args) throws Exception{
			// Récupération des paramètres dans l'argument "args"
			if (args.length != 1 || !(args[0] instanceof IMarking))
				throw new Error("\"isSuccessorMarking_process\" waiting one field in args parameter : an \"IMarking\"");
			IMarking child = (IMarking) args[0];
			
			// Accès à la classe englobante et appel de la fonction de sa classe mère
			return CoverabilityGraph.super.isSuccessorMarking(markingByRefMarking.get(parentRef), child);
		}
	}
	
	/** Fonction permettant de prendre en compte la stratégie 
	 * @throws Exception */
	protected boolean processStrategy(Command workingFunction, IMarking startingMarking, Object... args) throws Exception
	{
		// On récupère l'ensemble des références de marquage qui correspondent à "startingMarking"
		ArrayList<Integer> refMarkings = getRefClosestEquivalentMarkings(startingMarking);
		// On lance l'analyse sur chaque référence de marquage en fonction de la stratégie
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
