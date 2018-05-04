package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * ...
 * @author Mathieu Muratet
 */
public class AccessibleGraph implements IGraph {
	protected HashMap<String, ShadowMarking> refMarkingByMarkingCode;
	protected ArrayList<IAccessibleMarkings> accessibleMarkingsByRefMarking;
	protected ArrayList<IMarking> markingByRefMarking;
	protected ArrayList<IMarking> toExplore;
	protected ArrayList<ITransition> transitions;
	protected ArrayList<IPlaceInfo> places;
	protected IMarking initialMarking;
	protected IPetriNet pn;
	
	public static final String TYPE = "ACCESSIBILITY_GRAPH";
	
	protected class ShadowMarking {
		public int id;
		public boolean shadow;
		public ShadowMarking (int id, boolean shadow){
			this.id = id;
			this.shadow = shadow;
		}
	}
	
	protected class MarkingSeen {
		public String status;
		public IPathIntersection data;
		public int minDist;
		public MarkingSeen (String status, IPathIntersection data, int minDist){
			this.status = status;
			this.data = data;
			this.minDist = minDist;
		}
	}
	
	public AccessibleGraph(IMarking initialMark, ArrayList<IPlaceInfo> pl, ArrayList<ITransition> tr)
	{
		refMarkingByMarkingCode = new HashMap<String, ShadowMarking>();
		accessibleMarkingsByRefMarking = new ArrayList<IAccessibleMarkings>();
		markingByRefMarking = new ArrayList<IMarking>();
		toExplore = new ArrayList<IMarking>();
		initialMarking = initialMark.clone();
		toExplore.add(initialMarking);
		
		transitions = tr;
		places = pl;
		
		// a temporary PetriNet to check transition firing
		this.pn = new PetriNet(false, "", "");
		this.pn.setPlaces(this.places);
		this.pn.setTransitions(this.transitions);
		this.pn.setCurrentMarkings(this.initialMarking);
	}
	
