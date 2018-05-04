import java.awt.Dimension; 
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.*; 
import java.awt.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.w3c.dom.Document;

import fr.lip6.mocah.laalys.features.Features;
import fr.lip6.mocah.laalys.features.IFeatures;
import fr.lip6.mocah.laalys.petrinet.AccessibleGraph;
import fr.lip6.mocah.laalys.petrinet.CoverabilityGraph;
import fr.lip6.mocah.laalys.petrinet.IArc;
import fr.lip6.mocah.laalys.petrinet.IPetriNet;
import fr.lip6.mocah.laalys.petrinet.IPlaceInfo;
import fr.lip6.mocah.laalys.petrinet.ITransition;
import fr.lip6.mocah.laalys.petrinet.PetriNet;
import fr.lip6.mocah.laalys.traces.ITrace;
import fr.lip6.mocah.laalys.traces.ITraces;
import fr.lip6.mocah.laalys.traces.Traces;
import fr.lip6.mocah.laalys.traces.Trace;

import fr.lip6.mocah.laalys.labeling.Labeling_V9;
import fr.lip6.mocah.laalys.labeling.PathState;

class InterfaceLaalys extends JFrame implements ActionListener
{ 
	// r�pertoire de base pour le chargement des fichiers (r�seau complet, r�seau filtr�, transitions, traces)
	String adressereseau = "C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\";
	
	// les r�pertoires de sauvegarde
	// r�pertoire de sauvegarde des fichiers du r�seau de Petri que l'on a filtr�s soi-m�me
	String adressereseaufiltre = "C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\filtredNet";
	// r�pertoire de sauvegarde des nouveaux fichiers de traces
	String adress="C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\trace";
	// r�pertoire de sauvegarde des nouveaux fichiers de traces expertes
	String adressexpert="C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\trace\\trace_experte";
	// r�pertoire de sauvegarde des fichiers de traces labellisees
	String adresselabel="C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\trace-labellisee";
	// r�pertoire de sauvegarde des fichiers de traces graphml
	String adressegraphml="C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples\\trace-graphml";
		
	// dimensions de la fen�tre
	int largeur = 1200, hauteur = 800 ;
	// taille des textes si non standard (standard : 12)
	int tailleTexte1 = 14;
	int tailleTexte2 = 16;
	JLabel info, info1, info2, info3 ;
	JButton reseau1a, reseau1a2, reseau1a3, choixfiltre, reseau2,reseau3,trace,suppression, trac ;
	JButton analyse1, analyse2, analyse3 ;
	// JButton again ;
	JRadioButton bouton1, bouton2, bouton1a, bouton1b ; 
	DefaultListModel model1, model2,  model3, model4, model5;
	// DefaultListModel model2bis ;
	JList liste_actions, liste_actions_realisees  ;
	JList liste_actions_analysees, liste_labels, liste_results ;
	String choix="", type="accessibilit�", strategie="OR" ; // par d�faut
	JPanel aux4 ;
	int indice, nb1, nb2, nb3 ;
	String fullPnName ;
	String filteredPnName ;
	String featuresName ;
	String traceName, traceName2 ;
	IPetriNet fullPn, fullPn_travail, fullPnFiltered ;
	IPetriNet filteredPn ;
	IFeatures features ;
	Labeling_V9 algo ;
	ITraces traces, copie_traces, nouvelles_traces, traces2, traces_expert ;
	ITrace nouvelletrace ;
	ArrayList<ITrace> value, value1, value2 ;
	PieChart cv ;
	DefaultPieDataset dataset ;

//////////////////////////////////////////////////////////////////

