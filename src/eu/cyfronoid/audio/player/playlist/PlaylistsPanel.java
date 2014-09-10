package eu.cyfronoid.audio.player.playlist;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.MusicLibraryTree;
import eu.cyfronoid.audio.player.component.PlaylistTable;
import eu.cyfronoid.audio.player.resources.ActualViewSettings;
import eu.cyfronoid.audio.player.song.library.SongLibraryNode;
import eu.cyfronoid.framework.util.ExceptionHelper;

public class PlaylistsPanel extends JTabbedPane {
    private static final long serialVersionUID = -1380190314932473388L;
    public static final String TREE_SELECTION_LISTENER_NAME = "Selection Listener";
    private static final Logger logger = Logger.getLogger(PlaylistsPanel.class);
    private static final String NEW_TAB_PREFIX = "New Tab ";
    private Map<Integer, PlaylistTable> tablePerTab = Maps.newHashMap();
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private BiMap<Integer, String> openedPlaylistTables = HashBiMap.create();
    private Set<String> unknownTabNames = Sets.newHashSet();
    private long unknownIndex = 0;
    private JTable activeTable;
    private Map<Integer, Playlist> openedPlaylists = Maps.newHashMap();

    public PlaylistsPanel() throws IOException {
        super(JTabbedPane.TOP);
        setAutoscrolls(true);
        createTreeSelectionListenerTab();
        eventBus.register(tablePerTab.get(0));  // above statement guaranteed that there is at leased one item
        activeTable = tablePerTab.get(0);

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
            } catch (IOException e) {
                logger.warn(ExceptionHelper.getStackTrace(e));
            }
        }

        setSelectedIndex(i);
    }

    private void createTreeSelectionListenerTab() throws IOException {
        createTab(Optional.<Playlist>absent());
    }

    public void openTab(File file) throws IOException {
        Optional<Playlist> playlist = Playlist.loadPlaylist(file);
        if(!playlist.isPresent()) {
            JOptionPane.showMessageDialog(this, "Wrong file structure " + file);
            return;
        }
        String name = playlist.get().getName();
        if(openedPlaylistTables.containsValue(name)) {
            JOptionPane.showMessageDialog(this, "Cannot open two playlists with the same name " + name);
            return;
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
            playlistTable = PlaylistTable.create();
            playlistTable.setFiles(playlist.get().getOrderedSongs().values());
            name = establishName(playlist.get().getName());
            openedPlaylists.put(tabCount, playlist.get());
        } else {
            playlistTable = PlaylistTable.createSelectionListener();
            name = TREE_SELECTION_LISTENER_NAME;
        }

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        scrollPane.setBorder(null);
        scrollPane.setToolTipText("");

        tablePerTab.put(tabCount, playlistTable);

        playlistTable.setTableName(name);
        openedPlaylistTables.put(tabCount, name);
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
        int selectedIndex = getSelectedIndex();
        if(selectedIndex == -1) {
            return Optional.absent();
        }
        Preconditions.checkArgument(tablePerTab.containsKey(selectedIndex));
        return Optional.of(tablePerTab.get(selectedIndex));
    }

    public void updateTabsLabels() {
        for(int i = 1; i < getTabCount(); ++i) {
            updateTabLabel(i);
        }
    }

    private void updateTabLabel(int i) {
        setTitleAt(i, tablePerTab.get(i).toString());
    }

    public Collection<Playlist> getOpenedPlaylists() {
        return openedPlaylists.values();
    }

    public boolean areAllSaved() {
        for(PlaylistTable playlistTable : tablePerTab.values()) {
            if(playlistTable.hasUnsavedModifications()) {
                return false;
            }
        }
        return true;
    }

}
