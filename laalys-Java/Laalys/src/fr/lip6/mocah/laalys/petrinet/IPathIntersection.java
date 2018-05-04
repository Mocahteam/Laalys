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
	 * Vérifie si cette intersection est identique à celle passée en paramètre ("path").
	 */
	public boolean isEqualWith (IPathIntersection path);
	
	public void removeLink (IPathLink link);
	
	/**
	 * Si logger == null affichage sur la sortie standard
	 * @param logger
	 */
	public void print(Logger logger);
	
	/**
	 * Recherche dans cette intersection un (ou plusieurs) lien(s) défini(s) par la transition "tr"
	 * passée en paramètre
	 */
	public void removeLinksByTransition (ITransition tr);
	
	public void setDistance (int dist);
}
