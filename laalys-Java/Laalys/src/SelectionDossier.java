import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class SelectionDossier {
        // Boîte de sélection à partir du répertoire courant pour récupérer le nom d'un dossier
        File repertoireCourant = null;
        // adresse du répertoire d'enregistrement
        // String adresse = "C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples";
		
		public String getNomDossier(String adresse, Component parent) {
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
			dialogue.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         
			// affichage
			dialogue.showOpenDialog(null);
         
			// récupération du dossier sélectionné
			System.out.println("Dossier choisi : " + dialogue.getSelectedFile());
			if (dialogue.getSelectedFile() != null){
				String filePath =  dialogue.getSelectedFile().getPath();
				// recherche d'accent dans le path
				if (Pattern.matches(".*[éèàùäëüïöâêîûôñçÿ].*", filePath)){
					JOptionPane.showMessageDialog(parent, "Selected path includes at least one accented character or ç\n\nFolder selection aborted");
					return "";
				} else
					return dialogue.getSelectedFile().getPath();
			}
			else
				return "";
		}
    }
