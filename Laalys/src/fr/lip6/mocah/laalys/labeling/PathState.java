package fr.lip6.mocah.laalys.labeling;

import fr.lip6.mocah.laalys.petrinet.IMarking;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;

public class PathState {
	public ITrace action;
	public IMarking mark;
	public IMarking submark;
	public IPetriNet rdp;
	
	public PathState (ITrace action, IMarking mark, IMarking submark, IPetriNet rdp){
		this.action = action;
		this.mark = mark;
		this.submark = submark;
		this.rdp = rdp;
	}
}
