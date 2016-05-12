package fr.lip6.mocah.laalys.traces;

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * ...
 * @author Mathieu Muratet, Cl�ment Rouanet
 */
public interface ITrace {
	/**
	 * return a string representing the trace
	 * XML formated
	 * @return
	 */
	public String toString();
	/**
	 * return an XML structure containing trace, utilise le conrtucteur de document pass�
	 * en param�tre pour g�n�rer le noeud
	 * @return
	 */
	public Node toXML(Document racine);
	
	/**
	 * Supprime tous les labels de la trace
	 */
	public void freeLabels();
	/**
	 * @return tous les labels associ�s � la trace
	 */
	public Vector<String> getLabels();
	/**
	 * D�finit un ensemble de label pour la trace
	 * @param	labels
	 */
	public void setLabels(Vector<String> labels);
	/**
	 * Ajoute un label � la trace
	 * @param	value
	 */
	public void addLabel(String value);
	/**
	 * Cr�e un clone de cette trace sans recopier les labels
	 */
	public ITrace cloneWithoutLabels();
	
	/**
	 * GETTER
	 */
	public String getAction();
	public String getSource();
	public String getOrigin();
	/**
	 * it returns an object cause it can be not initialized (ie we don t know the value)
	 */
	public Boolean getIsTry();
	
	/**
	 * SETTER
	 */
	public void setAction(String value);
	public void setSource(String value);
	public void setOrigin(String value);
	/**
	 * only get boolean values
	 * it s an object cause it can be not initialized (ie we don t know the value)
	 */
	public void setIsTry(Boolean value);
}
