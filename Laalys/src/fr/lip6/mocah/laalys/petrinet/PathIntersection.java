package fr.lip6.mocah.laalys.petrinet;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 * @author Mathieu Muratet
 */
public class PathIntersection implements IPathIntersection {
	private ArrayList<IPathLink> links;
	private int distance;
	private boolean seen_rec; // permet d'éviter les boucles dans l'appel récursif
	
	public PathIntersection()
	{
		links = new ArrayList<IPathLink>();
		distance = -1;
	}
	
	public void addLink(IPathLink link)
	{
		links.add(link);
	}
	
	public int getDistance()
	{
		return distance;
	}
	
	public ArrayList<IPathLink> getLinks()
	{
		return links;
	}
	
	// initialise récursivement l'attribut seen_rec à faux pour tous les noeuds des chemins à partir de cette intersection
	private void initSeen_rec()
	{
		seen_rec = false;
		// Appel récursif sur tout les liens de cette intersection
		for (IPathLink link : links)
		{
			if (link.getNextIntersection() != null)
			{
				PathIntersection nextIntersection = (PathIntersection) link.getNextIntersection();
				nextIntersection.initSeen_rec();
			}
		}
	}
	
	/**
	 * Vérifie si cette intersection est identique à celle passée en paramètre ("path").
	 */
	public boolean isEqualWith(IPathIntersection path) {
		initSeen_rec();
		return isEqualWith_rec(path);
	}
	
	private boolean isEqualWith_rec(IPathIntersection path) {
		// Vérification d'un cas de boucle
		if (seen_rec)
			return true;
		
		// indiquer que cette intersection est en cours de traitement
		seen_rec = true;
		
		boolean isEqual = true;
		
		// Si la distance est différente ou que le nombre de lien est différent, on est 
		// sûr que les deux arbres sont différents et on peut arrêter là
		if (path.getLinks().size() != links.size() || path.getDistance() != distance)
			isEqual = false;
			
		// Parcours de tous les liens de cette intersection
		for (int i = 0 ; i < links.size() && isEqual ; i++)
		{
			// identification du nom de la transition associée à ce lien
			IPathLink link = links.get(i);
			String trId = link.getLink().getId();
			// recherche de cette transition dans les liens du paramètre
			for (int j = 0 ; j < path.getLinks().size() && isEqual ; j++) {
				IPathLink link2 = path.getLinks().get(j);
				// on vérifie si ces deux liens correspondent à la même transition
				if (trId.equals(link2.getLink().getId())) {
					// on lance l'appel récursif si les deux intersections filles sont définies
					if (link.getNextIntersection() != null && link2.getNextIntersection() != null)
						isEqual = link.getNextIntersection().isEqualWith(link2.getNextIntersection()); // appel récursif
					else if (link.getNextIntersection() == null && link2.getNextIntersection() == null)
						// on tombe sur deux noeuds terminaux
						isEqual = true;
					else
						// l'un des deux noeuds est un terminal => on est donc sur deux intersections différentes
						isEqual = false;
				}
			}
		}
		
		// l'appel récursif sur cette intersection est terminée
		seen_rec = false;
		
		return isEqual;
	}
	
	public void print(Logger logger)
	{
		initSeen_rec();
		print_rec("", logger);
	}
	
	// Affiche de manière récursive le graphe, l'affichage est stoppée si une intersection n'est
	// pas défini ou si elle ne contient pas de lien ou si on boucle (on retombe sur une
	// intersection déjà traité).
	private void print_rec(String tab, Logger logger)
	{
		// Vérification d'un cas de boucle
		if (seen_rec){
			if (logger != null) 
				logger.log(Level.INFO, tab + "Boucle détectée");
			else
				System.out.println(tab + "Boucle détectée");
		}
		
		// indiquer que cette intersection est en cours de traitement
		seen_rec = true;
		
		String print = tab + " [" + distance + "]";
		// Parcours de tous les liens de cette intersection
		for (IPathLink link : links)
		{
			if (link.getLink() == null)
				print += "[transition non définie]";
			else
				print += link.getLink().getId();
			if (link.getNextIntersection() == null){
				if (logger != null)
					logger.log(Level.INFO, print + " (fin de branche)");
				else
					System.out.println(print + " (fin de branche)");
			}
			else
			{
				// Appel récursif
				PathIntersection nextIntersection = (PathIntersection) link.getNextIntersection();
				if (logger != null)
					logger.log(Level.INFO, print);
				else
					System.out.println(print);
				nextIntersection.print_rec(tab + "\t", logger);
			}
		}
		
		// l'appel récursif sur cette intersection est terminée
		seen_rec = false;
	}
	
	public void removeLink(IPathLink link)
	{
		int id = links.indexOf(link);
		if (id != -1)
			links.remove(id);
	}
	
	/**
	 * Recherche dans cette intersection un (ou plusieurs) lien(s) défini(s) par la transition "tr"
	 * passée en paramètre
	 */
	public void removeLinksByTransition(ITransition tr)
	{
		for (int i = links.size() - 1; i >= 0; i--)
		{
			IPathLink link = links.get(i);
			if (link.getLink().getId().equals(tr.getId()))
				links.remove(i);
		}
	}
	
	public void setDistance(int dist)
	{
		distance = dist;
	}

}
