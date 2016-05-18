package fr.lip6.mocah.laalys.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.lip6.mocah.laalys.labeling.PathState;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.constants.ActionSource;

/**
 * ...
 * @author Mathieu Muratet, Amel Yessad, Cl�ment Rouanet
 */
public class PetriNet implements IPetriNet {
	private String id;
	private String name;
	
	private IMarking currentMarking;
	private IMarking initialMarking;
	
	private ArrayList<ITransition> transitions;
	private HashMap<String, Integer> refTransitionById;
	
	private ArrayList<IPlaceInfo> placesInfo;
	private HashMap<String, Integer> refPlaceInfoById; // this is an hash table of IPlaceInfo references
	
	private IGraph graph;
	private boolean computeGraph;
	private String _kindOfGraph;
	private String _globalStrategy;
	
	/**
	 * Contructeur de xml
	 */
	private DocumentBuilder builder = null;
	/**
	 * Le contenu de la trace en xml
	 */
	private Document xml;
	
	/**
	 * Si "ComputeGraph" est initialis� � "true", le graphe des �tats possibles atteingnable est g�n�r�. En fonction du param�tre "kindOfGraph"
	 * qui peut avoir comme valeur AccessibilityGraph::TYPE|CoverabilityGraph::TYPE. Le graphe construit sera soit un graphe d'accessibilit�,
	 * soit un graphe de couverture. Dans le cas d'un graphe de couverture, le dernier param�tre "strategy" permet de d�finir la strat�gie de
	 * parcours du graphe en cas d'ambig�it� sur l'identifiaction d'un marquage dans le graphe (voir commentaire du constructeur de la classe
	 * CoverabilityGraph pour plus de d�tail).
	 * @see CoverabilityGraph
	 */
	public PetriNet(boolean computeGraph, String kindOfGraph, String strategy)
	{
		id = "";
		currentMarking = new Marking();
		initialMarking = new Marking();
		transitions = new ArrayList<ITransition>();
		refTransitionById = new HashMap<String, Integer>();
		placesInfo = new ArrayList<IPlaceInfo>();
		refPlaceInfoById = new HashMap<String, Integer>();
		graph = null;
		
		this.computeGraph   = computeGraph;
		this._kindOfGraph    = kindOfGraph;
		this._globalStrategy = strategy;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean changeStatePetriNetById(String transitionId) throws Exception
	{
		int t = getRefTransitionById(transitionId);
		if (t == -1)
			throw new Exception("\"" + transitionId + "\" is not a known transition");
		return changeStatePetriNet(transitions.get(t));
	}
	
	public boolean changeStatePetriNet(ITransition t)
	{
		// execute a fired transition
		int i;
		if (enabledTransition(t))
		{
			for (IArc arcIn : t.getArcsIn())
			{
				if (arcIn.getType().equals(Arc.REGULAR_ARC))
				{
					i = refPlaceInfoById.get(arcIn.getSource().getId());
					currentMarking.setTokenAt(i, currentMarking.getTokenAt(i) != Integer.MAX_VALUE ? currentMarking.getTokenAt(i) - arcIn.getWeight() : Integer.MAX_VALUE);
				}
			}
			for (IArc arcOut : t.getArcsOut())
			{
				i = refPlaceInfoById.get(arcOut.getTarget().getId());
				currentMarking.setTokenAt(i, currentMarking.getTokenAt(i) != Integer.MAX_VALUE ? currentMarking.getTokenAt(i) + arcOut.getWeight() : Integer.MAX_VALUE);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/** V�rifie si le marquage "mark" est inclus dans l'ensemble des marquages accessible */
	public boolean contains(IMarking mark)
	{
		return this.graph.contains(mark);
	}
	
	public void createArcPl2Tr(String idPlace, String idTransition, int weight, String arcType) throws Exception
	{
		int pl = getRefPlaceById(idPlace);
		if (pl == -1)
			throw new Exception("\"" + idPlace + "\" is not a known place");
		int tr = getRefTransitionById(idTransition);
		if (tr == -1)
			throw new Exception("\"" + idTransition + "\" is not a known transition");
		// build arc
		placesInfo.get(pl).setArcOut(transitions.get(tr), weight, arcType);
		transitions.get(tr).setArcIn(placesInfo.get(pl), weight, arcType);
	}
	
	public void createArcTr2Pl(String idTransition, String idPlace, int weight) throws Exception
	{
		int tr = getRefTransitionById(idTransition);
		if (tr == -1)
			throw new Exception("\"" + idTransition + "\" is not a known transition");
		int pl = getRefPlaceById(idPlace);
		if (pl == -1)
			throw new Exception("\"" + idPlace + "\" is not a known place");
		// build arc
		transitions.get(tr).setArcOut(placesInfo.get(pl), weight);
		placesInfo.get(pl).setArcIn(transitions.get(tr), weight);
	}
	
	/**
	 * Charge le fichier XML � partir de "url" et construit le RdP.
	 * Cette fonction peut charger :
	 *  - des fichiers ".xml" (respectant le format de PiPe3.0)
	 *  - des fichiers ".pnml"
	 * @throws Exception when the extention of the url is not a supported one
	 */
	public void createPetriNet(String url)
	{
		String extension = url.substring(url.lastIndexOf(".")+1);

		setId(url);
		
		switch (extension)
		{// switch on appropriate functions depending on file extension
			case "xml": // xml file is ONLY for format file to Pipe3
				createPetriNetFromXMLFile (url);
				break;
			
			//case "ndr" : // ndr files are files generated by tina
			//	createPetriNetFromNDRFile function is not written completly yet => finish it before un commenting this case
			//	urlldr.addEventListener(Event.COMPLETE, createPetriNetFromNDRFile);
			//break;
			
			case "pnml": // pnml files are xml file for petri net (standard?)
				createPetriNetFromPNMLFile (url);
				break;
			
			default: 
				// dispatch error event cause it's not a file type (extension) handled
				throw new Exception ("unable to parse \"" + extension + "\" file - creation of petri net aborted");
				break;
		}
	}
	
	/**
	 * @see http://projects.laas.fr/tina/manuals/formats.html for grammar of ndr file =)
	 * @param e
	 */
	protected void createPetriNetFromNDRFile(String url)
	{
		String content = "";
		FileReader reader;
		try {
			reader = new FileReader(url);
			// lecture du fichier
			int c;
			while ((c=reader.read()) != -1)
				content += (char) c;
			reader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int placeCpt = 0;
		HashMap<String, String> placeIdByName = new HashMap<String, String>();
		int transitionCpt = 0;
		HashMap<String, String> transitionIdByName = new HashMap<String, String>();
		
		try{
			String [] lines = content.split("\n");
			for (String line : lines)
			{
				//all "key" of the line are separated by tabulation
				String [] splittedLine = line.split("\t");
				switch (splittedLine[0])
				{
				case "p": //it s a place so create it
					String plName = splittedLine[3];
					int initialMarking = Integer.parseInt(splittedLine[4]);
					//there is no id given so we generate one
					String plId = "place_" + placeCpt;
					placeIdByName.put(plName, plId);
					//increase cpt of place
					placeCpt++;
					createPlace(plId, plName);
					initializeTokens(plId, initialMarking);
					break;
				
				case "t": //it s a transtion so create it
					String trName = splittedLine[3];
					//there is no id given so we generate one
					String trId = "transition_" + transitionCpt;
					transitionIdByName.put(trName, trId);
					//increase the cpt of transtions
					transitionCpt++;
					createTransition(trId, trName);
					break;
				
				case "e": //it s an arc so create it
					//TODO =)
					break;
				
				case "h": //name of the petri net
					this.name = splittedLine[1];
					break;
				}
			}
			//default xml namespace = "";
			initialization();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Node getChildByName (Node node, String childName){
		NodeList childsXML = node.getChildNodes();
		// recherche d'un fils correspondant au noeud recherch�
		for (int i = 0 ; i < childsXML.getLength() ; i++ ){
			Node child = childsXML.item(i);
			if (child.getNodeName().equals(childName))	{
				return child;
			}
		}
		return null;
	}
	
	protected void createPetriNetFromPNMLFile(String url) throws Exception
	{
		this.xml = null;
		if (builder != null){
			try {
				// Parse document
				this.xml = builder.parse(url);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Analyse de l'xml
		if (this.xml != null){
			HashMap<String, String> hash = new HashMap<String, String>();
			HashSet<String> placeNameById = new HashSet<String> ();
			NodeList placesXML = xml.getElementsByTagName("place");
			for (int i = 0 ; i < placesXML.getLength() ; i++ ){
				Node placeXML = placesXML.item(i);
				String id = placeXML.getAttributes().getNamedItem("id").getNodeValue();
				// recherche et enregistrement du nom de cette place
				String name = id;
				Node nameXML = getChildByName(placeXML, "name");
				if (nameXML != null){
					Node nameContentXML = getChildByName(nameXML, "text");
					if (nameContentXML != null){
						name = nameContentXML.getTextContent();
						hash.put(id, name);
						id = name;
					}
				}
				// recherche est enregistrement du nombre de jeton de cette place
				int nbTokens = 0;
				Node tokenXML = getChildByName(placeXML, "initialMarking");
				if (tokenXML != null){
					Node tokenContentXML = getChildByName(tokenXML, "text");
					if (tokenContentXML != null)
						nbTokens = Integer.parseInt(tokenContentXML.getTextContent());
				}
				createPlace(id, name);
				placeNameById.add(id);
				initializeTokens(id, nbTokens);
			}
			
			NodeList transtitionsXML = xml.getElementsByTagName("transition");
			HashSet<String> transtitionNameById = new HashSet<String> ();
			for (int i = 0 ; i < transtitionsXML.getLength() ; i++ ){
				Node transitionXML = transtitionsXML.item(i);
				String id = transitionXML.getAttributes().getNamedItem("id").getNodeValue();
				String name = id;
				// recherche et enregistrement du nom de cette transition
				Node nameXML = getChildByName(transitionXML, "name");
				if (nameXML != null){
					Node nameContentXML = getChildByName(nameXML, "text");
					if (nameContentXML != null){
						name = nameContentXML.getTextContent();
						hash.put(id, name);
						id = name;
					}
				}
				createTransition(id, name);
				transtitionNameById.add(id);
			}
			
			NodeList arcsXML = xml.getElementsByTagName("arc");
			for (int i = 0 ; i < arcsXML.getLength() ; i++ ){
				Node arcXML = arcsXML.item(i);
				String tmp = arcXML.getAttributes().getNamedItem("source").getNodeValue();
				String source = hash.get(tmp);
				String tmp2 = arcXML.getAttributes().getNamedItem("target").getNodeValue();
				String target = hash.get(tmp2);
				int weight = 1;
				// recherche et enregistrement du poids de l'arc
				Node weightXML = getChildByName(arcXML, "inscription");
				if (weightXML != null){
					Node weightContentXML = getChildByName(weightXML, "text");
					if (weightContentXML != null)
						weight = Integer.parseInt(weightContentXML.getTextContent());
				}
				String type = Arc.REGULAR_ARC;
				// recherche et enregistrement du type d'arc
				Node typeXML = getChildByName(arcXML, "inscription");
				if (typeXML != null){
					if (typeXML.getAttributes().getNamedItem("value").getNodeValue().equals("test"))
						type = Arc.READ_ARC;
					if (typeXML.getAttributes().getNamedItem("value").getNodeValue().equals("inhibitor"))
						type = Arc.INHIBITOR_ARC;
				}
				
				if (transtitionNameById.contains(source) && placeNameById.contains(target))
					createArcTr2Pl(source, target, weight);
				else if (placeNameById.contains(source) && transtitionNameById.contains(target))
					createArcPl2Tr(source, target, weight, type);
				else
				{
					if (!transtitionNameById.contains(source) && !placeNameById.contains(source))
						throw new Exception("error: arc source \"" + source + "\" is not a transition or a place");
					if (!transtitionNameById.contains(target) && !placeNameById.contains(target))
						throw new Exception("error : arc target \"" + target + "\" is not a transition or a place");
				}
			}
			// On proc�de ensuite � l'initialisation
			initialization();
		}
	}
	
	protected function createPetriNetFromXMLFile(e:Event):void
	{
		urlldr.removeEventListener(Event.COMPLETE, createPetriNetFromXMLFile);
		
		// construction des tables: transitions et places
		try
		{
			xml = new XML(e.target.data);
			// construction de la table "places"
			var placesXML:XMLList = xml.net.place;
			for each (var placeXML:XML in placesXML)
			{
				if (parseInt(placeXML.initialMarking.value.toString()) is int)
				{
					createPlace(placeXML.@id, placeXML.name.value);
					initializeTokens(placeXML.@id, parseInt(placeXML.initialMarking.value.toString()));
				}
				else
				{
					createPlace(placeXML.@id, placeXML.name.value);
					initializeTokens(placeXML.@id, parseInt(placeXML.initialMarking.value.toString().substr(8, int.MAX_VALUE)));
				}
			}
			// construction de la table "transition"
			var transitionsXML:XMLList = xml.net.transition;
			for each (var transitionXML:XML in transitionsXML)
			{
				createTransition(transitionXML.@id, transitionXML.name.value);
				var arcsXML:XMLList = xml.net.arc;
				// Add places in and places out of a transition 
				for each (var arcXML:XML in arcsXML)
				{
					// if transition is the source
					if (transitionXML.@id == arcXML.@source.toString())
					{
						// create arc from transition to place
						if (parseInt(arcXML.inscription.value) is int)
							createArcTr2Pl(transitionXML.@id, arcXML.@target.toString(), arcXML.inscription.value);
						else
							createArcTr2Pl(transitionXML.@id, arcXML.@target.toString(), arcXML.inscription.value.substr(8, int.MAX_VALUE));
					}
					// if transition is the target
					if (transitionXML.@id == arcXML.@target.toString())
					{
						// create arc from place to transition
						if (parseInt(arcXML.inscription.value) is int)
							createArcPl2Tr(arcXML.@source.toString(), transitionXML.@id, arcXML.inscription.value, "regular");
						else
							createArcPl2Tr(arcXML.@source.toString(), transitionXML.@id, arcXML.inscription.value.substr(8, int.MAX_VALUE), "regular");
					}
				}
			}
			initialization();
		}
		catch (e:Error)
		{
			trace(e.message);
		}
	}
	
	public void createPlace(String id, String name) throws Exception
	{
		// check if a place with this id is already stored
		if (!refPlaceInfoById.containsKey(id))
		{
			// create the new place
			PlaceInfo pInfo = new PlaceInfo();
			pInfo.setId(id);
			pInfo.setName(name);
			// push "pInfo" into "placesInfo" and store its id into the hash table 
			placesInfo.add(pInfo);
			refPlaceInfoById.put(id, placesInfo.size() - 1);
		}
		else
			throw new Exception("A place with \"" + id + "\" id, already exists");
	}
	
	public void createTransition(String id, String name) throws Exception
	{
		// check if a place with this id is already stored
		if (!refTransitionById.containsKey(id))
		{
			ITransition t = new Transition();
			t.setId(id);
			t.setName(name);
			// push "t" into "transitions" and store its id into the hash table 
			transitions.add(t);
			refTransitionById.put(id, transitions.size() - 1);
		}
		else
			throw new Exception("A transition with \"" + id + "\" id, already exists");
	}
	
	/** Check if a transition could be fired in the current state of the petri net */
	public boolean enabledTransition(ITransition t)
	{
		// verifie si une transition est sensibilis�e ou pas
		for (IArc arcIn : t.getArcsIn())
		{
			IPlaceInfo pl = (IPlaceInfo) arcIn.getSource();
			if (arcIn.getType().equals(Arc.REGULAR_ARC) || arcIn.getType().equals(Arc.READ_ARC))
			{
				if (currentMarking.getTokenAt(refPlaceInfoById.get(pl.getId())) < arcIn.getWeight())
				{
					return false;
				}
			}
			else if (arcIn.getType().equals(Arc.INHIBITOR_ARC))
			{
				if (currentMarking.getTokenAt(refPlaceInfoById.get(pl.getId())) >= arcIn.getWeight())
				{
					return false;
				}
			}
			// Adds here other type arc
		}
		return true;
	}
	
	/**
	 * Extrait le sous marquage de rdpExtraction sur la base de la structure de rdpBase (pour chacune des places de rdpBase
	 * recherche la place �quivalent dans rdpSource et en extrait le marquage).
	 * Attention !!! rdpExtraction doit inclure rdpBase, la fonction renverra null sinon.
	 * @throws Exception 
	 */
	public static IMarking extractSubMarkings(IPetriNet rdpBase, IPetriNet rdpExtraction) throws Exception
	{
		// V�rification que rdpExtraction couvre bien rdpBase
		for (IPlaceInfo p : rdpBase.getPlaces())
		{
			if (rdpExtraction.getPlaceById(p.getId()) != null)
				return null;
		}
		// Toutes les places du rdpBase sont bien incluses dans le rdpExtraction
		// Cr�ation d'un Rdp temporaire pour travailler
		IPetriNet tmpRdP = new PetriNet(false, "", "");
		tmpRdP.setPlaces(rdpBase.getPlaces());
		// Pour toutes les places du rdp temporaire, recopier le marquage correspondant du RdpExtraction
		for (IPlaceInfo p : tmpRdP.getPlaces())
			tmpRdP.initializeTokens(p.getId(), rdpExtraction.getCurrentMarking(p.getId()));
		return tmpRdP.getCurrentMarkings();
	}
	
	/**
	 * Cette fonction exporte l'analyse au format .graphml.
	 * @param outputFile d�fini le nom du fichier � cr�er avec l'extension ".graphml" ce fichier sera plac� sur le bureau
	 * @param completeRdP contient le graphe d'accessibilit� du r�seau complet
	 * @param filteredRdP contient le graphe d'accessibilit� du r�seau filtr�
	 * @param artificialRdpList contient les graphes d'accessibilit� des r�seaux artificiels construits
	 * @param path contient l'ensemble des �tats par lesquels le joueur est pass�
	 * @param ends contient la liste des identifiants des transitions permettant de terminer le niveau.
	 * @throws Exception 
	 */
	public static void exportToGraphml(String outputFile, IPetriNet completeRdP, IPetriNet filteredRdP, ArrayList<IPetriNet> artificalRdpList, ArrayList<PathState> path, ArrayList<String> ends) throws Exception
	{
		// V�rification de l'existance d'un graphe pour le RdP filtr�
		if (filteredRdP.getGraph() == null)
			throw new Exception("PetriNet::exportToGraphml => no graph defined for filtered RdP");
			
		String export = "";
		// �criture de l'ent�te du format graphml
		export += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		export += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n";
		export += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		export += "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n";
		export += "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";
		// d�finition de cl�s pour param�trer les noeuds et les arcs
		export += "\t<key id=\"edgelabel\" for=\"edge\" attr.name=\"Edge Label\" attr.type=\"string\" />\n";
		export += "\t<key id=\"tag\" for=\"edge\" attr.name=\"tag\" attr.type=\"string\" />\n";
		export += "\t<key id=\"r\" for=\"node\" attr.name=\"r\" attr.type=\"int\" />\n";
		export += "\t<key id=\"g\" for=\"node\" attr.name=\"g\" attr.type=\"int\" />\n";
		export += "\t<key id=\"b\" for=\"node\" attr.name=\"b\" attr.type=\"int\" />\n";
		// d�finition de cl�s pour les positions
		export += "\t<key attr.name=\"x\" attr.type=\"float\" for=\"node\" id=\"x\" />\n";
		export += "\t<key attr.name=\"y\" attr.type=\"float\" for=\"node\" id=\"y\" />\n";
		// les types de base sont : filteredState|completeState|artificialState[X]
		export += "\t<key id=\"base_type\" for=\"node\" attr.name=\"base_type\" attr.type=\"string\" />\n"; 
		// les types sp�ciaux sont : initState|endState|stalemate|pathState
		export += "\t<key id=\"special_type\" for=\"node\" attr.name=\"special_type\" attr.type=\"string\" />\n"; 
		// cr�ation du graphe
		export += "\t<graph id=\"G\" edgedefault=\"directed\">\n";
		
		if (completeRdP.getGraph() != null) {
			export += exportWithCompleteGraph(completeRdP, filteredRdP, artificalRdpList, path, ends);
		} else {
			export += exportWithoutCompleteGraph(filteredRdP, artificalRdpList, path, ends);
		}
		export += "\t</graph>\n";
		export += "</graphml>\n";

		File file = new File (outputFile);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(export);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	private static String exportWithCompleteGraph(IPetriNet completeRdP, IPetriNet filteredRdP, ArrayList<IPetriNet> artificialRdpList, ArrayList<PathState> path, ArrayList<String> ends) throws Exception
	{
		// sauvegarde du marquage courant du RdP Complet
		IMarking saveMark = completeRdP.getCurrentMarkings().clone();
		
		// extraction des marquages complets
		ArrayList<IMarking> knownMarkings = new ArrayList<IMarking>();
		for (PathState state : path) {
			knownMarkings.add(state.mark);
		}
		
		String nodes = "";
		String edges = "";
		
		// calcul de la taille de la matrice pour afficher chaque noeud
		int size = (int) (Math.sqrt(completeRdP.getGraph().getAllMarkings().size()) + 1);
		int step = 20;
		int x = 0;
		int y = 0;
		
		// Analyse de tous les marquages
		for (IMarking mark : completeRdP.getGraph().getAllMarkings())
		{
			// Evaluer si le marquage courant est le marquage initial
			boolean isInitialState = completeRdP.getInitialMarkings().isEqualTo(mark);
			// Evaluer si le marquage courant est un marquage par lequel le joueur est pass�
			boolean isPathState = mark.existIn(knownMarkings);
			// Synchronisation du RdP complet sur le marquage en cours d'analyse
			completeRdP.setCurrentMarkings(mark);
			IMarking subMarking = extractSubMarkings(filteredRdP, completeRdP);
			// Evaluer si le sous-marquage courant est un marquage du Rdp filtr�
			boolean isFilteredState = filteredRdP.getGraph().contains(subMarking);
			// Evaluer si le sous-marquage courant est un marquage d'un Rdp artificiel
			int isArtificialState = 0;
			for (IPetriNet aRdP : artificialRdpList){
				if (aRdP.getGraph().contains(subMarking))
					break;
				isArtificialState++;
			}
			if (isArtificialState == artificialRdpList.size())
				isArtificialState = -1; // on n'a trouv� aucun RdP artifiel contenant notre sous-marquage
			// on v�rifie si on n'est pas dans un puits
			boolean isStalemate = true;
			for (String trId : ends) {
				ITransition tr = completeRdP.getTransitionById(trId);
				if (tr != null && filteredRdP.isQuasiAliveFromMarking(tr, subMarking, new ArrayList<String>()))
					isStalemate = false;
			}
			
			boolean isEndState = false;
			// puis pour chaque arc entrant on les exportes et on en profite pour v�rifier si notre �tat n'est pas un �tat final (<=> un arc entrant
			// correspondant � une action de fin experte
			IAccessibleMarkings am = completeRdP.getGraph().getAccessibleMarkings(mark);
			for (IIndirectMarking j : am.getInMarkings()) {
				// exportation des arcs
				edges += exportEdges(j.getRefMarking(), am.getRefMarking(), j.getTransition().getId(), true, mark, isPathState, path);
				// v�rification si parmis les arc entrant il n'y aurait pas une transition de fin experte
				for (String trId : ends) {
					if (trId == j.getTransition().getId())
						isEndState = true;
				}
			}
			
			nodes += exportNode(completeRdP.getGraph().getRefMarking(mark), isInitialState, isEndState, isStalemate, isPathState, isFilteredState, isArtificialState, x, y);
			x += step;
			if (x / step > size-1) {
				x = 0;
				y += step;
			}
		}
		// R�tablissement du marquage du Rdp Complet avant appel de cette fonction
		completeRdP.setCurrentMarkings(saveMark);
		return nodes + edges;
	}
	
	private static String exportWithoutCompleteGraph (IPetriNet filteredRdP, ArrayList<IPetriNet> artificialRdpList, ArrayList<PathState> path, ArrayList<String> ends) throws Exception
	{
		// sauvegarde du marquage courant du RdP Filtr�
		IMarking saveMark = filteredRdP.getCurrentMarkings().clone();
		
		// permet de garder l'association des markingCode avec les id de l'export
		HashMap<String, Integer> markingCode2exportId = new HashMap<String, Integer>();
		int currentId = 0;
		// permet de connaitre les arcs et les noeuds d�j� export�s
		HashMap<String, HashSet<String>> edgeExported = new HashMap<String, HashSet<String>>();
		HashSet<String> nodeExported = new HashSet<String>();
		
		String nodes = "";
		String edges = "";
		
		// Enregistrement des arcs parcourus par le joueur (les noeuds parcourus par le joueur seront
		// export�s apr�s l'ensemble des noeud des graphes)
		IMarking prevMark = null;
		ArrayList<IMarking> knownMarkings = new ArrayList<IMarking>();
		for (PathState node : path) {
			IMarking mark = node.submark;
			// d�termination d'un id d'export pour chaque marquage associ� � chaque edge (si ce n'est pas d�j� fait)
			if (!markingCode2exportId.containsKey(mark.getCode())) {
				markingCode2exportId.put(mark.getCode(), currentId);
				currentId++;
			}
			// Enregistrer l'arc si l'on ne l'a pas d�j� fait
			if (prevMark != null) {
				if (!edgeExported.containsKey(prevMark.getCode()) || !edgeExported.get(prevMark.getCode()).contains(mark.getCode())){
					edges += exportEdges(markingCode2exportId.get(prevMark.getCode()), markingCode2exportId.get(mark.getCode()), node.action.getAction(), false, mark, true, path);
					if (!edgeExported.containsKey(prevMark.getCode()))
						edgeExported.put(prevMark.getCode(), new HashSet<String>());
					edgeExported.get(prevMark.getCode()).add(mark.getCode());
				}
			}
			prevMark = mark.clone();
			knownMarkings.add(node.submark);
		}
		
		// Construction de la liste des marquages � �tudier (ceux du Rdp filtr� + ceux des Rdp artificiels)
		ArrayList<IMarking> markList = filteredRdP.getGraph().getAllMarkings();
		for (IPetriNet rdpA : artificialRdpList)
			markList.addAll(rdpA.getGraph().getAllMarkings());
			
		// calcul de la taille de la matrice pour afficher chaque noeud
		int size = (int) (Math.sqrt(markList.size()) + 1);
		int step = 20;
		int x = 0;
		int y = 0;
		
		// Analyse de tous les marquages des graphes (filtr� et artificiel)
		for (IMarking mark : markList)
		{
			// d�termination d'un id d'export pour ce marquage si ce n'est pas d�j� fait
			if (!markingCode2exportId.containsKey(mark.getCode())) {
				markingCode2exportId.put(mark.getCode(), currentId);
				currentId++;
			}
			// Evaluer si le marquage courant est le marquage initial
			boolean isInitialState = filteredRdP.getInitialMarkings().isEqualTo(mark);
			// Evaluer si le marquage courant est un marquage par lequel le joueur est pass�
			boolean isPathState = mark.existIn(knownMarkings);
			// Evaluer si le marquage courant est un marquage du Rdp filtr�
			boolean isFilteredState = filteredRdP.getGraph().contains(mark);
			// Evaluer si le marquage courant est un marquage d'un Rdp artificiel
			int isArtificialState = 0;
			for (IPetriNet aRdP : artificialRdpList){
				if (aRdP.getGraph().contains(mark))
					break;
				isArtificialState++;
			}
			if (isArtificialState == artificialRdpList.size())
				isArtificialState = -1; // on n'a trouv� aucun RdP artifiel contenant notre marquage
			// on v�rifie si on n'est pas dans un puits
			boolean isStalemate = true;
			for (String trId : ends) {
				ITransition tr = filteredRdP.getTransitionById(trId);
				if (tr != null && filteredRdP.isQuasiAliveFromMarking(tr, mark, new ArrayList<String>()))
					isStalemate = false;
			}
			
			boolean isEndState = false;
			// Pour d�terminer si on est sur un �tat final on doit consulter les arc entrant pour v�rifier s'il n'y aurait pas une action experte menant
			// � cet �tat. On a donc besoin de conna�tre le bon Rdp sur lequel travailler
			IPetriNet rdpW;
			if (isFilteredState)
				rdpW = filteredRdP;
			else if (isArtificialState != -1)
				rdpW = artificialRdpList.get(isArtificialState);
			else
				throw new Exception("PetriNet::exportWithoutCompleteGraph => marking \"" + mark.getCode() + "\" is not a known marking");
			// puis pour chaque arc entrant on les exportes et on en profite pour v�rifier si notre �tat n'est pas un �tat final (<=> un arc entrant
			// correspondant � une action de fin experte)
			IAccessibleMarkings am = rdpW.getGraph().getAccessibleMarkings(mark);
			for (IIndirectMarking _in : am.getInMarkings()) {
				IMarking parentMarking = rdpW.getGraph().getMarkingByRef(_in.getRefMarking());
				// v�rification de l'existance de ces marquages dans le dictionnaire
				if (!markingCode2exportId.containsKey(parentMarking.getCode())) {
					markingCode2exportId.put(parentMarking.getCode(), currentId);
					currentId++;
				}
				// exportation de l'arc si l'on ne l'a pas d�j� fait
				if (!edgeExported.containsKey(parentMarking.getCode()) || !edgeExported.get(parentMarking.getCode()).contains(mark.getCode())){
					edges += exportEdges(markingCode2exportId.get(parentMarking.getCode()), markingCode2exportId.get(mark.getCode()), _in.getTransition().getId(), false, mark, isPathState, path);
					if (!edgeExported.containsKey(parentMarking.getCode()))
						edgeExported.put(parentMarking.getCode(), new HashSet<String>());
					edgeExported.get(parentMarking.getCode()).add(mark.getCode());
				}
				
				// v�rification si parmis les arc entrant il n'y aurait pas une transition de fin experte
				for (String trId : ends) {
					if (trId == _in.getTransition().getId())
						isEndState = true;
				}
			}
			// on exporte aussi les arcs sortants
			am = rdpW.getGraph().getAccessibleMarkings(mark);
			for (IIndirectMarking _out : am.getOutMarkings()) {
				IMarking childMarking = rdpW.getGraph().getMarkingByRef(_out.getRefMarking());
				// v�rification de l'existance de ces marquages dans le dictionnaire
				if (!markingCode2exportId.containsKey(childMarking.getCode())) {
					markingCode2exportId.put(childMarking.getCode(), currentId);
					currentId++;
				}
				// exportation de l'arc si l'on ne l'a pas d�j� fait
				if (!edgeExported.containsKey(mark.getCode()) || !edgeExported.get(mark.getCode()).contains(childMarking.getCode())){
					edges += exportEdges(markingCode2exportId.get(mark.getCode()), markingCode2exportId.get(childMarking.getCode()), _out.getTransition().getId(), false, childMarking, isPathState, path);
					if (!edgeExported.containsKey(mark.getCode()))
						edgeExported.put(mark.getCode(), new HashSet<String>());
					edgeExported.get(mark.getCode()).add(childMarking.getCode());
				}
			}
			
			// Enregistrement du noeud si ce n'est pas d�j� fait
			if (!nodeExported.contains(mark.getCode())){
				nodes += exportNode(markingCode2exportId.get(mark.getCode()), isInitialState, isEndState, isStalemate, isPathState, isFilteredState, isArtificialState, x, y);
				nodeExported.add(mark.getCode());
			}
			x += step;
			if (x / step > size-1) {
				x = 0;
				y += step;
			}
		}
		
		// Construction des noeud manquant du chemin du joueur � l'ext�rieur des graphes filtr� et artificiels
		for (PathState node : path) {
			IMarking mark = node.submark;
			ITrace action = node.action;
			// V�rifier si ce marquage n'est pas � l'ext�rieur des graphes (le filtr� et les artificiels)
			boolean markInside = filteredRdP.contains(mark);
			if (!markInside){
				for (IPetriNet rdpA : artificialRdpList)
					if (rdpA.contains(mark))
						markInside = true;
			}
			// Si ce marquage est � l'ext�rieur des graphes...
			if (!markInside) {
				// ... v�rifie s'il n'aurait pas d�j� �t� trait�
				if (!nodeExported.contains(mark.getCode())) {
					// v�rification de l'existance de ce marquage dans le dictionnaire
					if (!markingCode2exportId.containsKey(mark.getCode())) {
						markingCode2exportId.put(mark.getCode(), currentId);
						currentId++;
					}
					// on ajoute ce noeud avec un id d'export unique
					nodes += exportNode(markingCode2exportId.get(mark.getCode()), false, ends.indexOf(action.getAction()) != -1, false, true, false, -1, x, y);
					x += step;
					if (x / step > size-1) {
						x = 0;
						y += step;
					}
					// enregistrer l'association marking/exportId
					markingCode2exportId.put(mark.getCode(), currentId);
					currentId++;
					nodeExported.add(mark.getCode());
				}
			}
		}
		
		// R�tablissement du marquage du Rdp Filtr� avant appel de cette fonction
		filteredRdP.setCurrentMarkings(saveMark);
		return nodes + edges;
	}
	
	private static String exportNode(int id, boolean isInitialState, boolean isEndState, boolean isStalemate, boolean isPathState, boolean isFilteredState, int isArtificialState, int x, int y) {
		String node = "\t\t<node id=\"" + id + "\">\n";
		// V�rifier si ce noeud n'est pas le marquage initial, si c'est le cas, on le colore en violet
		if (isInitialState) {
			// Si tel est le cas, on l'affiche en violet
			node += "\t\t\t<data key=\"r\">112</data>\n";
			node += "\t\t\t<data key=\"g\">48</data>\n";
			node += "\t\t\t<data key=\"b\">160</data>\n";
			node += "\t\t\t<data key=\"special_type\">initState</data>\n";
		} else {
			// V�rifier si ce noeud est un �tat final
			if (isEndState) {
				// Si tel est le cas, on l'affiche en rouge
				node += "\t\t\t<data key=\"r\">192</data>\n";
				node += "\t\t\t<data key=\"g\">80</data>\n";
				node += "\t\t\t<data key=\"b\">77</data>\n";
				node += "\t\t\t<data key=\"special_type\">endState</data>\n ";
			} else {
				// v�rifier si ce noeud ne serait pas un noeud atteint par le joueur
				if (isPathState) {
					// Si tel est le cas, on l'affiche en jaune
					node += "\t\t\t<data key=\"r\">255</data>\n";
					node += "\t\t\t<data key=\"g\">215</data>\n";
					node += "\t\t\t<data key=\"b\">0</data>\n";
					node += "\t\t\t<data key=\"special_type\">pathState</data>\n ";
				} else {
					// v�rifier si on n'est pas dans un puits
					if (isStalemate) {
						// si tel est le cas, on l'affiche en orange citrouille
						node += "\t\t\t<data key=\"r\">250</data>\n";
						node += "\t\t\t<data key=\"g\">191</data>\n";
						node += "\t\t\t<data key=\"b\">143</data>\n";
						node += "\t\t\t<data key=\"special_type\">stalemate</data>\n";
					} else {
						// v�rifier si ce noeud n'est pas inclus dans le Rdp Filtr�
						if (isFilteredState) {
							// si tel est le cas, on l'affiche en vert sans type sp�cial
							node += "\t\t\t<data key=\"r\">155</data>\n";
							node += "\t\t\t<data key=\"g\">187</data>\n";
							node += "\t\t\t<data key=\"b\">89</data>\n";
						} else {
							// v�rifier si ce noeud n'est pas inclus dans un Rdp artificiel
							if (isArtificialState != -1) {
								// si tel est le cas, on l'affiche en vert sans type sp�cial
								node += "\t\t\t<data key=\"r\">54</data>\n";
								node += "\t\t\t<data key=\"g\">240</data>\n";
								node += "\t\t\t<data key=\"b\">249</data>\n";
							} else {
								// couleur par d�faut = bleue, sans type sp�cial
								node += "\t\t\t<data key=\"r\">79</data>\n";
								node += "\t\t\t<data key=\"g\">129</data>\n";
								node += "\t\t\t<data key=\"b\">189</data>\n";
							}
						}
					}
				}
			}
		}
		// V�rifier le type de base de l'�tat
		if (isFilteredState)
			node += "\t\t\t<data key=\"base_type\">filteredState</data>\n";
		else{
			if (isArtificialState != -1)
				node += "\t\t\t<data key=\"base_type\">artificialState"+isArtificialState+"</data>\n";
			else
				node += "\t\t\t<data key=\"base_type\">completeState</data>\n";
		}
		// int�gration de la position
		node += "\t\t\t<data key=\"x\">" + x + "</data>\n";
		node += "\t\t\t<data key=\"y\">" + y + "</data>\n";
		
		node += "\t\t</node>\n";
		return node;
	}
	
	private static String exportEdges(int source, int target, String name, boolean completeGraphKnown, IMarking mark, boolean isPathState, ArrayList<PathState> path) {
		String edge = "\t\t<edge source=\"" + source + "\" target=\"" + target + "\">\n";
		edge += "\t\t\t<data key=\"edgelabel\">" + name + "</data>\n";
		// v�rifier si le marquage en cours de traitement a �t� visit� par le joueur
		if (isPathState) {
			// V�rifier si la transition ayant permis de g�n�rer cet �tat correspond au indirect marking en cours d'analyse
			// Attention le joueur � p� g�n�rer plusieurs fois un m�me �tat avec des transitions diff�rentes !!!
			int cpt = 0;
			String tags = "";
			for (PathState node : path) {
				IMarking pathMark;
				if (completeGraphKnown)
					pathMark = node.mark;
				else
					pathMark = node.submark;
				ITrace pathTrace = node.action;
				if (pathTrace != null && mark.getCode() == pathMark.getCode() && name == pathTrace.getAction()) {
					tags += cpt+":"+pathTrace.getLabels()+";";
				}
				cpt++;
			}
			edge += "\t\t\t<data key=\"tag\">" + tags + "</data>\n";
		}
		edge += "\t\t</edge>\n";
		return edge;
	}
	
	public String getAccessibleGraphString()
	{
		if (graph == null)
			return "there is no accessible/coverabilityGraph calculated for this petri net";
		return graph.toString();
	}
	
	public ArrayList<IMarking> getAllPossibleMarkings()
	{
		if (graph == null)
			return null;
		return graph.getAllMarkings();
	}
	
	/** returns transitions that can be fired in accordance with the current Petri Net state */
	public ArrayList<ITransition> getCurrentActivatedTransitions()
	{
		ArrayList<ITransition> currentTransitions = new ArrayList<ITransition>();
		for (ITransition tr : this.transitions)
		{
			if (this.enabledTransition(tr))
				currentTransitions.add(tr);
		}
		return currentTransitions;
	}
	
	/**
	 * renvoie le nombre de jeton present dans la place dans le marquage initial
	 * -1 si aucune des places presentes dans le rdp n'a le m�me id que la place pass� en argument
	 */
	public int getCurrentMarking(String placeId)
	{
		if (!refPlaceInfoById.containsKey(placeId) || refPlaceInfoById.get(placeId) == -1)
			return -1;
		else
			return currentMarking.getTokenAt(refPlaceInfoById.get(placeId));
	}
	
	public IMarking getCurrentMarkings()
	{
		return currentMarking;
	}
	
	public String getGlobalStrategy() 
	{
		return _globalStrategy;
	}
	
	public IGraph getGraph()
	{
		return this.graph;
	}
	
	public String getId()
	{
		return id;
	}
	
	/**
	 * retourne l'indice de marquage de la place
	 * ie si la place "p1" a l'indice i alors cela veut dire que dans les marquages de ce rdp le poid
	 * de cette place sera en i-�me position
	 */
	protected int getIndexOfPlace(IPlaceInfo pl)
	{
		IPlaceInfo p = null;
		for (IPlaceInfo place : placesInfo)
		{
			if (pl.getId() == place.getId())
				p = place;
		}
		if (p == null)
			return -1;
		return this.placesInfo.indexOf(p);
	}
	
	/**
	 * renvoie le nombre de jeton present dans la place dans le marquage initial
	 * -1 si aucune des places presentes dans le rdp n'a le m�me id que la place pass� en argument
	 */
	public int getInitialMarking(String placeId)
	{
		if (!refPlaceInfoById.containsKey(placeId) || refPlaceInfoById.get(placeId) == -1)
			return -1;
		else
			return initialMarking.getTokenAt(refPlaceInfoById.get(placeId));
	}
	
	/**
	 * @inheritDoc
	 * @param	tr
	 * @return
	 */
	public IMarking getLocalMarking (ITransition tr)
	{
		ArrayList<String> placesId = new ArrayList<String>();
		IMarking mark = new Marking();
		//pour tous les arcs sortant de la transition
		for (IArc arcOut : tr.getArcsOut() )
		{
			String id = arcOut.getTarget().getId();
			//si on as deja ajoute la place pas la peine de le re faire
			if ( placesId.indexOf( id ) != -1 ) continue;
			for (int ind = 0 ; ind < this.placesInfo.size() ; ind++)
			{
				if ( this.placesInfo.get(ind).getId() == id )
				{
					mark.setTokenAt( ind, this.currentMarking.getTokenAt( ind ) );
					placesId.add( id );
				}
			}
		}
		//pour tous les arcs entrant  de la transition
		for (IArc arcIn : tr.getArcsIn() )
		{
			String id2 = arcIn.getTarget().getId();
			//si on as deja ajoute la place pas la peine de le re faire
			if ( placesId.indexOf( id2 ) != -1 ) continue;
			for (int index = 0 ; index < this.placesInfo.size() ; index++)
			{
				if( this.placesInfo.get(index).getId() == id2 )
				{
					mark.setTokenAt (index, this.currentMarking.getTokenAt (index));
					placesId.add (id2);
				}
			}
		}
		return mark;
	}
	
	public IMarking getInitialMarkings()
	{
		return initialMarking;
	}
	
	public String getKindOfGraph() 
	{
		return _kindOfGraph;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**
	 * Renvoie l'ensemble des marquages les plus proches de "mark" qui permettent d'atteindre la transition "tr".
	 * Par plus proche on entend les marquages (satisfaisant la contrainte d'atteinte de la transition) pour lesquels
	 * la distance IMarking::distanceWith() est minimale.
	 * @throws Exception 
	 */
	public ArrayList<IMarking> getNearestMarkings(IMarking mark, ITransition tr) throws Exception {
		return this.graph.getNearestMarkings(mark, tr);
	}
	
	public IPlaceInfo getPlaceById(String placeId)
	{
		int ref = getRefPlaceById(placeId);
		if (ref == -1)
			return null;
		else
			return placesInfo.get(ref);
	}
	
	public ArrayList<IPlaceInfo> getPlaces()
	{
		return placesInfo;
	}
	
	/**
	 * returns places in of the "t" transition that haven't required tokens
	 * if arc between a place in and "t" is a regular arc or a read arc, the place haven't required tokens if the number of token inside the place is lesser than the weight of the arc
	 * if arc between a place in and "t" is inhibitor arc, the place haven't required tokens if the number of token inside the place is highter or equal than the weight of the arc
	 */
	public ArrayList<IPlaceInfo> getPlacesWithoutRequiredTokens(ITransition t)
	{
		ArrayList<IPlaceInfo> a = new ArrayList<IPlaceInfo>();
		for (IArc arcIn : t.getArcsIn())
		{
			int i = refPlaceInfoById.get(arcIn.getSource().getId());
			if (arcIn.getType().equals(Arc.REGULAR_ARC) || arcIn.getType().equals(Arc.READ_ARC))
			{
				if (currentMarking.getTokenAt(i) < arcIn.getWeight())
				{
					a.add((IPlaceInfo) arcIn.getSource());
				}
			}
			else if (arcIn.getType() == Arc.INHIBITOR_ARC)
			{
				if (currentMarking.getTokenAt(i) >= arcIn.getWeight())
				{
					a.add((IPlaceInfo) arcIn.getSource());
				}
			}
				// Adds here other type arc
		}
		return a;
	}
	
	public int getRefPlaceById(String placeId)
	{
		Integer val = refPlaceInfoById.get(placeId); 
		// return a place corresponding to an id from the local places Array
		if (val != null)
			return val.intValue();
		else
			return -1;
	}
	
	private int getRefTransitionById(String transitionId)
	{
		Integer val = refTransitionById.get(transitionId);
		// return a place corresponding to an id from the local places Array
		if (val != null)
			return val.intValue();
		else
			return -1;
	}
	
	/**
	 * Retourne l'ensemble des plus courts chemins sous la forme de graphes allant du marquage "startMarking" au(x) marquage(s)
	 * permettant de d�clancher la transition "target". Chaque noeud des graphes retourn�s contiennent leur distance � la fin du chemin.
	 * D'autre part, si un marquage d'un graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes. Dans ces cas l� les transitions syst�mes sont consid�r�es avec un poids
	 * de 0 dans le calcul de la distance du chemin.
	 * @throws Exception 
	 */
	public ArrayList<IPathIntersection> getShortestPathsToTransition(IMarking startMarking, ITransition target, ArrayList<String> systemTransition) throws Exception
	{
		return this.graph.getShortestPathsToTransition(startMarking, target, systemTransition);
	}
	
	public ITransition getTransitionById(String transitionId)
	{
		int i = getRefTransitionById(transitionId);
		if (i == -1)
			return null;
		else
			return transitions.get(i);
	}
	
	public ArrayList<ITransition> getTransitions()
	{
		return transitions;
	}
	
	/** @return la liste des transition contenant dans leur nom le keyWord */
	public ArrayList<ITransition> getTransitionsByKeyWord(String keyWord)
	{
		ArrayList<ITransition> trList = new ArrayList<ITransition>();
		//pour toutes les transition dans le rdp
		for (ITransition tr : this.getTransitions())
		{
			//si le nom de la transition contient le mot clef
			if (tr.getName().indexOf(keyWord) != -1)
				// on enregistre cette transition
				trList.add(tr);
		}
		return trList;
	}
	
	/**
	 * @throws Exception 
	 * @inheritDoc
	 */
	public void initialization() throws Exception
	{
		if (!initialMarking.isEqualTo(currentMarking))
			initialMarking = currentMarking.clone();
		if (graph != null)
		{
			if (initialMarking.isEqualTo(graph.getInitialMarking()))
				return;
		}
		if (computeGraph)
		{
			// compute the new graph
			if (_kindOfGraph.equals(CoverabilityGraph.TYPE))
				// construction d'un graphe de couverture
				// V�rification de la validit� de la strat�gie
				if (_globalStrategy.equals(CoverabilityGraph.STRATEGY_FIRST) || _globalStrategy.equals(CoverabilityGraph.STRATEGY_AND) || _globalStrategy.equals(CoverabilityGraph.STRATEGY_OR))
					graph = new CoverabilityGraph(initialMarking, placesInfo, transitions, _globalStrategy);
				else
					throw new Exception("PetriNet::initialization => strategy "+_globalStrategy+" is not a known strategy");
			else if (_kindOfGraph == AccessibleGraph.TYPE)
				// construction d'un graphe d'accessibilit�
				graph = new AccessibleGraph(initialMarking, placesInfo, transitions);
			else
				throw new Error("PetriNet::initialization => "+_kindOfGraph+" is not a known kind of graph");
			graph.computeGraph();
		}
	}
	
	public void initializeTokens(String placeId, int initialToken) throws Exception
	{
		int i = getRefPlaceById(placeId);
		if (i == -1)
			throw new Exception("\"" + placeId + "\" is not a known place");
		currentMarking.setTokenAt(i, initialToken);
	}
	
	/** Check if the transition "t" is quasi-alive for every marking of the reachableGraph 
	 * @throws Exception */
	public boolean isAlwaysAlive(ITransition t) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return graph.isAlwaysEnabled(t);
	}
	
	/**
	 * Check if the transition "t" is belated. A transition is belated if it is not enabled, previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isBelated(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return (!enabledTransition(t)) && (graph.isPreviouslyEnabled(t, currentMarking)) && (!graph.isSubsequentlyEnabled(t, currentMarking, systemTransition));
	}
	
	/** Check if the petri net is in deadlock. No transition can be fired. */
	public boolean isDeadlock()
	{
		for (ITransition tr : transitions)
		{
			if (enabledTransition(tr))
				return false;
		}
		return true;
	}
	
	/**
	 * Check if the transition "t" is inserted. A transition is inserted if it is not enabled, previously enabled in the
	 * graph of possible states and subsequently enabled in the graph of possible states.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isInserted(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return (!enabledTransition(t)) && (graph.isPreviouslyEnabled(t, currentMarking)) && (graph.isSubsequentlyEnabled(t, currentMarking, systemTransition));
	}
	
	/**
	 * Check if the transition "t" is premature. A transition is premature if it is not enabled, not previously enabled in
	 * the graph of possible states and subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isPremature(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return (!enabledTransition(t)) && (!graph.isPreviouslyEnabled(t, currentMarking)) && (graph.isSubsequentlyEnabled(t, currentMarking, systemTransition));
	}
	
	/**
	 * V�rifie si une transition "t" a �t� pr�c�dement sensibilis�e par rapport � l'�tat courant.
	 * @throws Exception 
	 */
	public boolean isPreviouslyEnabled(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return graph.isPreviouslyEnabled(t, currentMarking);
	}
	
	/**
	 * V�rifie si la transition "t" est quasi vivante � partir de l'�tat initial. Une transition est dite quasi vivante
	 * si � partir d'un �tat donn�, la transition pourra �tre sensibilis�e.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront ignor�es �
	 * l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isQuasiAlive(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return graph.isSubsequentlyEnabled(t, initialMarking, systemTransition);
	}
	
	/**
	 * V�rifie si la transition "t" est quasi vivante � partir d'un �tat donn� ("marking"). Une transition est dite quasi
	 * vivante si � partir d'un �tat donn�, la transition pourra �tre sensibilis�e.
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse
	 * dans le vecteur "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront
	 * ignor�es � l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isQuasiAliveFromMarking(ITransition t, IMarking marking, ArrayList<String> systemTransition) throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		return graph.isSubsequentlyEnabled(t, marking, systemTransition);
	}
	
	/**
	 * V�rifie si le marquage "to" est accessible dans le graphe � partir de "from".
	 * @throws Exception 
	 */
	public boolean isReachable(IMarking from, IMarking to) throws Exception
	{
		return this.graph.isMarkingAccessible(to, from);
	}
	
	/**
	 * V�rifie si le marquage "marking" est un successeur imm�diat du marquage courant.
	 * @throws Exception 
	 */
	public boolean isSuccessorMarking(IMarking marking) throws Exception
	{
		return this.graph.isSuccessorMarking(currentMarking, marking);
	}
	
	/**
	 * Check if the transition "t" is useless. A transition is useless if it is not enabled, not previously enabled in the
	 * graph of possible states and not subsequently enabled in the graph of possible states
	 * D'autre part, si un marquage du graphe est connect� (en sortie) � au moins une transition syst�me (ie. incluse dans le vecteur
	 * "systemTransition") toutes les autres transitions connect�es (en sortie) � ce marquage seront ignor�es �
	 * l'exception des transitions syst�mes
	 * @throws Exception 
	 */
	public boolean isUseless(ITransition t, ArrayList<String> systemTransition) throws Exception
	{
		return !isQuasiAlive(t, systemTransition);
	}
	
	/**
	 * Calcule la plus petite distance entre le marquage pass� en param�tre ("marking") et l'ensemble des marquages possibles
	 * du Rdp qui permettent d'atteindre la transition "tr"
	 * @param marking
	 * @return la plus petite distance
	 * @throws Exception 
	 */
	public int minimalDistanceWith(IMarking marking, ITransition tr) throws Exception
	{
		int diff = this.graph.minimalDistanceWith(marking, tr);
		//System.out.println("La plus petite distance avec " + marking.getCode() + " = " + diff );
		return diff;
	}
	
	
	public void printAccessibleGraph() throws Exception
	{
		if (graph == null)
			throw new Exception("Reachable graph is null, you have to call PetriNet \"compute()\" function before and wait \"COMPLETE\" event");
		graph.print();
	}
	
	public void resetCurrentMarkings()
	{
		currentMarking = initialMarking.clone();
	}
	
	public void setCurrentMarkings(IMarking p)
	{
		currentMarking = p.clone();
	}
	
	public void setId(String str)
	{
		id = str;
	}
	
	public void setName(String str)
	{
		name = str;
	}
	
	public void setPlaceId(String oldId, String newId) throws Exception
	{
		Integer val = refPlaceInfoById.get(oldId);
		if (val == null)
			throw new Exception ("Place with id "+oldId+" not found");
		refPlaceInfoById.remove(oldId);
		refPlaceInfoById.put(newId, val);
		placesInfo.get(val).setId(newId);
	}
	
	public void setPlaces(ArrayList<IPlaceInfo> pl)
	{
		this.placesInfo = pl;
		refPlaceInfoById = new HashMap<String, Integer>(); // reset dictionary
		for (int i = 0 ; i < placesInfo.size(); i++)
			refPlaceInfoById.put(placesInfo.get(i).getId(), i);
	}
	
	public void setTransitionId(String oldId, String newId) throws Exception
	{
		Integer value = refTransitionById.get(oldId);
		if (value == null){
			throw new Exception ("Transition with id "+oldId+" not found");
		}
		refTransitionById.remove(oldId);
		refTransitionById.put(newId, value);
		transitions.get(value).setId(newId);
	}
	
	public void setTransitions(ArrayList<ITransition> trans)
	{
		this.transitions = trans;
		refTransitionById = new HashMap<String, Integer>();
		for (int i = 0 ; i < transitions.size() ; i++)
			refTransitionById.put(transitions.get(i).getId(), i);
	}
}
