package eu.cyfronoid.audio.player.component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.playlist.Playlist;
import eu.cyfronoid.audio.player.song.Song;

public class PlaylistTable extends JTable {
    private static final long serialVersionUID = -1614591239377223113L;
    private Playlist playlist;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);

    public PlaylistTable() {
        playlist = new Playlist();
        setModel(playlist);
        addMouseListener(new PopupListener(null));
    }

    private class PopupListener extends MouseAdapter {

        private JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                Optional<Integer> modelElementIndex = getModelElementIndex();
                if(!modelElementIndex.isPresent()) {
                    return;
                }
                int index = modelElementIndex.get();
                Song song = getSong(index);
                eventBus.post(new SongChangeEvent(song));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()){


            }
        }

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
}