	public InterfaceLaalys() {
		super("Laalys"); 
		// fermeture application
		WindowListener l = new WindowAdapter() { 
			public void windowClosing(WindowEvent e){ 
				System.exit(0); 
				} 
			}; 
		addWindowListener(l);
		// taille et structure g�n�rale en onglets
		setSize(largeur, hauteur); 
		JPanel pannel = new JPanel(); 
		JTabbedPane onglets = new JTabbedPane(SwingConstants.TOP); 
		// polices de caract�res
		Font font1 = new Font("Arial",Font.PLAIN, tailleTexte1);
		Font font1b = new Font("Arial",Font.BOLD, tailleTexte1);
 
//////////////////////////////////////////////////////////////////

		// onglet1 pour chargement des r�seaux de Petri
		JPanel onglet1 = new JPanel(); 
		// si on veut un titre dans l'onglet1
		// JLabel titreOnglet1 = new JLabel("Type ..."); 
		// onglet1.add(titreOnglet1); 
		onglet1.setPreferredSize(new Dimension(largeur, hauteur)); 
		onglets.addTab("<html><font size=\"4\">Type de graphe et chargement des r�seaux</font></html>", onglet1);

		JPanel pannelBase = new JPanel(); 
		pannelBase.setLayout(new GridLayout(1, 3)); 
		
		JPanel pannel1 = new JPanel(); 
		pannel1.setLayout(new GridLayout(11,1));
		JPanel pannel1a = new JPanel();
		JLabel jLabel1 =new JLabel("<html><b>Chargement du r�seau complet sans graphe</b></html>");
		jLabel1.setFont(font1);
		pannel1a.add(jLabel1) ;
		pannel1.add(pannel1a);
		JPanel pannel1b = new JPanel();
		reseau1a = new JButton("Charger");
		reseau1a.addActionListener(this); 
		pannel1b.add(reseau1a); 
		pannel1.add(pannel1b);
		JPanel pannel1c = new JPanel();
		info =new JLabel(new String());
		info.setFont(font1);
		pannel1c.add(info) ;	
		pannel1.add(pannel1c);
		JPanel pannel0 = new JPanel();
		pannel1.add(pannel0);
		JPanel pannel1d = new JPanel();
		JLabel jLabel1d =new JLabel("<html><b>OPTION FACULTATIVE : cr�er un r�seau filtr�</b></html>");
		jLabel1d.setFont(font1);
		pannel1d.add(jLabel1d) ;
		pannel1.add(pannel1d);		
		JPanel pannel1e = new JPanel();
		reseau1a2 = new JButton("a. Choisir la trace");
		reseau1a2.addActionListener(this); 
		pannel1e.add(reseau1a2);
		pannel1.add(pannel1e);
		JPanel pannel1f = new JPanel();
		info1 =new JLabel(new String());
		info1.setFont(font1);
		pannel1f.add(info1) ;
		pannel1.add(pannel1f);	
		JPanel pannel1g = new JPanel();
		reseau1a3 = new JButton("b. G�n�rer le r�seau filtr�");
		reseau1a3.addActionListener(this); 
		pannel1g.add(reseau1a3);
		pannel1.add(pannel1g);		
		pannelBase.add(pannel1);
			
		JPanel pannel2 = new JPanel(); 
		pannel2.setLayout(new GridLayout(11,1));
		JPanel pannel2a = new JPanel();
		JLabel jLabel2 =new JLabel("<html><b>1. Choix du r�seau filtr�</b></html>");
		jLabel2.setFont(font1);
		pannel2a.add(jLabel2) ;
		pannel2.add(pannel2a);	
		JPanel pannel2b = new JPanel();	
		choixfiltre = new JButton("Choisir");
		choixfiltre.addActionListener(this); 
		pannel2b.add(choixfiltre); 
		pannel2.add(pannel2b);	
		JPanel pannel2h = new JPanel();
		info2 =new JLabel(new String());
		info2.setFont(font1);
		pannel2h.add(info2) ;	
		pannel2.add(pannel2h);	
		JPanel pannel21 = new JPanel();
		pannel2.add(pannel21);
		JPanel pannel2c = new JPanel();
		JLabel jLabel3 =new JLabel("<HTML><b>2. Type de graphe</b></html>"); 
		jLabel3.setFont(font1);
		pannel2c.add(jLabel3);  
		pannel2.add(pannel2c) ;			
		JPanel pannel2d = new JPanel();			
		ButtonGroup group = new ButtonGroup();
		bouton1 = new JRadioButton("Couverture", true); 
		bouton1.setFont(font1);
		bouton1.addActionListener(this); 
		group.add(bouton1);
		pannel2d.add(bouton1); 
		bouton2 = new JRadioButton("Accessibilit�");
		bouton2.setFont(font1);
		bouton2.addActionListener(this); 
		group.add(bouton2);
		pannel2d.add(bouton2);
		pannel2.add(pannel2d);		
		JPanel pannel2e = new JPanel();		
		JLabel jLabel1a =new JLabel("<HTML><b>Strat�gie d'analyse</b> pour type de graphe de <b>couverture</b></html>"); 
		jLabel1a.setFont(font1);
		pannel2e.add(jLabel1a); 
		pannel2.add(pannel2e);
		JPanel pannel2f = new JPanel();		
		ButtonGroup group2 = new ButtonGroup();
		bouton1a = new JRadioButton("FIRST", true);
		// en fait, FIRST == OU
		bouton1a.setFont(font1);
		bouton1a.addActionListener(this); 
		group2.add(bouton1a);
		pannel2f.add(bouton1a); 
		bouton1b = new JRadioButton("ALL", true);
		// en fait, ALL == ET
		bouton1b.setFont(font1);
		bouton1b.addActionListener(this); 
		group2.add(bouton1b);
		pannel2f.add(bouton1b);
		/*bouton1c = new JRadioButton("FIRST", true);
		bouton1c.setFont(font1);
		bouton1c.addActionListener(this); 
		group2.add(bouton1c);
		pannel2f.add(bouton1c);*/
		pannel2.add(pannel2f);		
		JPanel pannel22 = new JPanel();
		pannel2.add(pannel22);
		JPanel pannel2i = new JPanel();	
		JLabel jLabel6 =new JLabel("<html><b>3. Chargement du r�seau filtr�</b></html>");
		jLabel6.setFont(font1);
		pannel2i.add(jLabel6) ;
		pannel2.add(pannel2i);
		JPanel pannel2g = new JPanel();	
		reseau2 = new JButton("Charger");
		reseau2.addActionListener(this); 
		pannel2g.add(reseau2); 
		pannel2.add(pannel2g);
		pannelBase.add(pannel2);
		
		JPanel pannel3 = new JPanel(); 
		pannel3.setLayout(new GridLayout(11,1));
		JPanel pannel3a = new JPanel();
		JLabel jLabel4 =new JLabel("<html><b>Caract�ristiques du r�seau de P�tri</b></html>");
		jLabel4.setFont(font1);
		pannel3a.add(jLabel4) ;
		pannel3.add(pannel3a);
		JPanel pannel3b = new JPanel();
		reseau3 = new JButton("Charger"); 			
		reseau3.addActionListener(this); 
		pannel3b.add(reseau3);
		pannel3.add(pannel3b); 
		JPanel pannel3c = new JPanel();
		info3 =new JLabel(new String());
		info3.setFont(font1);
		pannel3c.add(info3) ;	
		pannel3.add(pannel3c);		
		pannelBase.add(pannel3);
		
		onglet1.add(pannelBase);

//////////////////////////////////////////////////////////////////

		// onglet2 pour traces
		JPanel onglet2 = new JPanel(); 
		onglet2.setPreferredSize(new Dimension(largeur, hauteur));
		onglet2.setLayout(new BorderLayout());
		onglets.addTab("<html><font size=\"4\">Chargement des traces</font></html>", onglet2); 

		JPanel haut = new JPanel();
		haut.setLayout(new GridLayout(3,1));
		JPanel haut2 = new JPanel();
		JLabel lab1 = new JLabel("<html>Pour <b>charger un fichier de traces</b>, cliquez sur le bouton : </html>"); 
		lab1.setFont(font1);
		haut2.add(lab1);
		trace = new JButton("Charger un fichier de traces"); 
		trace.addActionListener(this); 
		haut2.add(trace);
		haut.add(haut2);
		JPanel haut3 = new JPanel();
		JLabel lab2 = new JLabel("<html><center>Pour <b>construire</b> ou <b>modifier</b> une trace, choisissez dans l'ordre souhait� les actions de la liste de gauche et faites-les glisser dans la fen�tre de droite. <br/>Pour <b>supprimer</b> un �l�ment de trace, choisissez l'action dans la liste de droite et cliquez sur la touche SUPPR du clavier.</center></html>") ;lab2.setFont(font1);
		haut3.add(lab2);
		haut.add(haut3);
		JPanel haut4 = new JPanel();
		haut4.setLayout(new GridLayout(1,2));
		JPanel haut41 = new JPanel();
		JLabel lab3 = new JLabel("<html><center>Actions de jeu possibles</center></html>");lab3.setFont(font1b);
		haut41.add(lab3);
		haut4.add(haut41);
		JPanel haut42 = new JPanel();
		JLabel lab4 = new JLabel("<html><center>Fichier de traces : d�tails des actions r�alis�es</center></html>");lab4.setFont(font1b);
		haut42.add(lab4);
		haut4.add(haut42);
		haut.add(haut4);
		onglet2.add("North", haut);

		JPanel pannelactions = new JPanel();
		// cr�ation de la liste1, liste d'origine des actions possibles
		JPanel j1 = new JPanel();
		j1.setLayout(new BorderLayout());
		model1 = new DefaultListModel();
		liste_actions =new JList(model1);
        liste_actions.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        liste_actions.setDragEnabled(true);
		JScrollPane scrollPane = new JScrollPane(liste_actions);
        scrollPane.setPreferredSize(new Dimension(500,535));
		j1.add("Center", scrollPane);
		pannelactions.add(j1);

		// cr�ation de la liste2, liste de destination des �l�ments de trace
		JPanel j2 = new JPanel();
		j2.setLayout(new BorderLayout());
		model2 = new DefaultListModel();
		liste_actions_realisees = new JList(model2);
		liste_actions_realisees.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane scrollPane1 = new JScrollPane(liste_actions_realisees);
		scrollPane1.setPreferredSize(new Dimension(500,500));
		liste_actions_realisees.setDragEnabled(true);
		liste_actions_realisees.setDropMode(DropMode.INSERT);
		liste_actions_realisees.setTransferHandler(new ListTransferHandler());
		liste_actions_realisees.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				// System.out.println("Touche press�e : " + e.getKeyCode());
				model2.remove(liste_actions_realisees.getSelectedIndices()[0]);
				}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) { }
			} );
		j2.add("Center", scrollPane1);
		JPanel sauvetraces = new JPanel();
		trac = new JButton("Sauvegarder la trace");
		sauvetraces.add(trac);
		trac.addActionListener(this);
		j2.add("South", sauvetraces);		
		pannelactions.add(j2);
				
		onglet2.add("Center", pannelactions);
