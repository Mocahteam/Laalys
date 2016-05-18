package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IAccessibleMarkings {
	public void addInMarking (IIndirectMarking inMarking);
	
	public void addOutMarking(IIndirectMarking outMarking);
	
	public ArrayList<IIndirectMarking> getInMarkings();
	
	public int getRefMarking();
	
	public ArrayList<IIndirectMarking> getOutMarkings();
}
