import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Classe utilis&eacute;e pour l'affichage graphique de la simulation.
 * Pour afficher uniquement la modification sur une case, utiliser la m&eacute;thode {@link Grille#dessineCase(int, int, Vivant) dessineCase()}
 * Pour tout afficher d'un coup, appeler la m&eacute;thode {@link Grille#repaint() repaint()}
 */
public class Grille  extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    int taille = 45;
    int tailleCase = 10;
    private Vivant[][] simulation;
    private Timer fps;
    public EditListener editListener;
    public Vivant dernierVivantSelectionne;

    /**
     * @param simulation la r&eacute;f&eacute;rence d'un tableau de vivants
     */
    public Grille(Vivant[][] simulation) {
        this.simulation = simulation;
        this.taille = simulation.length;
        fps = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        //fps.start();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    /**
     * Dessine enti&egrave;rement toute la grille. Appeler avec la m&eacute;thode repaint()
     */
    public void paint(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0,0,getWidth(), getHeight());
        for (int y = 0; y < taille + 1; y++) {
            for (int x = 0; x < taille + 1; x++) { //itère le tableau "simulation" dans ses 2 dimensiosn
                if (x < taille && y < taille) {
                    dessineCase(x,y,simulation[y][x],g);
                }
            }
        }
    }

    /**
     * Dessine une seule case sur la grille sans se pr&eacute;occuper de rafra&icirc;chir l'affichache (c'est pour &ccedil;a que les cases nouvellement dessin&eacute;es se superposent aux menus
     * @param x coordonn&eacute;es x et y, peuvent &ecirc;tre remplac&eacute;es par un objet Pos
     * @param y coordonn&eacute;e y
     * @param ici le Vivant &agrave; afficher. peut &ecirc;tre "null" (case vide)
     * @param g Graphics sur lequel dessiner, optionnel
     */
    public void dessineCase(int x, int y, Vivant ici, Graphics g){
        if (ici == null)
            g.setColor(Vivant.COULEUR);
        else
            g.setColor(ici.getCouleur()); //utilise la couleur du vivant
        g.fillRect(indexApixels(x), //dessine la case relativement au milieu de l'écran
                indexApixels(y),
                tailleCase-2,
                tailleCase-2);
    }

    /**
     * Dessine une seule case sur la grille sans se pr&eacute;occuper de rafra&icirc;chir l'affichache (c'est pour &ccedil;a que les cases nouvellement dessin&eacute;es se superposent aux menus
     * @param pos la position du vivant
     * @param ici le vivant &agrave; afficher
     */
    public void dessineCase(Pos pos, Vivant ici){
        dessineCase(pos.getX(), pos.getY(), ici);
    }
    /**
     * Dessine une seule case sur la grille sans se pr&eacute;occuper de rafra&icirc;chir l'affichache (c'est pour &ccedil;a que les cases nouvellement dessin&eacute;es se superposent aux menus
     * @param x coordonn&eacute;es x et y, peuvent &ecirc;tre remplac&eacute;es par un objet Pos
     * @param y coordonn&eacute;e y
     * @param ici le Vivant &agrave; afficher. peut &ecirc;tre "null" (case vide)
     */
    public void dessineCase(int x, int y, Vivant ici) {
        if(isSingleRefresh())
            return;
        dessineCase(x,y,ici,getGraphics());
    }


    @Override
    public void run() {
        repaint();
    }

    public void zoom(int facteur) {
        tailleCase=facteur;
        repaint();
    }

    /**
     * Interface qui sert à effectuer proprement les modifications sur le tableau {@link Plateau#simulation} sans tout casser.
     */
    public interface EditListener{
        /**
         * Change un lapin en potiron, un potiron en rien, et rien en lapin.
         * @param pos la position de l'&eacute;l&eacute;ment
         * @return l'&eacute;l&eacute;ment cr&eacute;e
         */
        Vivant toggleVivant(Pos pos);
        void setVivant(Pos pos, Vivant vivant);
    }

    /**
     * Assigne l'interface un peu comme le font les codes Java Swing
     * @param that une classe qui impl&eacute;ment {@link EditListener}
     */
    public void addEditListener(EditListener that){
        this.editListener = that;
    }

    public void stopRefresh() {
        this.fps.stop();
    }
    public void startRefresh() {
        this.fps.start();
    }
    public void setRefreshDelay(int ms){this.fps.setDelay(ms);}

    private boolean isSingleRefresh(){return fps.isRunning();}

    public void afficherTexte(String str){
        Graphics g = getGraphics();
        g.fillRect(0,0,50,20);
        g.setColor(Color.red);
        g.drawString(str,5,10);
    }
    private int pixelsAindex(int xp){
        return (xp-(getWidth()-tailleCase*taille)/2)/tailleCase;
    }
    private int indexApixels(int i){
        return ((getWidth() - tailleCase*taille) / 2)+tailleCase *i;
    }
    private boolean pixelsAppartienentAImage(int p){
        return p>(getWidth() - tailleCase*taille)/2 && p<(getWidth() + tailleCase*taille)/2;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(pixelsAppartienentAImage(e.getX()) && pixelsAppartienentAImage(e.getY())){
            Pos clicked = new Pos(pixelsAindex(e.getX()), pixelsAindex(e.getY()));
            System.out.println(clicked);
            dernierVivantSelectionne = editListener.toggleVivant(clicked);
            //repaint();
        }else{
            System.out.println(e.getX()+" "+e.getY());
        }
    }
    public void mousePressed(MouseEvent e) {}public void mouseReleased(MouseEvent e) {}public void mouseEntered(MouseEvent e) {}public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {
        if(pixelsAppartienentAImage(e.getX()) && pixelsAppartienentAImage(e.getY())) {
            Pos clicked = new Pos(pixelsAindex(e.getX()), pixelsAindex(e.getY()));
            Vivant nouveau=null;//valeur par défaut
            if(dernierVivantSelectionne instanceof Lapin)
                nouveau=new Lapin();
            if(dernierVivantSelectionne instanceof Potiron)
                nouveau=new Potiron();
            editListener.setVivant(clicked,nouveau);
        }

    }public void mouseMoved(MouseEvent e) {}
}