/*		Component[] com = onglet2.getComponents();
		for (int a = 0; a < com.length; a++) {
		     com[a].setEnabled(false);
			} */
	
/////////////////////////////////////////////////////////////////

		// onglet3 pour analyse	
		JPanel onglet3 = new JPanel(); 
 		onglet3.setLayout(new BorderLayout());	
		onglet3.setPreferredSize(new Dimension(largeur, hauteur));
		onglets.addTab("<html><font size=\"4\">Analyse</font></html>", onglet3); 
		
		JPanel pannelhaut = new JPanel();
		pannelhaut.setLayout(new GridLayout(2,3));
		JPanel un = new JPanel();
		pannelhaut.add(un);		
		JPanel deux = new JPanel();
		analyse1 = new JButton("Analyser toutes les actions");
		analyse1.addActionListener(this);
		deux.add(analyse1);
		pannelhaut.add(deux);		
		JPanel trois = new JPanel();
		pannelhaut.add(trois);		
		JPanel aux1 = new JPanel();
		JPanel aux2 = new JPanel();
		JLabel t2 = new JLabel("Actions analys�es"); t2.setFont(font1);
		aux2.add(t2);
		pannelhaut.add(aux2);
		JPanel aux3 = new JPanel();
		JLabel t3 = new JLabel("Labels correspondants"); t3.setFont(font1);
		aux3.add(t3);
		pannelhaut.add(aux3);	
		JLabel t1 = new JLabel("Compte-rendu d'analyse");t1.setFont(font1);
		aux1.add(t1);
		pannelhaut.add(aux1);
		onglet3.add("North", pannelhaut);

		JPanel pannelbas = new JPanel();
		JPanel aux5 = new JPanel();
		JPanel aux5a = new JPanel();
		aux5a.setLayout(new BorderLayout());
		model3 = new DefaultListModel();
		liste_actions_analysees =new JList(model3);
        // liste_actions.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // liste_actions.setDragEnabled(true);
		JScrollPane scrollPane3 = new JScrollPane(liste_actions_analysees);
        scrollPane3.setPreferredSize(new Dimension(350,470));
		aux5a.add(scrollPane3);
		aux5.add("Center", aux5a);
		JPanel aux5b = new JPanel();
		analyse2 = new JButton("Exporter au format Graphml");
		analyse2.addActionListener(this);
		aux5b.add(analyse2);
		aux5.add("South", aux5b);
		pannelbas.add(aux5);
		
		JPanel aux6 = new JPanel();
		JPanel aux6a = new JPanel();
		aux6a.setLayout(new BorderLayout());
		model4 = new DefaultListModel();
		liste_labels =new JList(model4);
		JScrollPane scrollPane4 = new JScrollPane(liste_labels);
        scrollPane4.setPreferredSize(new Dimension(350,470));
		aux6a.add(scrollPane4);
		aux6.add("Center", aux6a);
		JPanel aux6b = new JPanel();
		analyse3 = new JButton("Exporter les labels");
		analyse3.addActionListener(this);
		aux6b.add(analyse3);
		aux6.add("South", aux6b);
		pannelbas.add(aux6);
		
		pannelbas.setLayout(new GridLayout(1,3));
		JPanel aux4 = new JPanel();
		JPanel aux4a = new JPanel();
		aux4a.setLayout(new BorderLayout());
		model5 = new DefaultListModel();
		liste_results =new JList(model5);
		JScrollPane scrollPane5 = new JScrollPane(liste_results);
        scrollPane5.setPreferredSize(new Dimension(350,470));
		aux4a.add(scrollPane5);
		aux4.add("Center", aux4a);
		JPanel aux4b = new JPanel();
		// again = new JButton("Recommencer");
		// again.addActionListener(this);
		// aux4b.add(again);
		aux4.add("South", aux4b);
		pannelbas.add(aux4);
		onglet3.add("Center", pannelbas);
		
