package eu.cyfronoid.audio.player.playlist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.component.PlayingProgress.PlaybackProgressFormatter;
import eu.cyfronoid.audio.player.event.TreeSelectedEvent;
import eu.cyfronoid.audio.player.song.Song;
import eu.cyfronoid.audio.player.song.SongProperties.SongProperty;
import eu.cyfronoid.framework.format.Format;
import eu.cyfronoid.framework.util.FileUtil;
import eu.cyfronoid.gui.tableModel.CommonTableModel;
import eu.cyfronoid.gui.tableModel.TableElement;

public class Playlist extends CommonTableModel {
    private static final Logger logger = Logger.getLogger(Playlist.class);
    private static final long serialVersionUID = -4308819424320624481L;
    private static final List<String> columnNames;
    static {
         Builder<String> columnNamesBuilder = ImmutableList.<String>builder();
         for(Column column : Column.values()) {
             columnNamesBuilder.add(column.columnName);
         }
         columnNames = columnNamesBuilder.build();
    }
    private boolean isTreeSelectionListener = false;

    public Playlist() {
        this(false);
    }

    public Playlist(boolean isTreeSelectionListener) {
        super(columnNames);
        this.isTreeSelectionListener = isTreeSelectionListener;
        List<File> files = getMP3FilesFromDirectory("MusicLibrary");
        setElements(tranformToModel(files));
    }

    public static List<File> getMP3FilesFromDirectory(String dir) {
        return getMP3FilesFromDirectory(new File(dir));
    }

    public static List<File> getMP3FilesFromDirectory(File dir) {
        List<File> files = FileUtil.listFilesRecursively(dir, MP3FileFilter.INSTANCE);
        return files;
    }

    @Subscribe
    public void listChanged(TreeSelectedEvent e) throws IOException {
        if(isTreeSelectionListener) {
            setFiles(e.getNode().getMP3Files());
        }
    }

    private void setFiles(List<File> files) throws IOException {
        clearResources();
        setElements(tranformToModel(files));
    }

    private void clearResources() throws IOException {
        for(TableElement element : getAllElements()) {
            ((Song) element).close();
        }
    }

    public static List<TableElement> tranformToModel(List<File> files) {
        return Lists.newArrayList(FluentIterable.from(files).transform(new FileToSongTransform<TableElement>()).toList());
    }

    private static class FileToSongTransform<T> implements Function<File, T> {

        @SuppressWarnings("unchecked")
        @Override
        public T apply(File input) {
            try {
                return (T) new Song(input);
            } catch (UnsupportedAudioFileException | IOException e) {
                logger.warn(e);
            }
            return null;
        }

    }

    public static enum MP3FileFilter implements Predicate<File> {
        INSTANCE;

        private static final String MP3_EXTENSION = ".mp3";

        @Override
        public boolean apply(File input) {
            return input.getName().endsWith(MP3_EXTENSION);
        }


    }

    public static enum Column {
        FILE_NAME("File Name", SongProperty.ALBUM, Optional.<Format>absent()),
        TITLE("Title", SongProperty.TITLE, Optional.<Format>absent()),
        AUTHOR("Author", SongProperty.AUTHOR, Optional.<Format>absent()),
        DURATION("Duration", SongProperty.DURATION, Optional.of(PlaybackProgressFormatter.INSTANCE)),
        ALBUM("Album", SongProperty.ALBUM, Optional.<Format>absent())
        ;

        public final String columnName;
        public final SongProperty property;
        private final Optional<? extends Format> formatter;

        Column(String columnName, SongProperty property, Optional<? extends Format> formatter) {
            this.columnName = columnName;
            this.property = property;
            this.formatter = formatter;
        }

        public Object format(Object object) {
            if(formatter.isPresent()) {
                return formatter.get().format(object);
            }
            return object;
        }

    }

}
