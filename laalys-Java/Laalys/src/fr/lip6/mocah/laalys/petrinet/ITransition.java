package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface ITransition extends INode {
	public Vector<IArc> getArcsIn();
	
	public Vector<IArc> getArcsOut();
	
	public void setArcIn (IPlaceInfo place, int weight, String type);
	
	public void setArcOut (IPlaceInfo place, int weight);
}
