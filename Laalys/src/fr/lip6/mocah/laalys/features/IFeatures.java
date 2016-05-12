package fr.lip6.mocah.laalys.features;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */
public interface IFeatures {
	/**
	 * check if the transition which the id is given as argument is a system transiton
	 * @param	id of the transition
	 * @return true/false
	 */
	public boolean isSystem (String id);
	/**
	 * check if the transition which the id is given as argument is a end transiton
	 * @param	id of the transition
	 * @return true/false
	 */
	public boolean isEnd (String id);
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
	public Vector<String> getSystemTransitions ();
	/**
	 * renvoie la listes de toutes les transitions fin de niveau
	 * @return
	 */
	public Vector<String> getEndLevelTransitions ();
}
