package fr.lip6.mocah.laalys.labeling;

import fr.lip6.mocah.laalys.petrinet.IMarking;
import fr.lip6.mocah.laalys.traces.ITrace;

public class PathState {
	public ITrace action;
	public IMarking mark;
	public IMarking submark;
	
	public PathState (ITrace action, IMarking mark, IMarking submark){
		this.action = action;
		this.mark = mark;
		this.submark = submark;
	}
}
