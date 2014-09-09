package eu.cyfronoid.audio.player.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.event.SongFinishedEvent;
import eu.cyfronoid.audio.player.playlist.PlaylistTableModel;
import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.audio.player.song.Song;

public class PlaylistTable extends JTable {
    private static final long serialVersionUID = -1614591239377223113L;
    private PlaylistTableModel playlist;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private boolean isTreeSelectionListener;
    private boolean hasUnsavedModifications = false;
    private String tableName;

    public PlaylistTable() {
        this(false);
    }

    public PlaylistTable(boolean isTreeSelectionListener) {
        this.isTreeSelectionListener = isTreeSelectionListener;
        playlist = new PlaylistTableModel(isTreeSelectionListener);
        if(isTreeSelectionListener) {
            eventBus.register(playlist);
        }
        setModel(playlist);
        addMouseListener(new PlaylistPopupListener());
    }

    public void setFiles(Collection<File> files) throws IOException {
        setHasUnsavedModifications(true);
        playlist.setFiles(files);
    }

    public void addFiles(Collection<File> files) throws IOException {
        setHasUnsavedModifications(true);
        playlist.addFiles(files);
    }

    public void save() {
        setHasUnsavedModifications(false);
        //TODO
    }

    @Subscribe
    public void receive(SongFinishedEvent event) {
        Optional<Integer> modelElementIndex = getModelElementIndex();
        if(!modelElementIndex.isPresent()) {
            return;
        }
        int nextIndex = modelElementIndex.get()+1;
        if(nextIndex < playlist.getRowCount()) {
            changeSelection(getSelectedRow()+1, getSelectedColumn(), false, false);
            Song song = getSong(nextIndex);
            eventBus.post(new SongChangeEvent(song));
        }
    }

    private class PlaylistPopupListener extends MouseAdapter {

        private JPopupMenu popup;

        PlaylistPopupListener() {
            popup = new JPopupMenu();
            JMenuItem playMenuItem = new JMenuItem(Resources.PLAYER.get(PropertyKey.PLAY));
            playMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    play();
                }

            });
            popup.add(playMenuItem );
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                play();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger()){
                JTable source = (JTable)e.getSource();
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());

                if(!source.isRowSelected(row)) {
                    source.changeSelection(row, column, false, false);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    private void play() {
        Optional<Song> song = getSelectedSong();
        if(song.isPresent()) {
            eventBus.post(new SongChangeEvent(song.get()));
        }
    }

    private Optional<Song> getSelectedSong() {
        Optional<Integer> modelElementIndex = getModelElementIndex();
        if(!modelElementIndex.isPresent()) {
            return Optional.absent();
        }
        int index = modelElementIndex.get();
        Song song = getSong(index);
        return Optional.of(song);
    }

    private Song getSong(int i) {
        return (Song) playlist.get(i);
    }

    private Optional<Integer> getModelElementIndex() {
        int selectedRow = getSelectedRow();
        if(selectedRow < 0) {
            return Optional.absent();
        }
        int modelElementIndex = convertRowIndexToModel(selectedRow);
        return Optional.of(modelElementIndex);
    }

    public boolean isTreeSelectionListener() {
        return isTreeSelectionListener;
    }

    public boolean hasUnsavedModifications() {
        return hasUnsavedModifications;
    }

    public void setHasUnsavedModifications(boolean hasUnsavedModifications) {
        this.hasUnsavedModifications = hasUnsavedModifications;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName + ((hasUnsavedModifications) ? " *" : "");
    }

}
