package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public class Marking implements IMarking {
	private ArrayList<Integer> marking;
	private String code;
	
	public Marking()
	{
		marking = new ArrayList<Integer>();
		code = "";
	}
	
	public IMarking clone()
	{
		// copy marking source to another
		IMarking myClone = new Marking();
		for (int element : marking)
			myClone.pushToken(element);
		return myClone;
	}
	
	/**
	 * "this" couvre "mark" ssi "mark" est inf�rieur ou �gal � "this" (c�d il n'y a pas de place dans mark dont le nombre jeton est strictement
	 * sup�rieur a son �quivalent dans "this")
	 */
	public boolean cover(IMarking mark)
	{
		// parcourir toutes les places
		for (int i = 0 ; i < this.marking.size() ; i++)
		{
			// si on trouve une place pour laquelle le nombre de jeton de "mark" est strictement sup�rieur � son �quivalent dans "this" alors
			// "this" ne couvre pas "mark"
			if (mark.getTokenAt(i) > this.marking.get(i).intValue())
				return false;
		}
		// aucune place de "mark" ne contient plus de jeton que sa correspondante dans "this"
		return true;
	}
	
	/**
	 * Calcule la distance de marquage avec le marquage pass� en param�tre
	 * la distance est la somme de la valeur absolue de la differences de ces places
	 * @param marking
	 * @param minimalWeight poid minimal d�finissant la limite d'ajout des om�gas dans 
	 * le cas d'un graphe de couverture
	 * @return la distance entre les marquages
	 */
	public int distanceWith(IMarking mark, int minimalWeight)
	{
		if (mark.getLength() != this.getLength())
			throw new Error("\"Marking::distanceWith\" : les marquages ne sont pas de la m�me taille");
		
		int diff = 0;
		for (int i = 0; i < this.getLength(); i++)
		{
			// Prise en compte du omega dans le cas d'un graphe de couverture
			if (this.getTokenAt(i) != Integer.MAX_VALUE && mark.getTokenAt(i) != Integer.MAX_VALUE)
				// les deux marquages n'ont pas de om�ga => On calcule la diff�rence
				diff += Math.abs(this.getTokenAt(i) - mark.getTokenAt(i));
			else if (this.getTokenAt(i) == Integer.MAX_VALUE) { // peut �tre que l'un des deux marquages a un om�ga, on regarde si c'est "this"
				// on v�rifie du coup si le param�tre n'a pas une valeur plus petite que le poids minimum
				if (mark.getTokenAt(i) < minimalWeight)
					// on comptabilise la diff�rence avec le poid minimal
					diff += Math.abs(mark.getTokenAt(i) - minimalWeight);
			} else if (mark.getTokenAt(i) == Integer.MAX_VALUE){ // ce n'�tait pas le cas pour "this" on regarde pour le param�tre "mark"
				// on v�rifie du coup si "this" n'a pas une valeur plus petite que le poids minimum
				if (this.getTokenAt(i) < minimalWeight)
					// on comptabilise la diff�rence avec le poid minimal
					diff += Math.abs(this.getTokenAt(i) - minimalWeight);
			}
			// Pas de sinon, en effet dans ce cas cela signifie que les deux marquages
			// contienent des om�ga => on ne modifie pas la distance
		}
		return diff;
	}
	
	/** check if this marking exists in a set of markings */
	public boolean existIn(ArrayList<IMarking> markings)
	{
		int diff = 0;
		for (int i = 0 ; i < markings.size(); i++)
		{
			if (!isEquivalentTo(markings.get(i), 1))
			{
				diff = diff + 1;
			}
		}
		return !(diff == markings.size());
	}
	
	public String getCode()
	{
		return code;
	}
	
	/** Retourne le nombre d'�l�ments constituant le marquage */
	public int getLength()
	{
		return marking.size();
	}
	
	public int getTokenAt(int i)
	{
		return marking.get(i);
	}
	
	// return true if "mark1" is equal to "mark2"
	public boolean isEqualTo(IMarking mark)
	{
		return code.equals(mark.getCode());
	}
	
	/**
	 * check if the marking given as argument is equivalent to this one considering a minimalWeight
	 * ie if they get same number of token on each place EXEPT for any place where one of the marking
	 * get Omega token (any number)
	 */
	public boolean isEquivalentTo(IMarking mark, int minimalWeight)
	{
		// parcourir toutes les places
		for (int i = 0 ; i < this.marking.size() ; i++)
		{
			if (mark.getTokenAt(i) >= minimalWeight && this.marking.get(i) >= minimalWeight)
				if ((mark.getTokenAt(i) == Integer.MAX_VALUE) || (this.marking.get(i) == Integer.MAX_VALUE))
					continue;
			// si on trouve une place pour laquelle le nombre de jeton de "mark" est diff�rent � son �quivalent dans "this" alors
			// "this" n'est pas �quivalent � "mark"
			if (mark.getTokenAt(i) != this.marking.get(i))
				return false;
		}
		// toutes les places de "mark" sont �quivalentes � sa correspondante dans "this"
		return true;
	}
	
	public void pushToken(int number)
	{
		marking.add(number);
		// update code
		if (code.equals(""))
			code = ""+number;
		else
			code = code + ":" + number;
	}
	
	public void setTokenAt(int i, int number)
	{
		while (i >= this.getLength())
			pushToken(0);
		marking.set(i, number);
		// update the code
		String[] tokens = code.split(":");
		if (tokens.length > 0){
			tokens[i] = ""+number;
			code = tokens[0];
			for (int cpt = 1 ; cpt < tokens.length ; cpt++){
				code += ":"+tokens[cpt];
			}
		}
	}
	
	/** "this" couvre STRICTEMENT "mark" ssi "this" couvre "mark" ET "this" n'est pas �gal � "mark" */
	public boolean strictlyCover(IMarking mark)
	{
		return this.cover(mark) && !this.isEqualTo(mark);
	}
}
