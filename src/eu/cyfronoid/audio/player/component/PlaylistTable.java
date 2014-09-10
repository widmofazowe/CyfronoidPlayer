package eu.cyfronoid.audio.player.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.event.Events;
import eu.cyfronoid.audio.player.event.Events.PlaylistSaveEvent;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.event.SongFinishedEvent;
import eu.cyfronoid.audio.player.playlist.Playlist;
import eu.cyfronoid.audio.player.playlist.PlaylistTableModel;
import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.audio.player.song.Song;
import eu.cyfronoid.framework.util.ExceptionHelper;
import eu.cyfronoid.gui.file.ExtensionFilter;
import eu.cyfronoid.gui.file.FileDialogBuilder;
import eu.cyfronoid.gui.tableModel.TableElement;

public class PlaylistTable extends JTable {
    private static final long serialVersionUID = -1614591239377223113L;
    private static final Logger logger = Logger.getLogger(PlaylistTable.class);
    private PlaylistTableModel playlistTableModel;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private boolean isTreeSelectionListener;
    private boolean hasUnsavedModifications = false;
    private String tableName;
    private final Playlist playlist;

    public static PlaylistTable createSelectionListener() {
        return new PlaylistTable(true, null);
    }

    public static PlaylistTable create(Playlist playlist) {
        Preconditions.checkNotNull(playlist);
        return new PlaylistTable(false, playlist);
    }

    private PlaylistTable(boolean isTreeSelectionListener, Playlist playlist) {
        this.isTreeSelectionListener = isTreeSelectionListener;
        this.playlist = playlist;
        prepareTableModel(isTreeSelectionListener);
    }

    private void prepareTableModel(boolean isTreeSelectionListener) {
        playlistTableModel = new PlaylistTableModel(isTreeSelectionListener);
        if(isTreeSelectionListener) {
            eventBus.register(playlistTableModel);
        }
        setModel(playlistTableModel);
        addMouseListener(new PlaylistPopupListener());
    }

    public void setFiles(Collection<File> files) throws IOException {
        setHasUnsavedModifications(true);
        playlistTableModel.setFiles(files);
    }

    public void addFiles(Collection<File> files) throws IOException {
        setHasUnsavedModifications(true);
        playlistTableModel.addFiles(files);
    }

    @Subscribe
    public void saveEvent(PlaylistSaveEvent event) {
        save();
    }

    public void save() {
        boolean isSaved = savePlaylist();
        setHasUnsavedModifications(!isSaved);
        eventBus.post(Events.updateTabsLabels);
    }

    public boolean savePlaylist() {
        File saveFile;
        if(playlist.getFile() != null) {
            saveFile = playlist.getFile();
        } else {
            JFileChooser fileChooser = FileDialogBuilder.create().withFilter(ExtensionFilter.xml).build();
            int result = fileChooser.showSaveDialog(null);
            if(result == JFileChooser.APPROVE_OPTION) {
                saveFile = fileChooser.getSelectedFile();
            } else {
                return false;
            }
        }
        playlist.setName(tableName);
        logger.debug("Saving playlist " + tableName + " to " + saveFile.getAbsoluteFile());
        playlist.setOrderedSongs(createOrderedFileList());
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Playlist.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(playlist, saveFile);
            return true;
        } catch (JAXBException e) {
            logger.error(ExceptionHelper.getStackTrace(e));
        }
        return false;
    }

    private SortedMap<Integer, File> createOrderedFileList() {
        List<TableElement> allElements = playlistTableModel.getAllElements();
        SortedMap<Integer, File> orderedSongs = Maps.newTreeMap();
        int i = 0;
        for(TableElement element : allElements) {
            orderedSongs.put(i++, ((Song) element).getFile());
        }

        return orderedSongs;
    }

    @Subscribe
    public void receive(SongFinishedEvent event) {
        Optional<Integer> modelElementIndex = getModelElementIndex();
        if(!modelElementIndex.isPresent()) {
            return;
        }
        int nextIndex = modelElementIndex.get()+1;
        if(nextIndex < playlistTableModel.getRowCount()) {
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
        return (Song) playlistTableModel.get(i);
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
