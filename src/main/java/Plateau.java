import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;

/**
 * Contient toute la logique et les m&eacute;thodes pratiques pour g&eacute;rer les r&egrave;gles de la simulation.
 * N&eacute;cessite un objet {@link Grille} pour l'affichage, &agrave; d&eacute;finir avec {@link Plateau#setDisplay(Grille)} car les deux classes ont des d&eacute;pendances mutuelles.
 */
public class Plateau implements ActionListener {
    public Vivant[][] simulation;
    public static int tailleCase = 15;
    protected int taille;

    private Timer perfTimer = new Timer(1000, this);
    private int perfCompteur = 0;
    public Grille display;
    public Timer simulTimer;

    /**
     * Cr&eacute;e le tableau de la simulation.
     * Il n'est pas encore pr&ecirc;t pour l'affichage, il faut lui assigner une {@link Grille}.
     * @param taille nombre de cases en hauteur/largeur
     * @param eventTimer le timer des tours de simulation.
     */
    public Plateau(int taille, Timer eventTimer) {
        super();
        this.taille = taille;
        simulTimer = eventTimer;
        genererPlateau(taille);
        perfTimer.start();
    }

    /**
     * D&eacute;finit l'affiche &agrave; mettre &agrave; jour lors des tours de simulation. Il n'y a que les cases modifi&eacute;es qui changeront.
     * @param display un objet Grille d&eacute;j&agrave; initialis&eacute;
     */
    public void setDisplay(Grille display) {
        this.display = display;
        display.fps.stop();
    }

    public void simuler() {
        //mouvementTest();
        menageDesMorts(); //supprimme les vivants qui n'ont plus de points de vie
        mangerBouger(); //action principale
        perfCompteur +=1;
    }

    /**
     * Entrée d'une action sur les vivants
     */
    private void mangerBouger() {
        Pos initial = getRandomVivant();
        if((selectVivant(initial) instanceof Lapin)) { //le fait manger si c'est un lapin
            lapinsMangent(initial);
        }
        deplacement(initial); //il se séplace dans tous les cas
    }

    private void deplacement(Pos initial) {
        int dx= new Random().nextInt(3)-1; //on bouge d'une case dans une direction au hazard (& en diagoanel)
        int dy= new Random().nextInt(3)-1;
        Pos destination = new Pos(initial.getX()+dx, initial.getY()+dy);
        if(destination.positionValide(taille) && estVide(destination)){
            Vivant vaBouger = enleverVivant(initial);
            ajouterVivant(vaBouger, destination);
        }
    }

    private void lapinsMangent(Pos initial) {
        //Pos initial = getRandomVivant();
            Lapin predateur = (Lapin) selectVivant(initial);
            for (int j = - 1; j <  1; j++) {
                for (int i = -1; i < 1; i++) {
                    Pos dest = new Pos(predateur.getPos().getX() + i, predateur.getPos().getY() + j);
                    if (dest.positionValide(taille)) {
                        if (selectVivant(dest) instanceof Potiron) {
                            enleverVivant(dest);
                            predateur.manger();
                        }
                    }
                }
            }
        /*
        Pos destination = new Pos(initial.getX(), initial.getY());
        if(destination.positionValide(taille)){
            Vivant vaBouger = enleverVivant(initial);
            if(estVide(destination))
                ajouterVivant(vaBouger, destination);
            if(selectVivant(destination) instanceof Potiron && vaBouger instanceof Lapin) {
                Lapin vaManger = (Lapin) vaBouger;
                vaManger.manger();
                enleverVivant(destination); //on enlève la proie mangée
                ajouterVivant(vaManger, destination); //on déplace le mangeur
            }
        }*/
    }

    /**
     * @param nouveau le Vivant &agrave; ins&eacute;rer
     * @param pos la position o&ugrave; le mettre
     */
    public void ajouterVivant(Vivant nouveau, Pos pos) {
        Pos.setTab(simulation, pos, nouveau);
        nouveau.setPos(pos);
        display.dessineCase(pos,nouveau);
    }

    /**
     * Enl&egrave;ve un vivant de la simulation.
     * @param aSuppr le vivant &agrave; tuer
     * @return l'argument d'entr&eacute;e
     */
    public Vivant enleverVivant(Vivant aSuppr) {
        display.dessineCase(aSuppr.getPos(),null);
        Pos.delTab(simulation, aSuppr.getPos());
        return aSuppr;
    }

    public Vivant enleverVivant(Pos pos) {
        Vivant suppr = selectVivant(pos);
        Pos.delTab(simulation, pos);
        display.dessineCase(pos,null);
        return suppr;
    }

    /**
     *
     * @param pos une position
     * @return Le vivant &agrave; la position demand&eacute;e, peut &ecirc;tre "null"
     */
    public Vivant selectVivant(Pos pos) {
        return (Vivant) Pos.getTab(simulation, pos);
    }

