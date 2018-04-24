package fr.lip6.mocah.laalys.labeling;

import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;

/**
 * ...
 * @author Mathieu Muratet, Cl�ment Rouanet, Amel Yessad
 */
public interface ILabeling {
	/**
	 * exporte l'analyse sous la forme d'un fichier au format .graphml
	 * @param	exportPath : emplacement dans lequel l'exportation va �tre r�alis�e
	 * @throws Exception 
	 */
	public void export(String exportPath) throws Exception;
	
	/**
	 * lance l'analyse de toutes les actions contenu dans "traces"
	 * @param	traces : liste des traces � analyser
	 * @throws Exception 
	 */
	public void label( ITraces traces ) throws Exception;
	

	/**
	 * labellise une action
	 * @param action
	 * @throws Exception
	 */
	public void labelAction( ITrace action ) throws Exception;
	
	/**
	 * Get the name of the next better action to perform
	 * @return
	 * @throws Exception 
	 */
	public String getNextBetterAction () throws Exception;
	
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
