package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IGraph {
	
	public void computeGraph();
	
	/** 
	 * Vérifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement présent dans le graphe (cas du graphe de couverture), on recherche un marquage 
	 * englobant.
	 */
	public boolean contains (IMarking mark);
	
	public IAccessibleMarkings getAccessibleMarkings(IMarking mark);
	public Vector<IMarking> getAllMarkings();
	public IMarking getInitialMarking();
	/**
	 * Renvoie l'ensemble des marquages les plus proches de "mark" qui permettent d'atteindre la transition "tr".
	 * Par plus proche on entend les marquages (satisfaisant la contrainte d'atteinte de la transition) pour lesquels
	 * la distance IMarking::distanceWith() est minimale.
	 */
	public Vector<IMarking> getNearestMarkings (IMarking mark, ITransition tr);
	public int getRefMarking (IMarking mark);
	public IMarking getMarkingByRef (int refMarking);
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "fromRef" au(x) marquage(s)
	 * permettant de déclancher la transition "to". Chaque noeud des graphes retournés contiennent leur distance à la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes. Dans ces cas là les transitions systèmes sont considérées avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 */
	public Vector<IPathIntersection> getShortestPathsToTransition (IMarking from, ITransition to, Vector<String> systemTransition);
	
	public String getType();
	
	/** Vérifie si la transition "t" est accessible dans le graphe quel que soit le marquage du graphe considéré */
	public boolean isAlwaysEnabled (ITransition t);
	
	/** Vérifie si le marquage "mark" est accessible dans le graphe à partir de "from". */
	public boolean isMarkingAccessible (IMarking mark, IMarking from);
	
	/** Vérifie si la transition "t" est présente en amont de "startingMarking" dans le graphe. */
	public boolean isPreviouslyEnabled (ITransition t, IMarking startingMarking);
	
	/**
	 * Vérifie si la transition "t" est présente en aval de "startingMarking" dans le graphe.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 */
	public boolean isSubsequentlyEnabled (ITransition t, IMarking startingMarking, Vector<String> systemTransition);
	
	/**
	 * Vérifie si le marquage "to" est un successeur immédiat du marquage "from".
	 */
	public boolean isSuccessorMarking (IMarking from, IMarking to);
	
	/**
	 * Calcule et retourne la plus petite distance entre le marquage passé en paramètre et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr". La distance entre deux marquages est la somme des valeurs absolues
	 * des differences des marquages de chaque place.
	 */
	public int minimalDistanceWith(IMarking marking, ITransition tr);
	
	public void print();
	
	public String toString();
}
