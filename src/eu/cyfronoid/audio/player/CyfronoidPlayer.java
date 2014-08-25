package eu.cyfronoid.audio.player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.component.Loudness;
import eu.cyfronoid.audio.player.component.PlayingProgress;
import eu.cyfronoid.audio.player.component.PlaylistTable;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.Icons;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.audio.player.song.Song;
import eu.cyfronoid.framework.util.ExceptionHelper;
import eu.cyfronoid.gui.action.CommonActionListener;

public class CyfronoidPlayer extends JFrame {
    private static final long serialVersionUID = -6864245175069172853L;
    private static final Logger logger = Logger.getLogger(CyfronoidPlayer.class);
    private JButton playPauseButton;
    private MusicPlayer musicPlayer = new MusicPlayer();
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private PlaylistTable playlistTable;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    CyfronoidPlayer window = new CyfronoidPlayer();
                    window.musicPlayer.setSong(new Song("Nightmare.mp3"));
                    window.setVisible(true);
                } catch (Exception e) {
                    logger.error(e + " " + ExceptionHelper.getStackTrace(e));
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public CyfronoidPlayer() {
        setPreferredSize(new Dimension(750, 400));
        setMinimumSize(new Dimension(750, 400));
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setBounds(100, 100, 750, 400);
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
        panel.setLayout(new BorderLayout(0, 0));

        playlistTable = new PlaylistTable();
        JScrollPane tableScrollPane = new JScrollPane(playlistTable);
        tableScrollPane.setAutoscrolls(true);
        panel.add(tableScrollPane);

        JTree tree = new JTree();
        panel.add(tree, BorderLayout.WEST);

        JPanel songPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) songPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        layeredPane.add(songPanel, BorderLayout.NORTH);

        JButton previousSongButton = createButton(Icons.LEFT_ARROW, getLabelFor(PropertyKey.PREVIOUS));
        songPanel.add(previousSongButton);

        playPauseButton = createButton(Icons.PLAY_ARROW, getLabelFor(PropertyKey.PLAY));
        playPauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.togglePlay();
                if(musicPlayer.isPlaying()) {
                    playPauseButton.setIcon(Icons.PAUSE_ARROW.getImageIcon());
                } else {
                    playPauseButton.setIcon(Icons.PLAY_ARROW.getImageIcon());
                }
            }
        });
        songPanel.add(playPauseButton);

        JButton nextSongButton = createButton(Icons.RIGHT_ARROW, getLabelFor(PropertyKey.NEXT));
        songPanel.add(nextSongButton);

        songPanel.add(new Loudness());
        PlayingProgress playingProgress = new PlayingProgress();
        eventBus.register(playingProgress);
        eventBus.register(musicPlayer);
        eventBus.register(playlistTable);
        eventBus.register(this);
        songPanel.add(playingProgress);

        addWindowListener(new PlayerWindowListener());
    }

    @Subscribe
    public void songChanged(SongChangeEvent event) {
        setTitle(PlayerConfigurator.APP_NAME + " - " + event.getSong());
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

    private static class PlayerWindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent evt) {
            logger.debug("Closing application");
            System.exit(0);
        }

    }


}
