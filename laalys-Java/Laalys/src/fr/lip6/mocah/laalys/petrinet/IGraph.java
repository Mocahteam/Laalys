package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IGraph {
	
	public void computeGraph();
	
	/** 
	 * V�rifie si le marquage "mark" est inclus dans le graphe
	 * Si "mark" n'est pas exactement pr�sent dans le graphe (cas du graphe de couverture), on recherche un marquage 
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
	 * permettant de d�clancher la transition "to". Chaque noeud des graphes retourn�s contiennent leur distance � la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 */
	public Vector<IPathIntersection> getShortestPathsToTransition (IMarking from, ITransition to, Vector<String> systemTransition);
	
	public String getType();
	
	/** V�rifie si la transition "t" est accessible dans le graphe quel que soit le marquage du graphe consid�r� */
	public boolean isAlwaysEnabled (ITransition t);
	
	/** V�rifie si le marquage "mark" est accessible dans le graphe � partir de "from". */
	public boolean isMarkingAccessible (IMarking mark, IMarking from);
	
	/** V�rifie si la transition "t" est pr�sente en amont de "startingMarking" dans le graphe. */
	public boolean isPreviouslyEnabled (ITransition t, IMarking startingMarking);
	
	/**
	 * V�rifie si la transition "t" est pr�sente en aval de "startingMarking" dans le graphe.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 */
	public boolean isSubsequentlyEnabled (ITransition t, IMarking startingMarking, Vector<String> systemTransition);
	
	/**
	 * V�rifie si le marquage "to" est un successeur imm�diat du marquage "from".
	 */
	public boolean isSuccessorMarking (IMarking from, IMarking to);
	
	/**
	 * Calcule et retourne la plus petite distance entre le marquage pass� en param�tre et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr". La distance entre deux marquages est la somme des valeurs absolues
	 * des differences des marquages de chaque place.
	 */
	public int minimalDistanceWith(IMarking marking, ITransition tr);
	
	public void print();
	
	public String toString();
}
