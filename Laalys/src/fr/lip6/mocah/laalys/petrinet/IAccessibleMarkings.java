package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IAccessibleMarkings {
	public void addInMarking (IIndirectMarking inMarking);
	
	public void addOutMarking(IIndirectMarking outMarking);
	
	public Vector<IIndirectMarking> getInMarkings();
	
	public int getRefMarking();
	
	public Vector<IIndirectMarking> getOutMarkings();
}
