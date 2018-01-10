package fr.lip6.mocah.laalys.features;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */
public interface IFeatures {
	/**
	 * check if the transition which the id is given as argument is a system transition
	 * @param	id of the transition
	 * @return true/false
	 */
	public boolean isSystem (String id);
	/**
	 * check if the transition which the id is given as argument is a end transition
	 * @param	id of the transition
	 * @return true/false
	 */
	public boolean isEnd (String id);
	/**
	 * Returns the readable human name associated to an id. If no human name is associated to this id the id is returned.
	 * @param id of the transition
	 * @return the readable public name 
	 */
	public String getPublicName (String id);
	/**
	 * remove all specifications
	 */
	public void reset();
	/**
	 * load the file for parsing it
	 * @see parse function
	 * @param	filePath
	 */
	public void loadFile (String url);
	/**
	 * renvoie la liste de toutes les transitions systemes
	 * @return
	 */
	public ArrayList<String> getSystemTransitions ();
	/**
	 * renvoie la listes de toutes les transitions fin de niveau
	 * @return
	 */
	public ArrayList<String> getEndLevelTransitions ();
}
