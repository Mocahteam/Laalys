package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface INode {
	public void setId(String id);
	public String getId();
	
	public void setName (String name);
	public String getName();	

	public ArrayList<IArc> getArcsIn();
	public ArrayList<IArc> getArcsOut();
}
