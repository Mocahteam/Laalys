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
	String basePathNet = "exemples";

	// les répertoires par défaut
	String fullPnPath = basePathNet+File.separator+"completeNets";
	String fullPnPathForFiltering = basePathNet+File.separator+"completeNets";
	String filteredPnPath = basePathNet+File.separator+"filteredNets";
	String featuresPath = basePathNet+File.separator+"features";
	String tracesPath = basePathNet+File.separator+"trace";
	String labelsPath = basePathNet+File.separator+"trace-labellisee";
	String graphmlPath = basePathNet+File.separator+"trace-graphml";

    private javax.swing.JTabbedPane jTabbedPane;
    
	// UI for the first tab
    private javax.swing.JPanel tab_pnFoldersSelection;
    // Full Pn selection
    private javax.swing.JPanel pan_fullPnFolder;
    private javax.swing.JButton bt_fullPnFolder;
    private javax.swing.JLabel lab_fullPnFolder;
    // Filtered Pn selection
    private javax.swing.JPanel pan_filteredPnFolder;
    private javax.swing.JPanel pan_graphProperties;
    private javax.swing.ButtonGroup grp_graphProperties;
    private javax.swing.JRadioButton opt_coverability;
    private javax.swing.JRadioButton opt_accessibility;
    private javax.swing.JPanel pan_analysisStrategy;
	private javax.swing.ButtonGroup grp_analysisStrategy;
    private javax.swing.JRadioButton opt_first;
    private javax.swing.JRadioButton opt_all;
    private javax.swing.JButton bt_filteredPnFolder;
    private javax.swing.JLabel lab_filteredPnFolder;
    // Features selection
    private javax.swing.JPanel pan_specificationsFolder;
    private javax.swing.JButton bt_specificationsFolder;
    private javax.swing.JLabel lab_specificationsFolder;
    // loading button
    private javax.swing.JButton bt_loadPnAndSpecif;
    private javax.swing.JCheckBox cb_enableDebug;
    
    // UI for the second tab
    private javax.swing.JPanel tab_tracesManagement;
    // Full Pn filter
    private javax.swing.JPanel pan_fullPnSelection;
    private javax.swing.JLabel lab_fullPnSelection;
    public javax.swing.JComboBox<String> combo_fullPnSelection;
    private javax.swing.JScrollPane scrollPan_fullPnActions;
    private DefaultListModel<Serializable> list_fullPnActions;
    // Drag&Drop options
    private javax.swing.JPanel pan_dragDropOptions;
    private javax.swing.JLabel lab_dragDropInfo;
    private javax.swing.ButtonGroup grp_playerSystem;
    public javax.swing.JRadioButton opt_player;
    public javax.swing.JRadioButton opt_system;
    // Traces building
    private javax.swing.JPanel pan_tracesBuilding;
    private javax.swing.JButton bt_loadTracesFromFile;
    private javax.swing.JScrollPane scrollPan_tracesActions;
    private DefaultListModel<Serializable> list_tracesActions;
    private javax.swing.JButton bt_saveTraces;
    
    // UI for the third tab
    private javax.swing.JPanel tab_analysis;
    private javax.swing.JButton bt_launchAnalysis;
    private javax.swing.JPanel pan_analysisColumns;
    // First column
    private javax.swing.JPanel pan_actionsAnalysed;
    private javax.swing.JScrollPane scrollPan_analysedActions;
	private DefaultListModel<Serializable> list_analysedActions;
    private javax.swing.JButton bt_exportGraphml;
    // Second column
    private javax.swing.JPanel pan_labelsComputed;
    private javax.swing.JScrollPane scrollPan_labelsComputed;
    private DefaultListModel<Serializable> list_labelsComputed;
    private javax.swing.JButton bt_exportLabels;
    // Third column
    private javax.swing.JPanel pan_synthesis;
    private javax.swing.JScrollPane scrollPan_synthesis;
    private DefaultListModel<Serializable> list_synthesis;
    
    // UI for the fourth tab
    private javax.swing.JPanel tab_filteredPnManagement;
    // Full Pn selection
    private javax.swing.JPanel pan_fullPnFile;
    private javax.swing.JButton bt_fullPnFile;
    private javax.swing.JLabel lab_fullPnFile;
    // Expert trace selection
    private javax.swing.JPanel pan_expertTraceFile;
    private javax.swing.JButton bt_expertTraceFile;
    private javax.swing.JLabel lab_expertTraceFile;
    // Build filtered Petri net
    private javax.swing.JButton bt_BuildFilteredPn;
    
	public HashMap<String, ILabeling> pnName2labelingAlgo = new HashMap<>();
	private ArrayList<ITrace> list_tracesForAnalysis;
	
	
	//DefaultListModel<Serializable> listeActionContent, listeNomActionsPourAnalyse;
	String type = CoverabilityGraph.TYPE, strategy = CoverabilityGraph.STRATEGY_OR; // par défaut
	
	PieChart cv;
	DefaultPieDataset dataset;

	boolean loadingTraces = false;
	//////////////////////////////////////////////////////////////////

	public InterfaceLaalys() {
		super("Laalys");

		// Vérifier les chemins
		if (!new File(fullPnPath).exists())
			fullPnPath = ".";
		if (!new File(fullPnPathForFiltering).exists())
			fullPnPathForFiltering = ".";
		if (!new File(filteredPnPath).exists())
			filteredPnPath = ".";
		if (!new File(featuresPath).exists())
			featuresPath = ".";
		if (!new File(tracesPath).exists())
			tracesPath = ".";
		if (!new File(labelsPath).exists())
			labelsPath = ".";
		if (!new File(graphmlPath).exists())
			graphmlPath = ".";

		grp_graphProperties = new javax.swing.ButtonGroup();
        grp_analysisStrategy = new javax.swing.ButtonGroup();
        grp_playerSystem = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        tab_pnFoldersSelection = new javax.swing.JPanel();
        pan_fullPnFolder = new javax.swing.JPanel();
        bt_fullPnFolder = new javax.swing.JButton();
        lab_fullPnFolder = new javax.swing.JLabel();
        pan_filteredPnFolder = new javax.swing.JPanel();
        pan_graphProperties = new javax.swing.JPanel();
        opt_coverability = new javax.swing.JRadioButton();
        opt_accessibility = new javax.swing.JRadioButton();
        pan_analysisStrategy = new javax.swing.JPanel();
        opt_first = new javax.swing.JRadioButton();
        opt_all = new javax.swing.JRadioButton();
        bt_filteredPnFolder = new javax.swing.JButton();
        lab_filteredPnFolder = new javax.swing.JLabel();
        pan_specificationsFolder = new javax.swing.JPanel();
        bt_specificationsFolder = new javax.swing.JButton();
        lab_specificationsFolder = new javax.swing.JLabel();
        bt_loadPnAndSpecif = new javax.swing.JButton();
        cb_enableDebug = new javax.swing.JCheckBox();
        tab_tracesManagement = new javax.swing.JPanel();
        pan_fullPnSelection = new javax.swing.JPanel();
        lab_fullPnSelection = new javax.swing.JLabel();
        combo_fullPnSelection = new javax.swing.JComboBox<>();
        scrollPan_fullPnActions = new javax.swing.JScrollPane();
        list_fullPnActions = new DefaultListModel<Serializable>();
        pan_dragDropOptions = new javax.swing.JPanel();
        lab_dragDropInfo = new javax.swing.JLabel();
        opt_player = new javax.swing.JRadioButton();
        opt_system = new javax.swing.JRadioButton();
        pan_tracesBuilding = new javax.swing.JPanel();
        bt_loadTracesFromFile = new javax.swing.JButton();
        scrollPan_tracesActions = new javax.swing.JScrollPane();
        list_tracesActions = new DefaultListModel<Serializable>();
        bt_saveTraces = new javax.swing.JButton();
        tab_analysis = new javax.swing.JPanel();
        tab_filteredPnManagement = new javax.swing.JPanel();
        list_tracesForAnalysis = new ArrayList<ITrace>();
        bt_launchAnalysis = new javax.swing.JButton();
        pan_analysisColumns = new javax.swing.JPanel();
        pan_actionsAnalysed = new javax.swing.JPanel();
        scrollPan_analysedActions = new javax.swing.JScrollPane();
    	list_analysedActions = new DefaultListModel<Serializable>();
        bt_exportGraphml = new javax.swing.JButton();
        pan_labelsComputed = new javax.swing.JPanel();
        scrollPan_labelsComputed = new javax.swing.JScrollPane();
        bt_exportLabels = new javax.swing.JButton();
        list_labelsComputed = new DefaultListModel<Serializable>();
        pan_synthesis = new javax.swing.JPanel();
        scrollPan_synthesis = new javax.swing.JScrollPane();
        list_synthesis = new DefaultListModel<Serializable>();
        pan_fullPnFile = new javax.swing.JPanel();
        bt_fullPnFile = new javax.swing.JButton();
        lab_fullPnFile = new javax.swing.JLabel();
        pan_expertTraceFile = new javax.swing.JPanel();
        bt_expertTraceFile = new javax.swing.JButton();
        lab_expertTraceFile = new javax.swing.JLabel();
        bt_BuildFilteredPn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        this.setMinimumSize(new java.awt.Dimension(550, 410));
        
        //////////////////////////////////////
        // Tab 1: Petri nets and features selection
        jTabbedPane.addTab("Petri nets selection", tab_pnFoldersSelection);
        
        //---------- First Bloc: Select Full Pn folder ----------
        pan_fullPnFolder.setBorder(javax.swing.BorderFactory.createTitledBorder("Full Petri nets selection"));
        pan_fullPnFolder.setMinimumSize(new java.awt.Dimension(400, 100));
        bt_fullPnFolder.setText("Select folder");
        bt_fullPnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FullPnSelectionActionPerformed(evt);
            }
        });
        lab_fullPnFolder.setText("No folder selected");
        lab_fullPnFolder.setForeground(Color.RED);
        lab_fullPnFolder.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FullPnSelectionLayout = new javax.swing.GroupLayout(pan_fullPnFolder);
        pan_fullPnFolder.setLayout(pan_FullPnSelectionLayout);
        pan_FullPnSelectionLayout.setHorizontalGroup(
            pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelectionLayout.createSequentialGroup()
                .addComponent(bt_fullPnFolder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_fullPnFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_FullPnSelectionLayout.setVerticalGroup(
            pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelectionLayout.createSequentialGroup()
                .addGroup(pan_FullPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_fullPnFolder)
                    .addComponent(lab_fullPnFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        // ---------- Second Bloc: Select Filtered Pn folder ----------
        pan_filteredPnFolder.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtered Petri nets selection"));
        pan_filteredPnFolder.setMinimumSize(new java.awt.Dimension(400, 100));
        pan_graphProperties.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph properties"));
        grp_graphProperties.add(opt_coverability);
        opt_coverability.setSelected(true);
        opt_coverability.setText("Coverability");
        grp_graphProperties.add(opt_accessibility);
        opt_accessibility.setText("Accessibility");
        javax.swing.GroupLayout pan_GraphPropertiesLayout = new javax.swing.GroupLayout(pan_graphProperties);
        pan_graphProperties.setLayout(pan_GraphPropertiesLayout);
        pan_GraphPropertiesLayout.setHorizontalGroup(
            pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_GraphPropertiesLayout.createSequentialGroup()
                .addGroup(pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opt_coverability)
                    .addComponent(opt_accessibility))
                .addGap(0, 32, Short.MAX_VALUE))
        );
        pan_GraphPropertiesLayout.setVerticalGroup(
            pan_GraphPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_GraphPropertiesLayout.createSequentialGroup()
                .addComponent(opt_coverability)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_accessibility))
        );
        opt_coverability.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        opt_accessibility.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        pan_analysisStrategy.setBorder(javax.swing.BorderFactory.createTitledBorder("Analysis strategy"));
        grp_analysisStrategy.add(opt_first);
        opt_first.setSelected(true);
        opt_first.setText("FIRST");
        grp_analysisStrategy.add(opt_all);
        opt_all.setText("ALL");
        javax.swing.GroupLayout pan_AnalysisStrategyLayout = new javax.swing.GroupLayout(pan_analysisStrategy);
        pan_analysisStrategy.setLayout(pan_AnalysisStrategyLayout);
        pan_AnalysisStrategyLayout.setHorizontalGroup(
            pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_AnalysisStrategyLayout.createSequentialGroup()
                .addGroup(pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opt_first)
                    .addComponent(opt_all))
                .addGap(0, 63, Short.MAX_VALUE))
        );
        pan_AnalysisStrategyLayout.setVerticalGroup(
            pan_AnalysisStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_AnalysisStrategyLayout.createSequentialGroup()
                .addComponent(opt_first)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_all))
        );
        opt_first.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        opt_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	optActionPerformed(evt);
            }
        });
        bt_filteredPnFolder.setText("Select folder");
        bt_filteredPnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_FilteredPnSelectionActionPerformed(evt);
            }
        });
        lab_filteredPnFolder.setText("No folder selected");
        lab_filteredPnFolder.setForeground(Color.RED);
        lab_filteredPnFolder.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FilteredPnSelectionLayout = new javax.swing.GroupLayout(pan_filteredPnFolder);
        pan_filteredPnFolder.setLayout(pan_FilteredPnSelectionLayout);
        pan_FilteredPnSelectionLayout.setHorizontalGroup(
            pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addComponent(pan_graphProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pan_analysisStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addComponent(bt_filteredPnFolder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_filteredPnFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
        );
        pan_FilteredPnSelectionLayout.setVerticalGroup(
            pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pan_FilteredPnSelectionLayout.createSequentialGroup()
                .addGroup(pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pan_graphProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pan_analysisStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pan_FilteredPnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_filteredPnFolder)
                    .addComponent(lab_filteredPnFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

		// ---------- Third Bloc: Select Specifications folder ----------
        pan_specificationsFolder.setBorder(javax.swing.BorderFactory.createTitledBorder("Petri nets specifications selection"));
        pan_specificationsFolder.setMinimumSize(new java.awt.Dimension(400, 100));
        bt_specificationsFolder.setText("Select folder");
        bt_specificationsFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_SpecificationSelectionActionPerformed(evt);
            }
        });
        lab_specificationsFolder.setText("No folder selected");
        lab_specificationsFolder.setForeground(Color.RED);
        lab_specificationsFolder.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_SpecificationsSelectionLayout = new javax.swing.GroupLayout(pan_specificationsFolder);
        pan_specificationsFolder.setLayout(pan_SpecificationsSelectionLayout);
        pan_SpecificationsSelectionLayout.setHorizontalGroup(
            pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SpecificationsSelectionLayout.createSequentialGroup()
                .addComponent(bt_specificationsFolder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_specificationsFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_SpecificationsSelectionLayout.setVerticalGroup(
            pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SpecificationsSelectionLayout.createSequentialGroup()
                .addGroup(pan_SpecificationsSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_specificationsFolder)
                    .addComponent(lab_specificationsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        
		// ---------- Fourth Bloc: loading button ----------
        bt_loadPnAndSpecif.setText("Load Petri nets and specifications");
        bt_loadPnAndSpecif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_LoadPnAndSpecifActionPerformed(evt);
            }
        });
        cb_enableDebug.setText("Enable debug");
        
        // ---------- Add blocs to the first tab ----------
        javax.swing.GroupLayout tab_PnSelectionLayout = new javax.swing.GroupLayout(tab_pnFoldersSelection);
        tab_pnFoldersSelection.setLayout(tab_PnSelectionLayout);
        tab_PnSelectionLayout.setHorizontalGroup(
            tab_PnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pan_fullPnFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pan_filteredPnFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pan_specificationsFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cb_enableDebug)
            .addComponent(bt_loadPnAndSpecif)
        );
        tab_PnSelectionLayout.setVerticalGroup(
            tab_PnSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_PnSelectionLayout.createSequentialGroup()
                .addComponent(pan_fullPnFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_filteredPnFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_specificationsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(cb_enableDebug)
                .addComponent(bt_loadPnAndSpecif))
        );
        
		//////////////////////////////////////
		// Tab 2: Traces management
        jTabbedPane.addTab("Traces management", tab_tracesManagement);
        tab_tracesManagement.setLayout(new java.awt.GridLayout(1, 3));
        
        // ---------- first column ----------
        lab_fullPnSelection.setText("Full Petri net filter");
        lab_fullPnSelection.setMinimumSize(new java.awt.Dimension(50, 14));
        combo_fullPnSelection.setMinimumSize(new java.awt.Dimension(50, 20));
        combo_fullPnSelection.addItemListener(this);
        JList<Serializable> listeActionsConteneur = new JList<Serializable>(list_fullPnActions);
        listeActionsConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Available game actions"));
		listeActionsConteneur.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listeActionsConteneur.setDragEnabled(true);
		listeActionsConteneur.setMinimumSize(new java.awt.Dimension(50, 103));
		scrollPan_fullPnActions.setViewportView(listeActionsConteneur);

        javax.swing.GroupLayout pan_FullPnFilterLayout = new javax.swing.GroupLayout(pan_fullPnSelection);
        pan_fullPnSelection.setLayout(pan_FullPnFilterLayout);
        pan_FullPnFilterLayout.setHorizontalGroup(
            pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combo_fullPnSelection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lab_fullPnSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                    .addComponent(scrollPan_fullPnActions, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pan_FullPnFilterLayout.setVerticalGroup(
            pan_FullPnFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lab_fullPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(combo_fullPnSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPan_fullPnActions, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addContainerGap())
        );

        tab_tracesManagement.add(pan_fullPnSelection);

        // ---------- second column ----------
        lab_dragDropInfo.setText("<html><center>Drag and drop game actions from the left panel to the right one to complete manually traces.<br/>Select below the simulated game action source (player or system)</center></html>");
        lab_dragDropInfo.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        grp_playerSystem.add(opt_player);
        opt_player.setText(ActionType.PLAYER);
        grp_playerSystem.add(opt_system);
        opt_system.setText(ActionType.SYSTEM);
        opt_player.setSelected(true);

        javax.swing.GroupLayout pan_dragDropOptionsLayout = new javax.swing.GroupLayout(pan_dragDropOptions);
        pan_dragDropOptions.setLayout(pan_dragDropOptionsLayout);
        pan_dragDropOptionsLayout.setHorizontalGroup(
            pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_dragDropOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(opt_player, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(opt_system, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lab_dragDropInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        pan_dragDropOptionsLayout.setVerticalGroup(
            pan_dragDropOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_dragDropOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lab_dragDropInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(opt_player)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt_system)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        tab_tracesManagement.add(pan_dragDropOptions);

        // ---------- third column ----------
        bt_loadTracesFromFile.setText("Load traces from files");
        bt_loadTracesFromFile.setMinimumSize(new java.awt.Dimension(50, 23));
        bt_loadTracesFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_loadTracesFromFileActionPerformed(evt);
            }
        });
		
		list_tracesActions.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				System.out.println("Remove : "+e.getIndex0()+" "+e.getIndex1());
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex1() ; i >= e.getIndex0() ; i--)
						list_tracesForAnalysis.remove(i);
				}
				if (list_tracesActions.isEmpty())
					enableOngletAnalyse(false);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				System.out.println("Add : "+e.getIndex0()+" "+e.getIndex1());
				// si on n'est pas en cours de chargement de la trace, on maintient synchronisé les deux listes
				if (!loadingTraces){
					for (int i = e.getIndex0() ; i <= e.getIndex1() ; i++){
						String action = list_tracesActions.getElementAt(i).toString();
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
						list_tracesForAnalysis.add(i, nouvelletrace);
					}
				}
				enableOngletAnalyse(true);
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				System.out.println("Change : "+e.getIndex0()+" "+e.getIndex1());
			}
		});

		JList<Serializable> listeNomActionsPourAnalyseConteneur = new JList<Serializable>(list_tracesActions);
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
						list_tracesActions.remove(listeNomActionsPourAnalyseConteneur.getSelectedIndices()[i]);
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		scrollPan_tracesActions.setViewportView(listeNomActionsPourAnalyseConteneur);

        bt_saveTraces.setText("Save traces");
        bt_saveTraces.setMinimumSize(new java.awt.Dimension(50, 23));
        bt_saveTraces.addActionListener(new java.awt.event.ActionListener() {
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
                    .addComponent(bt_saveTraces, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(bt_saveTraces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        tab_tracesManagement.add(pan_tracesBuilding);

		//////////////////////////////////////
		// Tab 3: Analysis
        jTabbedPane.addTab("Analysis", tab_analysis);

        bt_launchAnalysis.setText("Launch analysis");
        bt_launchAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	bt_LaunchAnalysisActionPerformed(evt);
            }
        });

        pan_analysisColumns.setLayout(new java.awt.GridLayout(1, 3, 10, 0));

        // ---------- first column ----------
        JList<Serializable> listeActionsAnalyseesConteneur = new JList<Serializable>(list_analysedActions);
        listeActionsAnalyseesConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Actions analysed"));
        listeActionsAnalyseesConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_analysedActions.setViewportView(listeActionsAnalyseesConteneur);

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
                .addComponent(scrollPan_analysedActions))
        );
        pan_actionsAnalysedLayout.setVerticalGroup(
            pan_actionsAnalysedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_actionsAnalysedLayout.createSequentialGroup()
                .addComponent(scrollPan_analysedActions, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_exportGraphml))
        );

        pan_analysisColumns.add(pan_actionsAnalysed);

        // ---------- second column ----------
        JList<Serializable> listeLabelsConteneur = new JList<Serializable>(list_labelsComputed);
        listeLabelsConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Labels computed"));
        listeLabelsConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_labelsComputed.setViewportView(listeLabelsConteneur);

        bt_exportLabels.setText("Export labels");
        bt_exportLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_exportLabelsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pan_LabelsComputedLayout = new javax.swing.GroupLayout(pan_labelsComputed);
        pan_labelsComputed.setLayout(pan_LabelsComputedLayout);
        pan_LabelsComputedLayout.setHorizontalGroup(
            pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(bt_exportLabels)
                .addComponent(scrollPan_labelsComputed))
        );
        pan_LabelsComputedLayout.setVerticalGroup(
            pan_LabelsComputedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_LabelsComputedLayout.createSequentialGroup()
                .addComponent(scrollPan_labelsComputed, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_exportLabels))
        );

        pan_analysisColumns.add(pan_labelsComputed);

        // ---------- third column ----------
        JList<Serializable> listeSyntheseConteneur = new JList<Serializable>(list_synthesis);
        listeSyntheseConteneur.setBorder(javax.swing.BorderFactory.createTitledBorder("Synthesis"));
        listeSyntheseConteneur.setMinimumSize(new java.awt.Dimension(50, 50));
        scrollPan_synthesis.setViewportView(listeSyntheseConteneur);

        javax.swing.GroupLayout pan_SynthesisLayout = new javax.swing.GroupLayout(pan_synthesis);
        pan_synthesis.setLayout(pan_SynthesisLayout);
        pan_SynthesisLayout.setHorizontalGroup(
            pan_SynthesisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPan_synthesis, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
        );
        pan_SynthesisLayout.setVerticalGroup(
            pan_SynthesisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_SynthesisLayout.createSequentialGroup()
                .addComponent(scrollPan_synthesis, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

        pan_analysisColumns.add(pan_synthesis);

        javax.swing.GroupLayout tab_AnalysisLayout = new javax.swing.GroupLayout(tab_analysis);
        tab_analysis.setLayout(tab_AnalysisLayout);
        tab_AnalysisLayout.setHorizontalGroup(
            tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(bt_launchAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(pan_analysisColumns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tab_AnalysisLayout.setVerticalGroup(
            tab_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_AnalysisLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bt_launchAnalysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pan_analysisColumns, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
        );

		//////////////////////////////////////
		// Tab 4: Filtered nets management
        jTabbedPane.addTab("Filtered nets management", tab_filteredPnManagement);
        // ---------- first bloc ----------
        pan_fullPnFile.setBorder(javax.swing.BorderFactory.createTitledBorder("Full Petri net selection"));
        pan_fullPnFile.setMinimumSize(new java.awt.Dimension(50, 100));
        bt_fullPnFile.setText("Select file");
        bt_fullPnFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FullPnSelection2ActionPerformed(evt);
            }
        });
        lab_fullPnFile.setText("No file selected");
        lab_fullPnFile.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_FullPnSelection2Layout = new javax.swing.GroupLayout(pan_fullPnFile);
        pan_fullPnFile.setLayout(pan_FullPnSelection2Layout);
        pan_FullPnSelection2Layout.setHorizontalGroup(
            pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelection2Layout.createSequentialGroup()
                .addComponent(bt_fullPnFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_fullPnFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pan_FullPnSelection2Layout.setVerticalGroup(
            pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_FullPnSelection2Layout.createSequentialGroup()
                .addGroup(pan_FullPnSelection2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_fullPnFile)
                    .addComponent(lab_fullPnFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        // ---------- second bloc ----------
        pan_expertTraceFile.setBorder(javax.swing.BorderFactory.createTitledBorder("Expert trace selection"));
        pan_expertTraceFile.setMinimumSize(new java.awt.Dimension(50, 100));
        bt_expertTraceFile.setText("Select file");
        bt_expertTraceFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_ExpertTraceSelectionActionPerformed(evt);
            }
        });
        lab_expertTraceFile.setText("No file selected");
        lab_expertTraceFile.setMinimumSize(new java.awt.Dimension(270, 14));
        javax.swing.GroupLayout pan_ExpertTraceSelectionLayout = new javax.swing.GroupLayout(pan_expertTraceFile);
        pan_expertTraceFile.setLayout(pan_ExpertTraceSelectionLayout);
        pan_ExpertTraceSelectionLayout.setHorizontalGroup(
            pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_ExpertTraceSelectionLayout.createSequentialGroup()
                .addComponent(bt_expertTraceFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lab_expertTraceFile, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
        );
        pan_ExpertTraceSelectionLayout.setVerticalGroup(
            pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pan_ExpertTraceSelectionLayout.createSequentialGroup()
                .addGroup(pan_ExpertTraceSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_expertTraceFile)
                    .addComponent(lab_expertTraceFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        // ---------- buil button ----------
        bt_BuildFilteredPn.setText("Build filtered Petri net");
        bt_BuildFilteredPn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_BuildFilteredPnActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout tab_FilteredPnManagementLayout = new javax.swing.GroupLayout(tab_filteredPnManagement);
        tab_filteredPnManagement.setLayout(tab_FilteredPnManagementLayout);
        tab_FilteredPnManagementLayout.setHorizontalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(pan_expertTraceFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bt_BuildFilteredPn))
            .addComponent(pan_fullPnFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        tab_FilteredPnManagementLayout.setVerticalGroup(
            tab_FilteredPnManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab_FilteredPnManagementLayout.createSequentialGroup()
                .addComponent(pan_fullPnFile, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(pan_expertTraceFile, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
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
		String folderName = new SelectionDossier().getNomDossier(fullPnPath, this);
		if (!folderName.isEmpty() && !folderName.equals(fullPnPath)){
			fullPnPath = folderName;
			System.out.println("dossier choisi : " + fullPnPath);
			lab_fullPnFolder.setText("Folder selected: "+fullPnPath);
			lab_fullPnFolder.setForeground(Color.BLACK);
			lab_fullPnFolder.setToolTipText(fullPnPath);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    } 
	
    private void bt_FilteredPnSelectionActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	System.out.println("Choisir un dossier de réseaux filtrés");
		String folderName = new SelectionDossier().getNomDossier(filteredPnPath, this);
		if (!folderName.isEmpty() && !folderName.equals(filteredPnPath)){
			filteredPnPath = folderName;
			System.out.println("dossier choisi : " + filteredPnPath);
			lab_filteredPnFolder.setText("Folder selected: "+filteredPnPath);
			lab_filteredPnFolder.setForeground(Color.BLACK);
			lab_filteredPnFolder.setToolTipText(filteredPnPath);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    }   
	
    private void optActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    	Object source = evt.getSource();

		// les 4 boutons de configuration
		if (source == opt_coverability) {
			System.out.println("couverture");
			type = CoverabilityGraph.TYPE;
			opt_first.setEnabled(true);
			opt_all.setEnabled(true);
		} else if (source == opt_accessibility) {
			System.out.println("accessibilité");
			type = AccessibleGraph.TYPE;
			// interdire le bouton1b
			opt_all.setEnabled(false);
			// activer le bouton1a automatiquement
			opt_first.setSelected(true);
			strategy = CoverabilityGraph.STRATEGY_OR;
		} else if (source == opt_first) {
			System.out.println("stratégie FIRST, en fait, OU");
			strategy = CoverabilityGraph.STRATEGY_OR;
		} else if (source == opt_all) {
			System.out.println("stratégie ALL, en fait, ET");
			strategy = CoverabilityGraph.STRATEGY_AND;
		}
    }                                           

    private void bt_SpecificationSelectionActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Choisir un dossier de caractéristiques");
		String folderName = new SelectionDossier().getNomDossier(featuresPath, this);
		if (!folderName.isEmpty() && !folderName.equals(featuresPath)){
			featuresPath = folderName;
			System.out.println("dossier choisi : " + featuresPath);
			lab_specificationsFolder.setText("Folder selected: "+featuresPath);
			lab_specificationsFolder.setForeground(Color.BLACK);
			lab_specificationsFolder.setToolTipText(featuresPath);
			enableOngletAnalyse(false);
			enableOngletTraces(false);
		}
    }                                           

    private void bt_loadTracesFromFileActionPerformed(java.awt.event.ActionEvent evt) {
    	System.out.println("Loading traces file.");
		loadingTraces = true;
		try {
			String fileName = new SelectionFichier().getNomFichier(tracesPath, this);
			if (!fileName.isEmpty()){
				tracesPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
				list_tracesActions.removeAllElements();
				list_tracesForAnalysis = new ArrayList<ITrace>();
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
					list_tracesForAnalysis.add(tr);
					// pour affichage, on ajoute le nom du fullPn et l'origine (player ou system)
					list_tracesActions.addElement(tr.getAction() + " ("+ tr.getPnName() + ") ("+ tr.getOrigin()+ ")");
				}
				if (!consistant) {
					list_tracesActions.removeAllElements();
					list_tracesForAnalysis = new ArrayList<ITrace>();
				}
				enableOngletAnalyse(true);
			}
		} catch (Exception e6) {
			JOptionPane.showMessageDialog(this, "Error on loading traces file\n\n"+e6.getMessage());
			list_tracesActions.removeAllElements();
			list_tracesForAnalysis = new ArrayList<ITrace>();
		}
		loadingTraces = false;
    }                                         

    private void bt_LoadPnAndSpecifActionPerformed(java.awt.event.ActionEvent evt) {
    	// onglet1 : charge tous les fichiers des trois dossiers
		System.out.println(fullPnPath);
		System.out.println(filteredPnPath);
		System.out.println(featuresPath);

		Logger monLog = Logger.getLogger(Main.class.getName());
		monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
		monLog.setUseParentHandlers(false); // pour supprimer la console par défaut
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau INFO
		monLog.addHandler(ch);
		File fullDir = new File(fullPnPath);
		pnName2labelingAlgo.clear();
		for (File fullChild : fullDir.listFiles()){
			// get equivalent file in filtered folder
			File filteredDir = new File(filteredPnPath);
			File filteredChild = new File(filteredDir, fullChild.getName());
			// get equivalent file in features folder
			File featuresDir = new File(featuresPath);
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
			IPetriNet filteredPn = new PetriNet(true, type, strategy);
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
			ILabeling algo = new Labeling_V10(monLog, cb_enableDebug.isSelected());
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
		if (list_tracesForAnalysis.size() > 0) {
			ITraces itraces = new Traces();
			itraces.setTraces(list_tracesForAnalysis);
			Document doc = itraces.toXML();
			// enregistrement du nouveau fichier de traces
			// choix du fichier
			String filename = "";
			try {
				JFileChooser chooser = new JFileChooser();
				// Dossier Courant
				chooser.setCurrentDirectory(new File(tracesPath + File.separator));
				// Affichage et récupération de la réponse de l'utilisateur
				int reponse = chooser.showDialog(chooser, "Save (.xml extension)");
				// Si l'utilisateur clique sur OK
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					filename = chooser.getSelectedFile().toString();
					if (filename.toLowerCase().endsWith(".xml"))
						filename = filename.substring(0, filename.length() - 4); // remove user extension
					tracesPath = filename.substring(0, filename.lastIndexOf(File.separator));
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
		if (pnName2labelingAlgo.size() <= 0 || list_tracesForAnalysis.size() == 0) {
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
			tracesPourAnalyse.setTraces(list_tracesForAnalysis);				
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
			list_analysedActions.removeAllElements();
			list_labelsComputed.removeAllElements();
			list_synthesis.removeAllElements();

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
				list_analysedActions.addElement(features.getPublicName(tr.getAction()));
				ArrayList<String> labs = tr.getLabels();
				list_labelsComputed.addElement(labs);
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
					list_synthesis.addElement(ligne);
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
		if (list_analysedActions.getSize() != 0) {
			String outputfolder = "";
			try {
				JFileChooser chooser = new JFileChooser(new File(graphmlPath + File.separator).getCanonicalFile());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				// Affichage et récupération de la réponse de l'utilisateur
				int reponse = chooser.showSaveDialog(null);
				// Si l'utilisateur clique sur OK
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					outputfolder = chooser.getSelectedFile().toString();
					// Identification dans les traces des Rdp utilisés
					ArrayList<String> pnUsed = new ArrayList<String>();
					for (ITrace t : list_tracesForAnalysis){
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
		if (list_labelsComputed.getSize() != 0) {
			String outputfile = "";
			try {
				JFileChooser chooser = new JFileChooser();
				// Dossier Courant
				chooser.setCurrentDirectory(new File(labelsPath + File.separator));
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
					labelsPath = outputfile.substring(0, outputfile.lastIndexOf(File.separator));
					System.out.println("file: " + outputfile + ".xml");
				}
				// contenu à écrire récupération de value1 complété par les
				// labels
				ITraces itraces = new Traces();
				itraces.setTraces(list_tracesForAnalysis);
				for (int k = 0; k < list_tracesForAnalysis.size(); k++)
					System.out.println("pos " + k + " : " + list_tracesForAnalysis.get(k));
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
		String fullPath = new SelectionFichier().getNomFichier(fullPnPathForFiltering, this);
		if (!fullPath.isEmpty()){
			fullPnPathForFiltering = fullPath;
			System.out.println("fichier de réseau choisi : " + fullPnPathForFiltering);
			lab_fullPnFile.setText("File selected: "+fullPnPathForFiltering);
			lab_fullPnFile.setToolTipText(fullPnPathForFiltering);
		}
    } 

    private void bt_ExpertTraceSelectionActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Selection d'une trace experte.");
		String fullPath = new SelectionFichier().getNomFichier(tracesPath, this);
		if (!fullPath.isEmpty()){
			tracesPath = fullPath;
			System.out.println("fichier de réseau choisi : " + tracesPath);
			lab_expertTraceFile.setText("File selected: "+tracesPath);
			lab_expertTraceFile.setToolTipText(tracesPath);
		}
    }

    private void bt_BuildFilteredPnActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			// Load full Pn
			System.out.println("Chargement du Rdp complet");
			IPetriNet fullPn = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			fullPn.loadPetriNet(fullPnPathForFiltering);
			try {
				// Load traces
				System.out.println("Chargement des traces");
				ITraces traces_expert = new Traces();
				traces_expert.loadFile(tracesPath);
				
				System.out.println("Généreration du réseau filtré");
				// pour l'onglet 0
				fullPn.filterXMLWith(traces_expert);

				String filename_new = "";
				// choisir le nom du réseau filtré
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier de réseaux filtrés
					chooser.setCurrentDirectory(new File(filteredPnPath + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Save (.pnml extension)");
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						filename_new = chooser.getSelectedFile().toString();
						if (filename_new.toLowerCase().endsWith(".pnml"))
							filename_new = filename_new.substring(0, filename_new.length() - 5); // remove user extension
						filteredPnPath = filename_new.substring(0, filename_new.lastIndexOf(File.separator));
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
			list_fullPnActions.clear();
			list_tracesActions.clear();
			for (HashMap.Entry<String,ILabeling> elem : pnName2labelingAlgo.entrySet()){
				combo_fullPnSelection.addItem(elem.getKey());			
			}
			combo_fullPnSelection.setSelectedIndex(0);
		}
	}

	private void enableOngletAnalyse(boolean state){
		jTabbedPane.setEnabledAt(2, state);
		if (state){
			list_analysedActions.clear();
			list_labelsComputed.clear();
			list_synthesis.clear();
		}
	}

	//////////////////////////////////////////////////////////////////

	public void itemStateChanged(ItemEvent e) {
		// attaché au combo
		// nom du full choisi
		String fullPnName = (String)combo_fullPnSelection.getSelectedItem();
		if (pnName2labelingAlgo.containsKey(fullPnName)){
			list_fullPnActions.clear();
			// on rempli la liste des actions incluses dans ce Rdp
			IPetriNet fullPn = pnName2labelingAlgo.get(fullPnName).getCompletePN();
			if (fullPn != null){
				for (ITransition tr : fullPn.getTransitions()) {
					System.out.println(tr.getName());
					list_fullPnActions.addElement(tr.getName());
				}
			} else {
				JOptionPane.showMessageDialog(this, "Error, No full Petri net loaded for \""+fullPnName+"\".");
			}
		} else {
			JOptionPane.showMessageDialog(this, "Error, Unknown full Petri net named \""+fullPnName+"\".");
		}
	}
}
