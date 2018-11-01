import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class Main {
	public static void main(String[] args) {
		if (args.length == 0)
			new InterfaceLaalys();
		else{
			// Parse options
			String fullDirName = null;
			String filteredDirName = null;
			String featuresDirName = null;
			String traceName = null;
			String serverIP = null;
			Integer serverPort = null;
			String outputName = null;
			boolean debug = false;
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
						System.out.println("\t\t-d\t\t\t\tshow debug logs");
						System.out.println("\t\t-fullPn <DIR>\t\t\tload full Petri nets included into DIR");
						System.out.println("\t\t-filteredPn <DIR>\t\tload filtered Petri nets included into DIR");
						System.out.println("\t\t-features <DIR>\t\tload features included into DIR");
						System.out.println("\t\t-traces <FILE>\t\t\tload file containing traces to analyse");
						System.out.println("\t\t-o <FILE>\t\t\toutput file to store analysis (xml extension is automaticaly added)");
						System.out.println("");
						System.out.println("\t\tIf -traces option is not set, two more options are parsed:");
						System.out.println("\t\t\t-serverIP <IP_ADRESS>\tTCP address that sends traces");
						System.out.println("\t\t\t-serverPort <PORT>\tTCP port used by server");
						System.exit(0);
					case "-d":
						i++;
						debug = true;
						break;
					case "-fullPn":
						i++;
						if (i < args.length)
							fullDirName = args[i];
						break;
					case "-filteredPn":
						i++;
						if (i < args.length)
							filteredDirName = args[i];
						break;
					case "-features":
						i++;
						if (i < args.length)
							featuresDirName = args[i];
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
			
			// Check if required directories are set and exist
			if (fullDirName == null){
				System.err.println("Error: Full Petri nets directory required for command line usage (see -fullPn option).");
				System.exit(-1);
			}
			File fullDir = new File(fullDirName);
			if (!fullDir.exists() || !fullDir.isDirectory()){
				System.err.println("Error with -fullPn option: "+fullDirName+": No such directory.");
				System.exit(-2);
			}
			if (filteredDirName == null){
				System.err.println("Error: Filtered Petri nets directory required for command line usage (see -filteredPn option).");
				System.exit(-3);
			}
			File filteredDir = new File(filteredDirName);
			if (!filteredDir.exists() || !filteredDir.isDirectory()){
				System.err.println("Error with -filteredPn option: "+filteredDirName+": No such directory.");
				System.exit(-4);
			}
			if (featuresDirName == null){
				System.err.println("Error: Features directory required for command line usage (see -features option.");
				System.exit(-5);
			}
			File featuresDir = new File(featuresDirName);
			if (!featuresDir.exists() || !featuresDir.isDirectory()){
				System.err.println("Error with -features option: "+featuresDirName+": No such directory.");
				System.exit(-6);
			}
			
			// Hash map to associate base Petri net name and labeling algorithm
			HashMap<String, ILabeling> pnName2labelingAlgo = new HashMap<>();
			
			// Check if for each file inside full Pn directory equivalent files exist in filtered and features directories
			for (File fullChild : fullDir.listFiles()){
				// get equivalent file in filtered directory
				File filteredChild = new File(filteredDir, fullChild.getName());
				// get equivalent file in features directory
				File featuresChild = new File(featuresDir, fullChild.getName().substring(0, fullChild.getName().length()-4)+"xml");
				if (!filteredChild.exists() || !featuresChild.exists()){
					System.err.println("Error: equivalent file of \""+fullDir.getName()+"/"+fullChild.getName()+"\" doesn't exist in \""+filteredDir.getName()+"\" or \""+featuresDir.getName()+"\".");
					System.exit(-7);
				}
				// Instantiate full Petri net
				IPetriNet fullPn = new PetriNet(false, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
				try {
					fullPn.loadPetriNet(fullChild.getAbsolutePath());
				} catch (Exception e) {
					System.err.println("Error with -fullPn option: unable to load "+fullChild.getAbsolutePath()+" file.\n"+e.getMessage());
					System.exit(-8);
				}
				// Instantiate filtered Petri net
				IPetriNet filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
				try {
					filteredPn.loadPetriNet(filteredChild.getAbsolutePath());
				} catch (Exception e) {
					System.err.println("Error with -filteredPn option: unable to load "+filteredChild.getAbsolutePath()+" file.\n"+e.getMessage());
					System.exit(-9);
				}
				// Instantiate features
				IFeatures features = new Features();
				try {
					features.loadFile(featuresChild.getAbsolutePath());
				} catch (IOException e) {
					System.err.println("Error with -features option: unable to load "+featuresChild.getAbsolutePath()+" file.\n"+e.getMessage());
					System.exit(-10);
				}
				
				// Init labeling algorithm
				ILabeling algo = new Labeling_V10(debug);
				algo.setCompletePN(fullPn);
				algo.setFilteredPN(filteredPn);
				algo.setFeatures(features);
				try {
					algo.reset();
				} catch (Exception e1) {
					System.out.println("Labeling algorithm initialisation fail. "+e1.getMessage());
				}
				String pnName = fullChild.getName();
				if (pnName.endsWith(".pnml"))
					pnName = pnName.substring(0, pnName.length()-5);
				pnName2labelingAlgo.put(pnName, algo);
			}
			
			ITraces tracesFromFile = null;
			Socket tracesFromSocket = null;
			if (traceName == null && (serverIP == null || serverPort == null)){
				System.err.println("Error: No input traces defined, you have to set -traces option OR -serverIP and -serverPort options.");
				System.exit(-11);
			} else if (traceName != null){
				File f = new File(traceName);
				if (!f.exists() || f.isDirectory()){
					System.err.println("Error with -traces option: "+traceName+": No such file.");
					System.exit(-12);
				} else {
					tracesFromFile = new Traces();
					try {
						tracesFromFile.loadFile(traceName);
					} catch (IOException e) {
						System.err.println("Error with -traces option: unable to load "+traceName+" file.\n"+e.getMessage());
						System.exit(-13);
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

			
			if (tracesFromFile != null){
				try {
					for (ITrace trace : tracesFromFile.getTraces())
					{
						ILabeling algo = pnName2labelingAlgo.get(trace.getPnName());
						if (algo != null)
							algo.labelAction( trace );
						else{
							System.err.println("Unknown Petri net \""+trace.getPnName()+"\" to label \""+trace.getAction()+"\" action.");
						}
					}
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
					        	String triggerableActions = "";
					        	for (Map.Entry<String, ILabeling> entry : pnName2labelingAlgo.entrySet()){
					        		ILabeling algo = entry.getValue();
						        	// get all available transitions
						        	IPetriNet workingPn = algo.getFilteredPN();
						        	ArrayList<ITransition> transitions = workingPn.getTransitions();
						        	// parse all transition and check if they are enabled
						        	for (int i = 0 ; i < transitions.size() ; i++){
						        		if (workingPn.enabledTransition(transitions.get(i))){
						        			// store this enabled transition
						        			triggerableActions += transitions.get(i).getId();
						        		}
						        	}
					        	}
					        	// send back actions
					        	System.out.println("Send actions: "+triggerableActions);
					        	pw.println(triggerableActions);
					        } else if (tokens[0].equalsIgnoreCase("NextActionToReach") && tokens.length == 4){
					        	// found associated labeling algorithm instance
					        	ILabeling algo = pnName2labelingAlgo.get(tokens[1]);
					        	if (algo != null){
					        		// compute and send back next action to perform
					        		String nextActions = algo.getNextBetterActionsToReach(tokens[2], Integer.parseInt(tokens[3]));
					        		System.out.println("Send actions: "+nextActions);
					        		pw.println(nextActions);
					        	} else {
					        		System.err.println("Unknown Petri net \""+tokens[1]+"\" to label \""+tokens[2]+"\" action.");
					        		pw.println("");
					        	}
					        } else if (tokens.length == 3){
					        	// Cas par défaut ou le contenu doit suivre le format suivant : pnName\tactionName\tperformedBy
					        	String mergedLabels = ""; 
					        	try{
						        	// found associated labeling algorithm instance
						        	ILabeling algo = pnName2labelingAlgo.get(tokens[0]);
						        	if (algo != null){
							        	ITrace trace = new Trace(tokens[0], tokens[1], null, tokens[2], null);
						        		algo.labelAction(trace);
							        	// merge labels 
							        	for (int i = 0 ; i < trace.getLabels().size() ; i++)
							        		mergedLabels += trace.getLabels().get(i)+"\t";
							        	// removing last \t
							        	if (mergedLabels.endsWith("\t"))
							        		mergedLabels = mergedLabels.substring(0, mergedLabels.length()-1);
						        	} else
						        		System.err.println("Unknown Petri net \""+tokens[0]+"\" to label \""+tokens[1]+"\" action.");
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