//////////////////////////////////////////////////////////////////

		// onglet4 pour d�buggage
		/* JPanel onglet4 = new JPanel(); 
		JLabel titreOnglet4 = new JLabel("D�buggage"); 
		onglet4.add(titreOnglet4); 
		onglet4.setPreferredSize(new Dimension(largeur, hauteur));
		onglets.addTab("<html><font size=\"4\">D�buggage</font></html>", onglet4); */

/////////////////////////////////////////////////////////////////

		onglets.setOpaque(true); 
		pannel.add(onglets); 
		getContentPane().add(pannel); 
		setVisible(true); 
		} 

//////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent e) { 
		SelectionFichier sf ;
		Object source = e.getSource(); 
		if (source==bouton1) {
				System.out.println("couverture"); type="couverture" ;
				bouton1a.getModel().setEnabled(true) ; 		
				bouton1b.getModel().setEnabled(true) ;	
				} 
		else if (source==bouton2) {
				System.out.println("accessibilit�"); type = "accessibilit�";
				// interdire le bouton1b 
				bouton1b.getModel().setEnabled(false) ;
				// activer le bouton1a automatiquement
				bouton1a.setSelected(true) ;
				strategie="OU";
				} 
		else if (source==bouton1a) {System.out.println("strat�gie FIRST, en fait, OU"); strategie="OU";}
		else if (source==bouton1b) {System.out.println("strat�gie ALL, en fait, ET");strategie="ET";}
		// else if (source==bouton1c) {System.out.println("strat�gie FIRST");strategie="FIRST";}
		
		else if(source == reseau1a){ System.out.println("Charger un r�seau complet sans graphe.");
			// vider tout
			reseau1a.setBackground(Color.WHITE); 
			reseau1a2.setBackground(Color.WHITE); 
			reseau1a3.setBackground(Color.WHITE); 
			choixfiltre.setBackground(Color.WHITE);
			reseau2.setBackground(Color.WHITE); 
			reseau3.setBackground(Color.WHITE); 
			trace.setBackground(Color.WHITE);
			analyse1.setBackground(Color.WHITE);
			analyse2.setBackground(Color.WHITE);
			analyse3.setBackground(Color.WHITE);
			model1.clear();
			model2.clear();
			model3.clear();
			model4.clear();
			model5.clear();

			// on choisit le fichier � charger
			sf = new SelectionFichier();
			fullPnName = sf.getNomFichier(adressereseau) ; 
			System.out.println("fichier de r�seau choisi : " + fullPnName);
			int index = fullPnName.lastIndexOf("\\");
			String nomfich = fullPnName.substring(index+1);
			info.setText("<html>Nom du r�seau complet : " +nomfich + "</html>");
			fullPn = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			// long stamp = System.currentTimeMillis();
			try {
				fullPn.loadPetriNet(fullPnName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			reseau1a.setBackground(Color.CYAN);
			// compl�ter model1
			for (ITransition tr : fullPn.getTransitions()) {
				model1.addElement(tr.getName());
				}
 			} 
	
		else if(source == reseau1a2){ System.out.println("Choisir la trace");
		// en fait on va recharger le m�me r�seau complet
		// mais cette fois pour filtrer le XML associ� avant de le r�enregister sous un autre nom
		// on l'appelle fullPn_travail
			fullPn_travail = new PetriNet(false, AccessibleGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
			// rechargement sous un autre nom
			try {
				fullPn_travail.loadPetriNet(fullPnName);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			// comment le lire ? c'est un IPetriNet
			/*System.out.println("----------------fullPn_travail-----------------------------");
			System.out.println(fullPn_travail.toString());
			System.out.println("---------------------------------------------");*/
			
			// on choisit une trace experte pour filtrer le r�seau fullPn_travail
			sf = new SelectionFichier();
			traceName2 = sf.getNomFichier(adressexpert) ; 
			int index2 = traceName2.lastIndexOf("\\");
			String nomfich2 = traceName2.substring(index2+1);
			// System.out.println("fichier de trace expert choisi : " + traceName2);
			info1.setText("<html>Nom du fichier de trace choisi : " +nomfich2 + "</html>");
			traces_expert = new Traces();
			traces_expert.loadFile(traceName2);	
			System.out.println("--------------------traces_expert-------------------------");
			System.out.println(traces_expert.toString());
			System.out.println("---------------------------------------------");
		}
		
		else if(source == reseau1a3){ 
			System.out.println("G�n�rer le r�seau filtr�");
			// � partir du XML r�seau complet d�j� charg� avec fullPn_travail
			// filtrage du xml en fonction des traces
			fullPn_travail.filterXMLWith(traces_expert);
				
			String  filename_new = "" ;					
			// choisir le nom du r�seau filtr�		
			try{		            
		           JFileChooser chooser = new JFileChooser();			            
		           // Dossier de r�seaux filtr�s
		          chooser.setCurrentDirectory(new  File(adressereseaufiltre+File.separator)); 			                    
		           //Affichage et r�cup�ration de la r�ponse de l'utilisateur
		           int reponse = chooser.showDialog(chooser,"Enregistrer (extension .pnml automatique)");		             
		          // Si l'utilisateur clique sur OK
		          if  (reponse == JFileChooser.APPROVE_OPTION){			                
		                 // R�cup�ration du chemin du fichier et de son nom
		                filename_new= chooser.getSelectedFile().toString(); 
		                System.out.println("fichier : " + filename_new+".pnml");
		           	}
				}
			catch (HeadlessException he) {
		          he.printStackTrace();
				}
			// enregistrement proprement dit
			Transformer transformer;
			Result output;
			try {					
				transformer = TransformerFactory.newInstance().newTransformer(); 
				output = new StreamResult(new File(filename_new+".pnml")); 
				Source input = new DOMSource(((PetriNet)fullPn_travail).xml); 
				transformer.transform(input, output);	
				}
			catch (Exception e5) {
				System.out.println("erreur d'enregistrement");
			}		
			reseau1a2.setBackground(Color.CYAN);
			} 
		
		else if (source == choixfiltre) {
			System.out.println("Choisir un r�seau de Petri filtr�."); 
			reseau2.setBackground(Color.WHITE);			
			sf = new SelectionFichier();
			filteredPnName = sf.getNomFichier(adressereseau) ; 
			int index = filteredPnName.lastIndexOf("\\");
			String nomfich = filteredPnName.substring(index+1);
			info2.setText("<html>Nom du r�seau filtr� : " +nomfich + "</html>");
			}
		
		else if(source == reseau2){ 
			System.out.println("Charger un r�seau de Petri filtr�."); 
			// sf = new SelectionFichier();
			// filteredPnName = sf.getNomFichier() ; 
			reseau2.setBackground(Color.WHITE);
			// reseau3.setBackground(Color.WHITE); 
			analyse1.setBackground(Color.WHITE);
			analyse2.setBackground(Color.WHITE);
			analyse3.setBackground(Color.WHITE);
			model3.clear();
			model4.clear();
			model5.clear();
			if (filteredPnName!="") {
				int index = filteredPnName.lastIndexOf("\\");
				String nomfich = filteredPnName.substring(index+1);
				// charger le fichier par IPetriNet - loadPetriNet()
				System.out.print("fichier de r�seau filtr� choisi : " + filteredPnName);	
				System.out.print(" - " + type);
				System.out.println(" - strat�gie : "+strategie);
				if (type.equalsIgnoreCase("accessibilit�")) {
					filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
					}
				else if (type.equalsIgnoreCase("couverture")) {	// couverture	
					if (strategie.equalsIgnoreCase("OU")) filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_OR);
					else if (strategie.equalsIgnoreCase("ET")) filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_AND);
					else filteredPn = new PetriNet(true, CoverabilityGraph.TYPE, CoverabilityGraph.STRATEGY_FIRST); 
					} 	
				try {
					filteredPn.loadPetriNet(filteredPnName);
					}
				catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					}	
				// System.out.println("filtered graph size : "+filteredPn.getGraph().getAllMarkings().size() + " " + (System.currentTimeMillis()-stamp));
				reseau2.setBackground(Color.CYAN);
			} 
		}
		
		else if(source == reseau3){ 
			System.out.println("Charger les caract�ristiques du r�seau de P�tri."); 
			analyse1.setBackground(Color.WHITE);
			analyse2.setBackground(Color.WHITE);
			analyse3.setBackground(Color.WHITE);
			model3.clear();
			model4.clear();
			model5.clear();
			features = new Features();
			sf = new SelectionFichier();
			featuresName = sf.getNomFichier(adressereseau) ; 
			// charger le fichier 
			System.out.println("fichier de caract�ristiques choisi : " + featuresName);	
			features.loadFile(featuresName);
			reseau3.setBackground(Color.CYAN);
			int index = fullPnName.lastIndexOf("\\");
			String nomfich = fullPnName.substring(index+1);
			info3.setText("<html>Nom du fichier de caract�ristiques : " +nomfich + "</html>");
			}
		
		else if (source==trace){ 
			// on arrive sur le deuxi�me onglet
			// vider l'analyse seulement
			// reseau1a, reseau1a2, reseau1a3, choixfiltre, reseau2,reseau3

			System.out.println("Charger un fichier de traces.");
			analyse1.setBackground(Color.WHITE);
			analyse2.setBackground(Color.WHITE);
			analyse3.setBackground(Color.WHITE);
			model3.clear();
			model4.clear();
			model5.clear();
			// System.out.println("===== " + model1.isEmpty());
			if (!model1.isEmpty()) {
				model2.removeAllElements();	
				sf = new SelectionFichier();
				traceName = sf.getNomFichier(adressereseau) ; 
				System.out.println("fichier de traces choisi : " + traceName);	
				traces = new Traces();
				traces.loadFile(traceName);
				// value va pour sauvegarder les informations compl�tes, avec tous les attributs des �l�ments de trace charg�s
				value = new ArrayList<ITrace>();
				// ICI copie_traces = new Traces();
				for (ITrace tr : traces.getTraces()) { // on parcourt les traces charg�es
					//pour affichage : l'action seulement
					model2.addElement(tr.getAction());
					// mais pour m�morisation, tous les attributs :
					value.add(tr);
					}
				trace.setBackground(Color.CYAN);
				// copie_traces : sauvegarde des informations compl�tes sur les �l�ments de trace charg�s
				// ICI copie_traces.setTraces(value);			
				nb1 = value.size(); 
				// System.out.println("AVANT : fichier de traces apr�s chargement : ");
				// for (int k = 0 ; k < nb1 ; k++) System.out.println("en pos " + k + " : "+ value.get(k));
 				}
			}
		
		else if (source==suppression) {
			System.out.println("Supprimer une ligne de trace.");
			int indice = liste_actions_realisees.getSelectedIndices()[0];
			if (indice != -1) model2.remove(indice);
			}
		
		else if (source == trac) {
			System.out.println("Enregistrer la trace.");
			// nombre d'actions possibles
			try {
				nb1 = value.size(); 
			}
			catch (Exception et) {
				nb1 = 0 ;
			}
			// taille de la trace �ventuellement compl�t�e
			nb3 = liste_actions_realisees.getModel().getSize();	
			if (nb3 != 0) {
				System.out.println("nb traces "+nb3);			
				// reconstruire les traces: on cr�e un nouveau value2 qui va contenir la reconstruction des traces ligne � ligne
				value2 = new ArrayList<ITrace>();
				// on le reconstruit
				// on lit les transitions une � une
				for (int k = 0 ; k < nb3 ; k++) {
					boolean trouve = false ;
					// on lit une ligne de la liste_actions_realisees (trace charg�e, ou compl�t�e, ou reconstruite)
					String action = liste_actions_realisees.getModel().getElementAt(k).toString();
					// existait-elle dans la liste de traces d'origine ? Si nb1=0 on ne fait rien
					for (int l = 0 ; l < nb1 ; l++) {
						if (value.get(l).toString().indexOf(action) !=-1) {
							// elle existait, on recopie la ligne compl�te
							value2.add(value.get(l));
							// System.out.println("transfert de " + value.get(l) + " vers value2");
							trouve = true;
							break ;
							}
						}
					if (!trouve) {
						// System.out.print(" n'existe pas");
						// elle n'existe pas dans la trace charg�e, donc on cr�e une nouvelle ligne
						// mais d'abord il faut rechercher l'origine de l'action : player ou system
						String origine = "player"; // par d�faut
						ArrayList<String> proprietes=features.getSystemTransitions(); // ensemble des transitions syst�me
						for (int m = 0 ; m < proprietes.size(); m++) {
							if (proprietes.get(m).indexOf(action) != -1) {
								// l'action est une action syst�me
								origine = "system" ;
								break;
								}
							}			
						// cr�ation de la nouvelle ligne de trace
						nouvelletrace = new Trace(action, "manual", origine, false);
						value2.add(nouvelletrace);
						}
					}				
				// 	nouveau fichier de traces
				ITraces itraces = new Traces();			
				itraces.setTraces(value2);
				for (int k = 0 ; k < value2.size() ; k++) System.out.println("en pos " + k + " : "+ value2.get(k));
				Document doc = itraces.toXML();
				// enregistrement du nouveau fichier de traces
				// choix du fichier
				String filename = "" ;
				try{		            
			           JFileChooser chooser = new JFileChooser();			            
			           // Dossier Courant
			          chooser.setCurrentDirectory(new  File(adress+File.separator)); 			                    
			           //Affichage et r�cup�ration de la r�ponse de l'utilisateur
			           int reponse = chooser.showDialog(chooser,"Enregistrer (extension .xml automatique)");		             
			          // Si l'utilisateur clique sur OK
			          if  (reponse == JFileChooser.APPROVE_OPTION){			                
			                 // R�cup�ration du chemin du fichier et de son nom
			                filename= chooser.getSelectedFile().toString(); 
			                // System.out.println("fichier : " + filename+".xml");
			           	}
					}
				catch (HeadlessException he) {
			          he.printStackTrace();
					}
				// enregistrement proprement dit
				Transformer transformer;
				Result output;
				try {					
					transformer = TransformerFactory.newInstance().newTransformer(); 
					output = new StreamResult(new File(filename+".xml")); 
					Source input = new DOMSource(doc); 
					transformer.transform(input, output);	
					}
				catch (Exception e5) {
					System.out.println("erreur d'enregistrement");
					}
				}
			}
		
		else if (source==analyse1) {

			System.out.println("Analyser.");
			// taille de la trace �ventuellement compl�t�e
			nb2 = liste_actions_realisees.getModel().getSize();	
			if (nb2 != 0) {
				System.out.println("nb1 trace d'origine "+nb1 + " nb2 trace apr�s manipulations "+nb2);			
				// reconstruire les traces: on cr�e un nouveau value1 qui va contenir la reconstruction des traces ligne � ligne
				value1 = new ArrayList<ITrace>();
				// on le reconstruit
				// on lit les transitions une � une
				for (int k = 0 ; k < nb2 ; k++) {
					boolean trouve = false ;
					// on lit une ligne de la liste_actions_realisees (trace charg�e ou compl�t�e ou reconstruite)
					String action = liste_actions_realisees.getModel().getElementAt(k).toString();
					// System.out.print("action : " + action);
					// existait-elle dans la liste de traces d'origine ? 
					for (int l = 0 ; l < nb1 ; l++) {
						// System.out.print(" a chercher dans "+value.get(l).toString() + " pour l= "+l+ "--");
						if (value.get(l).toString().indexOf(action) !=-1) {
							// elle existait, on recopie la ligne compl�te
							// System.out.print(" existe ");
							value1.add(value.get(l));
							// System.out.println("donc transfert de " + value.get(l) + " vers value1");
							trouve = true;
							break ;
							}
						}
					if (!trouve) {
						// System.out.print(" n'existe pas");
						// elle n'existe pas dans la trace charg�e, donc on cr�e une nouvelle ligne
						// mais d'abord il faut rechercher l'origine de l'action : player ou system
						String origine = "player"; // par d�faut
						ArrayList<String> proprietes=features.getSystemTransitions(); // ensemble des transitions syst�me
						for (int m = 0 ; m < proprietes.size(); m++) {
							if (proprietes.get(m).indexOf(action) != -1) {
								// l'action est une action syst�me
								origine = "system" ;
								break;
								}
							}			
						// cr�ation de la nouvelle ligne de trace
						nouvelletrace = new Trace(action, "manual", origine, false);
						value1.add(nouvelletrace);
						}
					}		
			
				// 	nouveau fichier de traces : on vide l'ancien traces et on transf�re value1 dans traces
				traces = new Traces();			
				traces.setTraces(value1);
				System.out.println("le fichier de traces apr�s re-cr�ation : ");
				for (int k = 0 ; k < value1.size() ; k++) System.out.println("en pos " + k + " : "+ value1.get(k));
			
				// le traitement 
				Logger monLog = Logger.getLogger(Main.class.getName());
				monLog.setLevel(Level.ALL); //pour envoyer les messages de tous les niveaux
				monLog.setUseParentHandlers(false); // pour supprimer la console par d�faut
				ConsoleHandler ch = new ConsoleHandler();
				ch.setLevel(Level.INFO); // pour n'accepter que les message de niveau &Ge; INFO
				monLog.addHandler(ch);
				algo = new Labeling_V9(monLog, false);
				algo.setCompletePN(fullPn);
				algo.setFilteredPN(filteredPn);
				algo.setFeatures(features);
				try {
					algo.label(traces);
					} 
				catch (Exception e3) {
					e3.printStackTrace();
					}		
				
				// vidage des trois fen�tres 
				model3.removeAllElements();	
				model4.removeAllElements();	
				model5.removeAllElements();
				
				int nbLabels = 18 ;
				int[] effectif = new int[nbLabels]; 
				for (int k = 0 ; k < nbLabels ; k++) effectif[k] = 0;
				String[] intitule = new String[nbLabels] ;
				intitule[0] = "autre-branche-de-resolution" ; intitule[1] = "correcte"; intitule[2] = "eloignement"; intitule[3] = "equivalente" ;
				intitule[4] = "erronee" ; intitule[5] = "intrusion"; intitule[6] = "inutile"; intitule[7] = "manquante" ;
				intitule[8] = "non-optimale" ; intitule[9] = "rapprochement"; intitule[10] = "rattapage"; intitule[11] = "retour_arriere" ;	
				intitule[12] = "saut-avant" ; intitule[13] = "stagnation"; intitule[14] = "trop-tard"; intitule[15] = "trop-tot" ;	
				intitule[16]="deja-vu"; intitule[17]="mauvais-choix";
				
				// calcul des effectifs de chaque label
				for (ITrace tr : traces.getTraces()){
					model3.addElement(tr.getAction());
					ArrayList<String> labs = tr.getLabels();
					model4.addElement(labs);
					for (String lab : labs) {
						for (int k = 0 ; k < nbLabels; k++) 
							if (lab==intitule[k]) { effectif[k] +=1 ; break;}
						}
					}
				
				// remplir l'analyse globale : traces and labels envoy�s dans l'interface
			    for (int k = 0 ; k < nbLabels; k++) {
			    	if (effectif[k]>0) {
			    		ArrayList<String> ligne = new ArrayList();
			    		ligne.add(intitule[k]); ligne.add(new Integer(effectif[k]).toString());
			    		model5.addElement(ligne);
			    		}
			    	}
			    
				// construire le diagramme
				analyse1.setBackground(Color.CYAN);	
				
				// on cr�e d'abord le dataset en �liminant les deja-vu et mauvais-choix
			    dataset = new DefaultPieDataset();
			    for (int k = 0 ; k < nbLabels-2; k++) 
			    	if (effectif[k]>0) dataset.setValue(intitule[k],  effectif[k]);
			      /* dataset.setValue("Linux", 29);
			       dataset.setValue("Mac", 20);
			       dataset.setValue("Windows", 51);*/
			       System.out.println("dataset cr��"); 
			     
			     // ensuite le PieChart qui fait tout le reste
			    cv = new PieChart("R�sultats de l'analyse", "", dataset);
				cv.setPreferredSize(new Dimension(500,270));								
				cv.pack();
		        cv.setVisible(true);
				}
			}
		else if (source == analyse2) {
			System.out.println("Exporter au format Graphml.");
			if (model3.getSize() != 0) {
				String outputfile = "" ;
				try{		            
			           JFileChooser chooser = new JFileChooser();			            
			           // Dossier Courant
			          chooser.setCurrentDirectory(new  File(adressegraphml+File.separator)); 			                    
			           //Affichage et r�cup�ration de la r�ponse de l'utilisateur
			           int reponse = chooser.showDialog(chooser,"Enregistrer (extension .graphml automatique)");		             
			          // Si l'utilisateur clique sur OK
			          if  (reponse == JFileChooser.APPROVE_OPTION){			                
			                 // R�cup�ration du chemin du fichier et de son nom
			                outputfile= chooser.getSelectedFile().toString(); 
			                System.out.println("fichier : " + outputfile+".graphml");
			           	}
			          algo.export(outputfile+".graphml");
					}
				catch (Exception he) {
			          he.printStackTrace();
					}
				analyse2.setBackground(Color.CYAN);
				}
			}
		else if (source == analyse3) {
			System.out.println("Exporter les labels.");
			if (model4.getSize() != 0) {				
				String outputfile = "" ;
				try{		            
			           JFileChooser chooser = new JFileChooser();			            
			           // Dossier Courant
			          chooser.setCurrentDirectory(new  File(adresselabel+File.separator)); 			                    
			           //Affichage et r�cup�ration de la r�ponse de l'utilisateur
			           int reponse = chooser.showDialog(chooser,"Enregistrer (extension .xml automatique)");		             
			          // Si l'utilisateur clique sur OK
			          if  (reponse == JFileChooser.APPROVE_OPTION){			                
			                 // R�cup�ration du chemin du fichier et de son nom
			                outputfile= chooser.getSelectedFile().toString(); 
			                System.out.println("fichier : " + outputfile+".xml");
			           	}
			          // contenu � �crire r�cup�ration de value1 compl�t� par les labels
					ITraces itraces = new Traces();			
					itraces.setTraces(value1);
					for (int k = 0 ; k < value1.size() ; k++) System.out.println("en pos " + k + " : "+ value1.get(k));
					Document doc = itraces.toXML();
					Transformer transformer= TransformerFactory.newInstance().newTransformer(); 
					Result output = new StreamResult(new File(outputfile+".xml")); 
					Source input = new DOMSource(doc); 
					transformer.transform(input, output);	
					}
				catch (Exception he) {
			          he.printStackTrace();
					}
				analyse3.setBackground(Color.CYAN);	
				}
			}
		}	
	}