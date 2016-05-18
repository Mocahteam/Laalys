package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;

/**
 * ...
 * @author Mathieu Muratet
 */
public interface IMarking {
	public IMarking clone();
	
	/**
	 * "this" couvre "mark" ssi "mark" est inf�rieur ou �gal � "this" (c�d il n'y a pas de place dans mark dont le nombre jeton est strictement
	 * sup�rieur a son �quivalent dans "this")
	 */
	public boolean cover (IMarking mark);
	
	/**
	 * Calcule la distance de marquage avec le marquage pass� en param�tre
	 * la distance est la somme de la valeur absolue de la differences de ces places
	 * @param marking
	 * @param minimalWeight poid minimal d�finissant la limite d'ajout des om�gas dans 
	 * le cas d'un graphe de couverture
	 * @return la distance entre les marquages
	 */
	public int distanceWith (IMarking mark, int minimalWeight);
	
	/** check if this marking exists in a set of markings */
	public boolean existIn(ArrayList<IMarking> markings);
	
	public String getCode();
	
	/** Retourne le nombre d'�l�ments constituant le marquage */
	public int getLength();
	
	public int getTokenAt(int i);
	
	public boolean isEqualTo (IMarking mark);
	
	/**
	 * check if the marking given as argument is equivalent to this one considering a minimalWeight
	 * ie if they get same number of token on each place EXEPT for any place where one of the marking
	 * get Omega token (any number)
	 */
	public boolean isEquivalentTo (IMarking mark, int minimalWeight);
	
	public void pushToken (int number);
	
	public void setTokenAt (int i, int number);
	
	/** "this" couvre STRICTEMENT "mark" ssi "this" couvre "mark" ET "this" n'est pas �gal � "mark" */
	public boolean strictlyCover (IMarking mark);
}
