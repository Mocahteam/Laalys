package fr.lip6.mocah.laalys.traces;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.lip6.mocah.laalys.traces.constants.ActionSource;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */
public class Traces implements ITraces {
	/**
	 * list of trace
	 */
	private ArrayList<ITrace> _traces = new ArrayList<ITrace>();
	/**
	 * the index of the current action in traces ArrayList
	 */
	private int currentIndex = 0;
	/**
	 * Contructeur de xml
	 */
	private DocumentBuilder builder = null;
	/**
	 * Le contenu de la trace en xml
	 */
	private Document xml;
	
	public Traces (){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public ITrace first()
	{
		this.currentIndex = 0;
		return current();
	}
	
	
	/**
	 * @inheritDoc
	 * @param	filePath
	 */
	public void loadFile(String url)
	{
		//remise à zero de l'action courante
		this.currentIndex = 0;
		
		if (builder != null){
			try {
				this.xml = builder.parse(url);
				// Parse document and build each Trace item
				NodeList transitionsList = xml.getElementsByTagName("transition");
				for (int i = 0 ; i < transitionsList.getLength() ; i++ )
					this._traces.add (new Trace (ActionSource.LOADED, transitionsList.item(i)));
				//pour le debug seulement A ENLEVER
				//trace( xml.toString() );
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public int length()
	{
		return _traces.size();
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public ITrace current()
	{
		return _traces.get(currentIndex);
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public boolean isNext()
	{
		return ( currentIndex + 1 ) < length();
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public ITrace next()
	{
		if ( !isNext() )
			return null;
		currentIndex++;
		return current();
	}
	
	/**
	 * @inheritDoc
	 */
	public void reset()
	{
		for (ITrace tr : _traces )
			tr.freeLabels();
		currentIndex = 0;
	}
	
	/**
	 * @inheritDoc
	 */
	public void clear()
	{
		_traces = new ArrayList<ITrace>();
		currentIndex = 0;
		xml = null;
	}
	
	/**
	 * @inheritDoc
	 */
	public void copy(ITraces source)
	{
		for(ITrace tr : source.getTraces() )
			_traces.add(tr.cloneWithoutLabels());
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public String toString() 
	{
		String result = "<transitions>\n";
		for (ITrace tr : _traces)
			result += tr.toString()+"\n";
		result += "</transitions>";
		return result;
	}
	
	/**
	 * @inheritDoc
	 * @return
	 */
	public Document toXML()
	{
		Document export = null;
		if (builder != null){
			export = builder.newDocument();
			Element racine = (Element) export.createElement("transitions");
			for (ITrace tr : _traces)
				racine.appendChild(tr.toXML(export));
			export.appendChild(racine);
		}
		return export;
	}
	
	/*
	 * GETTER AND SETTER
	 */
	
	public ArrayList<ITrace> getTraces() 
	{
		return _traces;
	}
	
	public void setTraces(ArrayList<ITrace> value) 
	{
		_traces = value;
	}
}
