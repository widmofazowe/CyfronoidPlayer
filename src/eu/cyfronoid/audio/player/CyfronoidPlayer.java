package eu.cyfronoid.audio.player;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.Icons;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.gui.action.CommonActionListener;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLayeredPane;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import java.awt.FlowLayout;

import javax.swing.JButton;

public class CyfronoidPlayer extends JFrame {
    private static final long serialVersionUID = -6864245175069172853L;
    private static final Logger logger = Logger.getLogger(CyfronoidPlayer.class);
    private JButton playPauseButton;

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
        setBounds(100, 100, 586, 397);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(PlayerConfigurator.APPLICATION_ICON.getImage());
        setTitle(PlayerConfigurator.APP_NAME);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu(getLabelFor(PropertyKey.FILE_MENU));
        menuBar.add(fileMenu);

        JMenuItem exitMenuItem = new JMenuItem(getLabelFor(PropertyKey.EXIT));
        exitMenuItem.addActionListener(CommonActionListener.SEND_CLOSE_EVENT.get(this));
        fileMenu.add(exitMenuItem);

        JMenu helpMenu = new JMenu(getLabelFor(PropertyKey.HELP_MENU));
        menuBar.add(helpMenu);

        JMenuItem aboutMenuItem = new JMenuItem(getLabelFor(PropertyKey.ABOUT));
        aboutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PlayerConfigurator.ABOUT_DIALOG.open();
            }
        });
        helpMenu.add(aboutMenuItem);

        JLayeredPane layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane, BorderLayout.CENTER);
        layeredPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        layeredPane.add(panel, BorderLayout.CENTER);

        JPanel songPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) songPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        layeredPane.add(songPanel, BorderLayout.NORTH);

        JButton previousSongButton = createButton(Icons.LEFT_ARROW, getLabelFor(PropertyKey.PREVIOUS));
        songPanel.add(previousSongButton);

        playPauseButton = createButton(Icons.PLAY_ARROW, getLabelFor(PropertyKey.PLAY));
        songPanel.add(playPauseButton);

        JButton nextSongButton = createButton(Icons.RIGHT_ARROW, getLabelFor(PropertyKey.NEXT));
        songPanel.add(nextSongButton);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                logger.debug("Closing application");
                System.exit(0);
            }

        });
    }

    private JButton createButton(Icons icon, String tooltipKey) {
        JButton button = new JButton("");
        button.setBorderPainted(false);
        button.setToolTipText(tooltipKey);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setSize(new Dimension(24, 24));
        button.setPreferredSize(new Dimension(24, 24));
        button.setMinimumSize(new Dimension(24, 24));
        button.setMaximumSize(new Dimension(24, 24));
        button.setIcon(icon.getImageIcon());
        return button;
    }

    private String getLabelFor(String propertyKey, Object... arguments) {
        return Resources.PLAYER.get(propertyKey, arguments);
    }

}
