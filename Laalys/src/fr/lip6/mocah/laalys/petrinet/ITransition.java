package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface ITransition extends INode {
	public void setArcIn(IPlaceInfo p, int w, String type);
	public void setArcOut(IPlaceInfo p, int w);
}