	/** 
	 * @inheritDoc
	 */
	public void computeGraph()
	{
		while (toExplore.size() != 0)
		{
			// compute next marking (the last)
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
					// calcul du nouvel incide de ce marquage
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
					
					String newCode = pn.getCurrentMarkings().getCode();
					int child;
					// vérifier si le marquage produit est bien inconnu
					ShadowMarking newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					if (newRefMarkingObj == null)
					{
						// enregistrer ce nouveau marquage à la liste des marquages à traiter
						toExplore.add(pn.getCurrentMarkings());
						// calcul du nouvel incide de ce marquage
						child = markingByRefMarking.size();
						// ajout de ce nouveau marquage aux marquages connus
						markingByRefMarking.add(pn.getCurrentMarkings());
						accessibleMarkingsByRefMarking.add(new AccessibleMarkings(child));
						// enregistrement dans le dictionnaire de la référence de ce marquage pour pouvoir
						// le retrouver à partir de son code
						refMarkingByMarkingCode.put(newCode, new ShadowMarking(child, true));
					}
					else
						// marquage simulé déjà connu, on note son identifiant
						child = newRefMarkingObj.id;
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
	}
	
	/** 
	 * Vérifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement présent dans le graphe (cas du graphe de couverture), on recherche un marquage 
	 * englobant.
	 */
	public boolean contains(IMarking mark)
	{
		for (IMarking m : markingByRefMarking)
		{
			if (m.isEqualTo(mark))
				return true;
		}
		return false;
	}
	
	public ArrayList<IMarking> getAllMarkings()
	{
		ArrayList<IMarking> list = new ArrayList<IMarking>();
		for (IMarking m : markingByRefMarking)
			list.add(m);
		return list;
	}
	
	public IAccessibleMarkings getAccessibleMarkings(IMarking mark) throws Exception {
		return accessibleMarkingsByRefMarking.get(getRefMarking(mark));
	}
	
	public IMarking getInitialMarking(){
		return this.initialMarking;
	}
	
	public IMarking getMarkingByRef(int refMarking) {
		return markingByRefMarking.get(refMarking);
	}
	
	/**
	 * Renvoie l'ensemble des marquages les plus proches de "mark" qui permettent d'atteindre la transition "tr" tout en prenant en compte les transitions systèmes "systemTransition".
	 * Par plus proche on entend les marquages (satisfaisant la contrainte d'atteinte de la transition) pour lesquels
	 * la distance IMarking::distanceWith() est minimale.
	 * @throws Exception 
	 */
	public ArrayList<IMarking> getNearestMarkings(IMarking mark, ITransition tr, ArrayList<String> systemTransition) throws Exception {
		int d_min = Integer.MAX_VALUE;
		ArrayList<IMarking> nearestMarkings = new ArrayList<IMarking>();
		// On parcours tout les marquages
		for (IMarking m : this.getAllMarkings())
		{
			// Vérifier si ce marquage permet d'atteindre la transition "tr"
			if (isSubsequentlyEnabled(tr, m, systemTransition)){
				// Calcul de la distance entre marking et m
				int dist = m.distanceWith(mark, 1);
				if (dist <= d_min){
					if (dist < d_min) {
						d_min = dist;
						nearestMarkings = new ArrayList<IMarking>();
					}
					nearestMarkings.add(m);
				}
			}
		}
		return nearestMarkings;
	}
	
	public int getRefMarking(IMarking mark) throws Exception {
		if (!refMarkingByMarkingCode.containsKey(mark.getCode()))
			throw new Exception("AccessibleGraph::getRefMarking => marking \"" + mark.getCode() + "\" is not a known marking");
		return refMarkingByMarkingCode.get(mark.getCode()).id;
	}
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme d'un graphe allant du marquage "fromRef" au(x) marquage(s) permettant de déclancher la
	 * transition "to". Chaque noeud du graphe retourné contient sa distance à la fin du chemin.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes. Dans ces cas là les transitions systèmes sont considérées avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 */
	protected IPathIntersection getShortestPaths_rec(int fromRef, ITransition to, HashMap<Integer, MarkingSeen> markingSeen, ArrayList<String> systemTransition)
	{
		// Définition de l'intersection à retourner
		IPathIntersection intersection = new PathIntersection();
		// check if this current marking hasn't already seen
		if (!markingSeen.containsKey(fromRef))
		{
			// On notifie la route comme en cours de traitement et data à null car la fin de la route à
			// partir du marquage en cours d'analyse n'est pas encore consistant
			markingSeen.put(fromRef, new MarkingSeen("inprocess", null, Integer.MAX_VALUE));
			// Obtenir les marquages accessibles à partir du marquage de référence en cours de traitement
			IAccessibleMarkings localAM = accessibleMarkingsByRefMarking.get(fromRef);
			// recherche d'une transition système parmis les outMarkings accessibles
			boolean exclusive = false;
			int weight = 1;
			if (systemTransition != null)
			{
				for (IIndirectMarking outM : localAM.getOutMarkings())
				{
					if (systemTransition.contains(outM.getTransition().getId()))
					{
						exclusive = true;
						// Dans le cas d'une transition système, on considère que son poid à une valeur de 0
						// pour ne pas que ces transitions systèmes, qui sont déclanchées automatiquement, pèsent
						// sur le chemin.
						weight = 0;
						break;
					}
				}
			}
			// On traite maintenant chaque transition menant aux outMarkings en considérant
			// la contrainte d'exclusivité
			for (IIndirectMarking outM : localAM.getOutMarkings())
			{
				// prise en compte de l'exclusivité
				if (!exclusive || systemTransition.contains(outM.getTransition().getId()))
				{
					// Ici on est soit dans le cas ou il n'y a pas d'exclusivité parmis les transitions accessibles
					// OU la transition en cours de traitement est justement une transition système
					// Vérifier si ce marquage courant est accessible via la transition "to" recherchée
					if (outM.getTransition().getId().equals(to.getId()))
					{
						// On a identifié la transition recherchée
						// On vérifie si l'intersection en cours de construction n'aurait pas déjà des branches identifiées
						// si tel est le cas on évalue les distances pour déterminer si l'on doit conserver les autres branches
						// ou pas ainsi que cette fin de parcours
						if (intersection.getDistance() == -1 || weight <= intersection.getDistance()){
							if (weight < intersection.getDistance())
								// on élimine les autres branches qui sont plus longues
								intersection = new PathIntersection();
							// ajoute le dernier noeud de la route (pour l'instant constituée seulement de la transition
							// recherchée)
							intersection.addLink(new PathLink(to, null));
							markingSeen.get(fromRef).minDist = weight;
							intersection.setDistance(weight);
						}
						// La cas sinon voudrait dire que le poid de cette dernière transition qui amène à un état final est
						// supérieur à un autre chemin déjà identifié. Pourra se produire si chaque transition a un coût de
						// franchissement différent (ce n'est actuellement pas le cas...)
						// Dans tous les cas on stoppe la récursion (on ne cherche pas à consulter les outMarkings de ce noeud)
					}
					else
					{
						// Poursuivre la récursion en demandant les plus courts chemins pour atteindre la transition cible
						// à partir de la référence du marquage courant
						IPathIntersection childIntersection = getShortestPaths_rec(outM.getRefMarking(), to, markingSeen, systemTransition);
						// On vérifie que la transition retournée est consistante
						if (childIntersection.getLinks().size() > 0)
						{
							// Prise en compte de la distance,  la route calculée n'est prise en compte que si elle est plus courte ou
							// équivalent à la route déjà calculée
							if (markingSeen.get(outM.getRefMarking()).minDist + weight <= markingSeen.get(fromRef).minDist)
							{
								if (markingSeen.get(outM.getRefMarking()).minDist + weight < markingSeen.get(fromRef).minDist)
								{
									// On trouve une route plus courte, on supprime l'ancienne intersection pour en créer
									// une nouvelle
									intersection = new PathIntersection();
									// mise à jour de la distance minimale
									int minD = markingSeen.get(outM.getRefMarking()).minDist + weight;
									markingSeen.get(fromRef).minDist = minD;
									intersection.setDistance(minD);
								}
								// Dans les deux cas, on ajoute un lien vers l'intersection retourné par la récursion
								intersection.addLink(new PathLink(outM.getTransition(), childIntersection));
							}
							// Le sinon à ce test intervient si la route retournée est plus longue que celle(s) déjà
							// enregistrée(s). Dans ce cas là, on l'ignore.
						}
					}
				}
			}
			
			// on notifie ce marquage comme traité (tous les fils ont été évalués)
			markingSeen.get(fromRef).status = "processed";
			markingSeen.get(fromRef).data = intersection;
		}
		else
		{
			// Dans le cas où la branche en cours a déjà été analysée en totalité celà veut dire
			// que l'on est sur un autre chemin. On récupère donc simplement l'intersection correspondante
			if (markingSeen.get(fromRef).status.equals("processed"))
			{
				// On récupère l'intersection de cette branche déjà analysée
				intersection = markingSeen.get(fromRef).data;
			}
			// Dans le cas où la branche est en cours de traitement (test précédent faux) cela signifie que l'on est 
			// sur une boucle qui ne mène pas à une sortie, on l'ignore donc. Les branches soeurs à cette branche donneront
			// peut être une branche valide. On a donc rien à faire.
		}
		return intersection;
	}
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "fromRef" au(x) marquage(s)
	 * permettant de déclancher la transition "to". Chaque noeud des graphes retournés contiennent leur distance à la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes. Dans ces cas là les transitions systèmes sont considérées avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 * @throws Exception 
	 */
	public ArrayList<IPathIntersection> getShortestPathsToTransition (IMarking from, ITransition to, ArrayList<String> systemTransition) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(from.getCode()))
			throw new Exception ("AccessibleGraph::getShortestPathsToTransition => target marking \""+from.getCode()+"\" is not a known marking");
		
