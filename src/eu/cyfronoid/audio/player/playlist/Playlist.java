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

import eu.cyfronoid.audio.player.component.PlayingProgress.PlaybackProgressFormatter;
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

    public Playlist() {
        super(columnNames);
        List<File> files = FileUtil.listFilesRecursively(new File("MusicLibrary"), MP3FileFilter.INSTANCE);
        setElements(Lists.newArrayList(FluentIterable.from(files).transform(FileToSongTransform.INSTANCE).toList()));
    }

    private static enum FileToSongTransform implements Function<File, TableElement> {
        INSTANCE;

        @Override
        public TableElement apply(File input) {
            try {
                return (TableElement) new Song(input);
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
