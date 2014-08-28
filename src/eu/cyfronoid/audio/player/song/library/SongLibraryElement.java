package eu.cyfronoid.audio.player.song.library;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import eu.cyfronoid.audio.player.song.Song;

public class SongLibraryElement {
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
}
