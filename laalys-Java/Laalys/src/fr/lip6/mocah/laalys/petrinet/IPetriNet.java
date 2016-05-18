package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPetriNet {
	public boolean changeStatePetriNetById (String t) throws Exception;
	public boolean changeStatePetriNet (ITransition t);
	
	/** V�rifie si le marquage "mark" est inclus dans l'ensemble des marquages accessible */
	public boolean contains(IMarking mark);
	
	public void createArcPl2Tr (String idPlace, String idTransition, int weight, String arcType) throws Exception;
	
	public void createArcTr2Pl (String idTransition, String idPlace, int weight) throws Exception;
	
	/**
	 * Charge le fichier XML � partir de "url" et construit le RdP.
	 * Cette fonction peut charger :
	 *  - des fichiers ".xml" (respectant le format de PiPe3.0)
	 *  - des fichiers ".pnml"
	 */
	public void createPetriNet(String url);
	
	public void createPlace (String id, String name) throws Exception;
	
	public void createTransition (String id, String name) throws Exception;
	
	/** Check if a transition could be fired in the current state of the petri net */
	public boolean enabledTransition (ITransition t);
	
	public String getAccessibleGraphString();
	
	public ArrayList<IMarking> getAllPossibleMarkings();
	
	/** returns transitions that can be fired in accordance with the current Petri Net state */
	public ArrayList<ITransition> getCurrentActivatedTransitions();
	
	/**
	 * renvoie le nombre de jeton present dans la place dans le marquage initial
	 * -1 si aucune des places presentes dans le rdp n'a le m�me id que la place pass� en argument
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
	 * -1 si aucune des places presentes dans le rdp n'a le m�me id que la place pass� en argument
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
	 * @throws Exception 
	 */
	public ArrayList<IMarking> getNearestMarkings (IMarking mark, ITransition tr) throws Exception;
	
	/** @return null if no place is found */
	public IPlaceInfo getPlaceById (String placeId);
	
	public ArrayList<IPlaceInfo> getPlaces();
	
	/**
	 * returns places in of the "t" transition that haven't required tokens
	 * if arc between a place in and "t" is a regular arc or a read arc, the place haven't required tokens if the number of token inside the place is lesser than the weight of the arc
	 * if arc between a place in and "t" is inhibitor arc, the place haven't required tokens if the number of token inside the place is highter or equal than the weight of the arc
	 */
	public ArrayList<IPlaceInfo> getPlacesWithoutRequiredTokens (ITransition t);
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "startMarking" au(x) marquage(s)
	 * permettant de d�clancher la transition "target". Chaque noeud des graphes retourn�s contiennent leur distance � la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 * @throws Exception 
	 */
	public ArrayList<IPathIntersection> getShortestPathsToTransition (IMarking startMarking, ITransition target, ArrayList<String> systemTransition) throws Exception;
	
	/** @return null if no transition is found */
	public ITransition getTransitionById (String transitionId);
	
	public ArrayList<ITransition> getTransitions();
	
	/** @return la liste des transition contenant dans leur nom le keyWord */
	public ArrayList<ITransition> getTransitionsByKeyWord(String keyWord);
	
	/**
	 * set initial marking equal to the current marking.
	 * compute the accessible/coverability graph if require
	 * @throws Exception 
	 */
	public void initialization() throws Exception;
	
	public void initializeTokens (String placeId, int initialToken) throws Exception;
	
	/** Check if the transition "t" is quasi-alive for every marking of the reachableGraph 
	 * @throws Exception */
	public boolean isAlwaysAlive(ITransition t) throws Exception;
	
	/**
	 * Check if the transition "t" is belated. A transition is belated if it is not enabled, previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isBelated (ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/** Check if the petri net is in deadlock. No transition can be fired. */
	public boolean isDeadlock ();
	
	/**
	 * Check if the transition "t" is inserted. A transition is inserted if it is not enabled, previously enabled in the
	 * graph of possible states and subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isInserted (ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * Check if the transition "t" is premature. A transition is premature if it is not enabled, not previously enabled in
	 * the graph of possible states and subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isPremature (ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * V�rifie si une transition "t" a �t� pr�c�dement sensibilis�e par rapport � l'�tat courant.
	 * @throws Exception 
	 */
	public boolean isPreviouslyEnabled (ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * V�rifie si la transition "t" est quasi vivante � partir de l'�tat initial. Une transition est dite quasi vivante
	 * si � partir d'un �tat donn�, la transition pourra �tre sensibilis�e.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront ignor�es �
	 * l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isQuasiAlive(ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * V�rifie si la transition "t" est quasi vivante � partir d'un �tat donn� ("marking"). Une transition est dite quasi
	 * vivante si � partir d'un �tat donn�, la transition pourra �tre sensibilis�e.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isQuasiAliveFromMarking(ITransition t, IMarking marking, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * V�rifie si le marquage "to" est accessible dans le graphe � partir de "from".
	 * @throws Exception 
	 */
	public boolean isReachable (IMarking from, IMarking to) throws Exception;
	
	/**
	 * V�rifie si le marquage "marking" est un successeur imm�diat du marquage courant.
	 * @throws Exception 
	 */
	public boolean isSuccessorMarking (IMarking marking) throws Exception;
	
	/**
	 * Check if the transition "t" is useless. A transition is useless if it is not enabled, not previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront ignor�es �
	 * l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isUseless (ITransition t, ArrayList<String> systemTransition) throws Exception;
	
	/**
	 * Calcule la plus petite distance entre le marquage pass� en param�tre et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr"
	 * @param marking
	 * @return la plus petite distance
	 * @throws Exception 
	 */
	public int minimalDistanceWith (IMarking marking, ITransition tr) throws Exception;
	
	public void printAccessibleGraph() throws Exception;
	
	public void resetCurrentMarkings();	
	
	public void setCurrentMarkings (IMarking m);
	
	public void setId (String str);
	
	public void setName (String str);
	
	public void setPlaces (ArrayList<IPlaceInfo> places);
	
	public void setTransitions (ArrayList<ITransition> trans);
}
