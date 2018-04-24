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
import java.util.logging.Level;
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

	boolean loadingTraces = false;
	//////////////////////////////////////////////////////////////////

	public InterfaceLaalys() {
		super("Laalys");
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
		onglets.addTab("Type de graphe et chargement des réseaux", ongletReseaux);
		// Création du panneau principal
		JPanel mainPanelReseaux = new JPanel();
		mainPanelReseaux.setLayout(new GridLayout(1, 3));
		
		// -------- 1ère colonne pour gérer le Rdp Complet --------
		JPanel pannelColonneRdpComplet = new JPanel();
		pannelColonneRdpComplet.setLayout(new GridLayout(11, 1));
		// Label : Chargement du réseau complet sans graphe
		JPanel tmpPanel = new JPanel();
		JLabel tmpLabel = new JLabel("<html><b>Chargement du réseau complet</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Charger un Rdp complet
		tmpPanel = new JPanel();
		boutonRdpComplet = new JButton("Charger un Rdp complet");
		boutonRdpComplet.addActionListener(this);
		tmpPanel.add(boutonRdpComplet);
		pannelColonneRdpComplet.add(tmpPanel);
		// Info chargement Rdp Complet
		tmpPanel = new JPanel();
		infoRdpComplet = new JLabel(new String());
		infoRdpComplet.setFont(font1);
		infoRdpComplet.setText("<html><center>Aucun réseau complet chargé<br>&nbsp;</center></html>");
		tmpPanel.add(infoRdpComplet);
		pannelColonneRdpComplet.add(tmpPanel);
		// Espaces vide
		tmpPanel = new JPanel();
		pannelColonneRdpComplet.add(tmpPanel);
		// Label : option facultative
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>OPTION FACULTATIVE : créer un réseau filtré</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Choisir trace experte
		tmpPanel = new JPanel();
		boutonTraceExperte = new JButton("a. Choisir une trace experte");
		boutonTraceExperte.addActionListener(this);
		boutonTraceExperte.setEnabled(false);
		tmpPanel.add(boutonTraceExperte);
		pannelColonneRdpComplet.add(tmpPanel);
		// Info chargement trace experte
		tmpPanel = new JPanel();
		infoTracesExpertes = new JLabel(new String());
		infoTracesExpertes.setFont(font1);
		infoTracesExpertes.setText("<html><center>Aucune trace experte choisie<br>&nbsp;</center></html>");
		tmpPanel.add(infoTracesExpertes);
		pannelColonneRdpComplet.add(tmpPanel);
		// Bouton : Générer Rdp Filtré
		tmpPanel = new JPanel();
		boutonGenererRdpFiltre = new JButton("b. Générer le réseau filtré");
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
		tmpLabel = new JLabel("<html><b>1. Choix du réseau filtré</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Bouton : Sélectionner un Rdp Filtré
		tmpPanel = new JPanel();
		boutonSelectionnerRdpFiltre = new JButton("Sélectionner un Rdp filtré");
		boutonSelectionnerRdpFiltre.addActionListener(this);
		boutonSelectionnerRdpFiltre.setEnabled(false);
		tmpPanel.add(boutonSelectionnerRdpFiltre);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Info chargement Rdp filtré
		tmpPanel = new JPanel();
		infoRdpFiltre = new JLabel(new String());
		infoRdpFiltre.setFont(font1);
		infoRdpFiltre.setText("<html><center>Aucun réseau filtré sélectionné<br>&nbsp;</center></html>");
		tmpPanel.add(infoRdpFiltre);
		pannelColonneRdpFiltre.add(tmpPanel);
		// espace vide
		tmpPanel = new JPanel();
		pannelColonneRdpFiltre.add(tmpPanel);
		// Label : Type de graphe
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>2. Type de graphe</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Boutons radio Couverture/Accessibilité
		tmpPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		radioCouverture = new JRadioButton("Couverture", true);
		radioCouverture.setFont(font1);
		radioCouverture.addActionListener(this);
		radioCouverture.setEnabled(false);
		group.add(radioCouverture);
		tmpPanel.add(radioCouverture);
		radioAccessibilite = new JRadioButton("Accessibilité");
		radioAccessibilite.setFont(font1);
		radioAccessibilite.addActionListener(this);
		radioAccessibilite.setEnabled(false);
		group.add(radioAccessibilite);
		tmpPanel.add(radioAccessibilite);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Label : Stratégie d'analyse
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<HTML><b>Stratégie d'analyse</b></html>");
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
		// Label : Chargement Rdp Filtré
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><b>3. Chargement du réseau filtré</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneRdpFiltre.add(tmpPanel);
		// Bouton : Charger Rdp filtré
		tmpPanel = new JPanel();
		boutonChargerRdpFiltre = new JButton("Charger le Rdp filtré sélectionné");
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
		tmpLabel = new JLabel("<html><b>Caractéristiques du réseau de Pétri</b></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelColonneSpecificites.add(tmpPanel);
		// Bouton : Charger les caractéristiques
		tmpPanel = new JPanel();
		boutonChargerCaracteristiques = new JButton("Charger des caractéristiques");
		boutonChargerCaracteristiques.addActionListener(this);
		boutonChargerCaracteristiques.setEnabled(false);
		tmpPanel.add(boutonChargerCaracteristiques);
		pannelColonneSpecificites.add(tmpPanel);
		// Info caractéristiques
		tmpPanel = new JPanel();
		infoCarateristiques = new JLabel(new String());
		infoCarateristiques.setFont(font1);
		infoCarateristiques.setText("<html><center>Aucune caractéristique chargée<br>&nbsp;</center></html>");
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
		onglets.addTab("Chargement des traces", ongletTraces);

		// ---------- Création de panneau supérieur -----------
		JPanel pannelTracesNorth = new JPanel();
		pannelTracesNorth.setLayout(new GridLayout(4, 1));
		// ligne 1 : Explication construction trace
		tmpPanel = new JPanel();
		tmpLabel = new JLabel(
				"<html><center>Pour <b>construire</b> ou <b>modifier</b> une trace, choisissez"
				+ " dans l'ordre souhaité les actions de la liste de gauche et faites-les glisser"
				+ " dans la fenêtre de droite. <br/>Pour <b>supprimer</b> un élément de trace,"
				+ " choisissez l'action dans la liste de droite et cliquez sur la touche SUPPR"
				+ " du clavier.</center></html>");
		tmpLabel.setFont(font1);
		// ligne 2 : OU
		tmpPanel.add(tmpLabel);
		pannelTracesNorth.add(tmpPanel);
		tmpPanel = new JPanel();
		tmpLabel = new JLabel(
				"<html><center>OU</center></html>");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelTracesNorth.add(tmpPanel);
		// ligne 3 : Charger un fichier de trace
		tmpPanel = new JPanel();
		boutonChargerTraces = new JButton("Charger un fichier de traces");
		boutonChargerTraces.addActionListener(this);
		tmpPanel.add(boutonChargerTraces);
		pannelTracesNorth.add(tmpPanel);
		// ligne 4 : Titre des colonnes
		JPanel titreColonnes = new JPanel();
		titreColonnes.setLayout(new GridLayout(1, 2));
		// Titre colonne gauche
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><center>Actions de jeu possibles</center></html>");
		tmpLabel.setFont(font1b);
		tmpPanel.add(tmpLabel);
		titreColonnes.add(tmpPanel);
		// Titre colonne droite
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("<html><center>Actions à analyser</center></html>");
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
		boutonSauvegarderTrace = new JButton("Sauvegarder la trace");
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
		onglets.addTab("Analyse", ongletAnalyse);

		// ---------- Création de panneau supérieur -----------
		JPanel pannelAnalyseNorth = new JPanel();
		pannelAnalyseNorth.setLayout(new GridLayout(2, 3));
		// 1ère ligne : vide / bouton / vide
		// espace vide
		tmpPanel = new JPanel();
		pannelAnalyseNorth.add(tmpPanel);
		// Bouton : Analyser actions
		tmpPanel = new JPanel();
		boutonAnalyserActions = new JButton("Lancer l'analyse");
		boutonAnalyserActions.addActionListener(this);
		tmpPanel.add(boutonAnalyserActions);
		pannelAnalyseNorth.add(tmpPanel);
		// espace vide
		tmpPanel = new JPanel();
		pannelAnalyseNorth.add(tmpPanel);
		// 2ème ligne : Titres colonnes
		// Label : Actions analysées
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("Actions analysées");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelAnalyseNorth.add(tmpPanel);
		// Label : Labels identifiés
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("Labels identifiés");
		tmpLabel.setFont(font1);
		tmpPanel.add(tmpLabel);
		pannelAnalyseNorth.add(tmpPanel);
		// Label : Compte-rendu d'analyse
		tmpPanel = new JPanel();
		tmpLabel = new JLabel("Synthèse de l'analyse");
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
		boutonExporterGraphml = new JButton("Exporter au format Graphml");
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
		boutonExporterLabels = new JButton("Exporter les labels");
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
					infoRdpComplet.setText("<html><center>Aucun réseau complet chargé<br>&nbsp;</center></html>");
					boutonTraceExperte.setBackground(UIManager.getColor("Bouton.background"));
					infoTracesExpertes.setText("<html><center>Aucune trace experte choisie<br>&nbsp;</center></html>");
					boutonGenererRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
					boutonGenererRdpFiltre.setEnabled(false);
					
					boutonSelectionnerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
					infoRdpFiltre.setText("<html><center>Aucun réseau filtré sélectionné<br>&nbsp;</center></html>");
					toggleFilteredFields(false);
					
					boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
					infoCarateristiques.setText("<html><center>Aucune caractéristique chargée<br>&nbsp;</center></html>");
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
						infoRdpComplet.setText("<html><center>Réseau complet chargé :<br>" + nomfich + "</center></html>");
						boutonRdpComplet.setBackground(Color.CYAN);
						success = true;
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(this, "Une erreur est survenue lors du chargement du Rdp complet\n\nErreur: "+e1.getMessage());
						boutonRdpComplet.setBackground(UIManager.getColor("Bouton.background"));
						infoRdpComplet.setText("<html><center>Aucun réseau complet chargé<br>&nbsp;</center></html>");
						listeActionContent.clear();
						fullPnName = null;
						fullPn = null;
					}
					boutonTraceExperte.setEnabled(success);
					boutonSelectionnerRdpFiltre.setEnabled(success);
					boutonChargerCaracteristiques.setEnabled(success);
				}
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, "Une erreur est survenue\n\nErreur: "+e3.getMessage());
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
				JOptionPane.showMessageDialog(this, "Une erreur est survenue lors du chargement du Rdp complet\nOperation avortée\n\nErreur : "+e1.getMessage());
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
						String nomfich2 = traceName2.substring(index2 + 1);
						// System.out.println("fichier de trace expert choisi : " +
						// traceName2);
						infoTracesExpertes.setText("<html><center>Trace experte choisi :<br>" + nomfich2 + "</center></html>");
						traces_expert = new Traces();
						traces_expert.loadFile(traceName2);
						System.out.println("--------------------traces_expert-------------------------");
						System.out.println(traces_expert.toString());
						System.out.println("---------------------------------------------");
						boutonTraceExperte.setBackground(Color.CYAN);
						boutonGenererRdpFiltre.setEnabled(true);
					}
				} catch (Exception e4) {
					JOptionPane.showMessageDialog(this, "Une erreur est survenue lors du chargement du fichier\n\nErreur : "+e4.getMessage());
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
				int reponse = chooser.showDialog(chooser, "Enregistrer (extension .pnml)");
				if (reponse == JFileChooser.APPROVE_OPTION) {
					// Récupération du chemin du fichier et de son nom
					filename_new = chooser.getSelectedFile().toString();
					if (filename_new.toLowerCase().endsWith(".pnml"))
						filename_new = filename_new.substring(0, filename_new.length() - 5); // remove user extension
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
					JOptionPane.showMessageDialog(this, "Enregistrement terminé");
				} catch (Exception e5) {
					System.out.println("Erreur d'enregistrement : "+e5.getMessage());
				}
			}
		}

		else if (source == boutonSelectionnerRdpFiltre) {
			System.out.println("Choisir un réseau de Petri filtré.");
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseReseauFiltre, this);
				if (!fileName.isEmpty()){
					filteredPnName  = fileName; 
					int index = filteredPnName.lastIndexOf(File.separator);
					String nomfich = filteredPnName.substring(index + 1);
					infoRdpFiltre.setText("<html><center>Réseau filtré sélectionné :<br>" + nomfich + "</center></html>");
					toggleFilteredFields(true);
					radioCouverture.setSelected(true);
					radioFirst.setSelected(true);
					enableOngletTraces(false);
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "Une erreur est survenue lors du chargement du fichier\n\nErreur : "+e5.getMessage());
			}
		}

		else if (source == boutonChargerRdpFiltre) {
			System.out.println("Charger un réseau de Petri filtré.");
			if ((filteredPnName == null) || (filteredPnName.isEmpty())) {
				JOptionPane.showMessageDialog(this,
						"Veuillez d'abord sélectionner le Réseau de Pétri filtré à charger");
			} else {
				boutonChargerRdpFiltre.setBackground(UIManager.getColor("Bouton.background"));
				if (filteredPnName != null && !filteredPnName.isEmpty()) { // test peut-être inutile
					// charger le fichier par IPetriNet - loadPetriNet()
					System.out.print("fichier de réseau filtré choisi : " + filteredPnName);
					System.out.print(" - " + type);
					System.out.println(" - stratégie : " + strategie);
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
							JOptionPane.showMessageDialog(this, "Ce Rdp filtré contient des transitions non incluses dans le Rdp complet\n\nChargement avorté");
							toggleFilteredFields(false);
							enableOngletTraces(false);
							infoRdpFiltre.setText("<html><center>Aucun réseau filtré sélectionné<br>&nbsp;</center></html>");
						}
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(this, "Echec lors du chargement du Rdp Filtré\n\n"+e2.getMessage());
						toggleFilteredFields(false);
						enableOngletTraces(false);
						infoRdpFiltre.setText("<html><center>Aucun réseau filtré sélectionné<br>&nbsp;</center></html>");
					}
				}
			}
		}

		else if (source == boutonChargerCaracteristiques) {
			System.out.println("Charger les caractéristiques du réseau de Pétri.");
			features = new Features();
			try {
				sf = new SelectionFichier();
				String fileName = sf.getNomFichier(adresseSpec, this);
				if (!fileName.isEmpty()){
					featuresName = fileName;
					// charger le fichier
					System.out.println("fichier de caractéristiques choisi : " + featuresName);
					features.loadFile(featuresName);
					// Vérifier s'il y a au moins une action de fin
					if (!features.getEndLevelTransitions().isEmpty()){
						boutonChargerCaracteristiques.setBackground(Color.CYAN);
						int index = fullPnName.lastIndexOf(File.separator);
						String nomfich = fullPnName.substring(index + 1);
						infoCarateristiques.setText("<html><center>Caractéristiques chargées :<br>" + nomfich + "</center></html>");
						// si le Rdp filtré est aussi chargée, on peut dévérouiller les traces
						if (filteredPn != null)
							enableOngletTraces(true);
					} else{
						JOptionPane.showMessageDialog(this, "Fichier de caractéristiques non compatible.\n\n"
								+ "Au moins une tansition de fin doit être définie");
						featuresName = null;
						boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
						infoCarateristiques.setText("<html><center>Aucune caractéristique chargée<br>&nbsp;</center></html>");
						enableOngletTraces(false);
					}
				}
			} catch (Exception e5) {
				JOptionPane.showMessageDialog(this, "Echec lors du chargement des caractéristiques\n\n"+e5.getMessage());
				featuresName = null;
				boutonChargerCaracteristiques.setBackground(UIManager.getColor("Bouton.background"));
				infoCarateristiques.setText("<html><center>Aucune caractéristique chargée<br>&nbsp;</center></html>");
				enableOngletTraces(false);
			}
		}

		else if (source == boutonChargerTraces) {
			// on arrive au deuxième onglet
			System.out.println("Charger un fichier de traces.");
			loadingTraces = true;
			// on vérifie qu'un fichier de réseau complet a été chargé
			if (fullPn == null) {
				JOptionPane.showMessageDialog(this, "Veuillez charger un Rdp complet");
			} else {
				if (!listeActionContent.isEmpty()) {
					try {
						sf = new SelectionFichier();
						String fileName = sf.getNomFichier(adresseTrace, this);
						if (!fileName.isEmpty()){
							traceName = fileName;
							listeNomActionsPourAnalyse.removeAllElements();
							listeTracePourAnalyse = new ArrayList<ITrace>();
							System.out.println("fichier de traces choisi : " + traceName);
							ITraces tracesToLoad = new Traces();
							tracesToLoad.loadFile(traceName);
							boolean consistant = true;
							// ICI copie_traces = new Traces();
							// on parcourt les traces chargées
							for (ITrace tr : tracesToLoad.getTraces()) { 
								// pour affichage du nom de l'action seulement
								listeNomActionsPourAnalyse.addElement(tr.getAction());
								if (!listeActionContent.contains(tr.getAction()))
									consistant = false;
								// mémorisation des traces avec tous les attributs :
								listeTracePourAnalyse.add(tr);
							}
							loadingTraces = false;
							if (!consistant){
								JOptionPane.showMessageDialog(this, "Attention, ce fichier de trace contient des actions non\n"
										+ "incluses dans celles définies par le réseau de Petri complet\n\nChargement avorté");
								traceName = null;
								listeNomActionsPourAnalyse.removeAllElements();
							} else
								onglets.setEnabledAt(2, true);
						}
					} catch (Exception e6) {
						JOptionPane.showMessageDialog(this, "Echec lors du chargement du fichier de traces\n\n"+e6.getMessage());
						traceName = null;
						listeNomActionsPourAnalyse.removeAllElements();
					}
				}
				else
					JOptionPane.showMessageDialog(this, "Veuillez charger un Rdp complet avec au moins une transition");
			}
			loadingTraces = false;
		}

		else if (source == boutonSauvegarderTrace) {
			System.out.println("Enregistrer la trace.");
			if (listeTracePourAnalyse.size() != 0) {
				// nouveau fichier de traces
				ITraces itraces = new Traces();
				itraces.setTraces(listeTracePourAnalyse);
				for (int k = 0; k < listeTracePourAnalyse.size(); k++)
					System.out.println("en pos " + k + " : " + listeTracePourAnalyse.get(k));
				Document doc = itraces.toXML();
				// enregistrement du nouveau fichier de traces
				// choix du fichier
				String filename = "";
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier Courant
					chooser.setCurrentDirectory(new File(adresseTrace + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Enregistrer (extension .xml)");
					// Si l'utilisateur clique sur OK
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						filename = chooser.getSelectedFile().toString();
						if (filename.toLowerCase().endsWith(".xml"))
							filename = filename.substring(0, filename.length() - 4); // remove user extension
						// System.out.println("fichier : " + filename+".xml");
						// enregistrement proprement dit
						Transformer transformer;
						Result output;
						try {
							transformer = TransformerFactory.newInstance().newTransformer();
							output = new StreamResult(new File(filename + ".xml"));
							Source input = new DOMSource(doc);
							transformer.transform(input, output);
						} catch (Exception e5) {
							JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de la trace\n\n"+e5.getMessage());
						}
					}
				} catch (HeadlessException he) {
					he.printStackTrace();
				}
			} else
				JOptionPane.showMessageDialog(this, "La liste des traces est vide, veuillez construire ou charger une trace avant de la sauvegarder");
		}

		else if (source == boutonAnalyserActions) {

			// vérifier que l'on a tout : s'il manque quelque chose, le dire
			if (fullPn == null || filteredPn == null || features == null || listeTracePourAnalyse.size() == 0) {
				JOptionPane.showMessageDialog(this,
						"Réseau complet ou réseau filtré ou caractéristiques ou traces non défini");
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

				// vidage des trois fenêtres
				listeActionsAnalysees.removeAllElements();
				listeLabels.removeAllElements();
				listeSynthese.removeAllElements();

				int nbLabels = 18;
				int[] effectif = new int[nbLabels];
				for (int k = 0; k < nbLabels; k++)
					effectif[k] = 0;
				String[] intitule = new String[nbLabels];
				intitule[0] = "autre-branche-de-resolution";
				intitule[1] = "correcte";
				intitule[2] = "eloignement";
				intitule[3] = "equivalente";
				intitule[4] = "erronee";
				intitule[5] = "intrusion";
				intitule[6] = "inutile";
				intitule[7] = "manquante";
				intitule[8] = "non-optimale";
				intitule[9] = "rapprochement";
				intitule[10] = "rattapage";
				intitule[11] = "retour_arriere";
				intitule[12] = "saut-avant";
				intitule[13] = "stagnation";
				intitule[14] = "trop-tard";
				intitule[15] = "trop-tot";
				intitule[16] = "deja-vu";
				intitule[17] = "mauvais-choix";

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
				cv = new PieChart("Résultats de l'analyse", "", dataset);
				cv.setPreferredSize(new Dimension(500, 270));
				cv.pack();
				cv.setVisible(true);
			}
		}

		else if (source == boutonExporterGraphml) {
			System.out.println("Exporter au format Graphml.");
			if (listeActionsAnalysees.getSize() != 0) {
				String outputfile = "";
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier Courant
					chooser.setCurrentDirectory(new File(adresseGraphml + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Enregistrer (extension .graphml)");
					// Si l'utilisateur clique sur OK
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						outputfile = chooser.getSelectedFile().toString();
						if (outputfile.toLowerCase().endsWith(".graphml"))
							outputfile = outputfile.substring(0, outputfile.length() - 8); // remove
																							// user
																							// extension
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
			System.out.println("Exporter les labels.");
			if (listeLabels.getSize() != 0) {
				String outputfile = "";
				try {
					JFileChooser chooser = new JFileChooser();
					// Dossier Courant
					chooser.setCurrentDirectory(new File(adresseLabel + File.separator));
					// Affichage et récupération de la réponse de l'utilisateur
					int reponse = chooser.showDialog(chooser, "Enregistrer (extension .xml)");
					// Si l'utilisateur clique sur OK
					if (reponse == JFileChooser.APPROVE_OPTION) {
						// Récupération du chemin du fichier et de son nom
						outputfile = chooser.getSelectedFile().toString();
						if (outputfile.toLowerCase().endsWith(".xml"))
							outputfile = outputfile.substring(0, outputfile.length() - 4); // remove
																							// user
																							// extension
						System.out.println("fichier : " + outputfile + ".xml");
					}
					// contenu à écrire récupération de value1 complété par les
					// labels
					ITraces itraces = new Traces();
					itraces.setTraces(listeTracePourAnalyse);
					for (int k = 0; k < listeTracePourAnalyse.size(); k++)
						System.out.println("en pos " + k + " : " + listeTracePourAnalyse.get(k));
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
