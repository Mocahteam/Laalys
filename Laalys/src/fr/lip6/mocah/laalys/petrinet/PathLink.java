package fr.lip6.mocah.laalys.petrinet;

/**
 * ...
 * @author Mathieu Muratet
 */
public class PathLink implements IPathLink {
	private ITransition link;
	private IPathIntersection to;
	
	public PathLink(ITransition link, IPathIntersection nextIntersection)
	{
		this.link = link;
		to = nextIntersection;
	}
	
	public ITransition getLink()
	{
		return link;
	}
	
	public IPathIntersection getNextIntersection()
	{
		return to;
	}
}
