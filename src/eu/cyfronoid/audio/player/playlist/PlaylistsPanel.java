package eu.cyfronoid.audio.player.playlist;

import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.PlaylistTable;
import eu.cyfronoid.framework.validator.annotation.NotNull;

public class PlaylistsPanel extends JTabbedPane {
    private static final long serialVersionUID = -1380190314932473388L;
    private static final String NEW_TAB_PREFIX = "New Tab ";
    private Map<Integer, JTable> tablePerTab = Maps.newHashMap();
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private BiMap<Integer, String> openedPlaylistTables = HashBiMap.create();
    private Set<String> unknownTabNames = Sets.newHashSet();
    private long unknownIndex = 0;
    private JTable activeTable;

    public PlaylistsPanel() {
        super(JTabbedPane.TOP);
        setAutoscrolls(true);
        createTab(PlaylistTabParameters.createSelectionListener());
        activeTable = tablePerTab.get(0);  // above statement guaranteed that there is at leased one item

        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Optional<JTable> newPlaylistTable = getSelectedPlaylistTable();
                if(newPlaylistTable.isPresent()) {
                    eventBus.unregister(activeTable);
                    activeTable = newPlaylistTable.get();
                    eventBus.register(activeTable);
                }
            }
        });
    }

    protected Optional<JTable> getSelectedPlaylistTable() {
        return Optional.absent();
    }

    public void openTab() {

    }

    public void createNewEmptyTab() {
        createTab(PlaylistTabParameters.EMPTY_TAB);
    }

    private void createTab(@NotNull PlaylistTabParameters playlistTabParameters) {
        Preconditions.checkNotNull(playlistTabParameters);
        PlaylistTable playlistTable = new PlaylistTable(playlistTabParameters.isTreeSelectionListener());
        JScrollPane scrollPane = new JScrollPane(playlistTable);
        scrollPane.setBorder(null);
        scrollPane.setToolTipText("");
        int tabCount = getTabCount();
        tablePerTab.put(tabCount, playlistTable);
        String name = establishName(playlistTabParameters.getName());
        openedPlaylistTables.put(tabCount, name);
        addTab(name, null, scrollPane, null);
        if(name.startsWith(NEW_TAB_PREFIX)) {
            unknownTabNames.add(name);
        }
    }

    private String establishName(Optional<String> name) {
        if(name.isPresent()) {
            return name.get();
        }

        String newName;
        do {
            newName = NEW_TAB_PREFIX + unknownIndex++;
        } while(unknownTabNames.contains(newName));

        return newName;
    }
}
