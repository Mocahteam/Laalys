package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public class PlaceInfo implements IPlaceInfo {
	// A place is defined as an id a name and arcs in and out
	private String id;
	private String placeName;
	// arcs IN of a place are defined as couple {transition:t, weight:w}
	private ArrayList<IArc> arcsIn;
	// arcs OUT of a place are defined as triplet {transition:t, weight:w, type:t}
	private ArrayList<IArc> arcsOut;
	
	public PlaceInfo()
	{
		arcsIn = new ArrayList<IArc>();
		arcsOut = new ArrayList<IArc>();
	}
	
	public ArrayList<IArc> getArcsIn()
	{
		return arcsIn;
	}
	
	public ArrayList<IArc> getArcsOut()
	{
		return arcsOut;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	protected int getIndexTransitionIn(String transitionId)
	{
		// tester la presence d une transition en entree
		for (int i = 0 ; i < arcsIn.size() ; i++)
		{
			if (arcsIn.get(i).getSource() instanceof ITransition && arcsIn.get(i).getSource().getId().equals(transitionId))
			{
				// la transition en parametre est une transition en entree de la place
				return i;
			}
		}
		// la transition en parametre n est pas une transition en entree de la place
		return -1;
	}
	
	protected int getIndexTransitionOut(String transitionId)
	{
		// tester la presence d une transition en sortie
		for (int i = 0 ; i < arcsOut.size() ; i++)
		{
			if (arcsOut.get(i).getTarget() instanceof ITransition && arcsOut.get(i).getTarget().getId().equals(transitionId))
			{
				// la transition en parametre est une transition en sortie de la place
				return i;
			}
		}
		// la transition en parametre n est pas une transition en sortie de la place
		return -1;
	}
	
	public String getName()
	{
		return this.placeName;
	}
	
	public void setArcIn(ITransition t, int w)
	{
		if (getIndexTransitionIn(t.getId()) == -1)
		{
			arcsIn.add(new Arc(t, this, w, Arc.REGULAR_ARC));
		}
	}
	
	public void setArcOut(ITransition t, int w, String type)
	{
		if (getIndexTransitionOut(t.getId()) == -1)
		{
			arcsOut.add(new Arc(this, t, w, type));
		}
	}
	
	public void setArcsIn(ArrayList<IArc> arcs)
	{
		arcsIn = arcs;
	}
	
	public void setArcsOut(ArrayList<IArc> arcs)
	{
		arcsOut = arcs;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setName(String name)
	{
		this.placeName = name;
	}
}
