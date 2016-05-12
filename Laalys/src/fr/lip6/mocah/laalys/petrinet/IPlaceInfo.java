package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPlaceInfo extends INode {
	public Vector<IArc> getArcsIn();
	public Vector<IArc> getArcsOut();
	public void setArcIn (ITransition tr, int weight);
	public void setArcOut (ITransition tr, int weight, String type);
	public void setArcsIn (Vector<IArc> arcsIn);
	public void setArcsOut (Vector<IArc> arcsOut);
}
