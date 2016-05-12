package fr.lip6.mocah.laalys.petrinet;

import java.util.Vector;

/**
 * ...
 * @author Mathieu Muratet
 */
public class Transition implements ITransition {
	// A transition is defined as an id a name and arcs in and out
	private String id;
	private String transitionName;
	// arcs IN of a transition are defined as triplet {transition:t, weight:w, type:t}	
	private Vector<IArc> arcsIn;
	// arcs OUT of a transition are defined as couple {transition:t, weight:w}
	private Vector<IArc> arcsOut;
	
	public Transition()
	{
		arcsIn = new Vector<IArc>();
		arcsOut = new Vector<IArc>();
	}
	
	public Vector<IArc> getArcsIn()
	{
		return arcsIn;
	}
	
	public Vector<IArc> getArcsOut()
	{
		return arcsOut;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	protected int getIndexPlaceIn(String placeId)
	{
		// tester la presence d une place en entree
		for (int i = 0 ; i < arcsIn.size() ; i++)
		{
			if (arcsIn.get(i).getSource() instanceof IPlaceInfo && arcsIn.get(i).getSource().getId().equals(placeId))
			{
				// la place en parametre est une place en entree de la transition
				return i;
			}
		}
		// la place en parametre n est pas une place en entree de la transition
		return -1;
	}
	
	protected int getIndexPlaceOut(String placeId)
	{
		// tester la presence d une place en sortie
		for (int i = 0 ; i < arcsOut.size() ; i++)
		{
			if (arcsOut.get(i).getTarget() instanceof IPlaceInfo && arcsOut.get(i).getTarget().getId().equals(placeId))
			{
				// la place en parametre est une place en sortie de la transition
				return i;
			}
		}
		// la place en parametre n est pas une place en sortie de la transition
		return -1;
	}
	
	public String getName()
	{
		return this.transitionName;
	}
	
	public void setArcIn(IPlaceInfo p, int w, String type)
	{
		int i = getIndexPlaceIn(p.getId());
		if (i == -1)
		{
			arcsIn.add(new Arc(p, this, w, type));
		}
	}
	
	public void setArcOut(IPlaceInfo p, int w)
	{
		if (getIndexPlaceOut(p.getId()) == -1)
		{
			arcsOut.add(new Arc(this, p, w, Arc.REGULAR_ARC));
		}
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setName(String name)
	{
		this.transitionName = name;
	}
	
	public String toString()
	{
		return getId();
	}
}
