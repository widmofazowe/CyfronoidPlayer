package eu.cyfronoid.audio.player;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class CyfronoidPlayer extends JFrame {
    private static final long serialVersionUID = -6864245175069172853L;
    private static final Logger logger = Logger.getLogger(CyfronoidPlayer.class);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    CyfronoidPlayer window = new CyfronoidPlayer();
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public CyfronoidPlayer() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setBounds(100, 100, 450, 300);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(PlayerConfigurator.APPLICATION_ICON);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                logger.debug("Closing application");
                System.exit(0);
            }

        });
    }

}
