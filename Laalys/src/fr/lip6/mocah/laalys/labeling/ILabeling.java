package fr.lip6.mocah.laalys.labeling;

import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet, Amel Yessad
 */
public interface ILabeling {
	/**
	 * exporte l'analyse sous la forme d'un fichier au format .graphml
	 * @param	exportPath : emplacement dans lequel l'exportation va être réalisée
	 * @throws Exception 
	 */
	public void export(String exportPath) throws Exception;
	

	/**
	 * labellise une action
	 * @param action
	 * @throws Exception
	 */
	public void labelAction( ITrace action ) throws Exception;
	
	/**
	 * Get the name of the next better action to perform to reach target action name. Try to compute a path between current
	 * state and the target action name. If found returns the first action of this path, if not found returns empty string "".
	 * @param targetActionName the action name to reach. Use "end" key word to target expertEndTransitions.
	 * @param maxActions the maximum number of actions returned.
	 * @return a string containing all name actions separated by "\t" separator
	 * @throws Exception 
	 */
	public String getNextBetterActionsToReach (String targetActionName, int maxActions) throws Exception;
	
	/**
	 * Try to identify bad choices and missing actions to reach one of the end transitions
	 * @return list of bad choices and missing actions to reach one of the end transitions
	 * @throws Exception 
	 */
	public ITraces analyseTransitionEndStep() throws Exception;
	/**
	 * re initialise toutes les variables interne pour ne pas
	 * risquer de fausser l'analyse d'une autre trace
	 * @throws Exception 
	 */
	public void reset() throws Exception;
	
	/**
	 * GETTERS
	 */
	public IPetriNet getCompletePN();
	public IPetriNet getFilteredPN();
	public IFeatures getFeatures();
	
	/**
	 * SETTERS
	 */
	public void setCompletePN(IPetriNet pn);
	public void setFilteredPN(IPetriNet pn);
	public void setFeatures(IFeatures value);
}
