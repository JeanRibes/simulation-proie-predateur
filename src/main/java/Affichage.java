import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Classe pour lancer le programme. Il y a dedans tous les contr&ocirc;les, et c'est elle qui poss&egrave;de et g&egrave;re les objets
 * {@link Grille} (affichage du plateau de simulation),
 * {@link Graph} (affiche l'&eacute;volution dans le temps des populations)
 * et {@link Plateau} (logique m&eacute;tier des tours de simulation, n'effectue aucun affichage)
 */
public class Affichage extends JFrame implements ActionListener {
    private Vivant[][] simulation = new Vivant[45][45];
    private Plateau plateau;
    private Grille grille;
    private Graph graphique;

    private JMenuBar barreMenus = new JMenuBar();
    private JMenu actions = new JMenu("Actions");
    private JMenuItem boutonAction = new JMenuItem("Simuler un tour");
    private JMenuItem boutonStart = new JMenuItem("Démarrer la simulation");
    private JMenuItem boutonStop = new JMenuItem("Arrêter la simulation");
    private JMenuItem plusDeVivants = new JMenuItem("Ajouter des vivants");
    private JMenuItem moinsDeVivants = new JMenuItem("Supprimer des vivants");
    private JMenuItem genVid = new JMenuItem("Générer la vidéo (ffmpeg requis)");
    private JRadioButtonMenuItem mLapin = new JRadioButtonMenuItem("Ajouter des lapins", new Icone(Lapin.COULEUR));
    private JRadioButtonMenuItem mPotiron = new JRadioButtonMenuItem("Ajouter des potirons", new Icone(Potiron.COULEUR));
    private JRadioButtonMenuItem mVide = new JRadioButtonMenuItem("Supprimer des vivants", new Icone(Vivant.COULEUR));

    private JMenu parametres = new JMenu("Paramètres");
    private JMenuItem infoZoom = new JMenuItem("Zoom");
    private JSlider tailleCase = new JSlider(4,50,Plateau.tailleCase);
    private JMenuItem vivantsTimer = new JMenuItem("Changer le délai de mort");
    private JSlider slideVitesse = new JSlider(1,100,50);
    private JMenuItem cleanGraph = new JMenuItem("Remise à zéro du graphique");
    private JCheckBoxMenuItem capturePix = new JCheckBoxMenuItem("Enregistrer la simulation");

    private JMenuItem textFPS = new JMenuItem("Vitesse d'affichage: T(s)");
    private JCheckBoxMenuItem singleRefresh = new JCheckBoxMenuItem("Rafraîchir tout le plateau");
    private JSlider slideFPS = new JSlider(1,60,30);

    public Affichage(){
        super();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800,850);
        setTitle("Simluation Proie-prédateur");
        setLocationRelativeTo(null);

        grille = new Grille(simulation);
        graphique = Graph.lancer();
        plateau = new Plateau(simulation, grille, graphique);
        plateau.setDisplay(grille);

        setContentPane(grille);

        boutonAction.addActionListener(this);
        boutonStart.addActionListener(this);
        boutonStop.addActionListener(this);
        vivantsTimer.addActionListener(this);
        plusDeVivants.addActionListener(this);
        moinsDeVivants.addActionListener(this);
        cleanGraph.addActionListener(this);
        capturePix.addActionListener(this);
        genVid.addActionListener(this);
        mLapin.addActionListener(this);
        mPotiron.addActionListener(this);
        mVide.addActionListener(this);


        tailleCase.setToolTipText("Taille des cases");
        tailleCase.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                grille.zoom(tailleCase.getValue());
            }
        });
        slideVitesse.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                plateau.setSimulationDelay(slideVitesse.getValue());
            }
        });
        infoZoom.setEnabled(false);
        textFPS.setEnabled(false);
        slideFPS.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float fps= (float) slideFPS.getValue();
                float hz = 1/fps;
                grille.setRefreshDelay((int) (1000*hz));
                textFPS.setText("Vitesse d'affichage (T="+hz+" s");
            }
        });
        singleRefresh.addActionListener(this);

        parametres.add(infoZoom);
        parametres.add(tailleCase);
        parametres.add(capturePix);
        parametres.add(new JSeparator());
        parametres.add(textFPS);
        parametres.add(slideFPS);
        parametres.add(singleRefresh);
        parametres.add(new JSeparator());
        JMenuItem a1 = new JMenuItem("Délai de simulation");
        a1.setEnabled(false);
        parametres.add(a1);
        parametres.add(slideVitesse);
        parametres.add(new JSeparator());
        parametres.add(vivantsTimer);
        parametres.add(new JSeparator());
        parametres.add(cleanGraph);
        barreMenus.add(parametres);

        actions.add(boutonAction);
        actions.add(boutonStart);
        actions.add(boutonStop);
        actions.add(new JSeparator());
        actions.add(plusDeVivants);
        actions.add(moinsDeVivants);
        actions.add(new JSeparator());
        actions.add(genVid);
        actions.add(new JSeparator());
        actions.add(mLapin);
        actions.add(mPotiron);
        actions.add(mVide);

        barreMenus.add(actions);
        //barreMenus.add(boutonStart);
        //barreMenus.add(boutonStop);
        setJMenuBar(barreMenus);

        setVisible(true);
        new Thread(()->{
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            plateau.genererAlea(1000,50, graphique);
        }).start();
        JOptionPane.showMessageDialog(null,new JLabel("Veuillez lancer la simulation depuis le menu Actions"),"Task failed successfully !",JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        URI nomJar = Affichage.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (args.length == 0 && nomJar.toString().endsWith("jar")) {
                // re-launch the app itselft with VM option passed
            System.out.println(nomJar.getPath());
            Runtime.getRuntime().exec(new String[] {"java", "-Xmx100m", "-jar", nomJar.getPath(), "test"}); //ça évite que le programme prenne 1Go de RAM ...
            System.exit(0);
        } //merci à internet pour l'astuce !
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        new Affichage();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(boutonAction)) {
            //System.out.println("On effectue un tour de simulation");
            //new Thread(()->{
            plateau.simuler();
            //}).start();
            //effectuer un tour
        }
        if(e.getSource().equals(cleanGraph)){graphique.clean();}


        if(e.getSource().equals(boutonStart)){plateau.startSimulation();}
        if(e.getSource().equals(boutonStop)){plateau.stopSimulation();}

        if(e.getSource().equals(plusDeVivants)){plateau.genererAlea(200,10, graphique);}
        if(e.getSource().equals(moinsDeVivants)){plateau.supprAlea(100, graphique);}

        /*if(e.getSource().equals(capturePix)){plateau.toggleCapture();}
        if(e.getSource().equals(genVid)){plateau.lancerFfmpeg();}*/
        if(e.getSource().equals(singleRefresh)){
            if(!singleRefresh.getState()){
                //plateau.setDisplay(grille);
                grille.stopRefresh();
            }else {
                grille.startRefresh();
            }
        }
        if(e.getSource().equals(vivantsTimer)){
            System.out.println("Choix de Vitesse de mort");
            String reponse = JOptionPane.showInputDialog(
                    this,
                    "Délai entre les pertes de PV (ms) :",
                    "Paramètres",
                    JOptionPane.PLAIN_MESSAGE);
            try {
                int vitesse = Integer.parseInt(reponse);
                System.out.println("Délai changé à "+vitesse);
                Vivant.DELAI_TIMER = vitesse;
                plateau.setMortDelay(vitesse);
            } catch (NumberFormatException e1){ }
        }
        if(e.getSource().equals(mLapin)) {
            grille.dernierVivantSelectionne = new Lapin();
            mPotiron.setSelected(false);
            mVide.setSelected(false);
        }
        if(e.getSource().equals(mPotiron)) {
            grille.dernierVivantSelectionne = new Potiron();
            mLapin.setSelected(false);
            mVide.setSelected(false);
        }
        if(e.getSource().equals(mVide)) {
            grille.dernierVivantSelectionne = null;
            mPotiron.setSelected(false);
            mLapin.setSelected(false);
        }
    }
}
class Icone implements Icon{
    private Color couleur;
    public Icone(Color color){couleur=color;}
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(couleur);
        g.fillRect(x,y,10,10);
    }

    @Override
    public int getIconWidth() {
        return 10;
    }

    @Override
    public int getIconHeight() {
        return 10;
    }
}

