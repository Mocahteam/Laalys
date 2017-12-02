import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class SelectionFichier {
        // Bo�te de s�lection � partir du r�pertoire courant
        File repertoireCourant = null;
        // adresse du r�pertoire d'enregistrement
        // String adresse = "C:\\Users\\auzende\\Desktop\\Laalys\\Trunk\\LaalysV9\\bin\\exemples";
		
		public String getNomFichier(String adresse, Component parent) {
			try {
				// obtention d'un objet File qui d�signe le r�pertoire courant. Le
				// "getCanonicalFile" n'est pas absolument n�cessaire mais permet
				// d'�viter les /Truc/./Chose/ ...
				// repertoireCourant = new File(".").getCanonicalFile();
				repertoireCourant = new File(adresse).getCanonicalFile();
				System.out.println("R�pertoire courant : " + repertoireCourant);
				} catch(IOException e) {}
         
			// cr�ation de la bo�te de dialogue dans ce r�pertoire courant
			// (ou dans "home" s'il y a eu une erreur d'entr�e/sortie, auquel
			// cas repertoireCourant vaut null)
			JFileChooser dialogue = new JFileChooser(repertoireCourant);
         
			// affichage
			dialogue.showOpenDialog(null);
         
			// r�cup�ration du fichier s�lectionn�
			System.out.println("Fichier choisi : " + dialogue.getSelectedFile());
			if (dialogue.getSelectedFile() != null){
				String filePath =  dialogue.getSelectedFile().getPath();
				// recherche d'accent dans le path
				if (Pattern.matches(".*[�����������������].*", filePath)){
					JOptionPane.showMessageDialog(parent, "Votre chemin d'acc�s ou le nom de votre fichier contient au moins un accent ou un �\n\nS�lection du fichier annul�");
					return "";
				} else
					return dialogue.getSelectedFile().getPath();
			}
			else
				return "";
		}
    }
