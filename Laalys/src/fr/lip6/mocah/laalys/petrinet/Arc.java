package fr.lip6.mocah.laalys.petrinet;

/**
 * ...
 * @author Mathieu Muratet
 */
public class Arc implements IArc {
	/**
	 * enable target transition if source place holds more token than the weight arc.
	 * Consumes the number of tokens equivalent to the weight arc.
	 */
	public static final String REGULAR_ARC = "regular";
	/**
	 * enable target transition if source place holds more token than the weight arc.
	 * Consumes no token. 
	 */
	public static final String READ_ARC = "read";
	/**
	 * enable target transition if source place holds less token than the weight arc.
	 * Consumes no token.
	 */
	public static final String INHIBITOR_ARC = "inhibitor";
	
	private INode src;
	private INode trg;
	private int weight;
	private String type;
	
	public Arc(INode source, INode target, int w, String t)
	{
		src = source;
		trg = target;
		weight = w;
		type = t;
	}
	
	public INode getSource()
	{
		return src;
	}
	
	public INode getTarget()
	{
		return trg;
	}
	
	public String getType()
	{
		return type;
	}
	
	public int getWeight()
	{
		return weight;
	}

	public boolean Equals (IArc compWith){
		return this.src.getId().equals(compWith.getSource().getId())
				&& this.trg.getId().equals(compWith.getTarget().getId())
				&& this.weight == compWith.getWeight()
				&& this.type.equals(compWith.getType());
	}
}
