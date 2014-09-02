package eu.cyfronoid.audio.player.song.library;

import java.io.File;
import java.util.List;

import eu.cyfronoid.audio.player.playlist.Playlist;

public class SongLibraryContainer implements SongLibraryNode {
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

    @Override
    public List<File> getMP3Files() {
        return Playlist.getMP3FilesFromDirectory(file);
    }
}
