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
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.component.Loudness;
import eu.cyfronoid.audio.player.component.MusicLibraryTree;
import eu.cyfronoid.audio.player.component.PlayingProgress;
import eu.cyfronoid.audio.player.dsp.AnalyzerDialog;
import eu.cyfronoid.audio.player.event.Events.NewPlaylistEvent;
import eu.cyfronoid.audio.player.event.Events.PlaylistOpenDialogShowEvent;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.playlist.Playlist;
import eu.cyfronoid.audio.player.playlist.PlaylistsPanel;
import eu.cyfronoid.audio.player.resources.ActualViewSettings;
import eu.cyfronoid.audio.player.resources.DefaultSettings;
import eu.cyfronoid.audio.player.resources.Resources.Icons;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.audio.player.song.Song;
import eu.cyfronoid.framework.scheduler.Scheduler;
import eu.cyfronoid.framework.scheduler.ThreadsDump;
import eu.cyfronoid.framework.util.ExceptionHelper;
import eu.cyfronoid.gui.tree.TreeState;

public class CyfronoidPlayer extends JFrame {
    private static final long serialVersionUID = -6864245175069172853L;
    private static final Logger logger = Logger.getLogger(CyfronoidPlayer.class);
    private JButton playPauseButton;
    private MusicPlayer musicPlayer = PlayerConfigurator.injector.getInstance(MusicPlayer.class);
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private MusicLibraryTree tree;
    private AnalyzerDialog analyzerPanel;
    private PlaylistsPanel playlistsPanel;
    private Scheduler scheduler;

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
                    logger.error(e + " " + ExceptionHelper.getStackTrace(e));
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws IOException
     */
    public CyfronoidPlayer() throws IOException {
        Dimension windowDimension = PlayerConfigurator.SETTINGS.getWindowDimension();
        Dimension defaultDimension = DefaultSettings.INSTANCE.getDimension();
        if(windowDimension == null) {
            windowDimension = defaultDimension;
            PlayerConfigurator.SETTINGS.setWindowDimension(defaultDimension);
        }
        setPreferredSize(windowDimension);
        setSize(windowDimension);
        setMinimumSize(defaultDimension);
        initialize();
        scheduler = PlayerConfigurator.injector.getInstance(Scheduler.class);
        scheduler.startTask(new ThreadsDump());
    }

    /**
     * Initialize the contents of the frame.
     * @throws IOException
     */
    private void initialize() throws IOException {
        setBounds(100, 100, 750, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(PlayerConfigurator.APPLICATION_ICON.getImage());
        setTitle(PlayerConfigurator.APP_NAME);

        PlayerMenu playerMenu = new PlayerMenu();
        setJMenuBar(playerMenu);

        JLayeredPane layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane, BorderLayout.CENTER);
        layeredPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        layeredPane.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));

        playlistsPanel = new PlaylistsPanel();
        panel.add(playlistsPanel);

        tree = new MusicLibraryTree();
        JScrollPane musicLibraryScrollPane = new JScrollPane(tree);
        musicLibraryScrollPane.setAutoscrolls(true);
        panel.add(musicLibraryScrollPane, BorderLayout.WEST);

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
                setPlayPauseButtonImage();
            }
        });
        songPanel.add(playPauseButton);

        JButton nextSongButton = createButton(Icons.RIGHT_ARROW, getLabelFor(PropertyKey.NEXT));
        songPanel.add(nextSongButton);

        songPanel.add(new Loudness());
        PlayingProgress playingProgress = new PlayingProgress();
        eventBus.register(playingProgress);
        eventBus.register(musicPlayer);

        eventBus.register(this);
        songPanel.add(playingProgress);

        addWindowListener(new PlayerWindowListener());

        analyzerPanel = AnalyzerDialog.open();
        eventBus.register(analyzerPanel);
        musicPlayer.setAnalyzer(analyzerPanel);
    }

    @Subscribe
    public void songChanged(SongChangeEvent event) {
        setPlayPauseButtonImage();
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
        return PlayerConfigurator.getLabelFor(propertyKey, arguments);
    }

    private void setPlayPauseButtonImage() {
        if(musicPlayer.isPlaying()) {
            playPauseButton.setIcon(Icons.PAUSE_ARROW.getImageIcon());
        } else {
            playPauseButton.setIcon(Icons.PLAY_ARROW.getImageIcon());
        }
    }

    @Subscribe
    public void newPlaylist(NewPlaylistEvent event) throws IOException {
        playlistsPanel.createNewEmptyTab();
    }

    @Subscribe
    public void openPlaylist(PlaylistOpenDialogShowEvent event) {
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(CyfronoidPlayer.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            logger.info("Opening: " + file.getName() + ".");
            try {
                playlistsPanel.openTab(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(CyfronoidPlayer.this, "File " + file + " is not correct format of playlist.");
                logger.warn("Problem while loading playlist " + file);
            }
        }
    }

    private class PlayerWindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent evt) {
            Collection<Playlist> openedPlaylists = playlistsPanel.getOpenedPlaylists();
            ActualViewSettings viewSettings = getViewSettings();
            if(!playlistsPanel.areAllSaved()) {
                logger.debug("There are unsaved playlists.");
            }
            List<String> files = FluentIterable.from(openedPlaylists).transform(PlaylistsPath.INSTANCE).toList();
            viewSettings.setOpenedPlaylists(files);
            viewSettings.setSelectedTab(playlistsPanel.getSelectedIndex());

            Optional<Song> actualSong = musicPlayer.getActualSong();

            if(actualSong.isPresent()) {
                viewSettings.setSelectedSong(actualSong.get());
            }
//            TreePath selectionPath = tree.getSelectionPath();
//
//            if(selectionPath != null) {
//                actualSelections.setSelectedTreePath(selectionPath);
//            }

            TreeState state = new TreeState(tree);
            viewSettings.setExpansionState(state.getExpansionState());

            PlayerConfigurator.SETTINGS.setWindowDimension(getSize());
            PlayerConfigurator.saveSettings();

            scheduler.stop();
            logger.debug("Closing application");
            System.exit(0);
        }

        private ActualViewSettings getViewSettings() {
            ActualViewSettings viewSettings = PlayerConfigurator.SETTINGS.getActualViewSettings();
            if(viewSettings == null) {
                viewSettings = new ActualViewSettings();
                PlayerConfigurator.SETTINGS.setActualViewSettings(viewSettings);
            }
            return viewSettings;
        }

    }

    private static enum PlaylistsPath implements Function<Playlist, String> {
        INSTANCE;

        @Override
        public String apply(Playlist input) {
            return input.getFile().getAbsolutePath();
        }
    }


}
