package fr.lip6.mocah.laalys.petrinet;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IArc {
	public INode getSource();
	public INode getTarget();
	public String getType();
	public int getWeight();
}
