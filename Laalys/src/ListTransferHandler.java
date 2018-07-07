import javax.swing.*;

import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.traces.constants.ActionType;

import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;

//////////////////////////////////////////////////////////////////
class ListTransferHandler extends TransferHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved. */
    private int[] indices = null;
    private int dropIndice;
    private InterfaceLaalys laalys;
    
    public ListTransferHandler(InterfaceLaalys laalys){
    	this.laalys = laalys;
    }
    
    /**
     * We only support importing strings.
     */
    public boolean canImport(TransferHandler.TransferSupport info) {
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
   }

    /**
     * Bundle up the selected items in a single list for export.
     * Each line is separated by a newline.
     */
    protected Transferable createTransferable(JComponent c) {
        JList<String> list = (JList) c;
        indices = list.getSelectedIndices();
        List<String> values = list.getSelectedValuesList();
        
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            buff.append(val == null ? "" : val.toString());
            if (i != values.size() - 1) {
                buff.append("\n");
            }
        }
        
        return new StringSelection(buff.toString());
    }
    
    /**
     * We support both copy and move actions.
     */
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }
    
    /**
     * Perform the actual import.  This demo only supports drag and drop.
     */
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        JList list = (JList)info.getComponent();
        DefaultListModel<String> listModel = (DefaultListModel)list.getModel();
        JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
        int index = dl.getIndex();
        dropIndice = index;
        boolean insert = dl.isInsert();

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        String data;
        try {
            data = (String)t.getTransferData(DataFlavor.stringFlavor);
        } 
        catch (Exception e) { return false; }
    	
        // Wherever there is a newline in the incoming data,
        // break it into a separate item in the list.
        String[] values = data.split("\n");
        
        // Perform the actual import.
        String newContent;
        for (int i = 0; i < values.length; i++) {
        	System.out.println("================" + values[i]);
        	newContent = values[i];
        	// ajout du complément que s'il n'y est pas déjà
        	if (!(newContent.endsWith("(player)") || newContent.endsWith("(system)"))){
        		String pnName = (String)laalys.combo_fullPnSelection.getSelectedItem();
        		IFeatures features = laalys.pnName2labelingAlgo.get(pnName).getFeatures();
        		String origin = ActionType.SYSTEM;
        		if (laalys.opt_player.isSelected()){
					origin = ActionType.PLAYER;
	        		// surcharger l'origine s'il s'agit d'une action système du Rdp (Note : une action non identifiée
					// comme "system" dans le Rdp peut être définie comme "system" dans la trace (cas d'une action de
					// jeu du joueur qui est automatiquement déclenchée par la simulation) en revanche une action
					// identifiée comme "system" dans le Rdp ne peut être définie comme "player" dans la trace (le
					// joueur ne peut explicitement déclencher une action qui est réservée à la simulation)). Si tel est
					// la cas on la force comme "system".  
					ArrayList<String> proprietes = features.getSystemTransitions(); // ensemble des transitions système
					for (int m = 0; m < proprietes.size(); m++) {
						if (proprietes.get(m).indexOf(newContent) != -1) {
							// l'action est une action système
							origin = ActionType.SYSTEM;
							JOptionPane.showMessageDialog(laalys, "Warning, This action is defined as a \"system\" action, you can add it as a \"player\" action.\n\nThis action is overrided as \"system\".");
							break;
						}
					}
        		}
        		newContent += " ("+pnName+ ") ("+origin+")";
        	}
            if (insert) {
                listModel.add(index++, newContent);
            } else {
                // If the items go beyond the end of the current
                // list, add them in.
                if (index < listModel.getSize()) {
                    listModel.set(index++, newContent);
                } else {
                    listModel.add(index++, newContent);
                }
            }
        }
        return true;
    }

    /**
     * Remove the items moved from the list.
     */
    protected void exportDone(JComponent c, Transferable data, int action) {
        JList source = (JList)c;
        DefaultListModel listModel  = (DefaultListModel)source.getModel();

        if (action == TransferHandler.MOVE) {
        	int nbElem = indices.length;
            for (int i = indices.length - 1; i >= 0; i--) {
                //listModel.remove(indices[i]);
            	if (indices[i] < dropIndice)
            		listModel.remove(indices[i]);
            	else
            		listModel.remove(indices[i]+nbElem);
            }
        }
        
        indices = null;
    }
}
