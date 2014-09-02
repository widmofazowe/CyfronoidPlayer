package eu.cyfronoid.audio.player.song.library;

import java.io.File;
import java.util.List;

public interface SongLibraryNode {
    public String toString();
    public List<File> getMP3Files();
}
