package fr.lip6.mocah.laalys.traces;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.lip6.mocah.laalys.traces.constants.ActionSource;
import fr.lip6.mocah.laalys.traces.constants.ActionType;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */
public class Trace implements ITrace
{
	
	private String _action = null;
	private String _source = ActionSource.UNKNOW;
	private String _origin   = ActionType.UNKNOW;
	private ArrayList<String> _labels = null;
	private Boolean _isTry = null;
	
	public Trace( String source, Node xml ) 
	{
		if ( xml != null )
		{
			//on fixe le nom de l'action
			if (xml.getAttributes().getNamedItem("action") != null)
				this._action = xml.getAttributes().getNamedItem("action").getNodeValue();
			//on fixe le type de l'action
			if ( xml.getAttributes().getNamedItem("origin") != null )
			{//ce test est la pour conserve la compatibilite avec les traces transforme des cristaux d'ehere
				if ( xml.getAttributes().getNamedItem("origin").getNodeValue().equals("system") )
					this._origin = ActionType.SYSTEM;
				else if ( xml.getAttributes().getNamedItem("origin").getNodeValue().equals("player") )
					this._origin = ActionType.PLAYER;
				else
					this._origin = ActionType.UNKNOW;
			}
			else if ( xml.getAttributes().getNamedItem("type") != null )
				this._origin = xml.getAttributes().getNamedItem("type").getNodeValue();
			//est ce que l'action a ete accepte par le jeu
			if ( xml.getAttributes().getNamedItem("try") != null )
				this._isTry = xml.getAttributes().getNamedItem("try").getNodeValue().toUpperCase().equals("TRUE");
		}
		this._source = source;
		this.freeLabels();
	}
	
	public Trace(String action, String source, String origin, Boolean isTry) 
	{
		this._action = action;
		this._origin = origin;
		this._isTry = isTry;
		this._source = source;
		this.freeLabels();
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public String toString()
	{
		return "<transition action=\"" + this._action + "\" try=\"" + this._isTry + "\" source=\"" + this._source + "\" origin=\"" + this._origin + "\" labels=\"" + this._labels +"\"/>";
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public Node toXML(Document racine)
	{
		Element tr = (Element) racine.createElement("transition");
		tr.setAttribute("action", _action);
		tr.setAttribute("try", (_isTry != null) ? _isTry.toString() : "null");
		tr.setAttribute("source", _source);
		tr.setAttribute("origin", _origin);
		tr.setAttribute("labels", (_labels != null) ? _labels.toString() : "null");
		return tr;
	}
	
	/**
	 * @inheritDoc
	 */
	public void freeLabels() {
		this._labels = new ArrayList<String>();
	}
	/**
	 * @inheritDoc
	 */
	public ArrayList<String> getLabels() {
		return this._labels;
	}
	/**
	 * @inheritDoc
	 * @param	labels
	 */
	public void setLabels(ArrayList<String> _labels) {
		this._labels = new ArrayList<String>(_labels); // crée un clone
	}
	/**
	 * @inheritDoc
	 * @param	value
	 */
	public void addLabel(String value) {
		this._labels.add(value);
	}
	/**
	 * @inheritDoc
	 */
	public ITrace cloneWithoutLabels() {
		return new Trace(_action, _source, _origin, _isTry);
	}
	
	/*
	 * GETTER AND SETTER 
	 */
	
	public String getAction() 
	{
		return _action;
	}
	
	public void setAction(String value) 
	{
		_action = value;
	}
	
	public String getSource() 
	{
		return _source;
	}
	
	public void setSource(String value) 
	{
		_source = value;
	}
	
	public String getOrigin() 
	{
		return _origin;
	}
	
	public void setOrigin(String value) 
	{
		_origin = value;
	}
	
	public Boolean getIsTry() 
	{
		return _isTry;
	}
	
	public void setIsTry(Boolean value) 
	{
		_isTry = value;
	}
	
}