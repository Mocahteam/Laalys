package fr.lip6.mocah.laalys.traces;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;

/**
 * ...
 * @author Mathieu Muratet, Clément Rouanet
 */

public interface ITraces {
	/**
	 * load the file which the path is given as argument
	 * then automatically parse it to create new traces
	 * @param	url can be a local file (relative or absolute path) or an internet adress
	 * @throws IOException 
	 */
	public void loadFile (String url) throws IOException;
	/**
	 * @return number of action in traces
	 */
	public int length();
	/**
	 * return current action
	 * @return
	 */
	public ITrace current();
	/**
	 * check if there is a next action
	 * @return true/false
	 */
	public boolean isNext();
	/**
	 * @return the next action or null if there is no other
	 */
	public ITrace next();
	/**
	 * set the current action to  the first one then return it
	 * @return the first trace  of the trace list
	 */
	public ITrace first();
	/**
	 * reset all labels stored on traces
	 * reset current trace to first one
	 */
	public void reset();
	/**
	 * clear all traces
	 */
	public void clear();
	/**
	 * clone all traces from "source" into this traces (without labels)
	 */
	public void  copy(ITraces source);
	/**
	 * append all traces from "source" into this traces
	 */
	public void  append(ITraces source);
	/**
	 * return a string representing traces
	 * XML formated
	 * @return
	 */
	public String toString();
	/**
	 * return an XML structure containing traces
	 * @return
	 */
	public Document toXML();
	
	/**
	 * GETTER
	 */
	public ArrayList<ITrace> getTraces();
	 
	/**
	 * SETTER
	 */
	public void setTraces(ArrayList<ITrace> value);
}
