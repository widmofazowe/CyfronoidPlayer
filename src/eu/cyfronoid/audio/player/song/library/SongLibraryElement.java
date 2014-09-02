package eu.cyfronoid.audio.player.song.library;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.common.collect.ImmutableList;

import eu.cyfronoid.audio.player.song.Song;

public class SongLibraryElement implements SongLibraryNode {
    private File file;
    private String name;

    public SongLibraryElement(File file) {
        this.file = file;
        name = file.getName();
    }

    @Override
    public String toString() {
        return name;
    }

    public Song getSong() throws IOException, UnsupportedAudioFileException {
        return new Song(file);
    }

    @Override
    public List<File> getMP3Files() {
        return ImmutableList.of(file);
    }
}
