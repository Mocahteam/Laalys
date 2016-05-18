package fr.lip6.mocah.laalys.features;

import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */
public class Features implements IFeatures {
	/**
	 * dictionary to store all id of system transition
	 */
	private HashSet<String> systems;
	/**
	 * dictionary to store all id of end transition
	 */
	private HashSet<String> ends;
	
	public Features() 
	{
		this.reset();
	}
	
	/* INTERFACE specificite.Interface.Ispecificitees */
	
	/**
	 * @inheritDoc
	 * @param	id
	 * @return
	 */
	public boolean isSystem (String id) 
	{
		return systems.contains(id);
	}
	
	/**
	 * @inheritDoc
	 * @param	id
	 * @return
	 */
	public boolean isEnd(String id) 
	{
		return ends.contains(id);
	}
	
	/**
	 * @inheritDoc
	 */
	public void reset() 
	{
		systems = new HashSet<String>();
		ends = new HashSet<String>();
	}
	
	/**
	 * @inheritDoc
	 * @param	url
	 */
	public void loadFile(String url) 
	{
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xml = builder.parse(url);
			// Récupération de toutes les transitions
			NodeList transitionsList = xml.getElementsByTagName("transition");
			for (int i = 0 ; i < transitionsList.getLength() ; i++ ){
				NamedNodeMap attr = transitionsList.item(i).getAttributes();
				// Vérifier si c'est une transition système
				if (attr.getNamedItem("system").getNodeValue().equals("true")){
					// Enregistrement de l'id de cette tansition dans le dictionnaire des actions systèmes
					systems.add(attr.getNamedItem("id").getNodeValue());
				}
				// Vérifier si c'est une transition de fin de niveau
				if (attr.getNamedItem("end").getNodeValue().equals("true")){
					// Enregistrement de l'id de cette tansition dans le dictionnaire des actions de fin de niveau
					ends.add(attr.getNamedItem("id").getNodeValue());
				}
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getSystemTransitions()
	{
		ArrayList<String> sys = new ArrayList<String>();
		for ( String id : systems )
			sys.add (id);
		return sys;
	}
	
	public ArrayList<String> getEndLevelTransitions()
	{
		ArrayList<String> ends = new ArrayList<String>();
		for ( String id : this.ends )
			ends.add (id);
		return ends;
	}
}