		ArrayList<IPathIntersection> roads = new ArrayList<IPathIntersection>();
		roads.add(getShortestPaths_rec(refMarkingByMarkingCode.get(from.getCode()).id, to, new HashMap<Integer, MarkingSeen>(), systemTransition));
		return roads;
	}
	
	public String getType()
	{
		return AccessibleGraph.TYPE;
	}
	
	/** Vérifie si la transition "t" est accessible dans le graphe quel que soit le marquage du graphe considéré */
	public boolean isAlwaysEnabled(ITransition t)
	{
		HashSet<Integer> seen = new HashSet<Integer>();
		for (IAccessibleMarkings am : accessibleMarkingsByRefMarking)
		{
			if (!seen.contains(am.getRefMarking()))
			{
				if (!isSubsequentlyEnabled_rec(am.getRefMarking(), t, null, seen))
					return false;
			}
		}
		return true;
	}
	
	/** Vérifie si le marquage "mark" est accessible dans le graphe à partir de "from". 
	 * @throws Exception */
	public boolean isMarkingAccessible(IMarking mark, IMarking from) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(mark.getCode()))
			throw new Exception ("AccessibleGraph::isMarkingAccessible => target marking \""+mark.getCode()+"\" is not a known marking");
		if (!refMarkingByMarkingCode.containsKey(from.getCode()))
			throw new Exception ("AccessibleGraph::isMarkingAccessible => source marking \""+from.getCode()+"\" is not a known marking");
		return isMarkingAccessible_rec (refMarkingByMarkingCode.get(from.getCode()).id, mark, new HashSet<String>());
	}
	
	/**
	 * Cette fonction prend en paramètre la référence du marquage source ("parentRef"), le
	 * marquage à rechercher ("child") et les marquages déjà analysés ("seen").
	 */
	protected boolean isMarkingAccessible_rec(int parentRef, IMarking child, HashSet<String> seen)
	{
		// Obtenir le marquage parent
		IMarking parent = markingByRefMarking.get(parentRef);
		
		String code = parent.getCode();
		//if we already seen this marking (usefull for loop in the graph)
		if (seen.contains(code))
		{
			//then it s not a child
			return false;
		}
		//remember that we already seen this marking
		seen.add(code);
		ArrayList<IIndirectMarking> outMarkings = accessibleMarkingsByRefMarking.get(refMarkingByMarkingCode.get(code).id).getOutMarkings();
		//if the marking doesn t have out marking (ie child)
		if (outMarkings.size() == 0)
		{
			//then it can t be a child
			return false;
		}
		//for each out marking (ie child)
		for (IIndirectMarking indirectMarking : outMarkings)
		{
			IMarking newMarking = markingByRefMarking.get(indirectMarking.getRefMarking());
			//if the out marking is the child we are looking for
			// Normalement dans le graphe d'accessibilité, on devrait utiliser la fonction Marking::isEqualTo mais
			// pour que la fonction "isMarkingAccessible_rec" puisse être appelée aussi dans la classe CoverabilityGraph
			// on utilise plutôt Marking::isEquivalentTo. Cela ne pose pas de problème dans le cadre d'un graphe
			// d'accessibilité, car dans ce cas "isEqualTo" et "isEquivalentTo" retournent les mêmes résultats car
			// le graphe ne contient pas d'omégas.
			if (newMarking != null && newMarking.isEquivalentTo(child, 1))
			{
				//then we find it
				return true;
			}
			//if there is an out marking and on of child (recursivly) of this out marking is the one we
			//are looking for
			if (newMarking != null && isMarkingAccessible_rec(indirectMarking.getRefMarking(), child, seen))
			{
				//then we find it
				return true;
			}
		}
		//the child we are looking for is not in this part of the graph
		return false;
	}
	
	/** Vérifie si la transition "t" est présente en amont de "startingMarking" dans le graphe. 
	 * @throws Exception */
	public boolean isPreviouslyEnabled(ITransition t, IMarking startingMarking) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(startingMarking.getCode()))
			throw new Exception("AccessibleGraph::isPreviouslyEnabled => \""+startingMarking.getCode()+"\" is not a known marking");
		return isPreviouslyEnabled_rec (refMarkingByMarkingCode.get(startingMarking.getCode()).id, t, new HashSet<Integer>());
	}
	
	/**
	 * Cette fonction prend en paramètre la référence du marquage source ("refMarking") la
	 * transition à rechercher ("t") et les marquages déjà analysés ("seen").
	 */
	protected boolean isPreviouslyEnabled_rec(int refMarking, ITransition t, HashSet<Integer> seen)
	{
		// check if this current marking hasn't already seen
		if (!seen.contains(refMarking))
		{
			seen.add(refMarking);
			// Obtenir les marquages accessibles à partir du marquage de référence en cours de traitement
			IAccessibleMarkings localAM = accessibleMarkingsByRefMarking.get(refMarking);
			// Parcourir ces marquage pour tenter de trouver la transition
			for (IIndirectMarking inMark : localAM.getInMarkings())
			{
				// check if it's the "t" transition that gives this marking
				if (inMark.getTransition().getId().equals(t.getId()))
				{
					// here we have found an in marking that gives the "refMarking" with the "t" transition
					// then we can return "true"
					return true;
				}
				else
				{
					// here we have found an in marking that gives the "refMarking" but not with the "t" transition
					// then we have to check recursively if parent accessible marking of this out marking is reachable with the "t" transition
					if (isPreviouslyEnabled_rec(inMark.getRefMarking(), t, seen))
						return true;
				}
			}
		}
		return false;
	
	}
	
	/**
	 * Vérifie si la transition "t" est présente en aval de "startingMarking" dans le graphe.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 * @throws Exception 
	 */
	public boolean isSubsequentlyEnabled(ITransition t, IMarking startingMarking, ArrayList<String> systemTransition) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(startingMarking.getCode()))
			throw new Exception ("AccessibleGraph::isSubsequentlyEnabled => \""+startingMarking.getCode()+"\" is not a known marking");
		return isSubsequentlyEnabled_rec (refMarkingByMarkingCode.get(startingMarking.getCode()).id, t, systemTransition, new HashSet<Integer>());
	}
	
	/**
	 * Cette fonction prend en paramètre la référence du marquage source ("refMarking"), la
	 * transition à rechercher ("t"), la liste des transitions systèmes ("systemTransition") et
	 * les marquages déjà analysés ("seen").
	 * Si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes.
	 */
	protected boolean isSubsequentlyEnabled_rec(int refMarking, ITransition t, ArrayList<String> systemTransition, HashSet<Integer> seen)
	{
		// check if this current marking hasn't already seen
		if (!seen.contains(refMarking))
		{
			seen.add(refMarking);
			// Obtenir les marquages accessibles à partir du marquage de référence en cours de traitement
			IAccessibleMarkings localAM = accessibleMarkingsByRefMarking.get(refMarking);
			// recherche d'une transition système parmis les outMarkings accessibles
			boolean exclusive = false;
			if (systemTransition != null)
			{
				for (IIndirectMarking outM : localAM.getOutMarkings())
				{
					if (systemTransition.contains(outM.getTransition().getId()))
						exclusive = true;
				}
			}
			
			// On traite maintenant chaque transition menant aux outMarkings en considérant
			// la contrainte d'exclusivité
			for (IIndirectMarking outM : localAM.getOutMarkings())
			{
				// prise en compte de l'exclusivité
				if (!exclusive || systemTransition.contains(outM.getTransition().getId()))
				{
					// Ici on est soit dans le cas ou il n'y a pas d'exclusivité parmis les transitions accessibles
					// OU la transition en cours de traitement est justement une transition système
					// check if this current out marking includes the "t" transition
					if (outM.getTransition().getId().equals(t.getId()))
					{
						return true;
					}
					else
					{
						// check recursively if child marking includes the "t" transition
						if (isSubsequentlyEnabled_rec(outM.getRefMarking(), t, systemTransition, seen))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Vérifie si le marquage "to" est un successeur immédiat du marquage "from".
	 * @throws Exception 
	 */
	public boolean isSuccessorMarking(IMarking from, IMarking to) throws Exception
	{
		// récupération les marquages en sortie de "from"
		ArrayList<IIndirectMarking> outMarkings = this.getAccessibleMarkings(from).getOutMarkings();
		for (IIndirectMarking outM : outMarkings)
		{
			// On vérifier si ce marquage accessible n'est pas celui recherché
			// NOTE IMPORTANTE : Normalement dans le graphe d'accessibilité, on devrait utiliser la fonction 
			// Marking::isEqualTo mais pour que la fonction "isSuccessorMarking" puisse être appelée aussi dans la
			// classe CoverabilityGraph on utilise plutôt Marking::isEquivalentTo. Cela ne pose pas de problème dans
			// le cadre d'un graphe d'accessibilité, car dans ce cas "isEqualTo" et "isEquivalentTo" retournent les
			// mêmes résultats car le graphe ne contient pas d'omégas.
			if (to.isEquivalentTo(markingByRefMarking.get(outM.getRefMarking()), 1)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calcule et retourne la plus petite distance entre le marquage passé en paramètre et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr". La distance entre deux marquages est la somme des valeurs absolues
	 * des differences des marquages de chaque place.
	 * @throws Exception 
	 */
	public int minimalDistanceWith(IMarking marking, ITransition tr) throws Exception
	{
		int d_min = Integer.MAX_VALUE;
		// On parcours tous les marquages
		for (IMarking m : this.getAllMarkings())
		{
			// Vérifier si ce marquage permet d'atteindre la transition "tr"
			if (isSubsequentlyEnabled(tr, m, null)){
				// Calcul de la distance entre marking et m
				d_min = Math.min(d_min, m.distanceWith(marking, 1));
			}
		}
		return d_min;
	}
	
	public void print()
	{
		System.out.println( this.toString() );
	}
	
	@Override
	public String toString()
	{
		String str = getType() +" :\n";
		for (IAccessibleMarkings am : accessibleMarkingsByRefMarking)
		{
			//trace(am.getMarking() );
			String code = "";
			for (String c : refMarkingByMarkingCode.keySet())
			{
				if (refMarkingByMarkingCode.get(c).id == am.getRefMarking())
				{
					code = c;
					break;
				}
			}
			str += am.getRefMarking() + " :: " + code + "\n";
			for (IIndirectMarking j : am.getOutMarkings())
				str += "  Out : " + j.getTransition().getName() + " -> " + j.getRefMarking() + "\n";
		}
		return str;
	}
}
