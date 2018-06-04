import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import fr.lip6.mocah.laalys.features.Features;
import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.labeling.ILabeling;
import fr.lip6.mocah.laalys.labeling.Labeling_V10;
import fr.lip6.mocah.laalys.petrinet.CoverabilityGraph;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.Traces;

/*import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import fr.lip6.mocah.laalys.features.Features;
import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.labeling.Labeling_V9;
import fr.lip6.mocah.laalys.labeling.PathState;
import fr.lip6.mocah.laalys.petrinet.CoverabilityGraph;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Traces;*/

public class Main {
	// transférer le contenu des chargements dans InterfaceLaalys.java ; ne garder que l'appel à InterfaceLaalys()
	PieChart cv ;
	
	public static void main(String[] args) {
/*//		String fullPnName = "murDeGlace.pnml";
//		String filteredPnName = "murDeGlace_contraintManuellement.pnml";
//		String featuresName = "murDeGlace.xml";
//		String traceName = "expe_paris_montagne\\MurDeGlace\\Vivianier.xml";
		String fullPnName = "Thermometer.pnml";
		String filteredPnName = "Thermometer.pnml";
		String featuresName = "Thermometer.xml";
		String traceName = "expe_paris_montagne\\Thermometer\\Vivianier.xml";
		
		// Chargement du Rdp Complet
		IPetriNet fullPn = new PetriNet(false, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
		long stamp = System.currentTimeMillis();
		try {
			fullPn.loadPetriNet("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\Mocah\\SVN_Mocah\\laalys-Java\\Laalys\\bin\\exemples\\completeNet\\"+fullPnName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("full graph size : "+fullPn.getGraph().getAllMarkings().size() + " " + (System.currentTimeMillis()-stamp));

		stamp = System.currentTimeMillis();
		// Chargement du Rdp Filtré
		IPetriNet filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
		try {
			filteredPn.loadPetriNet("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\Mocah\\SVN_Mocah\\laalys-Java\\Laalys\\bin\\exemples\\filteredNet\\"+filteredPnName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("filtered graph size : "+filteredPn.getGraph().getAllMarkings().size() + " " + (System.currentTimeMillis()-stamp));

		// Chargement des Spécificités
		IFeatures features = new Features();
		features.loadFile("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\Mocah\\SVN_Mocah\\laalys-Java\\Laalys\\bin\\exemples\\specifTransition\\"+featuresName);
//		System.out.println(features.getSystemTransitions());
//		System.out.println(features.getEndLevelTransitions());

		// Chargement des traces
		ITraces traces = new Traces();
		traces.loadFile("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\Mocah\\SVN_Mocah\\laalys-Java\\Laalys\\bin\\exemples\\trace\\"+traceName);

		Logger monLog = Logger.getLogger(Main.class.getName());
		monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
		monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau &Ge; INFO
		monLog.addHandler(ch);
		Labeling_V9 algo = new Labeling_V9(monLog, false);
		algo.setCompletePN(fullPn);
		algo.setFilteredPN(filteredPn);
		algo.setFeatures(features);
		try {
			algo.label(traces);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// print traces and labels
		for (ITrace tr : traces.getTraces()){
			System.out.println(tr.getAction()+ " "+tr.getLabels());
		}*/
		
		// Test Pour Mathieu
/*		System.out.println("Chargement du RdP et calcul de son graphe de couverture...");
		IPetriNet pn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
		try {
			long stamp = System.currentTimeMillis();
			//pn.loadPetriNet("C:\\Users\\mmuratet\\Google Drive\\Recherche\\LIP6\\SuiviEtudiants\\Mathieu\\PetriDoom.pnml");
			pn.loadPetriNet("C:\\Users\\mmuratet\\Google Drive\\Recherche\\LIP6\\SuiviEtudiants\\Mathieu\\testModelisation4.pnml");
			System.out.println("temps de calcul : "+(System.currentTimeMillis()-stamp)+"ms, pour un graphe de couverture d'une taille de : "+pn.getAllPossibleMarkings().size()+" états.");
			pn.printAccessibleGraph();
			// System.out.println("Exportation du graphe de couverture...");
			// ArrayList<String> ends = new ArrayList<String>();
			// ends.add("FinMission");
			// PetriNet.exportToGraphml(	"C:\\Users\\mmuratet\\Downloads\\testModelisation4.graphml",
										// null, pn, new ArrayList<IPetriNet>(), new ArrayList<PathState> (), ends);
			// System.out.println("... exportation du graphe de couverture terminée.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		if (args.length == 0)
			new InterfaceLaalys();
		else{
			// Parse options
			String fullPnName = null;
			String filteredPnName = null;
			String featuresName = null;
			String traceName = null;
			String serverIP = null;
			Integer serverPort = null;
			String outputName = null;
			for (int i = 0 ; i < args.length ; i++){
				switch (args[i]){
					case "-help":
					case "-h":
					case "--help":
					case "--h":
						System.out.println("Usage: LaalysV2 [OPTION]");
						System.out.println("No parameters shows GUI. In command line following options are available:");
						System.out.println("\tOptions:");
						System.out.println("\t\t-help\t\t\t\tprint this message");
						System.out.println("\t\t-fullPn <FILE>\t\t\tload file and use it as full Petri net");
						System.out.println("\t\t-filteredPn <FILE>\t\tload file and use it as filtered Petri net");
						System.out.println("\t\t-features <FILE>\t\tload features associated to Petri nets");
						System.out.println("\t\t-traces <FILE>\t\t\tload file containing traces to analyse");
						System.out.println("\t\t-o <FILE>\t\t\toutput file to store analysis (xml extension is automaticaly added)");
						System.out.println("");
						System.out.println("\t\tIf -traces option is not set, two more options are parsed:");
						System.out.println("\t\t\t-serverIP <IP_ADRESS>\tTCP address that sends traces");
						System.out.println("\t\t\t-serverPort <PORT>\tTCP port used by server");
						System.exit(0);
					case "-fullPn":
						i++;
						if (i < args.length)
							fullPnName = args[i];
						break;
					case "-filteredPn":
						i++;
						if (i < args.length)
							filteredPnName = args[i];
						break;
					case "-features":
						i++;
						if (i < args.length)
							featuresName = args[i];
						break;
					case "-traces":
						i++;
						if (i < args.length)
							traceName = args[i];
						break;
					case "-serverIP":
						i++;
						if (i < args.length)
							serverIP = args[i];
						break;
					case "-serverPort":
						i++;
						if (i < args.length)
							try{
								serverPort = new Integer(args[i]);
							} catch (NumberFormatException nfe){
								System.err.println("Error with -serverPort option: "+args[i]+": it is not a parsable integer");
								System.exit(-15);
							}
						break;
					case "-o":
						i++;
						if (i < args.length)
							outputName = args[i];
						break;
					default:
						System.err.println("Error invalid option: "+args[i]);
						System.err.println("Try 'LaalysV2 -help' for more information.");
						System.exit(-17);
				}
			}
			// Check if all required options are set
			IPetriNet fullPn = new PetriNet(false, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			if (fullPnName == null){
				System.err.println("Error: Full Petri net required for command line usage (see -fullPn option).");
				System.exit(-1);
			} else {
				File f = new File(fullPnName);
				if (!f.exists() || f.isDirectory()){
					System.err.println("Error with -fullPn option: "+fullPnName+": No such file.");
					System.exit(-2);
				} else {
					try {
						fullPn.loadPetriNet(fullPnName);
					} catch (Exception e) {
						System.err.println("Error with -fullPn option: unable to load "+fullPnName+" file.\n"+e.getMessage());
						System.exit(-3);
					}
				}
			}
			IPetriNet filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			if (filteredPnName == null){
				System.err.println("Error: Filtered Petri required for command line usage (see -filteredPn option).");
				System.exit(-4);
			} else {
				File f = new File(filteredPnName);
				if (!f.exists() || f.isDirectory()){
					System.err.println("Error with -filteredPn option: "+filteredPnName+": No such file.");
					System.exit(-5);
				} else {
					try {
						filteredPn.loadPetriNet(filteredPnName);
					} catch (Exception e) {
						System.err.println("Error with -filteredPn option: unable to load "+filteredPnName+" file.\n"+e.getMessage());
						System.exit(-6);
					}
				}
			}
			IFeatures features = new Features();
			if (featuresName == null){
				System.err.println("Error: Features required for command line usage (see -features option.");
				System.exit(-7);
			} else {
				File f = new File(featuresName);
				if (!f.exists() || f.isDirectory()){
					System.err.println("Error with -features option: "+featuresName+": No such file.");
					System.exit(-8);
				} else {
					try {
						features.loadFile(featuresName);
					} catch (IOException e) {
						System.err.println("Error with -features option: unable to load "+featuresName+" file.\n"+e.getMessage());
						System.exit(-9);
					}
				}
			}
			ITraces tracesFromFile = null;
			Socket tracesFromSocket = null;
			if (traceName == null && (serverIP == null || serverPort == null)){
				System.err.println("Error: No input traces defined, you have to set -traces option OR -serverIP and -serverPort options.");
				System.exit(-10);
			} else if (traceName != null){
				File f = new File(traceName);
				if (!f.exists() || f.isDirectory()){
					System.err.println("Error with -traces option: "+traceName+": No such file.");
					System.exit(-11);
				} else {
					tracesFromFile = new Traces();
					try {
						tracesFromFile.loadFile(traceName);
					} catch (IOException e) {
						System.err.println("Error with -traces option: unable to load "+traceName+" file.\n"+e.getMessage());
						System.exit(-12);
					}
				}
			} else { // serverIP != null && serverPort != null
				try {
					tracesFromSocket = new Socket(serverIP, serverPort.intValue());
				} catch (UnknownHostException e) {
					System.err.println("Error with -serverIP option: unable to resolve "+serverIP+" host.\n"+e.getMessage());
					System.exit(-14);
				} catch (IOException e) {
					System.err.println("Error with -serverPort option: "+serverPort.intValue()+" port is not open.\n"+e.getMessage());
					System.exit(-16);
				}
			}
			// Init labeling algorithm
			Logger monLog = Logger.getLogger(Main.class.getName());
			monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
			monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
			ConsoleHandler ch = new ConsoleHandler();
			ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau INFO
			monLog.addHandler(ch);
			ILabeling algo = new Labeling_V10(monLog, false);
			algo.setCompletePN(fullPn);
			algo.setFilteredPN(filteredPn);
			algo.setFeatures(features);
			try {
				algo.reset();
			} catch (Exception e1) {
				System.out.println("Labeling algorithm initialisation fail. "+e1.getMessage());
			}
			
			if (tracesFromFile != null){
				try {
					algo.label(tracesFromFile);
					if (outputName != null){
						if (outputName.toLowerCase().endsWith(".xml"))
							outputName = outputName.substring(0, outputName.length() - 4); // remove user extension
						Document doc = tracesFromFile.toXML();
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						Result output = new StreamResult(new File(outputName + ".xml"));
						Source input = new DOMSource(doc);
						transformer.transform(input, output);
					} else {
						System.out.println(tracesFromFile.toString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else { // tracesFromSocket == null
				try {
					// Get traces send by server
					System.out.println("Laalys connected and waiting traces...");
					BufferedInputStream bis = new BufferedInputStream(tracesFromSocket.getInputStream());
					PrintWriter pw = new PrintWriter(tracesFromSocket.getOutputStream(), true);
			        
			        // Il ne nous reste plus qu'à le lire
			        int stream = -1;
					do{
				        String content = "";
				        if (stream != -1)
				        	content += (char)stream;
				        while(bis.available()>0){
				        	content += (char)bis.read();
				        }
				        if (!content.isEmpty()){
					        System.out.println("Request received: "+content);
					        // parsing content
					        String[] tokens = content.split("\t");
					        if (tokens[0].equalsIgnoreCase("Quit")){
					        	break;
					        } else if (tokens[0].equalsIgnoreCase("TriggerableActions")){
					        	// get all available transitions
					        	IPetriNet workingPn = algo.getFilteredPN();
					        	ArrayList<ITransition> transitions = workingPn.getTransitions();
					        	// parse all transition and check if they are enabled
					        	String triggerableActions = "";
					        	for (int i = 0 ; i < transitions.size() ; i++){
					        		if (workingPn.enabledTransition(transitions.get(i))){
					        			// store this enabled transition
					        			triggerableActions += transitions.get(i).getId();
					        		}
					        	}
					        	// send back actions
					        	System.out.println("Send actions: "+triggerableActions);
					        	pw.println(triggerableActions);
					        } else if (tokens[0].equalsIgnoreCase("NextActionToReach") && tokens.length == 3){
				        		// compute and send back next action to perform
				        		String nextActions = algo.getNextBetterActionsToReach(tokens[1], Integer.parseInt(tokens[2]));
				        		System.out.println("Send actions: "+nextActions);
				        		pw.println(nextActions);
					        } else if (tokens.length == 2){
					        	// Cas par défaut ou le contenu doit suivre le format suivant : actionName\tperformedBy
					        	String mergedLabels = ""; 
					        	try{
						        	ITrace trace = new Trace(tokens[0], null, tokens[1], null);
					        		algo.labelAction(trace);
						        	// merge labels 
						        	for (int i = 0 ; i < trace.getLabels().size() ; i++)
						        		mergedLabels += trace.getLabels().get(i)+"\t";
						        	// removing last \t
						        	if (mergedLabels.endsWith("\t"))
						        		mergedLabels = mergedLabels.substring(0, mergedLabels.length()-1);
					        	}catch(Exception e){
					        		System.out.println("Warning!!! Labeling aborted: "+e.getMessage());
					        	}
					        	// send back labels
					        	System.out.println("Send labels: "+mergedLabels);
					        	pw.println(mergedLabels);
					        } else {
					        	System.out.println("Warning!!! invalid request.");
					        }
				        }
				        content = "";
					}while ((stream = bis.read()) != -1); 
					bis.close();
					pw.close();
					tracesFromSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.console().readLine();
			}
		}
	}

}
