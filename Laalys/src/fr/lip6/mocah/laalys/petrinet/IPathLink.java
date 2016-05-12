package fr.lip6.mocah.laalys.petrinet;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IPathLink {
	public ITransition getLink();
	public IPathIntersection getNextIntersection();
}
