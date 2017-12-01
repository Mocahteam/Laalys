package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