    public boolean estVide(Pos pos){
        return Pos.getTab(simulation, pos) == null;
    }

    public void genererPlateau(int taille) {
        simulation = new Vivant[taille][taille];
    }

    private void mouvementTest() { //fait un mouvement aléatoire
        Pos pos = Pos.getRandomPos(taille);
        while (Pos.getTab(simulation, pos) == null) { //on ne choisit pas une case vide
            pos = Pos.getRandomPos(taille);
        }
        Vivant ilVaBouger = (Vivant) Pos.getTab(simulation, pos);
        //Pos.delTab(simulation, pos);
        enleverVivant(pos);

        Pos newPos = Pos.getRandomPos(taille);
        while (Pos.getTab(simulation, newPos) != null) { //on ne choisit pas une case vide
            newPos = Pos.getRandomPos(taille);
        }
        //Pos.setTab(simulation, newPos, ilVaBouger);
        ajouterVivant(ilVaBouger, newPos);
    }

    public void genererAlea(int nbrePotiron, int nbreLapins, Graph graph) {
        final int[] nP = {0};
        final int[] nL = {0};
        Thread safe = new Thread(() -> {
            try {
                Thread.sleep(2000); //coupe l'action si elle dure plus de 2s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nP[0] = nbrePotiron;
            nL[0] = nbreLapins;
            System.out.println("Génération arrêtée");
        });
        safe.start();
        while (nP[0] < nbrePotiron) {
            Pos pos = Pos.getRandomPos(taille);
            if (Pos.getTab(simulation, pos) == null) {
                ajouterVivant(new Potiron(), pos);
                graph.addPotirons(getNbrePotirons());
                nP[0] += 1;
            }
        }
        while (nL[0] < nbreLapins) {
            Pos pos = Pos.getRandomPos(taille);
            if (Pos.getTab(simulation, pos) == null) {
                ajouterVivant(new Lapin(), pos);
                graph.addLapins(getNbreLapins());
                nL[0] += 1;
            }
        }
        safe.stop();
        //System.out.println("Génération de vivants terminée");
    }

    public void supprAlea(int nombre, Graph graph) {
        final int[] n = {0};
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            n[0] = 0;
            System.out.println("Génération arrêtée");
        }).start();
        while (n[0] < nombre) {
            Pos pos = Pos.getRandomPos(taille);
            if (Pos.getTab(simulation, pos) != null) {
                enleverVivant(pos);
                graph.addLapins(getNbreLapins());
                graph.addPotirons(getNbrePotirons());
                n[0] += 1;
            }
        }
    }

    public int getNbrePotirons() {
        int n = 0;
        for (int y = 0; y < simulation.length; y++) {
            for (int x = 0; x < simulation[y].length; x++) {
                if (simulation[y][x] instanceof Potiron)
                    n++;
            }
        }
        return n;
    }
    public int getNbreLapins() {
        int n = 0;
        for (int y = 0; y < simulation.length; y++) {
            for (int x = 0; x < simulation[y].length; x++) {
                if (simulation[y][x] instanceof Lapin)
                    n++;
            }
        }
        return n;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(perfCompteur +" itérations/seconde");
        perfCompteur =0;
    }

    private Pos getRandomVivant() {
        Pos pos = Pos.getRandomPos(taille);
        while (Pos.getTab(simulation, pos) == null) {
            pos = Pos.getRandomPos(taille);
        }
        return pos;
    }

    private Pos getRandomVide(){
        Pos pos = Pos.getRandomPos(taille);
        while (Pos.getTab(simulation, pos) != null) {
            pos = Pos.getRandomPos(taille);
        }
        return pos;
    }

    private void menageDesMorts() {
        int nbVivants=0;
        for (int y = 0; y < taille; y++) { //parcourt tout le tableau pour supprimmer
            for (int x = 0; x < taille; x++) { // les vivants qui n'ont plus de PV
                if(simulation[y][x]!=null) {
                    if (!simulation[y][x].visible()) {
                        simulation[y][x] = null;
                        display.dessineCase(x,y,null);
                    }
                    else
                        nbVivants+=1;
                }
            }
        }
        if(nbVivants == 0) {
            System.out.println("Arrêt de la simulation");
            simulTimer.stop(); //ça marche pas en fait
        }
    }

    /**
     * it&egrave;re tout le tableau des vivants pour leur changer leur d&eacute;lai de v&eacute;rification de mort
     * @param ms l'esp&eacute;rance de vie en milisecondes.
     */
    public void setMortDelay(int ms){
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                if(simulation[y][x]!=null)
                    simulation[y][x].setDelay(ms);
            }
        }
    }
}
