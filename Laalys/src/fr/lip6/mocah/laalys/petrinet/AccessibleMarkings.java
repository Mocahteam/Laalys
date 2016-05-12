package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public class AccessibleMarkings implements IAccessibleMarkings {
	private int marking;
	private Vector<IIndirectMarking> inMarkings;
	private Vector<IIndirectMarking> outMarkings;
	
	public AccessibleMarkings(int m)
	{
		marking = m;
		inMarkings = new Vector<IIndirectMarking>();
		outMarkings = new Vector<IIndirectMarking>();
	}
	
	public void addInMarking(IIndirectMarking inMarking)
	{
		inMarkings.add(inMarking);
	}
	
	public void addOutMarking(IIndirectMarking outMarking)
	{
		outMarkings.add(outMarking);
	}
	
	public Vector<IIndirectMarking> getInMarkings()
	{
		return inMarkings;
	}
	
	public int getRefMarking()
	{
		return marking;
	}
	
	public Vector<IIndirectMarking> getOutMarkings()
	{
		return outMarkings;
	}
}
