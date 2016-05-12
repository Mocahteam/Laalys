package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPetriNet {
	public boolean changeStatePetriNetById (String t);
	public boolean changeStatePetriNet (ITransition t);
	
	/** Vérifie si le marquage "mark" est inclus dans l'ensemble des marquages accessible */
	public boolean contains(IMarking mark);
	
	public void createArcPl2Tr (String idPlace, String idTransition, int weight, String arcType);
	
	public void createArcTr2Pl (String idTransition, String idPlace, int weight);
	
	/**
	 * Charge le fichier XML à partir de "url" et construit le RdP.
	 * Cette fonction peut charger :
	 *  - des fichiers ".xml" (respectant le format de PiPe3.0)
	 *  - des fichiers ".pnml"
	 */
	public void createPetriNet(String url);
	
	public void createPlace (String id, String name);
	
	public void createTransition (String id, String name);
	
	/** Check if a transition could be fired in the current state of the petri net */
	public boolean enabledTransition (ITransition t);
	
	public String getAccessibleGraphString();
	
	public Vector<IMarking> getAllPossibleMarkings();
	
	/** returns transitions that can be fired in accordance with the current Petri Net state */
	public Vector<ITransition> getCurrentActivatedTransitions();
	
	/**
	 * renvoie le nombre de jeton present dans la place dans le marquage initial
	 * -1 si aucune des places presentes dans le rdp n'a le même id que la place passé en argument
	 */
	public int getCurrentMarking (String placeId);
	
	public IMarking getCurrentMarkings();
	
	/**
	 * renvoie la strategie choisie
	 */
	public String getGlobalStrategy();
	
	public IGraph getGraph();
	
	public String getId();
	
	/**
	 * renvoie le nombre de jeton present dans la place dans le marquage initial
	 * -1 si aucune des places presentes dans le rdp n'a le même id que la place passé en argument
	 */
	public int getInitialMarking (String placeId);
	
	public IMarking getInitialMarkings();
	
	/**
	 * renvoie le type de graph que l'on a demande
	 */
	public String getKindOfGraph();
	
	/**
	 * renvoie le marquage local a  la transition
	 * ie : le marquage des places directement connectees (en entree ou en sortie) a la  transition
	 * @param	tr
	 * @return
	 */
	public IMarking getLocalMarking (ITransition tr);
	
	public String getName();
	
	/**
	 * Renvoie l'ensemble des marquages les plus proches de "mark" qui permettent d'atteindre la transition "tr".
	 * Par plus proche on entend les marquages (satisfaisant la contrainte d'atteinte de la transition) pour lesquels
	 * la distance IMarking::distanceWith() est minimale.
	 */
	public Vector<IMarking> getNearestMarkings (IMarking mark, ITransition tr);
	
	/** @return null if no place is found */
	public IPlaceInfo getPlaceById (String placeId);
	
	public Vector<IPlaceInfo> getPlaces();
	
	/**
	 * returns places in of the "t" transition that haven't required tokens
	 * if arc between a place in and "t" is a regular arc or a read arc, the place haven't required tokens if the number of token inside the place is lesser than the weight of the arc
	 * if arc between a place in and "t" is inhibitor arc, the place haven't required tokens if the number of token inside the place is highter or equal than the weight of the arc
	 */
	public Vector<IPlaceInfo> getPlacesWithoutRequiredTokens (ITransition t);
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "startMarking" au(x) marquage(s)
	 * permettant de déclancher la transition "target". Chaque noeud des graphes retournés contiennent leur distance à la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes. Dans ces cas là les transitions systèmes sont considérées avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 */
	public Vector<IPathIntersection> getShortestPathsToTransition (IMarking startMarking, ITransition target, Vector<String> systemTransition);
	
	/** @return null if no transition is found */
	public ITransition getTransitionById (String transitionId);
	
	public Vector<ITransition> getTransitions();
	
	/** @return la liste des transition contenant dans leur nom le keyWord */
	public Vector<ITransition> getTransitionsByKeyWord(String keyWord);
	
	/**
	 * set initial marking equal to the current marking.
	 * compute the accessible/coverability graph if require
	 */
	public void initialization();
	
	public void initializeTokens (String placeId, int initialToken);
	
	/** Check if the transition "t" is quasi-alive for every marking of the reachableGraph */
	public boolean isAlwaysAlive(ITransition t);
	
	/**
	 * Check if the transition "t" is belated. A transition is belated if it is not enabled, previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 */
	public boolean isBelated (ITransition t, Vector<String> systemTransition);
	
	/** Check if the petri net is in deadlock. No transition can be fired. */
	public boolean isDeadlock ();
	
	/**
	 * Check if the transition "t" is inserted. A transition is inserted if it is not enabled, previously enabled in the
	 * graph of possible states and subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 */
	public boolean isInserted (ITransition t, Vector<String> systemTransition);
	
	/**
	 * Check if the transition "t" is premature. A transition is premature if it is not enabled, not previously enabled in
	 * the graph of possible states and subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 */
	public boolean isPremature (ITransition t, Vector<String> systemTransition);
	
	/**
	 * Vérifie si une transition "t" a été précédement sensibilisée par rapport à l'état courant.
	 */
	public boolean isPreviouslyEnabled (ITransition t, Vector<String> systemTransition);
	
	/**
	 * Vérifie si la transition "t" est quasi vivante à partir de l'état initial. Une transition est dite quasi vivante
	 * si à partir d'un état donné, la transition pourra être sensibilisée.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront ignorées à
	 * l'exception des transitions systèmes
	 */
	public boolean isQuasiAlive(ITransition t, Vector<String> systemTransition);
	
	/**
	 * Vérifie si la transition "t" est quasi vivante à partir d'un état donné ("marking"). Une transition est dite quasi
	 * vivante si à partir d'un état donné, la transition pourra être sensibilisée.
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront
	 * ignorées à l'exception des transitions systèmes
	 */
	public boolean isQuasiAliveFromMarking(ITransition t, IMarking marking, Vector<String> systemTransition);
	
	/**
	 * Vérifie si le marquage "to" est accessible dans le graphe à partir de "from".
	 */
	public boolean isReachable (IMarking from, IMarking to);
	
	/**
	 * Vérifie si le marquage "marking" est un successeur immédiat du marquage courant.
	 */
	public boolean isSuccessorMarking (IMarking marking);
	
	/**
	 * Check if the transition "t" is useless. A transition is useless if it is not enabled, not previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connecté (en sortie) à au moins une transition système (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connectées (en sortie) à ce marquage seront ignorées à
	 * l'exception des transitions systèmes
	 */
	public boolean isUseless (ITransition t, Vector<String> systemTransition);
	
	/**
	 * Calcule la plus petite distance entre le marquage passé en paramètre et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr"
	 * @param marking
	 * @return la plus petite distance
	 */
	public int minimalDistanceWith (IMarking marking, ITransition tr);
	
	public void printAccessibleGraph();
	
	public void resetCurrentMarkings();	
	
	public void setCurrentMarkings (IMarking m);
	
	public void setId (String str);
	
	public void setName (String str);
	
	public void setPlaces (Vector<IPlaceInfo> places);
	
	public void setTransitions (Vector<ITransition> trans);
}
