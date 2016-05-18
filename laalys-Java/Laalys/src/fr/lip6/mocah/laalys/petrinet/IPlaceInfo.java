package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPlaceInfo extends INode {
	public ArrayList<IArc> getArcsIn();
	public ArrayList<IArc> getArcsOut();
	public void setArcIn (ITransition tr, int weight);
	public void setArcOut (ITransition tr, int weight, String type);
	public void setArcsIn (ArrayList<IArc> arcsIn);
	public void setArcsOut (ArrayList<IArc> arcsOut);
}
