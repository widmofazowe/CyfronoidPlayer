package eu.cyfronoid.audio.player.playlist;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.MusicLibraryTree;
import eu.cyfronoid.audio.player.component.PlaylistTable;
import eu.cyfronoid.audio.player.event.Events;
import eu.cyfronoid.audio.player.event.Events.UpdateTabsLabelsEvent;
import eu.cyfronoid.audio.player.resources.ActualViewSettings;
import eu.cyfronoid.audio.player.resources.OpeningException;
import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.audio.player.song.library.SongLibraryNode;
import eu.cyfronoid.framework.util.ExceptionHelper;

public class PlaylistsPanel extends JTabbedPane {
    private static final long serialVersionUID = -1380190314932473388L;
    public static final String TREE_SELECTION_LISTENER_NAME = "Selection Listener";
    private static final Logger logger = Logger.getLogger(PlaylistsPanel.class);
    private static final String NEW_TAB_PREFIX = "New Tab ";
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private Set<String> unknownTabNames = Sets.newHashSet();
    private long unknownIndex = 0;
    private PlaylistTable activeTable;

    public PlaylistsPanel() {
        super(JTabbedPane.TOP);
        setAutoscrolls(true);
        try {
            createTreeSelectionListenerTab();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(0));
        eventBus.register(playlistTable.get());
        activeTable = playlistTable.get();

        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Optional<PlaylistTable> newPlaylistTable = getSelectedPlaylistTable();
                if(newPlaylistTable.isPresent()) {
                    eventBus.unregister(activeTable);
                    activeTable = newPlaylistTable.get();
                    eventBus.register(activeTable);
                }
            }
        });

        new MusicTreeDropTargetListener(this);
        addMouseListener(new PlaylistPanelPopupListener());
        openPlaylistsFromSettings();
    }

    private void openPlaylistsFromSettings() {
        ActualViewSettings actualViewSettings = PlayerConfigurator.SETTINGS.getActualViewSettings();
        if(actualViewSettings == null) {
            return;
        }
        List<String> openedPlaylistsPaths = actualViewSettings.getOpenedPlaylists();
        if(openedPlaylistsPaths == null) {
            return;
        }

        int i = 0;
        int selectedTab = actualViewSettings.getSelectedTab();
        for(String path : openedPlaylistsPaths) {
            try {
                openTab(new File(path));
                if(selectedTab > i) {
                    i++;
                }
            } catch (IOException | OpeningException e) {
                logger.warn(ExceptionHelper.getStackTrace(e));
            }
        }

        setSelectedIndex(i);
    }

    private void createTreeSelectionListenerTab() throws IOException {
        createTab(Optional.<Playlist>absent());
    }

    public void openTab(File file) throws IOException, OpeningException {
        Optional<Playlist> playlist = Playlist.loadPlaylist(file);
        if(!playlist.isPresent()) {
            throw new OpeningException();
        }
        playlist.get().setFile(file);
        createTab(playlist);
    }

    public void createNewEmptyTab() throws IOException {
        createTab(Optional.of(new Playlist()));
    }

    private void createTab(Optional<Playlist> playlist) throws IOException {
        String name;
        PlaylistTable playlistTable;
        int tabCount = getTabCount();
        if(playlist.isPresent()) {
            playlistTable = PlaylistTable.create(playlist.get());
            playlistTable.setFiles(playlist.get().getOrderedSongs().values());
            name = establishName(playlist.get().getName());
        } else {
            playlistTable = PlaylistTable.createSelectionListener();
            name = TREE_SELECTION_LISTENER_NAME;
        }

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        scrollPane.setBorder(null);
        scrollPane.setToolTipText("");

        playlistTable.setTableName(name);
        playlistTable.forceNoUnsavedModifications();
        addTab(name, null, scrollPane, null);
        if(name.startsWith(NEW_TAB_PREFIX)) {
            unknownTabNames.add(name);
        }
        setSelectedIndex(tabCount);
    }

    private String establishName(String name) {
        if(name != null) {
            return name;
        }

        String newName;
        do {
            newName = NEW_TAB_PREFIX + unknownIndex++;
        } while(unknownTabNames.contains(newName));
        unknownTabNames.add(newName);
        return newName;
    }

    private class MusicTreeDropTargetListener extends DropTargetAdapter {

        public MusicTreeDropTargetListener(PlaylistsPanel panel) {
            new DropTarget(panel, DnDConstants.ACTION_COPY, this, true, null);
        }

        @Override
        public void drop(DropTargetDropEvent event) {

            try {
                Transferable tr = event.getTransferable();
                Optional<PlaylistTable> selectedPlaylistTable = getSelectedPlaylistTable();
                int selectedIndex = getSelectedIndex();
                if(!selectedPlaylistTable.isPresent()) {
                    return;
                }
                PlaylistTable playlistTable = selectedPlaylistTable.get();
                if(playlistTable.isTreeSelectionListener()) {
                    logger.debug("Dropping files to selection listener is not supported.");
                    return;
                }
                DefaultMutableTreeNode[] transferData = (DefaultMutableTreeNode[]) tr.getTransferData(MusicLibraryTree.NODES_FLAVOR);
                for(DefaultMutableTreeNode node : transferData) {
                    SongLibraryNode userObject = (SongLibraryNode) node.getUserObject();
                    logger.debug("Droped " + userObject + " to Active Playlist");
                    playlistTable.addFiles(userObject.getMP3Files());
                    updateTabLabel(selectedIndex);
                }

            } catch (Exception e) {
                logger.warn(ExceptionHelper.getStackTrace(e));
            }
        }
    }

    protected Optional<PlaylistTable> getSelectedPlaylistTable() {
        return convertComponentToPlaylistTable(getSelectedComponent());
    }

    @Subscribe
    public void updateTabsLabels(UpdateTabsLabelsEvent event) {
        updateTabsLabels();
    }


    public void updateTabsLabels() {
        for(int i = 1; i < getTabCount(); ++i) {
            updateTabLabel(i);
        }
    }

    private void updateTabLabel(int i) {
        Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(i));
        if(playlistTable.isPresent()) {
            setTitleAt(i, playlistTable.get().toString());
        }
    }

    public Collection<Playlist> getOpenedSavedPlaylists() {
        Collection<Playlist> openedPlaylists = Lists.newArrayList();
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(i));
            if(playlistTable.isPresent() && !playlistTable.get().hasUnsavedModifications()) {
                Playlist playlist = playlistTable.get().getPlaylist();
                if(playlist != null) {
                    openedPlaylists.add(playlist);
                }
            }
        }
        return openedPlaylists;
    }

    private Optional<PlaylistTable> convertComponentToPlaylistTable(Component component) {
        JScrollPane selectedComponent = (JScrollPane) component;
        if(selectedComponent == null) {
            return Optional.absent();
        }
        JViewport viewport = selectedComponent.getViewport();
        return Optional.fromNullable((PlaylistTable)viewport.getView());
    }

    public boolean areAllSaved() {
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(i));
            if(playlistTable.isPresent() && playlistTable.get().hasUnsavedModifications()) {
                return false;
            }
        }
        return true;
    }

    public void triggerSaveOnAllUnsaved() {
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(i));
            if(playlistTable.isPresent()) {
                playlistTable.get().save();
            }
        }
    }

    private class PlaylistPanelPopupListener extends MouseAdapter {
        private JPopupMenu popup;

        PlaylistPanelPopupListener() {
            popup = new JPopupMenu();

            JMenuItem renameMenuItem = new JMenuItem(Resources.PLAYER.get(PropertyKey.RENAME));
            renameMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Optional<PlaylistTable> selectedPlaylistTable = PlaylistsPanel.this.getSelectedPlaylistTable();
                    if(!selectedPlaylistTable.isPresent()) {
                        return;
                    }
                    PlaylistTable playlistTable = selectedPlaylistTable.get();
                    String newName = (String) JOptionPane.showInputDialog(null, Resources.PLAYER.get(PropertyKey.NEW_NAME), Resources.PLAYER.get(PropertyKey.NEW_NAME), JOptionPane.QUESTION_MESSAGE, null, null, playlistTable.getTableName());

                    if(newName != null && !newName.trim().equals("")) {
                        playlistTable.setTableName(newName);
                        updateTabLabel(PlaylistsPanel.this.getSelectedIndex());
                    }
                }

            });
            popup.add(renameMenuItem);

            JMenuItem saveMenuItem = new JMenuItem(Resources.PLAYER.get(PropertyKey.SAVE));
            saveMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    eventBus.post(Events.playlistSave);
                }

            });
            popup.add(saveMenuItem);

            JMenuItem closeMenuItem = new JMenuItem(Resources.PLAYER.get(PropertyKey.CLOSE));
            closeMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = PlaylistsPanel.this.getSelectedIndex();
                    if(index > 0) {
                        Optional<PlaylistTable> selectedPlaylistTable = PlaylistsPanel.this.getSelectedPlaylistTable();
                        if(selectedPlaylistTable.isPresent()) {
                            final PlaylistTable playlistTable = selectedPlaylistTable.get();
                            boolean isCanceled = false;
                            if(playlistTable.hasUnsavedModifications()) {
                                isCanceled = savePlaylistDialog(new Action() {

                                    @Override
                                    public void execute() {
                                        playlistTable.save();
                                    }

                                });
                            }
                            if(isCanceled) {
                                return;
                            }
                            playlistTable.clearResources();
                        }
                        PlaylistsPanel.this.setSelectedIndex(index-1);
                        PlaylistsPanel.this.removeTabAt(index);
                    }
                }

            });
            popup.add(closeMenuItem);

            JMenuItem closeAllMenuItem = new JMenuItem(Resources.PLAYER.get(PropertyKey.CLOSE_ALL));
            closeAllMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    saveIfModified();
                    int count = getTabCount();
                    boolean isCanceled = saveIfModified();
                    if(isCanceled) {
                        return;
                    }
                    for (int i = count-1; i > 0; i--) {
                        Optional<PlaylistTable> playlistTable = convertComponentToPlaylistTable(getComponentAt(i));
                        if(playlistTable.isPresent()) {
                            playlistTable.get().clearResources();
                        }
                        PlaylistsPanel.this.setSelectedIndex(i-1);
                        PlaylistsPanel.this.removeTabAt(i);
                    }
                }
            });
            popup.add(closeAllMenuItem);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger() && getSelectedIndex() != 0){
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    public boolean saveIfModified() {
        boolean isCanceled = false;
        if(!areAllSaved()) {
            logger.debug("There are unsaved playlists.");
            isCanceled = savePlaylistDialog(new Action() {

                @Override
                public void execute() {
                    triggerSaveOnAllUnsaved();
                }

            });
        }
        return isCanceled;
    }

    /**
     *
     * @param action
     * @return boolean true if cancel or exit option is clicked and execution should stop
     */
    private boolean savePlaylistDialog(Action action) {
        int result = JOptionPane.showConfirmDialog(null, Resources.PLAYER.get(PropertyKey.EXIT_WITH_UNSAVED), "Save Playlists", JOptionPane.YES_NO_CANCEL_OPTION);
        if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return true;
        }
        if(result == JOptionPane.OK_OPTION) {
            action.execute();
        }
        return false;
    }

    private static interface Action {
        void execute();
    }

}
