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
	 * "this" couvre "mark" ssi "mark" est inférieur ou égal à "this" (càd il n'y a pas de place dans mark dont le nombre jeton est strictement
	 * supérieur a son équivalent dans "this")
	 */
	public boolean cover(IMarking mark)
	{
		// parcourir toutes les places
		for (int i = 0 ; i < this.marking.size() ; i++)
		{
			// si on trouve une place pour laquelle le nombre de jeton de "mark" est strictement supérieur à son équivalent dans "this" alors
			// "this" ne couvre pas "mark"
			if (mark.getTokenAt(i) > this.marking.get(i).intValue())
				return false;
		}
		// aucune place de "mark" ne contient plus de jeton que sa correspondante dans "this"
		return true;
	}
	
	/**
	 * Calcule la distance de marquage avec le marquage passé en paramètre
	 * la distance est la somme de la valeur absolue de la differences de ces places
	 * @param marking
	 * @param minimalWeight poid minimal définissant la limite d'ajout des omégas dans 
	 * le cas d'un graphe de couverture
	 * @return la distance entre les marquages
	 */
	public int distanceWith(IMarking mark, int minimalWeight)
	{
		if (mark.getLength() != this.getLength())
			throw new Error("\"Marking::distanceWith\" : les marquages ne sont pas de la même taille");
		
		int diff = 0;
		for (int i = 0; i < this.getLength(); i++)
		{
			// Prise en compte du omega dans le cas d'un graphe de couverture
			if (this.getTokenAt(i) != Integer.MAX_VALUE && mark.getTokenAt(i) != Integer.MAX_VALUE)
				// les deux marquages n'ont pas de oméga => On calcule la différence
				diff += Math.abs(this.getTokenAt(i) - mark.getTokenAt(i));
			else if (this.getTokenAt(i) == Integer.MAX_VALUE) { // peut être que l'un des deux marquages a un oméga, on regarde si c'est "this"
				// on vérifie du coup si le paramètre n'a pas une valeur plus petite que le poids minimum
				if (mark.getTokenAt(i) < minimalWeight)
					// on comptabilise la différence avec le poid minimal
					diff += Math.abs(mark.getTokenAt(i) - minimalWeight);
			} else if (mark.getTokenAt(i) == Integer.MAX_VALUE){ // ce n'était pas le cas pour "this" on regarde pour le paramètre "mark"
				// on vérifie du coup si "this" n'a pas une valeur plus petite que le poids minimum
				if (this.getTokenAt(i) < minimalWeight)
					// on comptabilise la différence avec le poid minimal
					diff += Math.abs(this.getTokenAt(i) - minimalWeight);
			}
			// Pas de sinon, en effet dans ce cas cela signifie que les deux marquages
			// contienent des oméga => on ne modifie pas la distance
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
	
	/** Retourne le nombre d'éléments constituant le marquage */
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
			// si on trouve une place pour laquelle le nombre de jeton de "mark" est différent à son équivalent dans "this" alors
			// "this" n'est pas équivalent à "mark"
			if (mark.getTokenAt(i) != this.marking.get(i))
				return false;
		}
		// toutes les places de "mark" sont équivalentes à sa correspondante dans "this"
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
	
	/** "this" couvre STRICTEMENT "mark" ssi "this" couvre "mark" ET "this" n'est pas égal à "mark" */
	public boolean strictlyCover(IMarking mark)
	{
		return this.cover(mark) && !this.isEqualTo(mark);
	}
}
