import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
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
import fr.lip6.mocah.laalys.petrinet.AccessibleGraph;
import fr.lip6.mocah.laalys.petrinet.CoverabilityGraph;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Trace;
import fr.lip6.mocah.laalys.traces.Traces;

class InterfaceLaalys extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// répertoire de base pour le chargement des fichiers (réseau complet,
	// réseau filtré, transitions, traces)
	String adressereseau = "exemples";

	// les répertoires par défaut
	String adresseReseauComplet = adressereseau+File.separator+"completeNet";
	String adresseReseauFiltre = adressereseau+File.separator+"filteredNet";
	String adresseSpec = adressereseau+File.separator+"specifTransition";
	String adresseTrace = adressereseau+File.separator+"trace";
	String adresseLabel = adressereseau+File.separator+"trace-labellisee";
	String adresseGraphml = adressereseau+File.separator+"trace-graphml";

	// dimensions de la fenêtre
	int largeur = 1000, hauteur = 700;
	// taille des textes si non standard (standard : 12)
	int tailleTexte1 = 14;
	int tailleTexte2 = 16;
	JLabel infoRdpComplet, infoTracesExpertes, infoRdpFiltre, infoCarateristiques;
	JButton boutonRdpComplet, boutonTraceExperte, boutonGenererRdpFiltre, boutonSelectionnerRdpFiltre, boutonChargerRdpFiltre, boutonChargerCaracteristiques,
			boutonChargerTraces, boutonSauvegarderTrace;
	JButton boutonAnalyserActions, boutonExporterGraphml, boutonExporterLabels;
	// JButton again ;
	JRadioButton radioCouverture, radioAccessibilite, radioFirst, radioAll;
	DefaultListModel<Serializable> listeActionContent, listeNomActionsPourAnalyse, listeActionsAnalysees, listeLabels, listeSynthese;
	String choix = "", type = "accessibilité", strategie = "OU"; // par défaut
	JPanel aux4;
	int indice;
	String fullPnName;
	String filteredPnName;
	String featuresName;
	String traceName;
	IPetriNet fullPn, fullPn_travail, fullPnFiltered;
	IPetriNet filteredPn;
	IFeatures features;
	ILabeling algo;
	ITraces copie_traces, nouvelles_traces, traces2, traces_expert;
	ArrayList<ITrace> listeTracePourAnalyse;
	PieChart cv;
	DefaultPieDataset dataset;
	JTabbedPane onglets;

	private class MyFormater extends Formatter{
		@Override
		public String format(LogRecord record) {
			StringBuilder sb = new StringBuilder();
	        sb.append(record.getLevel()).append(':');
	        sb.append(record.getMessage()).append('\n');
	        return sb.toString();
		}
		
	}
	
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
		
		// fermeture application
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		addWindowListener(l);
		// taille et structure générale en onglets
		setSize(largeur, hauteur);
		JPanel mainPanel = new JPanel();
		onglets = new JTabbedPane(SwingConstants.TOP);
		// polices de caractères
		Font font1 = new Font("Arial", Font.PLAIN, tailleTexte1);
		Font font1b = new Font("Arial", Font.BOLD, tailleTexte1);

		//////////////////////////////////////////////////////////////////
		// onglet1 pour chargement des réseaux de Petri
		JPanel ongletReseaux = new JPanel();
		ongletReseaux.setPreferredSize(new Dimension(largeur, hauteur));
		// Ajout de cet onglet
		onglets.addTab("Petri nets management", ongletReseaux);
		// Création du panneau principal
		JPanel mainPanelReseaux = new JPanel();
		mainPanelReseaux.setLayout(new GridLayout(1, 3));
		
		// -------- 1ère colonne pour gérer le Rdp Complet --------
		JPanel pannelColonneRdpComplet = new JPanel();
		pannelColonneRdpComplet.setLayout(new GridLayout(11, 1));
		// Label : Chargement du réseau complet sans graphe
		JPanel tmpPanel = new JPanel();
		JLabel tmpLabel = new JLabel("<html><b>Full Petri net</b></html>");
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
		// Espaces vide
		tmpPanel = new JPanel();
		pannelColonneRdpComplet.add(tmpPanel);
		// Label : option facultative
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>OPTION: Build filtered Petri net</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Choisir trace experte
		tmpPanel = new JPanel();
		boutonTraceExperte = new JButton("a. Choose expert trace");
		boutonTraceExperte.addActionListener(this);
		boutonTraceExperte.setEnabled(false);
		tmpPanel.add(boutonTraceExperte);
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
		boutonGenererRdpFiltre = new JButton("b. Build filtered Petri net");
		boutonGenererRdpFiltre.addActionListener(this);
		boutonGenererRdpFiltre.setEnabled(false);
		tmpPanel.add(boutonGenererRdpFiltre);
		pannelColonneRdpComplet.add(tmpPanel);
		// Ajout de la première colonne au panneau global
		mainPanelReseaux.add(pannelColonneRdpComplet);

		// -------- 2ème colonne pour gérer le Rdp Filtré --------
		JPanel pannelColonneRdpFiltre = new JPanel();
		pannelColonneRdpFiltre.setLayout(new GridLayout(11, 1));
		// Label : Choix du réseau filtré
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>1. Choose filtered Petri net</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Bouton : Sélectionner un Rdp Filtré
		tmpPanel = new JPanel();
		boutonSelectionnerRdpFiltre = new JButton("Select filtered Petri net");
		boutonSelectionnerRdpFiltre.addActionListener(this);
		boutonSelectionnerRdpFiltre.setEnabled(false);
		tmpPanel.add(boutonSelectionnerRdpFiltre);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Info chargement Rdp filtré
		tmpPanel = new JPanel();
		infoRdpFiltre = new JLabel(new String());
		infoRdpFiltre.setFont(font1);
		infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
		tmpPanel.add(infoRdpFiltre);
		pannelColonneRdpFiltre.add(tmpPanel);
		// espace vide
		tmpPanel = new JPanel();
		pannelColonneRdpFiltre.add(tmpPanel);
		// Label : Type de graphe
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>2. Graph properties</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Boutons radio Couverture/Accessibilité
		tmpPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		radioCouverture = new JRadioButton("Coverability", true);
		radioCouverture.setFont(font1);
		radioCouverture.addActionListener(this);
		radioCouverture.setEnabled(false);
		group.add(radioCouverture);
		tmpPanel.add(radioCouverture);
		radioAccessibilite = new JRadioButton("Accessibility");
		radioAccessibilite.setFont(font1);
		radioAccessibilite.addActionListener(this);
		radioAccessibilite.setEnabled(false);
		group.add(radioAccessibilite);
		tmpPanel.add(radioAccessibilite);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Label : Stratégie d'analyse
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>Analysis strategy</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Boutons radio FIRST/ALL
		tmpPanel = new JPanel();
		group = new ButtonGroup();
		radioFirst = new JRadioButton("FIRST", true); // en fait, FIRST == OU
		radioFirst.setFont(font1);
		radioFirst.addActionListener(this);
		radioFirst.setEnabled(false);
		group.add(radioFirst);
		tmpPanel.add(radioFirst);
		radioAll = new JRadioButton("ALL", true); // en fait, ALL == ET
		radioAll.setFont(font1);
		radioAll.addActionListener(this);
		radioAll.setEnabled(false);
		group.add(radioAll);
		tmpPanel.add(radioAll);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Espace vide
		tmpPanel = new JPanel();
		pannelColonneRdpFiltre.add(tmpPanel);
		// Bouton : Charger Rdp filtré
		tmpPanel = new JPanel();
		boutonChargerRdpFiltre = new JButton("Load filtered Petri net");
		boutonChargerRdpFiltre.addActionListener(this);
		boutonChargerRdpFiltre.setEnabled(false);
		tmpPanel.add(boutonChargerRdpFiltre);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Ajout de la deuxième colonne au panneau principal
		mainPanelReseaux.add(pannelColonneRdpFiltre);

		// -------- 3ème colonne pour gérer les spécificités --------
		JPanel pannelColonneSpecificites = new JPanel();
		pannelColonneSpecificites.setLayout(new GridLayout(11, 1));
		// Label : Caractéristiques du réseau de Pétri
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>Petri net specifications</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneSpecificites.add(tmpPanel);
		// Bouton : Charger les caractéristiques
		tmpPanel = new JPanel();
		boutonChargerCaracteristiques = new JButton("Load specifications");
		boutonChargerCaracteristiques.addActionListener(this);
		boutonChargerCaracteristiques.setEnabled(false);
		tmpPanel.add(boutonChargerCaracteristiques);
		pannelColonneSpecificites.add(tmpPanel);
		// Info caractéristiques
		tmpPanel = new JPanel();
		infoCarateristiques = new JLabel(new String());
		infoCarateristiques.setFont(font1);
		infoCarateristiques.setText("<html><center>No specification loaded<br>&nbsp;</center></html>");
		tmpPanel.add(infoCarateristiques);
		pannelColonneSpecificites.add(tmpPanel);
		// Ajout de la troisième colonne au panneau principal
		mainPanelReseaux.add(pannelColonneSpecificites);
		
		// Ajout du panneau principal à son onglet
		ongletReseaux.add(mainPanelReseaux);

		//////////////////////////////////////////////////////////////////
		// onglet2 pour traces
		JPanel ongletTraces = new JPanel();
		ongletTraces.setPreferredSize(new Dimension(largeur, hauteur));
		ongletTraces.setLayout(new BorderLayout());
		// Ajout de cet onglet
		onglets.addTab("Traces management", ongletTraces);

		// ---------- Création de panneau supérieur -----------
		JPanel pannelTracesNorth = new JPanel();
		pannelTracesNorth.setLayout(new GridLayout(4, 1));
		// ligne 1 : Explication construction trace
		tmpPanel = new JPanel();
		tmpLabel = new JLabel(
				"<html><center>Build trace manually by drag and drop game actions from left panel"
				+ " to the right one. <br/>Press SUPPR key in the right panel to remove a game action");
		tmpLabel.setFont(font1);
		// ligne 2 : OU
		tmpPanel.add(tmpLabel);
		pannelTracesNorth.add(tmpPanel);
		tmpPanel = new JPanel();
		tmpLabel = new JLabel(
				"<html><center>OR</center></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelTracesNorth.add(tmpPanel);
		// ligne 3 : Charger un fichier de trace
		tmpPanel = new JPanel();
		boutonChargerTraces = new JButton("Load traces from file");
		boutonChargerTraces.addActionListener(this);
		tmpPanel.add(boutonChargerTraces);
		pannelTracesNorth.add(tmpPanel);
		// ligne 4 : Titre des colonnes
		JPanel titreColonnes = new JPanel();
		titreColonnes.setLayout(new GridLayout(1, 2));
		// Titre colonne gauche
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><center>Available game actions</center></html>");
		tmpLabel.setFont(font1b);
		tmpPanel.add(tmpLabel);
		titreColonnes.add(tmpPanel);
		// Titre colonne droite
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><center>Game actions for analysis</center></html>");
		tmpLabel.setFont(font1b);
		tmpPanel.add(tmpLabel);
		titreColonnes.add(tmpPanel);
		// Ajout des titres de colonne
		pannelTracesNorth.add(titreColonnes);
		// Ajout du panneau supérieur à l'onglet des traces 
		ongletTraces.add(pannelTracesNorth, BorderLayout.NORTH);
		
		// ---------- Création de panneau central -----------
		JPanel pannelTracesCenter = new JPanel();
		pannelTracesCenter.setLayout(new GridLayout(1, 2));
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
		scrollPane.setPreferredSize(new Dimension(400, 385));
		colonneGaucheTracesContent.add(scrollPane, BorderLayout.CENTER);
		colonneGaucheTraces.add("Center", colonneGaucheTracesContent);
		// Ajout de la colonne de gauche
		pannelTracesCenter.add(colonneGaucheTraces);
		
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
						ITrace nouvelletrace = new Trace(action, "manual", origine, false);
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
		scrollPane.setPreferredSize(new Dimension(400, 350));
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

		// Ajout de panneau principal à son onglet
		ongletTraces.add(pannelTracesCenter, BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////
		// onglet3 pour analyse
		JPanel ongletAnalyse = new JPanel();
		ongletAnalyse.setPreferredSize(new Dimension(largeur, hauteur));
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
		scrollPane.setPreferredSize(new Dimension(300, 470));
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
		scrollPane.setPreferredSize(new Dimension(300, 470));
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
		scrollPane.setPreferredSize(new Dimension(300, 470));
		colonneDroiteAnalyseContent.add(scrollPane);
		colonneDroiteAnalyse.add("Center", colonneDroiteAnalyseContent);
		// Ajout de la colonne de droite
		pannelAnalyseCenter.add(colonneDroiteAnalyse);
		
		// Ajout du panneau principal à son onglet
		ongletAnalyse.add("Center", pannelAnalyseCenter);
		enableOngletTraces(false);
		// Ajout des onglet au panneau principal
		mainPanel.add(onglets);
		// Ajout du panneau principal à this
		getContentPane().add(mainPanel);
		setVisible(true);
	}
	
	private void toggleFilteredFields(boolean state){
		radioCouverture.setEnabled(state);
		radioAccessibilite.setEnabled(state);
		radioFirst.setEnabled(state);
		radioAll.setEnabled(state);
		boutonChargerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
		boutonChargerRdpFiltre.setEnabled(state);
		if (!state){
			filteredPn = null;
			filteredPnName = null;
		}
	}
	
	private void enableOngletTraces(boolean state){
		onglets.setEnabledAt(1, state);
		if (!state){
			enableOngletAnalyse(false);
		} else {
			listeActionContent.clear();
			listeNomActionsPourAnalyse.clear();
			boutonChargerTraces.setBackground(UIManager.getColor("Bouton.background"));
			if (fullPn != null){
				for (ITransition tr : fullPn.getTransitions()) {
					listeActionContent.addElement(tr.getName());
				}
			}
		}
	}
	
	private void enableOngletAnalyse(boolean state){
		onglets.setEnabledAt(2, state);
		if (state){
			listeActionsAnalysees.clear();
			listeLabels.clear();
			listeSynthese.clear();
			boutonAnalyserActions.setBackground(UIManager.getColor("Bouton.background"));
			boutonExporterGraphml.setBackground(UIManager.getColor("Bouton.background"));
			boutonExporterLabels.setBackground(UIManager.getColor("Bouton.background"));
		}
	}

	//////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent e) {
		SelectionFichier sf;
		Object source = e.getSource();
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

			// on choisit le fichier à charger
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseReseauComplet, this);
				if (!fileName.isEmpty()){
					// vider tout
					boutonRdpComplet.setBackground(UIManager.getColor("Bouton.background"));
					infoRdpComplet.setText("<html><center>No full Petri net loaded<br>&nbsp;</center></html>");
					boutonTraceExperte.setBackground(UIManager.getColor("Bouton.background"));
					infoTracesExpertes.setText("<html><center>No expert traces selected<br>&nbsp;</center></html>");
					boutonGenererRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
					boutonGenererRdpFiltre.setEnabled(false);
					
					boutonSelectionnerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
					infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
					toggleFilteredFields(false);
					
					boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
					infoCarateristiques.setText("<html><center>No features loaded<br>&nbsp;</center></html>");
					featuresName = null;
					
					enableOngletTraces(false);
					
					// Chargement
					fullPnName = fileName;
					System.out.println("fichier de réseau choisi : " + fullPnName);
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
						infoRdpComplet.setText("<html><center>No full Petri net loaded<br>&nbsp;</center></html>");
						listeActionContent.clear();
						fullPnName = null;
						fullPn = null;
					}
					boutonTraceExperte.setEnabled(success);
					boutonSelectionnerRdpFiltre.setEnabled(success);
					boutonChargerCaracteristiques.setEnabled(success);
				}
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, "An error occurs\n\nError: "+e3.getMessage());
				fullPnName = null;
				fullPn = null;
			}
		}

		else if (source == boutonTraceExperte) {
			System.out.println("Choisir la trace");
			// on recharge le même réseau complet
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
						boutonTraceExperte.setBackground(Color.CYAN);
						boutonGenererRdpFiltre.setEnabled(true);
					}
				} catch (Exception e4) {
					JOptionPane.showMessageDialog(this, "An error occurs on loading file\n\nError : "+e4.getMessage());
				}
		}

		else if (source == boutonGenererRdpFiltre) {
			System.out.println("Générer le réseau filtré");
			
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

		else if (source == boutonSelectionnerRdpFiltre) {
			System.out.println("Select a filtered Petri net.");
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
					enableOngletTraces(false);
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "An error occurs on loading the file\n\nError : "+e5.getMessage());
			}
		}

		else if (source == boutonChargerRdpFiltre) {
			System.out.println("Loading filtered Petri net.");
			if ((filteredPnName == null) || (filteredPnName.isEmpty())) {
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
							enableOngletTraces(false);
							infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
						}
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(this, "Loading filtered Petri net fail\n\n"+e2.getMessage());
						toggleFilteredFields(false);
						enableOngletTraces(false);
						infoRdpFiltre.setText("<html><center>No filtered Petri net selected<br>&nbsp;</center></html>");
					}
				}
			}
		}

		else if (source == boutonChargerCaracteristiques) {
			System.out.println("Loading specifications.");
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
						int index = fullPnName.lastIndexOf(File.separator);
						String nomfich = fullPnName.substring(index + 1);
						infoCarateristiques.setText("<html><center>Specifications selected:<br>" + nomfich + "</center></html>");
						// si le Rdp filtré est aussi chargée, on peut dévérouiller les traces
						if (filteredPn != null)
							enableOngletTraces(true);
					} else{
						JOptionPane.showMessageDialog(this, "Incomplet specifications.\n\n"
								+ "No end transition defined");
						featuresName = null;
						boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
						infoCarateristiques.setText("<html><center>No specifications loaded<br>&nbsp;</center></html>");
						enableOngletTraces(false);
					}
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "Error on loading specifications\n\n"+e5.getMessage());
				featuresName = null;
				boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
				infoCarateristiques.setText("<html><center>No specifications loaded<br>&nbsp;</center></html>");
				enableOngletTraces(false);
			}
		}

		else if (source == boutonChargerTraces) {
			// on arrive au deuxième onglet
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
							boolean consistant = true;
							// ICI copie_traces = new Traces();
							// on parcourt les traces chargées
							for (ITrace tr : tracesToLoad.getTraces()) { 
								// pour affichage du nom de l'action seulement
								listeNomActionsPourAnalyse.addElement(tr.getAction());
								if (!listeActionContent.contains(tr.getAction())){
									System.out.println("The trace \""+tr.getAction()+"\" is not included into liste of available actions.");
									consistant = false;
								}
								// mémorisation des traces avec tous les attributs :
								listeTracePourAnalyse.add(tr);
							}
							loadingTraces = false;
							if (!consistant){
								JOptionPane.showMessageDialog(this, "Warning, this traces include game actions not\n"
										+ "included into the full Petri net\n\nLoading aborted");
								traceName = null;
								listeNomActionsPourAnalyse.removeAllElements();
							} else
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
			System.out.println("Save traces.");
			if (listeTracePourAnalyse.size() != 0) {
				// nouveau fichier de traces
				ITraces itraces = new Traces();
				itraces.setTraces(listeTracePourAnalyse);
				for (int k = 0; k < listeTracePourAnalyse.size(); k++)
					System.out.println("pos " + k + ": " + listeTracePourAnalyse.get(k));
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
							JOptionPane.showMessageDialog(this, "Savong OK");
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

			// vérifier que l'on a tout : s'il manque quelque chose, le dire
			if (fullPn == null || filteredPn == null || features == null || listeTracePourAnalyse.size() == 0) {
				JOptionPane.showMessageDialog(this,
						"Full Petri net or filtered Petri net or specifications or traces not defined");
			} else {
				// nouveau fichier de traces : on vide l'ancien traces et on
				// transfère met à jour les traces
				ITraces tracesPourAnalyse = new Traces();
				tracesPourAnalyse.setTraces(listeTracePourAnalyse);

				// le traitement
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
				monLog.removeHandler(ch);

				// vidage des trois fenêtres
				listeActionsAnalysees.removeAllElements();
				listeLabels.removeAllElements();
				listeSynthese.removeAllElements();

				int nbLabels = 18;
				int[] effectif = new int[nbLabels];
				for (int k = 0; k < nbLabels; k++)
					effectif[k] = 0;
				String[] intitule = new String[nbLabels];
				intitule[0] = "unsynchronized";//"autre-branche-de-resolution";
				intitule[1] = "correct";//"correcte";
				intitule[2] = "farther";//"eloignement";
				intitule[3] = "equivalent";//"equivalente";
				intitule[4] = "erroneous";//"erronee";
				intitule[5] = "intrusion";
				intitule[6] = "useless";//"inutile";
				intitule[7] = "missing";//"manquante";
				intitule[8] = "non-optimal";//"non-optimale";
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
					listeActionsAnalysees.addElement(features.getPublicName(tr.getAction()));
					ArrayList<String> labs = tr.getLabels();
					listeLabels.addElement(labs);
					for (String lab : labs) {
						for (int k = 0; k < nbLabels; k++)
							if (lab == intitule[k]) {
								effectif[k] += 1;
								break;
							}
					}
				}

				// remplir l'analyse globale : traces and labels envoyés
				// dans l'interface
				for (int k = 0; k < nbLabels; k++) {
					if (effectif[k] > 0) {
						ArrayList<String> ligne = new ArrayList<String>();
						ligne.add(intitule[k]);
						ligne.add(new Integer(effectif[k]).toString());
						listeSynthese.addElement(ligne);
					}
				}

				// construire le diagramme
				boutonAnalyserActions.setBackground(Color.CYAN);

				// on crée d'abord le dataset en éliminant les deja-vu et
				// mauvais-choix
				dataset = new DefaultPieDataset();
				for (int k = 0; k < nbLabels - 2; k++)
					if (effectif[k] > 0)
						dataset.setValue(intitule[k], effectif[k]);

				// ensuite le PieChart qui fait tout le reste
				cv = new PieChart("Analysis results", "", dataset);
				cv.setPreferredSize(new Dimension(500, 270));
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
}
