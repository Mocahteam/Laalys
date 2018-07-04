import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
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

class InterfaceLaalys extends JFrame implements ActionListener, ItemListener, ComponentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// répertoire de base pour le chargement des fichiers (réseau complet,
	// réseau filtré, transitions, traces)
	String adressereseau = "exemples";

	// les répertoires par défaut
	String adresseReseauComplet = adressereseau+File.separator+"completeNets";
	String adresseReseauFiltre = adressereseau+File.separator+"filteredNets";
	String adresseSpec = adressereseau+File.separator+"features";
	String adresseTrace = adressereseau+File.separator+"trace";
	String adresseLabel = adressereseau+File.separator+"trace-labellisee";
	String adresseGraphml = adressereseau+File.separator+"trace-graphml";

	// dimensions de la fenêtre
	int largeur = 1100, hauteur = 700;
	// taille des textes si non standard (standard : 12)
	int tailleTexte1 = 12;
	int tailleTexte2 = 16;
	int indice;	

    private javax.swing.JTabbedPane jTabbedPane;
    
	// UI for the first tab
    private javax.swing.JPanel tab_PnSelection;
    // Full Pn selection
    private javax.swing.JPanel pan_FullPnSelection;
    private javax.swing.JToggleButton bt_FullPnSelection;
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
    private javax.swing.JToggleButton bt_FilteredPnSelection;
    private javax.swing.JLabel lab_FilteredPnSelection;
    // Features selection
    private javax.swing.JPanel pan_SpecificationsSelection;
    private javax.swing.JToggleButton bt_SpecificationSelection;
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

	public HashMap<String, ILabeling> pnName2labelingAlgo = new HashMap<>();
	ArrayList<ITrace> listeTracePourAnalyse;
	DefaultListModel<Serializable> listeLabels, listeSynthese;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*JLabel infoRdpComplet, infoFullFolderSelected, infoTracesExpertes, infoFilteredFolderSelected, infoCaracteristiques;
	JButton boutonRdpComplet,bt_SelectFolderFullPn, bt_SelectExpertTraces, bt_BuildFilteredPn, bt_SelectFolderFilteredPn, boutonChargerRdpFiltre, bt_SelectFolderFeatures,
	boutonChargerTraces, boutonSauvegarderTrace;
	JButton boutonAnalyserActions, boutonExporterGraphml, boutonExporterLabels;
	JRadioButton radioCouverture, radioAccessibilite, radioFirst, radioAll;
	JPanel aux4;
	JTabbedPane onglets;
	JPanel panel_fullPnSelection, panel_filteredPnSelection;*/
	
	DefaultListModel<Serializable> listeActionContent, listeNomActionsPourAnalyse;
	String choix = "", type = "accessibilité", strategie = "OU"; // par défaut
	String fullPnName;
	String filteredPnName;
	String featuresName;
	/*IPetriNet fullPn, fullPn_travail, filteredPn, fullPnFiltered;
	IFeatures features;
	ILabeling algo;*/
	ITraces copie_traces, nouvelles_traces, traces2, traces_expert;
	PieChart cv;
	DefaultPieDataset dataset;

	boolean loadingTraces = false;
	//////////////////////////////////////////////////////////////////

	public InterfaceLaalys() {
		super("Laalys");

		// Vérifier les chemins
		if (!new File(adresseReseauComplet).exists())
			adresseReseauComplet = ".";
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
        bt_FullPnSelection = new javax.swing.JToggleButton();
        lab_FullPnSelection = new javax.swing.JLabel();
        pan_FilteredPnSelection = new javax.swing.JPanel();
        pan_GraphProperties = new javax.swing.JPanel();
        opt_Coverability = new javax.swing.JRadioButton();
        opt_Accessibility = new javax.swing.JRadioButton();
        pan_AnalysisStrategy = new javax.swing.JPanel();
        opt_First = new javax.swing.JRadioButton();
        opt_All = new javax.swing.JRadioButton();
        bt_FilteredPnSelection = new javax.swing.JToggleButton();
        lab_FilteredPnSelection = new javax.swing.JLabel();
        pan_SpecificationsSelection = new javax.swing.JPanel();
        bt_SpecificationSelection = new javax.swing.JToggleButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        // ---------- Add blocs to the fourth tab ----------
        javax.swing.GroupLayout tab_FilteredPnManagementLayout = new javax.swing.GroupLayout(tab_FilteredPnManagement);
        tab_FilteredPnManagement.setLayout(tab_FilteredPnManagementLayout);
        tab_FilteredPnManagementLayout.setHorizontalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 519, Short.MAX_VALUE)
        );
        tab_FilteredPnManagementLayout.setVerticalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 322, Short.MAX_VALUE)
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
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		
		// fermeture application
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		addWindowListener(l);
		addComponentListener(this);
		// taille et structure générale en onglets
		setSize(largeur, hauteur);
		JPanel mainPanel = new JPanel(new BorderLayout());
		onglets = new JTabbedPane();
		// polices de caractères
		Font font1 = new Font("Arial", Font.PLAIN, tailleTexte1);
		JPanel tmpPanel;
		TitledBorder tmpBorder;
		JLabel tmpLabel;

		///////////////////////////////////////////////////////////////	
		// onglet1 (ongletReseaux) pour le chargement des réseaux de Petri complets, filtrés et des spécifications
		JPanel ongletSelectionReseaux = new JPanel();
		//ongletSelectionReseaux.setPreferredSize(n	ew Dimension(largeur, hauteur));
		// Ajout de cet onglet
		onglets.addTab("Petri nets selection", ongletSelectionReseaux);

		// -------- 1ère ligne pour gérer le dossier des Rdp Complets --------
		panel_fullPnSelection = new JPanel();
		tmpBorder = new TitledBorder("Full Petri nets selection");
		panel_fullPnSelection.setBorder(tmpBorder);
		panel_fullPnSelection.setLayout(new FlowLayout(FlowLayout.LEFT));
		// Bouton : Charger un dossier de Rdp complet
		tmpPanel = new JPanel();
		bt_SelectFolderFullPn = new JButton("Select folder");
		bt_SelectFolderFullPn.addActionListener(this);
		tmpPanel.add(bt_SelectFolderFullPn);
		panel_fullPnSelection.add(tmpPanel, BorderLayout.LINE_START);
		// Info sur le chargement du Rdp Complet
		tmpPanel = new JPanel();
		infoFullFolderSelected = new JLabel(new String());
		infoFullFolderSelected.setFont(font1);
		infoFullFolderSelected.setText("<html><font color=\"red\">No folder selected</font></html>");
		tmpPanel.add(infoFullFolderSelected);
		panel_fullPnSelection.add(tmpPanel);
		panel_fullPnSelection.setPreferredSize(new Dimension(largeur-40, 200));
		// Ajout de la première ligne au panneau global
		ongletSelectionReseaux.add(panel_fullPnSelection); 

		// -------- 2ème ligne pour gérer le dossier des Rdp Filtrés --------
		panel_filteredPnSelection = new JPanel();
		tmpBorder = new TitledBorder("Filtered Petri nets selection");
		panel_filteredPnSelection.setBorder(tmpBorder);
		panel_filteredPnSelection.setLayout(new FlowLayout(FlowLayout.LEFT));		
		// Bouton pour sélectionner un dossier de Rdp Filtré
		tmpPanel = new JPanel();
		bt_SelectFolderFilteredPn = new JButton("Select folder");
		bt_SelectFolderFilteredPn.addActionListener(this);
		tmpPanel.add(bt_SelectFolderFilteredPn);
		panel_filteredPnSelection.add(tmpPanel);
		// Info sur le chargement du Rdp filtré
		tmpPanel = new JPanel();
		infoFilteredFolderSelected = new JLabel(new String());
		infoFilteredFolderSelected.setFont(font1);
		infoFilteredFolderSelected.setText("<html><font color=\"red\">No folder selected</font></html>");
		tmpPanel.add(infoFilteredFolderSelected);
		panel_filteredPnSelection.add(tmpPanel);
		panel_filteredPnSelection.setPreferredSize(new Dimension(largeur-40, 70));
		ongletSelectionReseaux.add(panel_filteredPnSelection);

		// -------- 3ème ligne pour annoncer qu'il y a des spécifications --------
		JPanel pannelFiltre = new JPanel();
		pannelFiltre.setLayout(new GridLayout(1, 3));	
		// Label : Type de graphe
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>Graph properties</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelFiltre.add(tmpPanel);		
		ongletSelectionReseaux.add(pannelFiltre);		

		// -------- 4ème ligne pour choisir couverture ou accessibilité --------
		JPanel pannelFiltre2 = new JPanel();
		tmpPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		radioCouverture = new JRadioButton("Coverability", true);
		radioCouverture.setFont(font1);
		radioCouverture.addActionListener(this);
		radioCouverture.setEnabled(true);
		group.add(radioCouverture);
		tmpPanel.add(radioCouverture);
		radioAccessibilite = new JRadioButton("Accessibility");
		radioAccessibilite.setFont(font1);
		radioAccessibilite.addActionListener(this);
		radioAccessibilite.setEnabled(true);
		group.add(radioAccessibilite);
		tmpPanel.add(radioAccessibilite);
		pannelFiltre2.add(tmpPanel);	
		ongletSelectionReseaux.add(pannelFiltre2);			

		// -------- 5ème ligne pour annoncer le choix de stratégie --------
		JPanel pannelFiltre3 = new JPanel();
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>Analysis strategy</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);		
		pannelFiltre3.add(tmpPanel);
		ongletSelectionReseaux.add(pannelFiltre3);

		// -------- 6ème ligne pour boutons FIRST ou All --------		
		JPanel pannelFiltre4 = new JPanel();
		tmpPanel = new JPanel();
		group = new ButtonGroup();
		radioFirst = new JRadioButton("FIRST", true); // en fait, FIRST == OU
		radioFirst.setFont(font1);
		radioFirst.addActionListener(this);
		radioFirst.setEnabled(true);
		group.add(radioFirst);
		tmpPanel.add(radioFirst);
		radioAll = new JRadioButton("ALL", true); // en fait, ALL == ET
		radioAll.setFont(font1);
		radioAll.addActionListener(this);
		radioAll.setEnabled(true);
		group.add(radioAll);
		tmpPanel.add(radioAll);
		pannelFiltre4.add(tmpPanel);
		ongletSelectionReseaux.add(pannelFiltre4);		

		// -------- 7ème ligne pour choisir le dossier des spécificités --------
		JPanel pannelColonneSpecificites = new JPanel();
		pannelColonneSpecificites.setLayout(new GridLayout(1, 3));
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>Folder of Petri net specifications</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneSpecificites.add(tmpPanel);
		tmpPanel = new JPanel();
		bt_SelectFolderFeatures = new JButton("Select folder of specifications");
		bt_SelectFolderFeatures.addActionListener(this);
		bt_SelectFolderFeatures.setEnabled(true);
		tmpPanel.add(bt_SelectFolderFeatures);
		pannelColonneSpecificites.add(tmpPanel);
		tmpPanel = new JPanel();
		infoCaracteristiques = new JLabel(new String());
		infoCaracteristiques.setFont(font1);
		infoCaracteristiques.setText("<html><center>No folder of Petri Net specification selected<br>&nbsp;</center></html>");
		tmpPanel.add(infoCaracteristiques);
		pannelColonneSpecificites.add(tmpPanel);
		ongletSelectionReseaux.add(pannelColonneSpecificites); 

		// ---------- 8ème ligne vide pour aérer ------------
		JPanel panelVide = new JPanel();
		ongletSelectionReseaux.add(panelVide);	

		// ---------- 9ème ligne pour le bouton de confirmation : charger les réseaux et les spécifications -----------
		JPanel panelBouton = new JPanel();
		tmpPanel = new JPanel();
		boutonChargerRdpFiltre = new JButton("Load Petri nets and specifications");
		boutonChargerRdpFiltre.addActionListener(this);
		boutonChargerRdpFiltre.setEnabled(true);
		tmpPanel.add(boutonChargerRdpFiltre);
		panelBouton.add(tmpPanel); 
		ongletSelectionReseaux.add(panelBouton);

		//////////////////////////////////////////////////////////////////
		// onglet2 pour traces
		JPanel ongletTraces = new JPanel();
		//ongletTraces.setPreferredSize(new Dimension(largeur, hauteur));
		ongletTraces.setLayout(new BorderLayout());
		// Ajout de cet onglet
		onglets.addTab("Traces management", ongletTraces);

		// ---------- Création de panneau supérieur -----------
		JPanel pannelTracesNorth = new JPanel();
		pannelTracesNorth.setLayout(new BorderLayout());

		// Panel nord : Explication de la construction de la trace
		tmpPanel = new JPanel();
		tmpLabel = new JLabel(
				"<html><center>Build trace manually by drag and drop game actions from left panel"
						+ " to the right one. <br/>Press SUPPR key in the right panel to remove a game action");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelTracesNorth.add("North", tmpPanel);

		// un tableau 3x3 au centre
		JPanel pannelTracesNorthSouth = new JPanel();
		pannelTracesNorthSouth.setLayout(new GridLayout(3,3));

		// ligne 1 : texte de choix du full Petri Net et texte "OR"		
		tmpPanel = new JPanel();
		JLabel tmpLabel1 = new JLabel("Full Petri Net");
		tmpLabel1.setFont(font1);
		tmpPanel.add(tmpLabel1);
		pannelTracesNorthSouth.add(tmpPanel);	
		pannelTracesNorthSouth.add(new JPanel()); // une case vide	
		JPanel tmpPanel2 = new JPanel();
		JLabel tmpLabel2 = new JLabel("Traces");
		tmpLabel2.setFont(font1);
		tmpPanel2.add(tmpLabel2);
		pannelTracesNorthSouth.add(tmpPanel2); 

		// ligne 2 : choix d'un Full Petri Net et chargement d'un fichier de trace
		tmpPanel2 = new JPanel();// pour liste de sélection
		// Object[] elements = new Object[]{"murDeGlace", "FrozenDoor", "Thermometer"};
		petriNetsCombo = new JComboBox<String>();
		petriNetsCombo.addItemListener(this);
		tmpPanel2.add(petriNetsCombo);
		pannelTracesNorthSouth.add(tmpPanel2); 	
		pannelTracesNorthSouth.add(new JPanel()); // une case vide
		JPanel tmpPanel3 = new JPanel();
		boutonChargerTraces = new JButton("Load traces from file");
		boutonChargerTraces.addActionListener(this);
		tmpPanel3.add(boutonChargerTraces);
		pannelTracesNorthSouth.add(tmpPanel3);

		// ligne 3 : Titre des colonnes
		// Titre colonne gauche
		tmpPanel = new JPanel();
		tmpPanel.setLayout(new FlowLayout());
		tmpPanel.add(new JLabel("Available game actions"));
		pannelTracesNorthSouth.add(tmpPanel);
		// milieu
		pannelTracesNorthSouth.add(new JPanel());
		// Titre colonne droite
		tmpPanel = new JPanel();
		tmpPanel.setLayout(new FlowLayout());
		tmpPanel.add(new JLabel("Game actions for analysis"));
		pannelTracesNorthSouth.add(tmpPanel);
		// Ajout des titres de colonne
		pannelTracesNorth.add("Center", pannelTracesNorthSouth);

		// Ajout du panneau supérieur à l'onglet des traces 
		ongletTraces.add(pannelTracesNorth, BorderLayout.NORTH);

		// ---------- Création du panneau central -----------
		JPanel pannelTracesCenter = new JPanel();
		pannelTracesCenter.setLayout(new GridLayout(1, 3));
		// colonne de gauche : Actions de jeu possible
		JPanel colonneGaucheTraces = new JPanel();
		JPanel colonneGaucheTracesContent = new JPanel();
		colonneGaucheTracesContent.setLayout(new BorderLayout());
		// Liste contenant les actions possibles
		listeActionContent = new DefaultListModel<Serializable>();
		JList<Serializable> listeActionsConteneur = new JList<Serializable>(listeActionContent);
		listeActionsConteneur.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listeActionsConteneur.setDragEnabled(true);
		JScrollPane scrollPane = new JScrollPane(listeActionsConteneur);
		//scrollPane.setPreferredSize(new Dimension(320, 385));
		colonneGaucheTracesContent.add(scrollPane, BorderLayout.CENTER);
		colonneGaucheTraces.add("Center", colonneGaucheTracesContent);
		// Ajout de la colonne de gauche
		pannelTracesCenter.add(colonneGaucheTraces);

		// colonne du centre
		JPanel panelBoutons = new JPanel();
		panelBoutons.setLayout(new GridLayout(10,1));
		panelBoutons.add(new JPanel());
		JPanel p0 = new JPanel();
		p0.add(new JLabel("<html>By moving an action from left to right, <br/>select option \"system\" or option \"player\"</html>"));
		panelBoutons.add(p0);
		JPanel p1 = new JPanel();
		ButtonGroup systemPlayer = new ButtonGroup();
		radioSystem = new JRadioButton("system", false);
		radioSystem.setFont(font1);
		radioSystem.addActionListener(this);
		radioSystem.setEnabled(true);
		systemPlayer.add(radioSystem);
		p1.add(radioSystem);
		panelBoutons.add(p1);
		JPanel p2 = new JPanel();		
		radioPlayer = new JRadioButton(ActionType.PLAYER, true);
		radioPlayer.setFont(font1);
		radioPlayer.addActionListener(this);
		radioPlayer.setEnabled(true);
		systemPlayer.add(radioPlayer);
		p2.add(radioPlayer);
		panelBoutons.add(p2);
		panelBoutons.add(new JPanel());	
		panelBoutons.add(new JPanel());
		panelBoutons.add(new JPanel());
		panelBoutons.add(new JPanel());
		panelBoutons.add(new JPanel());
		panelBoutons.add(new JPanel());
		pannelTracesCenter.add(panelBoutons);

		// colonne de droite : actions à analyser
		JPanel colonneDroiteTraces = new JPanel();
		JPanel colonneDroiteTracesContent = new JPanel();
		colonneDroiteTracesContent.setLayout(new BorderLayout());
		// Liste contenant les actions à analyser
		listeNomActionsPourAnalyse = new DefaultListModel<Serializable>();
		listeTracePourAnalyse = new ArrayList<ITrace>();
		listeNomActionsPourAnalyse.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Remove : "+e.getIndex0()+" "+e.getIndex1());
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex1() ; i >= e.getIndex0() ; i--)
						listeTracePourAnalyse.remove(i);
				}
				if (listeNomActionsPourAnalyse.isEmpty())
					enableOngletAnalyse(false);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				// TODO Auto-generated method stub
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex0() ; i <= e.getIndex1() ; i++){
						String action = listeNomActionsPourAnalyse.getElementAt(i).toString();
						// Création d'un objet trace pour stocker les données
						// Vérifier si l'action courante fait référence à une action système
						String origine = "player"; // par défaut
						ArrayList<String> proprietes = features.getSystemTransitions(); // ensemble des transitions système
						for (int m = 0; m < proprietes.size(); m++) {
							if (proprietes.get(m).indexOf(action) != -1) {
								// l'action est une action système
								origine = "system";
								break;
							}
						}
						// création de la nouvelle ligne de trace
						ITrace nouvelletrace = new Trace(fullPn.getName(), action, "manual", origine, false);
						listeTracePourAnalyse.add(i, nouvelletrace);
					}
				}
				enableOngletAnalyse(true);
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				// TODO Auto-generated method stub
			}
		});

		JList<Serializable> listeNomActionsPourAnalyseConteneur = new JList<Serializable>(listeNomActionsPourAnalyse);
		listeNomActionsPourAnalyseConteneur.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		scrollPane = new JScrollPane(listeNomActionsPourAnalyseConteneur);
		//scrollPane.setPreferredSize(new Dimension(320, 350));
		listeNomActionsPourAnalyseConteneur.setDragEnabled(true);
		listeNomActionsPourAnalyseConteneur.setDropMode(DropMode.INSERT);
		listeNomActionsPourAnalyseConteneur.setTransferHandler(new ListTransferHandler());
		listeNomActionsPourAnalyseConteneur.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				// System.out.println("Touche pressée : " + e.getKeyCode());
				if (e.getKeyCode()== KeyEvent.VK_DELETE){
					for (int i = listeNomActionsPourAnalyseConteneur.getSelectedIndices().length-1 ; i >= 0 ; i--){
						listeNomActionsPourAnalyse.remove(listeNomActionsPourAnalyseConteneur.getSelectedIndices()[i]);
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		colonneDroiteTracesContent.add(scrollPane);
		colonneDroiteTraces.add("Center", colonneDroiteTracesContent);
		// Bouton : Sauvegarde de la trace
		tmpPanel = new JPanel();
		boutonSauvegarderTrace = new JButton("Save traces");
		tmpPanel.add(boutonSauvegarderTrace);
		boutonSauvegarderTrace.addActionListener(this);
		colonneDroiteTraces.add("South", tmpPanel);
		// Ajout de la colonne de droite
		pannelTracesCenter.add(colonneDroiteTraces);

		// Ajout du panneau principal à son onglet
		ongletTraces.add(pannelTracesCenter, BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////
		// onglet3 pour analyse
		JPanel ongletAnalyse = new JPanel();
		//ongletAnalyse.setPreferredSize(new Dimension(largeur, hauteur));
		ongletAnalyse.setLayout(new BorderLayout());
		// Ajout de cet onglet
		onglets.addTab("Analysis", ongletAnalyse);

		// ---------- Création de panneau supérieur -----------
		JPanel pannelAnalyseNorth = new JPanel();
		pannelAnalyseNorth.setLayout(new GridLayout(2, 3));
		// 1ère ligne : vide / bouton / vide
		// espace vide
		tmpPanel = new JPanel();
		pannelAnalyseNorth.add(tmpPanel);
		// Bouton : Analyser actions
		tmpPanel = new JPanel();
		boutonAnalyserActions = new JButton("Launch analysis");
		boutonAnalyserActions.addActionListener(this);
		tmpPanel.add(boutonAnalyserActions);
		pannelAnalyseNorth.add(tmpPanel);
		// espace vide
		tmpPanel = new JPanel();
		pannelAnalyseNorth.add(tmpPanel);
		// 2ème ligne : Titres colonnes
		// Label : Actions analysées
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("Analysed actions");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelAnalyseNorth.add(tmpPanel);
		// Label : Labels identifiés
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("computed labels");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelAnalyseNorth.add(tmpPanel);
		// Label : Compte-rendu d'analyse
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("Synthesis");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelAnalyseNorth.add(tmpPanel);
		// Ajout du panneau supérieur  l'onglet d'analyse
		ongletAnalyse.add(pannelAnalyseNorth, BorderLayout.NORTH);

		// ---------- Création de panneau central -----------
		JPanel pannelAnalyseCenter = new JPanel();
		pannelAnalyseCenter.setLayout(new GridLayout(1, 3));
		// colonne de gauche : Actions analysées
		JPanel colonneGaucheAnalyse = new JPanel();
		JPanel colonneGaucheAnalyseContent = new JPanel();
		colonneGaucheAnalyseContent.setLayout(new BorderLayout());
		// Liste contenant les actions analysées
		listeActionsAnalysees = new DefaultListModel<Serializable>();
		JList<Serializable> listeActionsAnalyseesConteneur = new JList<Serializable>(listeActionsAnalysees);
		scrollPane = new JScrollPane(listeActionsAnalyseesConteneur);
		//scrollPane.setPreferredSize(new Dimension(300, 470));
		colonneGaucheAnalyseContent.add(scrollPane);
		colonneGaucheAnalyse.add("Center", colonneGaucheAnalyseContent);
		// Bouton : Exporter Graphml
		tmpPanel = new JPanel();
		boutonExporterGraphml = new JButton("Export to Graphml format");
		boutonExporterGraphml.addActionListener(this);
		tmpPanel.add(boutonExporterGraphml);
		colonneGaucheAnalyse.add("South", tmpPanel);
		// Ajout de la colonne de gauche
		pannelAnalyseCenter.add(colonneGaucheAnalyse);

		// colonne du centre : Labels identifiés
		JPanel colonneCentreAnalyse = new JPanel();
		JPanel colonneCentreAnalyseContent = new JPanel();
		colonneCentreAnalyseContent.setLayout(new BorderLayout());
		// Liste contenant les labels identifiés
		listeLabels = new DefaultListModel<Serializable>();
		JList<Serializable> listeLabelsConteneur = new JList<Serializable>(listeLabels);
		scrollPane = new JScrollPane(listeLabelsConteneur);
		//scrollPane.setPreferredSize(new Dimension(300, 470));
		colonneCentreAnalyseContent.add(scrollPane);
		colonneCentreAnalyse.add("Center", colonneCentreAnalyseContent);
		// Bouton : Exporter les labels
		tmpPanel = new JPanel();
		boutonExporterLabels = new JButton("Export labels");
		boutonExporterLabels.addActionListener(this);
		tmpPanel.add(boutonExporterLabels);
		colonneCentreAnalyse.add("South", tmpPanel);
		// Ajout de la colonne du centre
		pannelAnalyseCenter.add(colonneCentreAnalyse);

		// colonne de droite : Résultats
		JPanel colonneDroiteAnalyse = new JPanel();
		JPanel colonneDroiteAnalyseContent = new JPanel();
		colonneDroiteAnalyseContent.setLayout(new BorderLayout());
		// Liste contentant la synthèse de l'analyse
		listeSynthese = new DefaultListModel<Serializable>();
		JList<Serializable> listeSyntheseConteneur = new JList<Serializable>(listeSynthese);
		scrollPane = new JScrollPane(listeSyntheseConteneur);
		//scrollPane.setPreferredSize(new Dimension(300, 470));
		colonneDroiteAnalyseContent.add(scrollPane);
		colonneDroiteAnalyse.add("Center", colonneDroiteAnalyseContent);
		// Ajout de la colonne de droite
		pannelAnalyseCenter.add(colonneDroiteAnalyse);

		// Ajout du panneau principal à son onglet
		ongletAnalyse.add("Center", pannelAnalyseCenter);
		enableOngletTraces(false);
		// enableOngletTraces(true);

		//////////////////////////////////////////////////////////////////		
		// 4ème onglet (dit à tort onglet0) pour la création d'un filtré à partir d'un complet et d'une trace experte
		JPanel ongletReseaux0 = new JPanel();
		//ongletReseaux0.setPreferredSize(new Dimension(largeur, hauteur));
		// onglets.addTab("Filtered nets management", ongletReseaux0); reporté à la fin, pour qu'il soit le dernier	
		// son contenu : 
		JPanel mainPanelReseaux0 = new JPanel();
		mainPanelReseaux0.setLayout(new GridLayout(1, 1));  // -------- une  colonne suffit --------
		JPanel pannelColonneRdpComplet = new JPanel();
		pannelColonneRdpComplet.setLayout(new GridLayout(11, 1));
		// Label : Chargement du réseau complet sans graphe
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>Full Petri net</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Charger un Rdp complet
		tmpPanel = new JPanel();
		boutonRdpComplet = new JButton("Load full Petri net");
		boutonRdpComplet.addActionListener(this);
		tmpPanel.add(boutonRdpComplet);
		pannelColonneRdpComplet.add(tmpPanel);
		// Info chargement Rdp Complet
		tmpPanel = new JPanel();
		infoRdpComplet = new JLabel(new String());
		infoRdpComplet.setFont(font1);
		infoRdpComplet.setText("<html><center>No Full Petri net loaded<br>&nbsp;</center></html>");
		tmpPanel.add(infoRdpComplet);
		pannelColonneRdpComplet.add(tmpPanel);
		// espace vide pour aérer
		tmpPanel = new JPanel();
		pannelColonneRdpComplet.add(tmpPanel);
		// Label : option facultative pour créer un réseau filtré
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>OPTION: Build filtered Petri net</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Choisir trace experte
		tmpPanel = new JPanel();
		bt_SelectExpertTraces = new JButton("a. Choose expert trace");
		bt_SelectExpertTraces.addActionListener(this);
		bt_SelectExpertTraces.setEnabled(false);
		tmpPanel.add(bt_SelectExpertTraces);
		pannelColonneRdpComplet.add(tmpPanel);
		// Info chargement trace experte
		tmpPanel = new JPanel();
		infoTracesExpertes = new JLabel(new String());
		infoTracesExpertes.setFont(font1);
		infoTracesExpertes.setText("<html><center>No expert trace selected<br>&nbsp;</center></html>");
		tmpPanel.add(infoTracesExpertes);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Générer Rdp Filtré
		tmpPanel = new JPanel();
		bt_BuildFilteredPn = new JButton("b. Build filtered Petri net");
		bt_BuildFilteredPn.addActionListener(this);
		bt_BuildFilteredPn.setEnabled(false);
		tmpPanel.add(bt_BuildFilteredPn);
		pannelColonneRdpComplet.add(tmpPanel);
		// Ajout au panneau global
		mainPanelReseaux0.add(pannelColonneRdpComplet);
		ongletReseaux0.add(mainPanelReseaux0);
		// ajout de l'onglet0 en tout dernier lieu
		onglets.addTab("Filtered nets management", ongletReseaux0);
		
		// Ajout de tous les onglets au panneau principal
		mainPanel.add(onglets);
		// Ajout du panneau principal à this
		getContentPane().add(mainPanel);
		setVisible(true);*/
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
		bt_FullPnSelection.setSelected(false);
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
		bt_FilteredPnSelection.setSelected(false);
    }   
	
    private void optActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	Object source = evt.getSource();

		// les 4 boutons de configuration
		if (source == opt_Coverability) {
			System.out.println("couverture");
			type = "couverture";
			opt_First.setEnabled(true);
			opt_All.setEnabled(true);
		} else if (source == opt_Accessibility) {
			System.out.println("accessibilité");
			type = "accessibilité";
			// interdire le bouton1b
			opt_All.setEnabled(false);
			// activer le bouton1a automatiquement
			opt_First.setSelected(true);
			strategie = "OU";
		} else if (source == opt_First) {
			System.out.println("stratégie FIRST, en fait, OU");
			strategie = "OU";
		} else if (source == opt_All) {
			System.out.println("stratégie ALL, en fait, ET");
			strategie = "ET";
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
		bt_SpecificationSelection.setSelected(false);
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
					listeNomActionsPourAnalyse.removeAllElements();								
				}
				enableOngletAnalyse(true);
			}
		} catch (Exception e6) {
			JOptionPane.showMessageDialog(this, "Error on loading traces file\n\n"+e6.getMessage());
			tracesActions.removeAllElements();
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
			IPetriNet filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
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
		fullPnName = (String)combo_FullPnFilter.getSelectedItem();
		if (pnName2labelingAlgo.containsKey(fullPnName)){
			fullPnActions.clear();
			// on rempli la liste des actions incluses dans ce Rdp
			IPetriNet fullPn = pnName2labelingAlgo.get(fullPnName).getCompletePN();
			if (fullPn != null){
				System.out.println("START");
				for (ITransition tr : fullPn.getTransitions()) {
					System.out.println(tr.getName());
					fullPnActions.addElement(tr.getName());
				}
				System.out.println("END");
			} else {
				JOptionPane.showMessageDialog(this, "Error, No full Petri net loaded for \""+fullPnName+"\".");
			}
		} else {
			JOptionPane.showMessageDialog(this, "Error, Unknown full Petri net named \""+fullPnName+"\".");
		}
	}

	public void actionPerformed(ActionEvent e) {
		SelectionFichier sf;
		Object source = e.getSource();

		// les 4 boutons de configuration
		if (source == radioCouverture) {
			System.out.println("couverture");
			type = "couverture";
			radioFirst.setEnabled(true);
			radioAll.setEnabled(true);
		} else if (source == radioAccessibilite) {
			System.out.println("accessibilité");
			type = "accessibilité";
			// interdire le bouton1b
			radioAll.setEnabled(false);
			// activer le bouton1a automatiquement
			radioFirst.setSelected(true);
			strategie = "OU";
		} else if (source == radioFirst) {
			System.out.println("stratégie FIRST, en fait, OU");
			strategie = "OU";
		} else if (source == radioAll) {
			System.out.println("stratégie ALL, en fait, ET");
			strategie = "ET";
		}

		else if (source == boutonRdpComplet) {
			System.out.println("Charger un réseau complet sans graphe.");
			// seulement pour créer un réseau de Petri filtré à partir de traces expertes
			// pour l'onglet0
			// on choisit le FICHIER à charger
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseReseauComplet, this);
				if (!fileName.isEmpty()){
					// vider tout
					boutonRdpComplet.setBackground(UIManager.getColor("Bouton.background"));
					infoRdpComplet.setText("<html><center>No full Petri net loaded<br>&nbsp;</center></html>");
					bt_SelectExpertTraces.setBackground(UIManager.getColor("Bouton.background"));
					infoTracesExpertes.setText("<html><center>No expert traces selected<br>&nbsp;</center></html>");
					bt_BuildFilteredPn.setBackground(UIManager.getColor("Bouton.background"));
					bt_BuildFilteredPn.setEnabled(false);

					bt_SelectFolderFilteredPn.setBackground(UIManager.getColor("Bouton.background"));
					infoFilteredFolderSelected.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
					// toggleFilteredFields(false);

					bt_SelectFolderFeatures.setBackground(UIManager.getColor("Bouton.background"));
					infoCaracteristiques.setText("<html><center>No features loaded<br>&nbsp;</center></html>");
					featuresName = null;

					enableOngletTraces(false);
					// enableOngletTraces(true);

					// Chargement
					fullPnName = fileName;
					System.out.println("fichier de réseau choisi : " + fullPnName);
					infoRdpComplet.setText("<html><center>Full Petri net loaded:<br>" + fileName + "</center></html>");
					fullPn = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
					boolean success = false;
					try {
						fullPn.loadPetriNet(fullPnName);
						int index = fullPnName.lastIndexOf(File.separator);
						String nomfich = fullPnName.substring(index + 1);
						adresseReseauComplet = fullPnName.substring(0, index);
						infoRdpComplet.setText("<html><center>Full Petri net loaded:<br>" + nomfich + "</center></html>");
						boutonRdpComplet.setBackground(Color.CYAN);
						success = true;
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(this, "An error occurs on loading full Petri net\n\nError: "+e1.getMessage());
						boutonRdpComplet.setBackground(UIManager.getColor("Bouton.background"));
						infoRdpComplet.setText("<html><center>No full Petri net loaded<br>"+fileName+"</center></html>");
						listeActionContent.clear();
						fullPnName = null;
						fullPn = null;
					}
					bt_SelectExpertTraces.setEnabled(success);
					// boutonSelectionnerRdpFiltre.setEnabled(success);
					// boutonChargerCaracteristiques.setEnabled(success);
				}
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, "An error occurs\n\nError: "+e3.getMessage());
				fullPnName = null;
				fullPn = null;
			}
		}


		else if (source == bt_SelectExpertTraces) {
			System.out.println("Choisir la trace");
			// pour l'onglet0, on recharge le même réseau complet
			// mais cette fois pour filtrer le XML associé avant de le
			// réenregister sous un autre nom
			// on l'appelle fullPn_travail
			boolean correct = true;
			// rechargement sous un autre nom
			fullPn_travail = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			try {
				fullPn_travail.loadPetriNet(fullPnName);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "An error occurs on loading full Petri net\nProcess aborted\n\nError : "+e1.getMessage());
				correct = false;
			}
			// on choisit une trace experte pour filtrer le réseau
			// fullPn_travail
			if (correct)
				try {
					sf = new SelectionFichier();
					String traceName2 = sf.getNomFichier(adresseTrace, this);
					if (!traceName2.isEmpty()){
						int index2 = traceName2.lastIndexOf(File.separator);
						adresseTrace = traceName2.substring(0, index2);
						String nomfich2 = traceName2.substring(index2 + 1);
						// System.out.println("fichier de trace expert choisi : " +
						// traceName2);
						infoTracesExpertes.setText("<html><center>Expert traces chosen:<br>" + nomfich2 + "</center></html>");
						traces_expert = new Traces();
						traces_expert.loadFile(traceName2);
						System.out.println("--------------------traces_expert-------------------------");
						System.out.println(traces_expert.toString());
						System.out.println("---------------------------------------------");
						bt_SelectExpertTraces.setBackground(Color.CYAN);
						bt_BuildFilteredPn.setEnabled(true);
					}
				} catch (Exception e4) {
					JOptionPane.showMessageDialog(this, "An error occurs on loading file\n\nError : "+e4.getMessage());
				}
		}

		else if (source == bt_BuildFilteredPn) {
			// toujours pour l'onglet0
			System.out.println("Générer le réseau filtré");
			// pour l'onglet 0
			fullPn_travail.filterXMLWith(traces_expert);

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
					Source input = new DOMSource(((PetriNet) fullPn_travail).xml);
					transformer.transform(input, output);
					JOptionPane.showMessageDialog(this, "Saving OK");
				} catch (Exception e5) {
					System.out.println("Saving error: "+e5.getMessage());
				}
			}
		}


		else if (source == bt_SelectFolderFullPn) {
			// choisir un DOSSIER de réseaux complets
			System.out.println("Choisir un dossier de réseau complet sans graphe.");
			String folderName = new SelectionDossier().getNomDossier(adresseReseauComplet, this);
			if (!folderName.isEmpty() && !folderName.equals(adresseReseauComplet)){
				adresseReseauComplet = folderName;
				System.out.println("dossier choisi : " + adresseReseauComplet);
				infoFullFolderSelected.setText("<html>Folder selected: <b>"+adresseReseauComplet+"</b></html>");
				enableOngletAnalyse(false);
				enableOngletTraces(false);
			}
		}

		else if (source == bt_SelectFolderFilteredPn) {
			// onglet1 : ancienne version de sélection d'un réseau filtré 
			/* System.out.println("Select a filtered Petri net.");
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseReseauFiltre, this);
				if (!fileName.isEmpty()){
					filteredPnName  = fileName; 
					int index = filteredPnName.lastIndexOf(File.separator);
					String nomfich = filteredPnName.substring(index + 1);
					adresseReseauFiltre = filteredPnName.substring(0, index);
					infoRdpFiltre.setText("<html><center>Filtered Petri net selected:<br>" + nomfich + "</center></html>");
					toggleFilteredFields(true);
					radioCouverture.setSelected(true);
					radioFirst.setSelected(true);
					boutonSelectionnerRdpFiltre.setBackground(Color.CYAN);
					// enableOngletTraces(false);
					enableOngletTraces(true);
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "An error occurs on loading the file\n\nError : "+e5.getMessage());
			}  */

			// onglet1 : choisir un DOSSIER pour les réseaux filtrés
			System.out.println("Choisir un dossier de réseaux filtrés");
			String folderName = new SelectionDossier().getNomDossier(adresseReseauFiltre, this);
			if (!folderName.isEmpty() && !folderName.equals(adresseReseauFiltre)){
				adresseReseauFiltre = folderName;
				System.out.println("dossier choisi : " + adresseReseauFiltre);
				infoFilteredFolderSelected.setText("<html>Folder selected: <b>"+adresseReseauFiltre+"</b></html>");
				enableOngletAnalyse(false);
				enableOngletTraces(false);
			}
		}

		else if (source == bt_SelectFolderFeatures) {
			// onglet1 : ancienne version pour charger un fichier de spécifications
			/* System.out.println("Loading specifications.");
			features = new Features();
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseSpec, this);
				if (!fileName.isEmpty()){
					featuresName = fileName;
					// charger le fichier
					System.out.println("Specification selected: " + featuresName);
					features.loadFile(featuresName);
					adresseSpec = featuresName.substring(0, featuresName.lastIndexOf(File.separator));
					// Vérifier s'il y a au moins une action de fin
					if (!features.getEndLevelTransitions().isEmpty()){
						boutonChargerCaracteristiques.setBackground(Color.CYAN);
						int index = featuresName.lastIndexOf(File.separator);
						String nomfich = featuresName.substring(index + 1);
						infoCaracteristiques.setText("<html><center>Specifications selected:<br>" + nomfich + "</center></html>");
						// si le Rdp filtré est aussi chargée, on peut dévérouiller les traces
						boutonChargerCaracteristiques.setBackground(Color.CYAN);
						if (filteredPn != null)
							enableOngletTraces(true);
					} else{
						JOptionPane.showMessageDialog(this, "Incomplet specifications.\n\n"
								+ "No end transition defined");
						featuresName = null;
						boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
						infoCaracteristiques.setText("<html><center>No specifications loaded<br>&nbsp;</center></html>");
						// enableOngletTraces(false);
						enableOngletTraces(true);
					}
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "Error on loading specifications\n\n"+e5.getMessage());
				featuresName = null;
				boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
				infoCaracteristiques.setText("<html><center>No specifications loaded<br>&nbsp;</center></html>");
				// enableOngletTraces(false);
				enableOngletTraces(true);
			} */

			// nouvelle version pour choisir un dossier de caractéristiques
			/*System.out.println("Choisir un dossier de caractéristiques");
			boutonChargerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
			nomDossierCaracteristiques = new SelectionDossier().getNomDossier(adressereseau, this) ;
			System.out.println("dossier choisi : " + nomDossierCaracteristiques);
			int index = nomDossierCaracteristiques.lastIndexOf(File.separator);
			String nomfich = nomDossierCaracteristiques.substring(index + 1);
			infoCaracteristiques.setText("<html><center>Folder of specifications selected:<br>"+nomfich+"<br/></center></html>");
			bt_SelectFolderFeatures.setBackground(Color.CYAN);*/
		}


		else if (source == boutonChargerRdpFiltre) {
			// onglet1 : ancienne version qui chargeait seulement le réseau filtré
			/* if ((filteredPnName == null) || (filteredPnName.isEmpty())) {
				JOptionPane.showMessageDialog(this,
						"Please, select first the filtered Petri net to load.");
			} else {
				boutonChargerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
				if (filteredPnName != null && !filteredPnName.isEmpty()) { // test peut-être inutile
					// charger le fichier par IPetriNet - loadPetriNet()
					System.out.print("Filtered Petri net selected: " + filteredPnName);
					System.out.print(" - " + type);
					System.out.println(" - strategy : " + strategie);
					if (type.equalsIgnoreCase("accessibilité")) {
						filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
					} else if (type.equalsIgnoreCase("couverture")) { // couverture
						if (strategie.equalsIgnoreCase("OU"))
							filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
						else if (strategie.equalsIgnoreCase("ET"))
						
							filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_AND);
						else
							filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_FIRST);
					}
					try {
						filteredPn.loadPetriNet(filteredPnName);
						// Vérifier que toutes les transitions du Rdp filtré sont bien incluses dans le Rdp Complet
						System.out.println("filteredPn chargé");
						boolean consistant = true;
						ArrayList<ITransition> filteredTransitions = filteredPn.getTransitions();
						for (int i = 0 ; i < filteredTransitions.size() ; i++){
							if (fullPn.getTransitionById(filteredTransitions.get(i).getName()) == null){
								consistant = false;
								break;
							}
						}
						if (consistant){		
							// System.out.println("filtered graph size :
							// "+filteredPn.getGraph().getAllMarkings().size() + " "
							// + (System.currentTimeMillis()-stamp));
							boutonChargerRdpFiltre.setBackground(Color.CYAN);
							// si les features sont aussi chargées, on peut dévérouiller les traces
							if (featuresName != null && !featuresName.isEmpty())
								enableOngletTraces(true);
						} else {
							JOptionPane.showMessageDialog(this, "This filtered Petri net includes transitions not included into full petri net\n\nLoading aborted");
							toggleFilteredFields(false);
							// enableOngletTraces(false);
							enableOngletTraces(true);
							infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
						}
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(this, "Loading filtered Petri net fail\n\n"+e2.getMessage());
						toggleFilteredFields(false);
						// enableOngletTraces(false);
						enableOngletTraces(true);
						infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
					}
				}
			} */

			// onglet1 : nouvelle version qui charge tous les fichiers des trois dossiers
			/*System.out.println(adresseReseauComplet);
			System.out.println(nomDossierRdpFiltre);
			System.out.println(nomDossierCaracteristiques);

			Logger monLog = Logger.getLogger(Main.class.getName());
			monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
			monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
			ConsoleHandler ch = new ConsoleHandler();
			ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau INFO
			monLog.addHandler(ch);
			File fullDir = new File(adresseReseauComplet);
			for (File fullChild : fullDir.listFiles()){
				// get equivalent file in filtered folder
				File filteredDir = new File(nomDossierRdpFiltre);
				File filteredChild = new File(filteredDir, fullChild.getName());
				// get equivalent file in features folder
				File featuresDir = new File(nomDossierCaracteristiques);
				File featuresChild = new File(featuresDir, fullChild.getName().substring(0, fullChild.getName().length()-4)+"xml");
				if (!filteredChild.exists() || !featuresChild.exists()){
					System.err.println("Error: equivalent files of full Petri net don't exist in selected folders\n\nLoading aborted");
					JOptionPane.showMessageDialog(this, "Error, files of full Petri net don't exist in selected folders\n\nLoading aborted");
					pnName2labelingAlgo.clear();
					return ;
				}
				// Instantiate full Petri net
				fullPn = new PetriNet(false, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
				try {
					fullPn.loadPetriNet(fullChild.getAbsolutePath());
				} catch (Exception e0) {
					System.err.println("Error with -fullPn option: unable to load "+fullChild.getAbsolutePath()+" file.\n"+e0.getMessage());
					JOptionPane.showMessageDialog(this, "Warning, unable to load the full Petri net\n\nLoading aborted");
					pnName2labelingAlgo.clear();
					return ;
				}
				// Instantiate filtered Petri net
				filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
				try {
					filteredPn.loadPetriNet(filteredChild.getAbsolutePath());
				} catch (Exception e2) {
					System.err.println("Error with -filteredPn option: unable to load "+filteredChild.getAbsolutePath()+" file.\n"+e2.getMessage());
					JOptionPane.showMessageDialog(this, "Warning, unable to load the filtered Petri net\n\nLoading aborted");
					pnName2labelingAlgo.clear();
					return;
				}
				// Instantiate features
				features = new Features();
				try {
					features.loadFile(featuresChild.getAbsolutePath());
				} catch (IOException e1) {
					System.err.println("Error with -features option: unable to load "+featuresChild.getAbsolutePath()+" file.\n"+e1.getMessage());
					JOptionPane.showMessageDialog(this, "Warning, unable to load the features\n\nLoading aborted");
					pnName2labelingAlgo.clear();
					return ;
				}

				// Init labeling algorithm
				algo = new Labeling_V10(monLog, false);
				algo.setCompletePN(fullPn);
				algo.setFilteredPN(filteredPn);
				algo.setFeatures(features);
				try {
					algo.reset();
				} catch (Exception e2) {
					System.out.println("Labeling algorithm initialisation fail. "+e2.getMessage());
					JOptionPane.showMessageDialog(this, "Warning, Labeling algorithm initialisation fail.\n\nInitialization aborted");
					pnName2labelingAlgo.clear();
					return ;
				}
				String pnName = fullChild.getName();
				if (pnName.endsWith(".pnml"))
					pnName = pnName.substring(0, pnName.length()-5);
				pnName2labelingAlgo.put(pnName, algo);
			}
			// System.out.println("Nb Full found: "+pnName2labelingAlgo.size());

			// initialisation du combo et de la liste des actions possibles de l'onglet2
			for (HashMap.Entry<String,ILabeling> elem : pnName2labelingAlgo.entrySet()){
				algo = elem.getValue() ;
				fullPn = algo.getCompletePN();
				// on a trouvé le premier fullPn qui apparaitra dans le combo
				break; 			
			}
			// on remplit la listeActionContent correspondant à ce premier fullPn
			listeActionContent.clear();
			listeNomActionsPourAnalyse.clear();
			if (fullPn != null){
				for (ITransition tr : fullPn.getTransitions()) {
					listeActionContent.addElement(tr.getName());
				}
			}
			// ensuite on les parcourt tous pour remplir les intitulés du combo
			for (HashMap.Entry<String,ILabeling> elem : pnName2labelingAlgo.entrySet()){
				System.out.println("elem : " + elem.getKey() + " : " + elem.getValue());
				petriNetsCombo.addItem(elem.getKey());			
			}
			boutonChargerRdpFiltre.setBackground(Color.CYAN);
			enableOngletTraces(true);	*/
		}

		else if (source == boutonChargerTraces) {
			// onglet2 : charger un fichier de traces
			System.out.println("Loading traces file.");
			loadingTraces = true;
			// on vérifie qu'un fichier de réseau complet a été chargé
			if (fullPn == null) {
				JOptionPane.showMessageDialog(this, "Please, select first a full Petri net");
			} else {
				if (!listeActionContent.isEmpty()) {
					try {
						sf = new SelectionFichier();
						String fileName = sf.getNomFichier(adresseTrace, this);
						if (!fileName.isEmpty()){
							traceName = fileName;
							adresseTrace = traceName.substring(0, traceName.lastIndexOf(File.separator));
							listeNomActionsPourAnalyse.removeAllElements();
							listeTracePourAnalyse = new ArrayList<ITrace>();
							System.out.println("traces selected: " + traceName);
							ITraces tracesToLoad = new Traces();
							tracesToLoad.loadFile(traceName);
							boolean consistant1 =true ;
							boolean consistant = true ;
							// ICI copie_traces = new Traces();
							// on parcourt les traces chargées
							for (ITrace tr : tracesToLoad.getTraces()) { 
								// pour chaque trace
								algo = pnName2labelingAlgo.get(tr.getPnName());
								if (algo == null) {						
									JOptionPane.showMessageDialog(this, "Warning, this traces correspond to not existing\n"
											+ " full Petri net\n\nLoading aborted");
									consistant1 = false ;
								} else {
									ITransition trans = algo.getCompletePN().getTransitionById(tr.getAction());
									if (trans == null){
										JOptionPane.showMessageDialog(this, "Warning, this traces include game actions not\n"
												+ "included into the full Petri nets\n\nLoading aborted");
										consistant = false;
									}
								}

								// mémorisation des traces avec tous les attributs :
								listeTracePourAnalyse.add(tr);
								// pour affichage, on ajoute le nom du fullPn et l'origine (player ou system)
								listeNomActionsPourAnalyse.addElement(tr.getAction() + " ("+ tr.getPnName() + ") ("+ tr.getOrigin()+ ")");
							}
							if ((!consistant1) || (!consistant)) { 
								traceName = null;
								listeNomActionsPourAnalyse.removeAllElements();								
							}
							// boutonChargerTraces.setBackground(Color.CYAN);
							onglets.setEnabledAt(2, true);
						}
					} catch (Exception e6) {
						JOptionPane.showMessageDialog(this, "Error on loading traces file\n\n"+e6.getMessage());
						traceName = null;
						listeNomActionsPourAnalyse.removeAllElements();
					}
				}
				else
					JOptionPane.showMessageDialog(this, "Please, load a full Petri net with at least one transition");
			}
			loadingTraces = false;
		}

		else if (source == boutonSauvegarderTrace) {
			// onglet2 : sauvegarde de la trace
			// ITrace it ; 
			System.out.println("Save traces.");
			if (listeTracePourAnalyse.size() != 0) {
				// nouveau fichier de traces
				for (int k = 0; k < listeTracePourAnalyse.size(); k++) {
					// System.out.println("k : " + listeTracePourAnalyse.get(k));
					// le nettoyer : il ne faut pas de .pnml dans pnName
					// ITrace it = listeTracePourAnalyse.get(k) ;
					String nom = listeTracePourAnalyse.get(k).getPnName() ;
					int pos = nom.indexOf(".pnml");
					if (pos != -1) { // on l'enlève
						listeTracePourAnalyse.get(k).setPnName(nom.substring(0,pos));
					}
					// il ne pas le nom du full entre parenthèses, ni les mots player ou system
					String act = listeTracePourAnalyse.get(k).getAction();
					pos = act.indexOf(" (");
					if (pos != -1) {
						listeTracePourAnalyse.get(k).setAction(act.substring(0,pos));
					}
				}
				System.out.println("Resultat : ");
				for (int k = 0; k < listeTracePourAnalyse.size(); k++) {
					System.out.println(listeTracePourAnalyse.get(k));
				}
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

		else if (source == boutonAnalyserActions) {
			// onglet3 : lancement de l'analyse
			// vérifier que l'on a tout : s'il manque quelque chose, le dire
			if (pnName2labelingAlgo.size() <= 0 || listeTracePourAnalyse.size() == 0) {
				JOptionPane.showMessageDialog(this,
						"Full Petri nets or filtered Petri nets or specifications or traces not defined");
			} else {
				// Réinitialisation des algos
				for (ILabeling algo : pnName2labelingAlgo.values())
					try {
						algo.reset();
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				
				// nouveau fichier de traces : on vide l'ancien traces et on y transfère les traces	
				for (int i = 0 ; i < listeTracePourAnalyse.size(); i++) {
					// nettoyer les .pnml dans les noms des full des traces importées
					String nom = listeTracePourAnalyse.get(i).getPnName() ;
					int pos = nom.indexOf(".pnml");
					if (pos != -1) { // on l'enlève
						listeTracePourAnalyse.get(i).setPnName(nom.substring(0,pos));
					}
					// nettoyer (fullPn) (system/player) dans les traces importées
					String act = listeTracePourAnalyse.get(i).getAction();
					pos = act.indexOf(" (");
					if (pos != -1) {
						listeTracePourAnalyse.get(i).setAction(act.substring(0,pos));
					}
					// System.out.println("tr "+listeTracePourAnalyse.get(i));
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
						System.err.println("Unknown Petri net \""+trace.getPnName()+"\" to label \""+trace.getAction()+"\" action.");
					}
				}
				
				/* for (ITrace tr : tracesPourAnalyse.getTraces()) {
					System.out.println("tr "+tr);
				} */

				/*// Ancien traitement
				Logger monLog = Logger.getLogger(Main.class.getName());
				monLog.setLevel(Level.ALL); // pour envoyer les messages de tous les niveaux
				monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
				ConsoleHandler ch = new ConsoleHandler();
				ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau INFO
				ch.setFormatter(new MyFormater()); // define formater
				monLog.addHandler(ch);
				algo = new Labeling_V10(monLog, true);
				algo.setCompletePN(fullPn);
				algo.setFilteredPN(filteredPn);
				algo.setFeatures(features);
				try {
					algo.label(tracesPourAnalyse); 
				} catch (Exception e3) {
					e3.printStackTrace();
				}
				monLog.removeHandler(ch);*/

				// vidage des trois fenêtres
				listeActionsAnalysees.removeAllElements();
				listeLabels.removeAllElements();
				listeSynthese.removeAllElements();

				int nbLabels = 18;
				int[] effectif = new int[nbLabels];
				for (int k = 0; k < nbLabels; k++)
					effectif[k] = 0;
				String[] intitule = new String[nbLabels];
				intitule[0] = "useless";//"inutile";				
				intitule[1] = "non-optimal";//"non-optimale";
				intitule[2] = "correct";//"correcte";
				intitule[3] = "equivalent";//"equivalente";
				intitule[4] = "erroneous";//"erronee";
				intitule[5] = "intrusion";
				intitule[6] = "farther";//"eloignement";
				intitule[7] = "missing";//"manquante";
				intitule[8] = "unsynchronized";//"autre-branche-de-resolution";
				intitule[9] = "becoming-closer";//"rapprochement";
				intitule[10] = "recovery";//"rattrapage";
				intitule[11] = "leap-backward";//"retour_arriere";
				intitule[12] = "leap-forward";//"saut-avant";
				intitule[13] = "stagnation";
				intitule[14] = "too-late";//"trop-tard";
				intitule[15] = "too-early";//"trop-tot";
				intitule[16] = "already-seen";//"deja-vu";
				intitule[17] = "bad-choice";//"mauvais-choix";

				// calcul des effectifs de chaque label
				for (ITrace tr : tracesPourAnalyse.getTraces()) {
					// System.out.println("tr "+tr);
					// System.out.println("tr.getAction : " + tr.getAction());
					listeActionsAnalysees.addElement(features.getPublicName(tr.getAction()));
					ArrayList<String> labs = tr.getLabels(); // ERREUR : est vide
					// System.out.println("labs " + labs);
					listeLabels.addElement(labs);
					for (String lab : labs) {
						for (int k = 0; k < nbLabels; k++)
							if (lab == intitule[k]) {
								effectif[k] += 1;
								break;
							}
					}
				}

				// remplir l'analyse globale : traces and labels envoyés dans l'interface
				for (int k = 0; k < nbLabels; k++) {
					if (effectif[k] > 0) {
						ArrayList<String> ligne = new ArrayList<String>();
						ligne.add(intitule[k]);
						ligne.add(new Integer(effectif[k]).toString());
						listeSynthese.addElement(ligne);
					}
				}

				// construire le diagramme
				// boutonAnalyserActions.setBackground(Color.CYAN);

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

		else if (source == boutonExporterGraphml) {
			System.out.println("Export to Graphml format");
			if (listeActionsAnalysees.getSize() != 0) {
				String outputfile = "";
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier Courant
					chooser.setCurrentDirectory(new File(adresseGraphml + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Save (.graphml extension)");
					// Si l'utilisateur clique sur OK
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						outputfile = chooser.getSelectedFile().toString();
						if (outputfile.toLowerCase().endsWith(".graphml"))
							outputfile = outputfile.substring(0, outputfile.length() - 8); // remove
						// user
						// extension
						adresseGraphml = outputfile.substring(0, outputfile.lastIndexOf(File.separator));
						System.out.println("fichier : " + outputfile + ".graphml");
					}
					algo.export(outputfile + ".graphml");
				} catch (Exception he) {
					he.printStackTrace();
				}
				boutonExporterGraphml.setBackground(Color.CYAN);
			}
		}

		else if (source == boutonExporterLabels) {
			System.out.println("Export labels.");
			if (listeLabels.getSize() != 0) {
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
				boutonExporterLabels.setBackground(Color.CYAN);
			}
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (panel_fullPnSelection != null){
			panel_fullPnSelection.setPreferredSize(new Dimension(this.getWidth()-40, panel_fullPnSelection.getHeight()));
			/*String txt = "<html>Folder selected: <b>"+adresseReseauComplet+"</b></html>";
			infoFullFolderSelected.setText(txt);
			infoFullFolderSelected.validate();
			int size = txt.length()-4;
			while (infoFullFolderSelected.getAlignmentX()+infoFullFolderSelected.getWidth() > this.getWidth()-40){
				txt = txt.substring(0, size)+"...";
				infoFullFolderSelected.setText(txt);
				infoFullFolderSelected.validate();
			}*/
		}
		if (panel_filteredPnSelection != null) panel_filteredPnSelection.setPreferredSize(new Dimension(this.getWidth()-40, panel_filteredPnSelection.getHeight()));
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
}
