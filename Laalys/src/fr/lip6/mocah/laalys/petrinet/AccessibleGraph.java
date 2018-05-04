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
					// calcul du nouvel incide de ce marquage
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
					
					String newCode = pn.getCurrentMarkings().getCode();
					int child;
					// v�rifier si le marquage produit est bien inconnu
					ShadowMarking newRefMarkingObj = refMarkingByMarkingCode.get(newCode);
					if (newRefMarkingObj == null)
					{
						// enregistrer ce nouveau marquage � la liste des marquages � traiter
						toExplore.add(pn.getCurrentMarkings());
						// calcul du nouvel incide de ce marquage
						child = markingByRefMarking.size();
						// ajout de ce nouveau marquage aux marquages connus
						markingByRefMarking.add(pn.getCurrentMarkings());
						accessibleMarkingsByRefMarking.add(new AccessibleMarkings(child));
						// enregistrement dans le dictionnaire de la r�f�rence de ce marquage pour pouvoir
						// le retrouver � partir de son code
						refMarkingByMarkingCode.put(newCode, new ShadowMarking(child, true));
					}
					else
						// marquage simul� d�j� connu, on note son identifiant
						child = newRefMarkingObj.id;
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
	}
	
	/** 
	 * V�rifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), on recherche un marquage 
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
	 * Renvoie l'ensemble des marquages les plus proches de "mark" qui permettent d'atteindre la transition "tr" tout en prenant en compte les transitions syst�mes "systemTransition".
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
			// V�rifier si ce marquage permet d'atteindre la transition "tr"
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
	 * Retourne l'ensemble des plus courts chemins sous la forme d'un graphe allant du marquage "fromRef" au(x) marquage(s) permettant de d�clancher la
	 * transition "to". Chaque noeud du graphe retourn� contient sa distance � la fin du chemin.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 */
	protected IPathIntersection getShortestPaths_rec(int fromRef, ITransition to, HashMap<Integer, MarkingSeen> markingSeen, ArrayList<String> systemTransition)
	{
		// D�finition de l'intersection � retourner
		IPathIntersection intersection = new PathIntersection();
		// check if this current marking hasn't already seen
		if (!markingSeen.containsKey(fromRef))
		{
			// On notifie la route comme en cours de traitement et data � null car la fin de la route �
			// partir du marquage en cours d'analyse n'est pas encore consistant
			markingSeen.put(fromRef, new MarkingSeen("inprocess", null, Integer.MAX_VALUE));
			// Obtenir les marquages accessibles � partir du marquage de r�f�rence en cours de traitement
			IAccessibleMarkings localAM = accessibleMarkingsByRefMarking.get(fromRef);
			// recherche d'une transition syst�me parmis les outMarkings accessibles
			boolean exclusive = false;
			int weight = 1;
			if (systemTransition != null)
			{
				for (IIndirectMarking outM : localAM.getOutMarkings())
				{
					if (systemTransition.contains(outM.getTransition().getId()))
					{
						exclusive = true;
						// Dans le cas d'une transition syst�me, on consid�re que son poid � une valeur de 0
						// pour ne pas que ces transitions syst�mes, qui sont d�clanch�es automatiquement, p�sent
						// sur le chemin.
						weight = 0;
						break;
					}
				}
			}
			// On traite maintenant chaque transition menant aux outMarkings en consid�rant
			// la contrainte d'exclusivit�
			for (IIndirectMarking outM : localAM.getOutMarkings())
			{
				// prise en compte de l'exclusivit�
				if (!exclusive || systemTransition.contains(outM.getTransition().getId()))
				{
					// Ici on est soit dans le cas ou il n'y a pas d'exclusivit� parmis les transitions accessibles
					// OU la transition en cours de traitement est justement une transition syst�me
					// V�rifier si ce marquage courant est accessible via la transition "to" recherch�e
					if (outM.getTransition().getId().equals(to.getId()))
					{
						// On a identifi� la transition recherch�e
						// On v�rifie si l'intersection en cours de construction n'aurait pas d�j� des branches identifi�es
						// si tel est le cas on �value les distances pour d�terminer si l'on doit conserver les autres branches
						// ou pas ainsi que cette fin de parcours
						if (intersection.getDistance() == -1 || weight <= intersection.getDistance()){
							if (weight < intersection.getDistance())
								// on �limine les autres branches qui sont plus longues
								intersection = new PathIntersection();
							// ajoute le dernier noeud de la route (pour l'instant constitu�e seulement de la transition
							// recherch�e)
							intersection.addLink(new PathLink(to, null));
							markingSeen.get(fromRef).minDist = weight;
							intersection.setDistance(weight);
						}
						// La cas sinon voudrait dire que le poid de cette derni�re transition qui am�ne � un �tat final est
						// sup�rieur � un autre chemin d�j� identifi�. Pourra se produire si chaque transition a un co�t de
						// franchissement diff�rent (ce n'est actuellement pas le cas...)
						// Dans tous les cas on stoppe la r�cursion (on ne cherche pas � consulter les outMarkings de ce noeud)
					}
					else
					{
						// Poursuivre la r�cursion en demandant les plus courts chemins pour atteindre la transition cible
						// � partir de la r�f�rence du marquage courant
						IPathIntersection childIntersection = getShortestPaths_rec(outM.getRefMarking(), to, markingSeen, systemTransition);
						// On v�rifie que la transition retourn�e est consistante
						if (childIntersection.getLinks().size() > 0)
						{
							// Prise en compte de la distance,  la route calcul�e n'est prise en compte que si elle est plus courte ou
							// �quivalent � la route d�j� calcul�e
							if (markingSeen.get(outM.getRefMarking()).minDist + weight <= markingSeen.get(fromRef).minDist)
							{
								if (markingSeen.get(outM.getRefMarking()).minDist + weight < markingSeen.get(fromRef).minDist)
								{
									// On trouve une route plus courte, on supprime l'ancienne intersection pour en cr�er
									// une nouvelle
									intersection = new PathIntersection();
									// mise � jour de la distance minimale
									int minD = markingSeen.get(outM.getRefMarking()).minDist + weight;
									markingSeen.get(fromRef).minDist = minD;
									intersection.setDistance(minD);
								}
								// Dans les deux cas, on ajoute un lien vers l'intersection retourn� par la r�cursion
								intersection.addLink(new PathLink(outM.getTransition(), childIntersection));
							}
							// Le sinon � ce test intervient si la route retourn�e est plus longue que celle(s) d�j�
							// enregistr�e(s). Dans ce cas l�, on l'ignore.
						}
					}
				}
			}
			
			// on notifie ce marquage comme trait� (tous les fils ont �t� �valu�s)
			markingSeen.get(fromRef).status = "processed";
			markingSeen.get(fromRef).data = intersection;
		}
		else
		{
			// Dans le cas o� la branche en cours a d�j� �t� analys�e en totalit� cel� veut dire
			// que l'on est sur un autre chemin. On r�cup�re donc simplement l'intersection correspondante
			if (markingSeen.get(fromRef).status.equals("processed"))
			{
				// On r�cup�re l'intersection de cette branche d�j� analys�e
				intersection = markingSeen.get(fromRef).data;
			}
			// Dans le cas o� la branche est en cours de traitement (test pr�c�dent faux) cela signifie que l'on est 
			// sur une boucle qui ne m�ne pas � une sortie, on l'ignore donc. Les branches soeurs � cette branche donneront
			// peut �tre une branche valide. On a donc rien � faire.
		}
		return intersection;
	}
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "fromRef" au(x) marquage(s)
	 * permettant de d�clancher la transition "to". Chaque noeud des graphes retourn�s contiennent leur distance � la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
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
	
	/** V�rifie si la transition "t" est accessible dans le graphe quel que soit le marquage du graphe consid�r� */
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
	
	/** V�rifie si le marquage "mark" est accessible dans le graphe � partir de "from". 
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
	 * Cette fonction prend en param�tre la r�f�rence du marquage source ("parentRef"), le
	 * marquage � rechercher ("child") et les marquages d�j� analys�s ("seen").
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
			// Normalement dans le graphe d'accessibilit�, on devrait utiliser la fonction Marking::isEqualTo mais
			// pour que la fonction "isMarkingAccessible_rec" puisse �tre appel�e aussi dans la classe CoverabilityGraph
			// on utilise plut�t Marking::isEquivalentTo. Cela ne pose pas de probl�me dans le cadre d'un graphe
			// d'accessibilit�, car dans ce cas "isEqualTo" et "isEquivalentTo" retournent les m�mes r�sultats car
			// le graphe ne contient pas d'om�gas.
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
	
	/** V�rifie si la transition "t" est pr�sente en amont de "startingMarking" dans le graphe. 
	 * @throws Exception */
	public boolean isPreviouslyEnabled(ITransition t, IMarking startingMarking) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(startingMarking.getCode()))
			throw new Exception("AccessibleGraph::isPreviouslyEnabled => \""+startingMarking.getCode()+"\" is not a known marking");
		return isPreviouslyEnabled_rec (refMarkingByMarkingCode.get(startingMarking.getCode()).id, t, new HashSet<Integer>());
	}
	
	/**
	 * Cette fonction prend en param�tre la r�f�rence du marquage source ("refMarking") la
	 * transition � rechercher ("t") et les marquages d�j� analys�s ("seen").
	 */
	protected boolean isPreviouslyEnabled_rec(int refMarking, ITransition t, HashSet<Integer> seen)
	{
		// check if this current marking hasn't already seen
		if (!seen.contains(refMarking))
		{
			seen.add(refMarking);
			// Obtenir les marquages accessibles � partir du marquage de r�f�rence en cours de traitement
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
	 * V�rifie si la transition "t" est pr�sente en aval de "startingMarking" dans le graphe.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isSubsequentlyEnabled(ITransition t, IMarking startingMarking, ArrayList<String> systemTransition) throws Exception
	{
		if (!refMarkingByMarkingCode.containsKey(startingMarking.getCode()))
			throw new Exception ("AccessibleGraph::isSubsequentlyEnabled => \""+startingMarking.getCode()+"\" is not a known marking");
		return isSubsequentlyEnabled_rec (refMarkingByMarkingCode.get(startingMarking.getCode()).id, t, systemTransition, new HashSet<Integer>());
	}
	
	/**
	 * Cette fonction prend en param�tre la r�f�rence du marquage source ("refMarking"), la
	 * transition � rechercher ("t"), la liste des transitions syst�mes ("systemTransition") et
	 * les marquages d�j� analys�s ("seen").
	 * Si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes.
	 */
	protected boolean isSubsequentlyEnabled_rec(int refMarking, ITransition t, ArrayList<String> systemTransition, HashSet<Integer> seen)
	{
		// check if this current marking hasn't already seen
		if (!seen.contains(refMarking))
		{
			seen.add(refMarking);
			// Obtenir les marquages accessibles � partir du marquage de r�f�rence en cours de traitement
			IAccessibleMarkings localAM = accessibleMarkingsByRefMarking.get(refMarking);
			// recherche d'une transition syst�me parmis les outMarkings accessibles
			boolean exclusive = false;
			if (systemTransition != null)
			{
				for (IIndirectMarking outM : localAM.getOutMarkings())
				{
					if (systemTransition.contains(outM.getTransition().getId()))
						exclusive = true;
				}
			}
			
			// On traite maintenant chaque transition menant aux outMarkings en consid�rant
			// la contrainte d'exclusivit�
			for (IIndirectMarking outM : localAM.getOutMarkings())
			{
				// prise en compte de l'exclusivit�
				if (!exclusive || systemTransition.contains(outM.getTransition().getId()))
				{
					// Ici on est soit dans le cas ou il n'y a pas d'exclusivit� parmis les transitions accessibles
					// OU la transition en cours de traitement est justement une transition syst�me
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
	 * V�rifie si le marquage "to" est un successeur imm�diat du marquage "from".
	 * @throws Exception 
	 */
	public boolean isSuccessorMarking(IMarking from, IMarking to) throws Exception
	{
		// r�cup�ration les marquages en sortie de "from"
		ArrayList<IIndirectMarking> outMarkings = this.getAccessibleMarkings(from).getOutMarkings();
		for (IIndirectMarking outM : outMarkings)
		{
			// On v�rifier si ce marquage accessible n'est pas celui recherch�
			// NOTE IMPORTANTE : Normalement dans le graphe d'accessibilit�, on devrait utiliser la fonction 
			// Marking::isEqualTo mais pour que la fonction "isSuccessorMarking" puisse �tre appel�e aussi dans la
			// classe CoverabilityGraph on utilise plut�t Marking::isEquivalentTo. Cela ne pose pas de probl�me dans
			// le cadre d'un graphe d'accessibilit�, car dans ce cas "isEqualTo" et "isEquivalentTo" retournent les
			// m�mes r�sultats car le graphe ne contient pas d'om�gas.
			if (to.isEquivalentTo(markingByRefMarking.get(outM.getRefMarking()), 1)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calcule et retourne la plus petite distance entre le marquage pass� en param�tre et l'ensemble des marquages possibles
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
			// V�rifier si ce marquage permet d'atteindre la transition "tr"
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
