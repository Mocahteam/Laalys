import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jfree.data.general.DefaultPieDataset;
import org.w3c.dom.Document;

import fr.lip6.mocah.laalys.features.Features;
import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.labeling.*;
import fr.lip6.mocah.laalys.labeling.constants.Labels;
import fr.lip6.mocah.laalys.petrinet.AccessibleGraph;
import fr.lip6.mocah.laalys.petrinet.CoverabilityGraph;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.Traces;
import fr.lip6.mocah.laalys.traces.constants.ActionSource;
import fr.lip6.mocah.laalys.traces.constants.ActionType;

class InterfaceLaalys extends JFrame implements ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// répertoire de base pour le chargement des fichiers (réseau complet,
	// réseau filtré, transitions, traces)
	String adressereseau = "exemples";

	// les répertoires par défaut
	String adresseReseauComplet = adressereseau+File.separator+"completeNets";
	String adresseReseauCompletPourFiltrage = adressereseau+File.separator+"completeNets";
	String adresseReseauFiltre = adressereseau+File.separator+"filteredNets";
	String adresseSpec = adressereseau+File.separator+"features";
	String adresseTrace = adressereseau+File.separator+"trace";
	String adresseLabel = adressereseau+File.separator+"trace-labellisee";
	String adresseGraphml = adressereseau+File.separator+"trace-graphml";

    private javax.swing.JTabbedPane jTabbedPane;
    
	// UI for the first tab
    private javax.swing.JPanel tab_PnSelection;
    // Full Pn selection
    private javax.swing.JPanel pan_FullPnSelection;
    private javax.swing.JButton bt_FullPnSelection;
    private javax.swing.JLabel lab_FullPnSelection;
    // Filtered Pn selection
    private javax.swing.JPanel pan_FilteredPnSelection;
    private javax.swing.JPanel pan_GraphProperties;
    private javax.swing.ButtonGroup graphPropertiesGroup;
    private javax.swing.JRadioButton opt_Coverability;
    private javax.swing.JRadioButton opt_Accessibility;
    private javax.swing.JPanel pan_AnalysisStrategy;
	private javax.swing.ButtonGroup analysisStrategyGroup;
    private javax.swing.JRadioButton opt_First;
    private javax.swing.JRadioButton opt_All;
    private javax.swing.JButton bt_FilteredPnSelection;
    private javax.swing.JLabel lab_FilteredPnSelection;
    // Features selection
    private javax.swing.JPanel pan_SpecificationsSelection;
    private javax.swing.JButton bt_SpecificationSelection;
    private javax.swing.JLabel lab_SpecificationSelection;
    // loading button
    private javax.swing.JButton bt_LoadPnAndSpecif;
    
    // UI for the second tab
    private javax.swing.JPanel tab_TracesManagement;
    // Full Pn filter
    private javax.swing.JPanel pan_FullPnFilter;
    private javax.swing.JLabel lab_FullPnFilter;
    public javax.swing.JComboBox<String> combo_FullPnFilter;
    private javax.swing.JScrollPane scrollPan_FullPnActions;
    private DefaultListModel<Serializable> fullPnActions;
    // Drag&Drop options
    private javax.swing.JPanel pan_dragDropOptions;
    private javax.swing.JLabel lab_dragDropInfo;
    private javax.swing.ButtonGroup playerSystemGroup;
    public javax.swing.JRadioButton opt_Player;
    public javax.swing.JRadioButton opt_System;
    // Traces building
    private javax.swing.JPanel pan_tracesBuilding;
    private javax.swing.JButton bt_loadTracesFromFile;
    private javax.swing.JScrollPane scrollPan_tracesActions;
    private DefaultListModel<Serializable> tracesActions;
    private javax.swing.JButton bt_SaveTraces;
    
    // UI for the third tab
    private javax.swing.JPanel tab_Analysis;
    private javax.swing.JButton bt_LaunchAnalysis;
    private javax.swing.JPanel pan_analysisColumns;
    // First column
    private javax.swing.JPanel pan_actionsAnalysed;
    private javax.swing.JScrollPane scrollPan_AnalysedActions;
	private DefaultListModel<Serializable> analysedActions;
    private javax.swing.JButton bt_exportGraphml;
    // Second column
    private javax.swing.JPanel pan_LabelsComputed;
    private javax.swing.JScrollPane scrollPan_LabelsComputed;
    private javax.swing.JButton bt_exportLabels;
    private DefaultListModel<Serializable> labelsComputed;
    // Third column
    private javax.swing.JPanel pan_Synthesis;
    private javax.swing.JScrollPane scrollPan_Synthesis;
    private DefaultListModel<Serializable> synthesis;
    
    // UI for the fourth tab
    private javax.swing.JPanel tab_FilteredPnManagement;
    // Full Pn selection
    private javax.swing.JPanel pan_FullPnSelection2;
    private javax.swing.JButton bt_FullPnSelection2;
    private javax.swing.JLabel lab_FullPnSelection2;
    // Expert trace selection
    private javax.swing.JPanel pan_ExpertTraceSelection;
    private javax.swing.JButton bt_ExpertTraceSelection;
    private javax.swing.JLabel lab_ExpertTraceSelection;
    // Build filtered Petri net
    private javax.swing.JButton bt_BuildFilteredPn;
    
	public HashMap<String, ILabeling> pnName2labelingAlgo = new HashMap<>();
	private ArrayList<ITrace> listeTracePourAnalyse;
	
	
	//DefaultListModel<Serializable> listeActionContent, listeNomActionsPourAnalyse;
	String type = CoverabilityGraph.TYPE, strategie = CoverabilityGraph.STRATEGY_OR; // par défaut
	
	PieChart cv;
	DefaultPieDataset dataset;

	boolean loadingTraces = false;
	//////////////////////////////////////////////////////////////////

	public InterfaceLaalys() {
		super("Laalys");

		// Vérifier les chemins
		if (!new File(adresseReseauComplet).exists())
			adresseReseauComplet = ".";
		if (!new File(adresseReseauCompletPourFiltrage).exists())
			adresseReseauCompletPourFiltrage = ".";
		if (!new File(adresseReseauFiltre).exists())
			adresseReseauFiltre = ".";
		if (!new File(adresseSpec).exists())
			adresseSpec = ".";
		if (!new File(adresseTrace).exists())
			adresseTrace = ".";
		if (!new File(adresseLabel).exists())
			adresseLabel = ".";
		if (!new File(adresseGraphml).exists())
			adresseGraphml = ".";

		graphPropertiesGroup = new javax.swing.ButtonGroup();
        analysisStrategyGroup = new javax.swing.ButtonGroup();
        playerSystemGroup = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        tab_PnSelection = new javax.swing.JPanel();
        pan_FullPnSelection = new javax.swing.JPanel();
        bt_FullPnSelection = new javax.swing.JButton();
        lab_FullPnSelection = new javax.swing.JLabel();
        pan_FilteredPnSelection = new javax.swing.JPanel();
        pan_GraphProperties = new javax.swing.JPanel();
        opt_Coverability = new javax.swing.JRadioButton();
        opt_Accessibility = new javax.swing.JRadioButton();
        pan_AnalysisStrategy = new javax.swing.JPanel();
        opt_First = new javax.swing.JRadioButton();
        opt_All = new javax.swing.JRadioButton();
        bt_FilteredPnSelection = new javax.swing.JButton();
        lab_FilteredPnSelection = new javax.swing.JLabel();
        pan_SpecificationsSelection = new javax.swing.JPanel();
        bt_SpecificationSelection = new javax.swing.JButton();
        lab_SpecificationSelection = new javax.swing.JLabel();
        bt_LoadPnAndSpecif = new javax.swing.JButton();
        tab_TracesManagement = new javax.swing.JPanel();
        pan_FullPnFilter = new javax.swing.JPanel();
        lab_FullPnFilter = new javax.swing.JLabel();
        combo_FullPnFilter = new javax.swing.JComboBox<>();
        scrollPan_FullPnActions = new javax.swing.JScrollPane();
        fullPnActions = new DefaultListModel<Serializable>();
        pan_dragDropOptions = new javax.swing.JPanel();
        lab_dragDropInfo = new javax.swing.JLabel();
        opt_Player = new javax.swing.JRadioButton();
        opt_System = new javax.swing.JRadioButton();
        pan_tracesBuilding = new javax.swing.JPanel();
        bt_loadTracesFromFile = new javax.swing.JButton();
        scrollPan_tracesActions = new javax.swing.JScrollPane();
        tracesActions = new DefaultListModel<Serializable>();
        bt_SaveTraces = new javax.swing.JButton();
        tab_Analysis = new javax.swing.JPanel();
        tab_FilteredPnManagement = new javax.swing.JPanel();
        listeTracePourAnalyse = new ArrayList<ITrace>();
        bt_LaunchAnalysis = new javax.swing.JButton();
        pan_analysisColumns = new javax.swing.JPanel();
        pan_actionsAnalysed = new javax.swing.JPanel();
        scrollPan_AnalysedActions = new javax.swing.JScrollPane();
    	analysedActions = new DefaultListModel<Serializable>();
        bt_exportGraphml = new javax.swing.JButton();
        pan_LabelsComputed = new javax.swing.JPanel();
        scrollPan_LabelsComputed = new javax.swing.JScrollPane();
        bt_exportLabels = new javax.swing.JButton();
        labelsComputed = new DefaultListModel<Serializable>();
        pan_Synthesis = new javax.swing.JPanel();
        scrollPan_Synthesis = new javax.swing.JScrollPane();
        synthesis = new DefaultListModel<Serializable>();
        pan_FullPnSelection2 = new javax.swing.JPanel();
        bt_FullPnSelection2 = new javax.swing.JButton();
        lab_FullPnSelection2 = new javax.swing.JLabel();
        pan_ExpertTraceSelection = new javax.swing.JPanel();
        bt_ExpertTraceSelection = new javax.swing.JButton();
        lab_ExpertTraceSelection = new javax.swing.JLabel();
        bt_BuildFilteredPn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        this.setMinimumSize(new java.awt.Dimension(550, 410));
        
        //////////////////////////////////////
        // Tab 1: Petri nets and features selection
        jTabbedPane.addTab("Petri nets selection", tab_PnSelection);
        
        //---------- First Bloc: Select Full Pn folder ----------
        pan_FullPnSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Full Petri nets selection"));
        pan_FullPnSelection.setMinimumSize(new java.awt.Dimension(400, 100));
        bt_FullPnSelection.setText("Select folder");
        bt_FullPnSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FullPnSelectionActionPerformed(evt);
            }
        });
        lab_FullPnSelection.setText("No folder selected");
        lab_FullPnSelection.setForeground(Color.RED);
        lab_FullPnSelection.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FullPnSelectionLayout = new javax.swing.GroupLayout(pan_FullPnSelection);
        pan_FullPnSelection.setLayout(pan_FullPnSelectionLayout);
        pan_FullPnSelectionLayout.setHorizontalGroup(
            pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelectionLayout.createSequentialGroup()
                .addComponent(bt_FullPnSelection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_FullPnSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_FullPnSelectionLayout.setVerticalGroup(
            pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelectionLayout.createSequentialGroup()
                .addGroup(pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_FullPnSelection)
                    .addComponent(lab_FullPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        // ---------- Second Bloc: Select Filtered Pn folder ----------
        pan_FilteredPnSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtered Petri nets selection"));
        pan_FilteredPnSelection.setMinimumSize(new java.awt.Dimension(400, 100));
        pan_GraphProperties.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph properties"));
        graphPropertiesGroup.add(opt_Coverability);
        opt_Coverability.setSelected(true);
        opt_Coverability.setText("Coverability");
        graphPropertiesGroup.add(opt_Accessibility);
        opt_Accessibility.setText("Accessibility");
        javax.swing.GroupLayout pan_GraphPropertiesLayout = new javax.swing.GroupLayout(pan_GraphProperties);
        pan_GraphProperties.setLayout(pan_GraphPropertiesLayout);
        pan_GraphPropertiesLayout.setHorizontalGroup(
            pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_GraphPropertiesLayout.createSequentialGroup()
                .addGroup(pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opt_Coverability)
                    .addComponent(opt_Accessibility))
                .addGap(0, 32, Short.MAX_VALUE))
        );
        pan_GraphPropertiesLayout.setVerticalGroup(
            pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_GraphPropertiesLayout.createSequentialGroup()
                .addComponent(opt_Coverability)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_Accessibility))
        );
        opt_Coverability.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        opt_Accessibility.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        pan_AnalysisStrategy.setBorder(javax.swing.BorderFactory.createTitledBorder("Analysis strategy"));
        analysisStrategyGroup.add(opt_First);
        opt_First.setSelected(true);
        opt_First.setText("FIRST");
        analysisStrategyGroup.add(opt_All);
        opt_All.setText("ALL");
        javax.swing.GroupLayout pan_AnalysisStrategyLayout = new javax.swing.GroupLayout(pan_AnalysisStrategy);
        pan_AnalysisStrategy.setLayout(pan_AnalysisStrategyLayout);
        pan_AnalysisStrategyLayout.setHorizontalGroup(
            pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_AnalysisStrategyLayout.createSequentialGroup()
                .addGroup(pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opt_First)
                    .addComponent(opt_All))
                .addGap(0, 63, Short.MAX_VALUE))
        );
        pan_AnalysisStrategyLayout.setVerticalGroup(
            pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_AnalysisStrategyLayout.createSequentialGroup()
                .addComponent(opt_First)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_All))
        );
        opt_First.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        opt_All.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        bt_FilteredPnSelection.setText("Select folder");
        bt_FilteredPnSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_FilteredPnSelectionActionPerformed(evt);
            }
        });
        lab_FilteredPnSelection.setText("No folder selected");
        lab_FilteredPnSelection.setForeground(Color.RED);
        lab_FilteredPnSelection.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FilteredPnSelectionLayout = new javax.swing.GroupLayout(pan_FilteredPnSelection);
        pan_FilteredPnSelection.setLayout(pan_FilteredPnSelectionLayout);
        pan_FilteredPnSelectionLayout.setHorizontalGroup(
            pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addComponent(pan_GraphProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pan_AnalysisStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addComponent(bt_FilteredPnSelection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_FilteredPnSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
        );
        pan_FilteredPnSelectionLayout.setVerticalGroup(
            pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addGroup(pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pan_GraphProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pan_AnalysisStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_FilteredPnSelection)
                    .addComponent(lab_FilteredPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

		// ---------- Third Bloc: Select Specifications folder ----------
        pan_SpecificationsSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Petri nets specifications selection"));
        pan_SpecificationsSelection.setMinimumSize(new java.awt.Dimension(400, 100));
        bt_SpecificationSelection.setText("Select folder");
        bt_SpecificationSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_SpecificationSelectionActionPerformed(evt);
            }
        });
        lab_SpecificationSelection.setText("No folder selected");
        lab_SpecificationSelection.setForeground(Color.RED);
        lab_SpecificationSelection.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_SpecificationsSelectionLayout = new javax.swing.GroupLayout(pan_SpecificationsSelection);
        pan_SpecificationsSelection.setLayout(pan_SpecificationsSelectionLayout);
        pan_SpecificationsSelectionLayout.setHorizontalGroup(
            pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SpecificationsSelectionLayout.createSequentialGroup()
                .addComponent(bt_SpecificationSelection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_SpecificationSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_SpecificationsSelectionLayout.setVerticalGroup(
            pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SpecificationsSelectionLayout.createSequentialGroup()
                .addGroup(pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_SpecificationSelection)
                    .addComponent(lab_SpecificationSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        
		// ---------- Fourth Bloc: loading button ----------
        bt_LoadPnAndSpecif.setText("Load Petri nets and specifications");
        bt_LoadPnAndSpecif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_LoadPnAndSpecifActionPerformed(evt);
            }
        });
        
        // ---------- Add blocs to the first tab ----------
        javax.swing.GroupLayout tab_PnSelectionLayout = new javax.swing.GroupLayout(tab_PnSelection);
        tab_PnSelection.setLayout(tab_PnSelectionLayout);
        tab_PnSelectionLayout.setHorizontalGroup(
            tab_PnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pan_FullPnSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pan_FilteredPnSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pan_SpecificationsSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(tab_PnSelectionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bt_LoadPnAndSpecif)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tab_PnSelectionLayout.setVerticalGroup(
            tab_PnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_PnSelectionLayout.createSequentialGroup()
                .addComponent(pan_FullPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_FilteredPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_SpecificationsSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(bt_LoadPnAndSpecif))
        );
        
		//////////////////////////////////////
		// Tab 2: Traces management
        jTabbedPane.addTab("Traces management", tab_TracesManagement);
        tab_TracesManagement.setLayout(new java.awt.GridLayout(1, 3));
        
        // ---------- first column ----------
        lab_FullPnFilter.setText("Full Petri net filter");
        lab_FullPnFilter.setMinimumSize(new java.awt.Dimension(50, 14));
        combo_FullPnFilter.setMinimumSize(new java.awt.Dimension(50, 20));
        combo_FullPnFilter.addItemListener(this);
        JList<Serializable> listeActionsConteneur = new JList<Serializable>(fullPnActions);
        listeActionsConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Available game actions"));
		listeActionsConteneur.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listeActionsConteneur.setDragEnabled(true);
		listeActionsConteneur.setMinimumSize(new java.awt.Dimension(50, 103));
		scrollPan_FullPnActions.setViewportView(listeActionsConteneur);

        javax.swing.GroupLayout pan_FullPnFilterLayout = new javax.swing.GroupLayout(pan_FullPnFilter);
        pan_FullPnFilter.setLayout(pan_FullPnFilterLayout);
        pan_FullPnFilterLayout.setHorizontalGroup(
            pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combo_FullPnFilter, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lab_FullPnFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                    .addComponent(scrollPan_FullPnActions, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pan_FullPnFilterLayout.setVerticalGroup(
            pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lab_FullPnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(combo_FullPnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPan_FullPnActions, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addContainerGap())
        );

        tab_TracesManagement.add(pan_FullPnFilter);

        // ---------- second column ----------
        lab_dragDropInfo.setText("<html><center>Drag and drop game actions from the left panel to the right one to complete manually traces.<br/>Select below the simulated game action source (player or system)</center></html>");
        lab_dragDropInfo.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        playerSystemGroup.add(opt_Player);
        opt_Player.setText(ActionType.PLAYER);
        playerSystemGroup.add(opt_System);
        opt_System.setText(ActionType.SYSTEM);
        opt_Player.setSelected(true);

        javax.swing.GroupLayout pan_dragDropOptionsLayout = new javax.swing.GroupLayout(pan_dragDropOptions);
        pan_dragDropOptions.setLayout(pan_dragDropOptionsLayout);
        pan_dragDropOptionsLayout.setHorizontalGroup(
            pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_dragDropOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(opt_Player, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(opt_System, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lab_dragDropInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        pan_dragDropOptionsLayout.setVerticalGroup(
            pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_dragDropOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lab_dragDropInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(opt_Player)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_System)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        tab_TracesManagement.add(pan_dragDropOptions);

        // ---------- third column ----------
        bt_loadTracesFromFile.setText("Load traces from files");
        bt_loadTracesFromFile.setMinimumSize(new java.awt.Dimension(50, 23));
        bt_loadTracesFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_loadTracesFromFileActionPerformed(evt);
            }
        });
		
		tracesActions.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				System.out.println("Remove : "+e.getIndex0()+" "+e.getIndex1());
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex1() ; i >= e.getIndex0() ; i--)
						listeTracePourAnalyse.remove(i);
				}
				if (tracesActions.isEmpty())
					enableOngletAnalyse(false);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				System.out.println("Add : "+e.getIndex0()+" "+e.getIndex1());
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex0() ; i <= e.getIndex1() ; i++){
						String action = tracesActions.getElementAt(i).toString();
						// Extraction du nom du Rdp inclus de l'action. Format : nomAction (nomRdp) (origin)
						String [] tokens = action.split(" \\(");
						// extraction du nom de l'action
						String actionName = "";
						for (int j = 0 ; j < tokens.length-2 ; j++){
							if (j>0) actionName += " (";
							actionName += tokens[j];
						}
						// extraction du nom du Rdp (avant dernier token)
						String pnName = tokens[tokens.length-2];
						// suppression de la dernière ) et de tout ce qui suit
						pnName = pnName.substring(0, pnName.indexOf(')'));
						// extraction de l'origine de l'action(dernier token)
						String origin = tokens[tokens.length-1];
						// suppression de la dernière ) et de tout ce qui suit
						origin = origin.substring(0, origin.indexOf(')'));
						// Création d'un objet trace pour stocker les données
						ITrace nouvelletrace = new Trace(pnName, actionName, ActionSource.MANUAL, origin, false);
						listeTracePourAnalyse.add(i, nouvelletrace);
					}
				}
				enableOngletAnalyse(true);
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				System.out.println("Change : "+e.getIndex0()+" "+e.getIndex1());
			}
		});

		JList<Serializable> listeNomActionsPourAnalyseConteneur = new JList<Serializable>(tracesActions);
		listeNomActionsPourAnalyseConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Game actions for analysis"));
		listeNomActionsPourAnalyseConteneur.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listeNomActionsPourAnalyseConteneur.setDragEnabled(true);
		listeNomActionsPourAnalyseConteneur.setDropMode(DropMode.INSERT);
		listeNomActionsPourAnalyseConteneur.setTransferHandler(new ListTransferHandler(this));
		listeNomActionsPourAnalyseConteneur.setMinimumSize(new java.awt.Dimension(50, 103));
		listeNomActionsPourAnalyseConteneur.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				// System.out.println("Touche pressée : " + e.getKeyCode());
				if (e.getKeyCode()== KeyEvent.VK_DELETE){
					for (int i = listeNomActionsPourAnalyseConteneur.getSelectedIndices().length-1 ; i >= 0 ; i--){
						tracesActions.remove(listeNomActionsPourAnalyseConteneur.getSelectedIndices()[i]);
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		scrollPan_tracesActions.setViewportView(listeNomActionsPourAnalyseConteneur);

        bt_SaveTraces.setText("Save traces");
        bt_SaveTraces.setMinimumSize(new java.awt.Dimension(50, 23));
        bt_SaveTraces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_SaveTracesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pan_tracesBuildingLayout = new javax.swing.GroupLayout(pan_tracesBuilding);
        pan_tracesBuilding.setLayout(pan_tracesBuildingLayout);
        pan_tracesBuildingLayout.setHorizontalGroup(
            pan_tracesBuildingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_tracesBuildingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pan_tracesBuildingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bt_loadTracesFromFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollPan_tracesActions)
                    .addComponent(bt_SaveTraces, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pan_tracesBuildingLayout.setVerticalGroup(
            pan_tracesBuildingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_tracesBuildingLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(bt_loadTracesFromFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPan_tracesActions, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_SaveTraces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        tab_TracesManagement.add(pan_tracesBuilding);

		//////////////////////////////////////
		// Tab 3: Analysis
        jTabbedPane.addTab("Analysis", tab_Analysis);

        bt_LaunchAnalysis.setText("Launch analysis");
        bt_LaunchAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_LaunchAnalysisActionPerformed(evt);
            }
        });

        pan_analysisColumns.setLayout(new java.awt.GridLayout(1, 3, 10, 0));

        // ---------- first column ----------
        JList<Serializable> listeActionsAnalyseesConteneur = new JList<Serializable>(analysedActions);
        listeActionsAnalyseesConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Actions analysed"));
        listeActionsAnalyseesConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_AnalysedActions.setViewportView(listeActionsAnalyseesConteneur);

        bt_exportGraphml.setText("Export to Graphml");
        bt_exportGraphml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_exportGraphmlActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pan_actionsAnalysedLayout = new javax.swing.GroupLayout(pan_actionsAnalysed);
        pan_actionsAnalysed.setLayout(pan_actionsAnalysedLayout);
        pan_actionsAnalysedLayout.setHorizontalGroup(
            pan_actionsAnalysedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_actionsAnalysedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(bt_exportGraphml, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(scrollPan_AnalysedActions))
        );
        pan_actionsAnalysedLayout.setVerticalGroup(
            pan_actionsAnalysedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_actionsAnalysedLayout.createSequentialGroup()
                .addComponent(scrollPan_AnalysedActions, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_exportGraphml))
        );

        pan_analysisColumns.add(pan_actionsAnalysed);

        // ---------- second column ----------
        JList<Serializable> listeLabelsConteneur = new JList<Serializable>(labelsComputed);
        listeLabelsConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Labels computed"));
        listeLabelsConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_LabelsComputed.setViewportView(listeLabelsConteneur);

        bt_exportLabels.setText("Export labels");
        bt_exportLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_exportLabelsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pan_LabelsComputedLayout = new javax.swing.GroupLayout(pan_LabelsComputed);
        pan_LabelsComputed.setLayout(pan_LabelsComputedLayout);
        pan_LabelsComputedLayout.setHorizontalGroup(
            pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(bt_exportLabels)
                .addComponent(scrollPan_LabelsComputed))
        );
        pan_LabelsComputedLayout.setVerticalGroup(
            pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_LabelsComputedLayout.createSequentialGroup()
                .addComponent(scrollPan_LabelsComputed, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_exportLabels))
        );

        pan_analysisColumns.add(pan_LabelsComputed);

        // ---------- third column ----------
        JList<Serializable> listeSyntheseConteneur = new JList<Serializable>(synthesis);
        listeSyntheseConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Synthesis"));
        listeSyntheseConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_Synthesis.setViewportView(listeSyntheseConteneur);

        javax.swing.GroupLayout pan_SynthesisLayout = new javax.swing.GroupLayout(pan_Synthesis);
        pan_Synthesis.setLayout(pan_SynthesisLayout);
        pan_SynthesisLayout.setHorizontalGroup(
            pan_SynthesisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPan_Synthesis, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
        );
        pan_SynthesisLayout.setVerticalGroup(
            pan_SynthesisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SynthesisLayout.createSequentialGroup()
                .addComponent(scrollPan_Synthesis, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

        pan_analysisColumns.add(pan_Synthesis);

        javax.swing.GroupLayout tab_AnalysisLayout = new javax.swing.GroupLayout(tab_Analysis);
        tab_Analysis.setLayout(tab_AnalysisLayout);
        tab_AnalysisLayout.setHorizontalGroup(
            tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(bt_LaunchAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(pan_analysisColumns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tab_AnalysisLayout.setVerticalGroup(
            tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_AnalysisLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bt_LaunchAnalysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pan_analysisColumns, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
        );

		//////////////////////////////////////
		// Tab 4: Filtered nets management
        jTabbedPane.addTab("Filtered nets management", tab_FilteredPnManagement);
        // ---------- first bloc ----------
        pan_FullPnSelection2.setBorder(javax.swing.BorderFactory.createTitledBorder("Full Petri net selection"));
        pan_FullPnSelection2.setMinimumSize(new java.awt.Dimension(50, 100));
        bt_FullPnSelection2.setText("Select file");
        bt_FullPnSelection2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FullPnSelection2ActionPerformed(evt);
            }
        });
        lab_FullPnSelection2.setText("No file selected");
        lab_FullPnSelection2.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FullPnSelection2Layout = new javax.swing.GroupLayout(pan_FullPnSelection2);
        pan_FullPnSelection2.setLayout(pan_FullPnSelection2Layout);
        pan_FullPnSelection2Layout.setHorizontalGroup(
            pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelection2Layout.createSequentialGroup()
                .addComponent(bt_FullPnSelection2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_FullPnSelection2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_FullPnSelection2Layout.setVerticalGroup(
            pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelection2Layout.createSequentialGroup()
                .addGroup(pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_FullPnSelection2)
                    .addComponent(lab_FullPnSelection2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        // ---------- second bloc ----------
        pan_ExpertTraceSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Expert trace selection"));
        pan_ExpertTraceSelection.setMinimumSize(new java.awt.Dimension(50, 100));
        bt_ExpertTraceSelection.setText("Select file");
        bt_ExpertTraceSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_ExpertTraceSelectionActionPerformed(evt);
            }
        });
        lab_ExpertTraceSelection.setText("No file selected");
        lab_ExpertTraceSelection.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_ExpertTraceSelectionLayout = new javax.swing.GroupLayout(pan_ExpertTraceSelection);
        pan_ExpertTraceSelection.setLayout(pan_ExpertTraceSelectionLayout);
        pan_ExpertTraceSelectionLayout.setHorizontalGroup(
            pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_ExpertTraceSelectionLayout.createSequentialGroup()
                .addComponent(bt_ExpertTraceSelection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_ExpertTraceSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
        );
        pan_ExpertTraceSelectionLayout.setVerticalGroup(
            pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_ExpertTraceSelectionLayout.createSequentialGroup()
                .addGroup(pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_ExpertTraceSelection)
                    .addComponent(lab_ExpertTraceSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        // ---------- buil button ----------
        bt_BuildFilteredPn.setText("Build filtered Petri net");
        bt_BuildFilteredPn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_BuildFilteredPnActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout tab_FilteredPnManagementLayout = new javax.swing.GroupLayout(tab_FilteredPnManagement);
        tab_FilteredPnManagement.setLayout(tab_FilteredPnManagementLayout);
        tab_FilteredPnManagementLayout.setHorizontalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(pan_ExpertTraceSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bt_BuildFilteredPn))
            .addComponent(pan_FullPnSelection2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        tab_FilteredPnManagementLayout.setVerticalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_FilteredPnManagementLayout.createSequentialGroup()
                .addComponent(pan_FullPnSelection2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_ExpertTraceSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(bt_BuildFilteredPn)
                .addContainerGap(162, Short.MAX_VALUE))
        );

		//////////////////////////////////////
		// Add tabs to this Frame
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane.getAccessibleContext().setAccessibleDescription("");
        
		enableOngletAnalyse(false);
		enableOngletTraces(false);

        pack();
		
		setVisible(true);
	}
	

    private void bt_FullPnSelectionActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	System.out.println("Choisir un dossier de réseau complet sans graphe.");
		String folderName = new SelectionDossier().getNomDossier(adresseReseauComplet, this);
		if (!folderName.isEmpty() && !folderName.equals(adresseReseauComplet)){
			adresseReseauComplet = folderName;
			System.out.println("dossier choisi : " + adresseReseauComplet);
			lab_FullPnSelection.setText("Folder selected: "+adresseReseauComplet);
			lab_FullPnSelection.setForeground(Color.BLACK);
			lab_FullPnSelection.setToolTipText(adresseReseauComplet);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    } 
	
    private void bt_FilteredPnSelectionActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	System.out.println("Choisir un dossier de réseaux filtrés");
		String folderName = new SelectionDossier().getNomDossier(adresseReseauFiltre, this);
		if (!folderName.isEmpty() && !folderName.equals(adresseReseauFiltre)){
			adresseReseauFiltre = folderName;
			System.out.println("dossier choisi : " + adresseReseauFiltre);
			lab_FilteredPnSelection.setText("Folder selected: "+adresseReseauFiltre);
			lab_FilteredPnSelection.setForeground(Color.BLACK);
			lab_FilteredPnSelection.setToolTipText(adresseReseauFiltre);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    }   
	
    private void optActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	Object source = evt.getSource();

		// les 4 boutons de configuration
		if (source == opt_Coverability) {
			System.out.println("couverture");
			type = CoverabilityGraph.TYPE;
			opt_First.setEnabled(true);
			opt_All.setEnabled(true);
		} else if (source == opt_Accessibility) {
			System.out.println("accessibilité");
			type = AccessibleGraph.TYPE;
			// interdire le bouton1b
			opt_All.setEnabled(false);
			// activer le bouton1a automatiquement
			opt_First.setSelected(true);
			strategie = CoverabilityGraph.STRATEGY_OR;
		} else if (source == opt_First) {
			System.out.println("stratégie FIRST, en fait, OU");
			strategie = CoverabilityGraph.STRATEGY_OR;
		} else if (source == opt_All) {
			System.out.println("stratégie ALL, en fait, ET");
			strategie = CoverabilityGraph.STRATEGY_AND;
		}
    }                                           

    private void bt_SpecificationSelectionActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Choisir un dossier de caractéristiques");
		String folderName = new SelectionDossier().getNomDossier(adresseSpec, this);
		if (!folderName.isEmpty() && !folderName.equals(adresseSpec)){
			adresseSpec = folderName;
			System.out.println("dossier choisi : " + adresseSpec);
			lab_SpecificationSelection.setText("Folder selected: "+adresseSpec);
			lab_SpecificationSelection.setForeground(Color.BLACK);
			lab_SpecificationSelection.setToolTipText(adresseSpec);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    }                                           

    private void bt_loadTracesFromFileActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Loading traces file.");
		loadingTraces = true;
		try {
			String fileName = new SelectionFichier().getNomFichier(adresseTrace, this);
			if (!fileName.isEmpty()){
				adresseTrace = fileName.substring(0, fileName.lastIndexOf(File.separator));
				tracesActions.removeAllElements();
				listeTracePourAnalyse = new ArrayList<ITrace>();
				System.out.println("traces selected: " + fileName);
				ITraces tracesToLoad = new Traces();
				tracesToLoad.loadFile(fileName);
				boolean consistant = true ;
				// ICI copie_traces = new Traces();
				// on parcourt les traces chargées
				for (ITrace tr : tracesToLoad.getTraces()) { 
					// pour chaque trace
					System.out.println(tr.getAction()+" "+tr.getPnName());
					ILabeling algo = pnName2labelingAlgo.get(tr.getPnName());
					if (algo == null) {						
						JOptionPane.showMessageDialog(this, "Error, trace \""+tr.getAction()+"\" refers unknown Petri net: \""+tr.getPnName()+"\".\n\nLoading aborted");
						consistant = false ;
						break;
					} else {
						ITransition trans = algo.getCompletePN().getTransitionById(tr.getAction());
						if (trans == null){
							JOptionPane.showMessageDialog(this, "Error, Petri net \""+tr.getPnName()+"\" doesn't include \""+tr.getAction()+"\" action.\n\nLoading aborted");
							consistant = false;
							break;
						}
					}
					
					// Check if trace origin is consistent with systemTransitions
					if (tr.getOrigin().equals(ActionType.PLAYER)){
						ArrayList<String> properties = algo.getFeatures().getSystemTransitions();
						for (int m = 0; m < properties.size(); m++) {
							if (properties.contains(tr.getAction())) {
								// reset action type to SYSTEM
								tr.setOrigin(ActionType.SYSTEM);
								JOptionPane.showMessageDialog(this, "Warning, The action \""+tr.getAction()+"\" of the Petri net \""+tr.getPnName()+"\" is known as a \"system\" action\nbut is defined inside trace as a \"player\" action. This is not allowed.\n\nThis action is overrided as \"system\".");
								break;
							}
						}
					}

					// mémorisation des traces avec tous les attributs :
					listeTracePourAnalyse.add(tr);
					// pour affichage, on ajoute le nom du fullPn et l'origine (player ou system)
					tracesActions.addElement(tr.getAction() + " ("+ tr.getPnName() + ") ("+ tr.getOrigin()+ ")");
				}
				if (!consistant) {
					tracesActions.removeAllElements();
					listeTracePourAnalyse = new ArrayList<ITrace>();
				}
				enableOngletAnalyse(true);
			}
		} catch (Exception e6) {
			JOptionPane.showMessageDialog(this, "Error on loading traces file\n\n"+e6.getMessage());
			tracesActions.removeAllElements();
			listeTracePourAnalyse = new ArrayList<ITrace>();
		}
		loadingTraces = false;
    }                                         

    private void bt_LoadPnAndSpecifActionPerformed(java.awt.event.ActionEvent evt) {
    	// onglet1 : charge tous les fichiers des trois dossiers
		System.out.println(adresseReseauComplet);
		System.out.println(adresseReseauFiltre);
		System.out.println(adresseSpec);

		Logger monLog = Logger.getLogger(Main.class.getName());
		monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
		monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau INFO
		monLog.addHandler(ch);
		File fullDir = new File(adresseReseauComplet);
		pnName2labelingAlgo.clear();
		for (File fullChild : fullDir.listFiles()){
			// get equivalent file in filtered folder
			File filteredDir = new File(adresseReseauFiltre);
			File filteredChild = new File(filteredDir, fullChild.getName());
			// get equivalent file in features folder
			File featuresDir = new File(adresseSpec);
			File featuresChild = new File(featuresDir, fullChild.getName().substring(0, fullChild.getName().length()-4)+"xml");
			if (!filteredChild.exists() || !featuresChild.exists()){
				JOptionPane.showMessageDialog(this, "Error, equivalent file of full Petri net named \""+fullChild.getName()+"\" doesn't exist in selected folders\n\nLoading aborted");
				pnName2labelingAlgo.clear();
				return ;
			}
			// Instantiate full Petri net
			IPetriNet fullPn = new PetriNet(false, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			try {
				fullPn.loadPetriNet(fullChild.getAbsolutePath());
			} catch (Exception e0) {
				JOptionPane.showMessageDialog(this, "Error, unable to load \""+fullChild.getAbsolutePath()+"\" file\n\nLoading aborted");
				pnName2labelingAlgo.clear();
				return ;
			}
			// Instantiate filtered Petri net
			IPetriNet filteredPn = new PetriNet(true, type, strategie);
			try {
				filteredPn.loadPetriNet(filteredChild.getAbsolutePath());
			} catch (Exception e2) {
				JOptionPane.showMessageDialog(this, "Error, unable to load \""+filteredChild.getAbsolutePath()+"\" file\n\nLoading aborted");
				pnName2labelingAlgo.clear();
				return;
			}
			// Instantiate features
			IFeatures features = new Features();
			try {
				features.loadFile(featuresChild.getAbsolutePath());
				// Vérifier s'il y a au moins une action de fin
				if (features.getEndLevelTransitions().isEmpty()){
					JOptionPane.showMessageDialog(this, "Incomplet specifications.\n\n"
							+ "No end transition defined for \""+fullChild.getName()+"\" Petri net.");
					pnName2labelingAlgo.clear();
					return;
				}
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Error, unable to load "+featuresChild.getAbsolutePath()+" file\n\nLoading aborted");
				pnName2labelingAlgo.clear();
				return ;
			}

			// Init labeling algorithm
			ILabeling algo = new Labeling_V10(monLog, false);
			algo.setCompletePN(fullPn);
			algo.setFilteredPN(filteredPn);
			algo.setFeatures(features);
			try {
				algo.reset();
			} catch (Exception e2) {
				JOptionPane.showMessageDialog(this, "Error, Labeling algorithm initialisation fail.\n\nInitialization aborted");
				pnName2labelingAlgo.clear();
				return ;
			}
			String pnName = fullChild.getName();
			if (pnName.endsWith(".pnml"))
				pnName = pnName.substring(0, pnName.length()-5);
			pnName2labelingAlgo.put(pnName, algo);
		}
		
		if (pnName2labelingAlgo.isEmpty()){
			JOptionPane.showMessageDialog(this, "Error, No Petri nets found.\n\nInitialization aborted");
			return ;
		}
		// System.out.println("Nb Full found: "+pnName2labelingAlgo.size());

		enableOngletTraces(true);
		jTabbedPane.setSelectedIndex(1); // switch to trace management
    }   
    
    private void bt_SaveTracesActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Save traces.");
		if (listeTracePourAnalyse.size() > 0) {
			ITraces itraces = new Traces();
			itraces.setTraces(listeTracePourAnalyse);
			Document doc = itraces.toXML();
			// enregistrement du nouveau fichier de traces
			// choix du fichier
			String filename = "";
			try {
				JFileChooser chooser = new JFileChooser();
				// Dossier Courant
				chooser.setCurrentDirectory(new File(adresseTrace + File.separator));
				// Affichage et récupération de la réponse de l'utilisateur
				int reponse = chooser.showDialog(chooser, "Save (.xml extension)");
				// Si l'utilisateur clique sur OK
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					filename = chooser.getSelectedFile().toString();
					if (filename.toLowerCase().endsWith(".xml"))
						filename = filename.substring(0, filename.length() - 4); // remove user extension
					adresseTrace = filename.substring(0, filename.lastIndexOf(File.separator));
					// System.out.println("fichier : " + filename+".xml");
					// enregistrement proprement dit
					Transformer transformer;
					Result output;
					try {
						transformer = TransformerFactory.newInstance().newTransformer();
						output = new StreamResult(new File(filename + ".xml"));
						Source input = new DOMSource(doc);
						transformer.transform(input, output);
						JOptionPane.showMessageDialog(this, "Save OK");
					} catch (Exception e5) {
						JOptionPane.showMessageDialog(this, "Error on saving traces\n\n"+e5.getMessage());
					}
				}
			} catch (HeadlessException he) {
				he.printStackTrace();
			}
		} else
			JOptionPane.showMessageDialog(this, "Traces are empty, build a trace by drag and drop or load an existing trace first");
    }     
    
    private void bt_LaunchAnalysisActionPerformed(java.awt.event.ActionEvent evt) {
    	// onglet3 : lancement de l'analyse
		// vérifier que l'on a tout : s'il manque quelque chose, le dire
		if (pnName2labelingAlgo.size() <= 0 || listeTracePourAnalyse.size() == 0) {
			JOptionPane.showMessageDialog(this,
					"Full Petri nets or filtered Petri nets or specifications or traces not defined");
		} else {
			// Réinitialisation des algos
			for (ILabeling algo : pnName2labelingAlgo.values()){
				try {
					algo.reset();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			
			ITraces tracesPourAnalyse = new Traces();
			tracesPourAnalyse.setTraces(listeTracePourAnalyse);				
			for (ITrace trace : tracesPourAnalyse.getTraces())
			{
				ILabeling algo = pnName2labelingAlgo.get(trace.getPnName());
				if (algo != null)
					try {
						algo.labelAction( trace );
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				else{
					JOptionPane.showMessageDialog(this,
							"Error, Unknown Petri net \""+trace.getPnName()+"\" to label \""+trace.getAction()+"\" action.");
				}
			}

			// vidage des trois colonnes
			analysedActions.removeAllElements();
			labelsComputed.removeAllElements();
			synthesis.removeAllElements();

			int nbLabels = 18;
			int[] effectif = new int[nbLabels];
			for (int k = 0; k < nbLabels; k++)
				effectif[k] = 0;
			String[] intitule = new String[nbLabels];
			intitule[0] = Labels.INUTILE;//"useless";				
			intitule[1] = Labels.NON_OPTIMALE;//"non-optimal";
			intitule[2] = Labels.CORRECTE;//"correct";
			intitule[3] = Labels.EQUIVALENTE;//"equivalent";
			intitule[4] = Labels.ERRONEE;//"erroneous";
			intitule[5] = Labels.INTRUSION;//"intrusion"
			intitule[6] = Labels.ELOIGNEMENT;//"farther";
			intitule[7] = Labels.MANQUANTE;//"missing";
			intitule[8] = Labels.AUTRE_BRANCHE_DE_RESOLUTION;//"unsynchronized";
			intitule[9] = Labels.RAPPROCHEMENT;//"becoming-closer";
			intitule[10] = Labels.RATTRAPAGE;//"recovery";
			intitule[11] = Labels.RETOUR_ARRIERE;//"leap-backward";
			intitule[12] = Labels.SAUT_AVANT;//"leap-forward";
			intitule[13] = Labels.STAGNATION;//"stagnation"
			intitule[14] = Labels.TROP_TARD;//"too-late";
			intitule[15] = Labels.TROP_TOT;//"too-early";
			intitule[16] = Labels.DEJA_VU;//"already-seen";
			intitule[17] = Labels.MAUVAIS_CHOIX;//"bad-choice";

			// calcul des effectifs de chaque label
			for (ITrace tr : tracesPourAnalyse.getTraces()) {
				IFeatures features = pnName2labelingAlgo.get(tr.getPnName()).getFeatures();
				analysedActions.addElement(features.getPublicName(tr.getAction()));
				ArrayList<String> labs = tr.getLabels();
				labelsComputed.addElement(labs);
				for (String lab : labs) {
					for (int k = 0; k < nbLabels; k++){
						if (lab == intitule[k]) {
							effectif[k] += 1;
							break;
						}
					}
				}
			}

			// remplir l'analyse globale : traces and labels envoyés dans l'interface
			for (int k = 0; k < nbLabels; k++) {
				if (effectif[k] > 0) {
					ArrayList<String> ligne = new ArrayList<String>();
					ligne.add(intitule[k]);
					ligne.add(new Integer(effectif[k]).toString());
					synthesis.addElement(ligne);
				}
			}

			// on crée d'abord le dataset en éliminant les deja-vu et
			// mauvais-choix
			dataset = new DefaultPieDataset();
			for (int k = 0; k < nbLabels - 2; k++)
				if (effectif[k] > 0)
					dataset.setValue(intitule[k], effectif[k]);

			// ensuite le PieChart qui fait tout le reste
			cv = new PieChart("Analysis results", "", dataset);
			//cv.setPreferredSize(new Dimension(500, 270));
			cv.pack();
			cv.setVisible(true);
		}
    }                                             

    private void bt_exportGraphmlActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Export to Graphml format");
		if (analysedActions.getSize() != 0) {
			String outputfolder = "";
			try {
				JFileChooser chooser = new JFileChooser(new File(adresseGraphml + File.separator).getCanonicalFile());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				// Affichage et récupération de la réponse de l'utilisateur
				int reponse = chooser.showSaveDialog(null);
				// Si l'utilisateur clique sur OK
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					outputfolder = chooser.getSelectedFile().toString();
					// Identification dans les traces des Rdp utilisés
					ArrayList<String> pnUsed = new ArrayList<String>();
					for (ITrace t : listeTracePourAnalyse){
						if (!pnUsed.contains(t.getPnName()))
							pnUsed.add(t.getPnName());
					}
					// Exportation de chacun de ces réseaux
					for (String pnName : pnUsed){
						System.out.println("exporation du Rdp " + pnName);
						pnName2labelingAlgo.get(pnName).export(outputfolder+File.separator+pnName+".graphml");
					}
				}
			} catch (Exception he) {
				he.printStackTrace();
				JOptionPane.showMessageDialog(this,	"Error, "+he.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(this,	"Please, launch analysis first.");
		}
    }                                           
    
    private void bt_exportLabelsActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Export labels.");
		if (labelsComputed.getSize() != 0) {
			String outputfile = "";
			try {
				JFileChooser chooser = new JFileChooser();
				// Dossier Courant
				chooser.setCurrentDirectory(new File(adresseLabel + File.separator));
				// Affichage et récupération de la réponse de l'utilisateur
				int reponse = chooser.showDialog(chooser, "Save (.xml extension)");
				// Si l'utilisateur clique sur OK
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					outputfile = chooser.getSelectedFile().toString();
					if (outputfile.toLowerCase().endsWith(".xml"))
						outputfile = outputfile.substring(0, outputfile.length() - 4); // remove
					// user
					// extension
					adresseLabel = outputfile.substring(0, outputfile.lastIndexOf(File.separator));
					System.out.println("file: " + outputfile + ".xml");
				}
				// contenu à écrire récupération de value1 complété par les
				// labels
				ITraces itraces = new Traces();
				itraces.setTraces(listeTracePourAnalyse);
				for (int k = 0; k < listeTracePourAnalyse.size(); k++)
					System.out.println("pos " + k + " : " + listeTracePourAnalyse.get(k));
				Document doc = itraces.toXML();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				Result output = new StreamResult(new File(outputfile + ".xml"));
				Source input = new DOMSource(doc);
				transformer.transform(input, output);
			} catch (Exception he) {
				he.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(this,	"Please, launch analysis first.");
		}
    }

    private void bt_FullPnSelection2ActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Selection d'un rdp complet.");
		String fullPath = new SelectionFichier().getNomFichier(adresseReseauCompletPourFiltrage, this);
		if (!fullPath.isEmpty()){
			adresseReseauCompletPourFiltrage = fullPath;
			System.out.println("fichier de réseau choisi : " + adresseReseauCompletPourFiltrage);
			lab_FullPnSelection2.setText("File selected: "+adresseReseauCompletPourFiltrage);
			lab_FullPnSelection2.setToolTipText(adresseReseauCompletPourFiltrage);
		}
    } 

    private void bt_ExpertTraceSelectionActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Selection d'une trace experte.");
		String fullPath = new SelectionFichier().getNomFichier(adresseTrace, this);
		if (!fullPath.isEmpty()){
			adresseTrace = fullPath;
			System.out.println("fichier de réseau choisi : " + adresseTrace);
			lab_ExpertTraceSelection.setText("File selected: "+adresseTrace);
			lab_ExpertTraceSelection.setToolTipText(adresseTrace);
		}
    }

    private void bt_BuildFilteredPnActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			// Load full Pn
			System.out.println("Chargement du Rdp complet");
			IPetriNet fullPn = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			fullPn.loadPetriNet(adresseReseauCompletPourFiltrage);
			try {
				// Load traces
				System.out.println("Chargement des traces");
				ITraces traces_expert = new Traces();
				traces_expert.loadFile(adresseTrace);
				
				System.out.println("Généreration du réseau filtré");
				// pour l'onglet 0
				fullPn.filterXMLWith(traces_expert);

				String filename_new = "";
				// choisir le nom du réseau filtré
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier de réseaux filtrés
					chooser.setCurrentDirectory(new File(adresseReseauFiltre + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Save (.pnml extension)");
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						filename_new = chooser.getSelectedFile().toString();
						if (filename_new.toLowerCase().endsWith(".pnml"))
							filename_new = filename_new.substring(0, filename_new.length() - 5); // remove user extension
						adresseReseauFiltre = filename_new.substring(0, filename_new.lastIndexOf(File.separator));
						System.out.println("fichier : " + filename_new + ".pnml");
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
				// enregistrement proprement dit
				if (!filename_new.isEmpty()){
					Transformer transformer;
					Result output;
					try {
						transformer = TransformerFactory.newInstance().newTransformer();
						output = new StreamResult(new File(filename_new + ".pnml"));
						Source input = new DOMSource(((PetriNet) fullPn).xml);
						transformer.transform(input, output);
						JOptionPane.showMessageDialog(this, "Saving OK");
					} catch (Exception e5) {
						System.out.println("Saving error: "+e5.getMessage());
					}
				}
			} catch (Exception e4) {
				JOptionPane.showMessageDialog(this, "An error occurs on loading traces\n\nError : "+e4.getMessage());
				e4.printStackTrace();
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "An error occurs on loading full Petri net\n\nError: "+e1.getMessage());
		}
    }
    
	private void enableOngletTraces(boolean state){
		jTabbedPane.setEnabledAt(1, state);
		if (!state){
			enableOngletAnalyse(false);
		} else {
			fullPnActions.clear();
			tracesActions.clear();
			for (HashMap.Entry<String,ILabeling> elem : pnName2labelingAlgo.entrySet()){
				combo_FullPnFilter.addItem(elem.getKey());			
			}
			combo_FullPnFilter.setSelectedIndex(0);
		}
	}

	private void enableOngletAnalyse(boolean state){
		jTabbedPane.setEnabledAt(2, state);
		if (state){
			analysedActions.clear();
			labelsComputed.clear();
			synthesis.clear();
		}
	}

	//////////////////////////////////////////////////////////////////

	public void itemStateChanged(ItemEvent e) {
		// attaché au combo
		// nom du full choisi
		String fullPnName = (String)combo_FullPnFilter.getSelectedItem();
		if (pnName2labelingAlgo.containsKey(fullPnName)){
			fullPnActions.clear();
			// on rempli la liste des actions incluses dans ce Rdp
			IPetriNet fullPn = pnName2labelingAlgo.get(fullPnName).getCompletePN();
			if (fullPn != null){
				for (ITransition tr : fullPn.getTransitions()) {
					System.out.println(tr.getName());
					fullPnActions.addElement(tr.getName());
				}
			} else {
				JOptionPane.showMessageDialog(this, "Error, No full Petri net loaded for \""+fullPnName+"\".");
			}
		} else {
			JOptionPane.showMessageDialog(this, "Error, Unknown full Petri net named \""+fullPnName+"\".");
		}
	}
}
