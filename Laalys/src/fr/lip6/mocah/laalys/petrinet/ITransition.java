package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface ITransition extends INode {
	public ArrayList<IArc> getArcsIn();
	
	public ArrayList<IArc> getArcsOut();
	
	public void setArcIn (IPlaceInfo place, int weight, String type);
	
	public void setArcOut (IPlaceInfo place, int weight);
}
