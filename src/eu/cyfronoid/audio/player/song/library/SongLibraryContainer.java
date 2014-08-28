package eu.cyfronoid.audio.player.song.library;

import java.io.File;

public class SongLibraryContainer {
    private File file;
    private String name;

    public SongLibraryContainer(File file) {
        this.file = file;
        name = file.getName();
    }

    @Override
    public String toString() {
        return name;
    }

    public File getDirectory() {
        return file;
    }
}
