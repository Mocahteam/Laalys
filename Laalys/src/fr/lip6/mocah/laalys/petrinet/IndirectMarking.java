package fr.lip6.mocah.laalys.petrinet;

/**
 * ...
 * @author Mathieu Muratet
 */
public class IndirectMarking implements IIndirectMarking {
	private ITransition transition;
	private int refMarking;
	
	public IndirectMarking(ITransition tr, int mark)
	{
		transition = tr;
		refMarking = mark;
	}
	
	public int getRefMarking()
	{
		return refMarking;
	}
	
	public ITransition getTransition()
	{
		return transition;
	}
}
