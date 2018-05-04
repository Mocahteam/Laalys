package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPathIntersection {
	public void addLink (IPathLink link);
	
	public int getDistance();
	
	public ArrayList<IPathLink> getLinks();
	
	/**
	 * V�rifie si cette intersection est identique � celle pass�e en param�tre ("path").
	 */
	public boolean isEqualWith (IPathIntersection path);
	
	public void removeLink (IPathLink link);
	
	/**
	 * Si logger == null affichage sur la sortie standard
	 * @param logger
	 */
	public void print(Logger logger);
	
	/**
	 * Recherche dans cette intersection un (ou plusieurs) lien(s) d�fini(s) par la transition "tr"
	 * pass�e en param�tre
	 */
	public void removeLinksByTransition (ITransition tr);
	
	public void setDistance (int dist);
}
