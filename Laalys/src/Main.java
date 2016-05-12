import java.io.StringWriter;
import java.util.Vector;

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
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Traces;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ITraces traces = new Traces();
		traces.loadFile("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\SVN_Mocah\\laalys-As3\\Trunk\\LaalysV9\\bin\\exemples\\trace\\expe_paris_montagne\\MurDeGlace\\Bem.xml");
		Document exp = traces.toXML();
		
		StringWriter sw = new StringWriter();
		try {
		 Transformer t = TransformerFactory.newInstance().newTransformer();
		 t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		 t.setOutputProperty(OutputKeys.INDENT, "yes");
		 t.transform(new DOMSource(exp), new StreamResult(sw));
		} catch (TransformerException te) {
		 System.out.println("nodeToString Transformer Exception");
		}
		System.out.println(sw.toString());
		
		IFeatures features = new Features();
		features.loadFile("C:\\Users\\mmuratet\\Documents\\INSHEA\\Recherche\\SVN_Mocah\\laalys-As3\\Trunk\\LaalysV9\\bin\\exemples\\specifTransition\\murDeGlace.xml");
		System.out.println(features.getSystemTransitions());
		System.out.println(features.getEndLevelTransitions());
		
		// create an empty Vector vec with an initial capacity of 4      
		Vector<String> vec = new Vector<String>(4);
		
		// use add() method to add elements in the vector
		vec.add("a");
		vec.add("bb");
		vec.add("kjg");
		vec.add("mlj mj");
		
		// convert the contents into string
		System.out.println(vec.toString());
	}

}
