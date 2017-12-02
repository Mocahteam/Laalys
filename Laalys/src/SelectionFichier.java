import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class SelectionFichier {
        // Boîte de sélection à partir du répertoire courant
        File repertoireCourant = null;
        // adresse du répertoire d'enregistrement
        // String adresse = "C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples";
		
		public String getNomFichier(String adresse, Component parent) {
			try {
				// obtention d'un objet File qui désigne le répertoire courant. Le
				// "getCanonicalFile" n'est pas absolument nécessaire mais permet
				// d'éviter les /Truc/./Chose/ ...
				// repertoireCourant = new File(".").getCanonicalFile();
				repertoireCourant = new File(adresse).getCanonicalFile();
				System.out.println("Répertoire courant : " + repertoireCourant);
				} catch(IOException e) {}
         
			// création de la boîte de dialogue dans ce répertoire courant
			// (ou dans "home" s'il y a eu une erreur d'entrée/sortie, auquel
			// cas repertoireCourant vaut null)
			JFileChooser dialogue = new JFileChooser(repertoireCourant);
         
			// affichage
			dialogue.showOpenDialog(null);
         
			// récupération du fichier sélectionné
			System.out.println("Fichier choisi : " + dialogue.getSelectedFile());
			if (dialogue.getSelectedFile() != null){
				String filePath =  dialogue.getSelectedFile().getPath();
				// recherche d'accent dans le path
				if (Pattern.matches(".*[éèàùäëüïöâêîûôñçÿ].*", filePath)){
					JOptionPane.showMessageDialog(parent, "Votre chemin d'accès ou le nom de votre fichier contient au moins un accent ou un ç\n\nSélection du fichier annulé");
					return "";
				} else
					return dialogue.getSelectedFile().getPath();
			}
			else
				return "";
		}
    }
