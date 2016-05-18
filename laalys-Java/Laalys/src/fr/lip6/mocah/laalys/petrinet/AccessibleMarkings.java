package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public class AccessibleMarkings implements IAccessibleMarkings {
	private int marking;
	private ArrayList<IIndirectMarking> inMarkings;
	private ArrayList<IIndirectMarking> outMarkings;
	
	public AccessibleMarkings(int m)
	{
		marking = m;
		inMarkings = new ArrayList<IIndirectMarking>();
		outMarkings = new ArrayList<IIndirectMarking>();
	}
	
	public void addInMarking(IIndirectMarking inMarking)
	{
		inMarkings.add(inMarking);
	}
	
	public void addOutMarking(IIndirectMarking outMarking)
	{
		outMarkings.add(outMarking);
	}
	
	public ArrayList<IIndirectMarking> getInMarkings()
	{
		return inMarkings;
	}
	
	public int getRefMarking()
	{
		return marking;
	}
	
	public ArrayList<IIndirectMarking> getOutMarkings()
	{
		return outMarkings;
	}
}
