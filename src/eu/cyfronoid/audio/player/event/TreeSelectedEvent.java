package eu.cyfronoid.audio.player.event;

import java.util.List;

import eu.cyfronoid.audio.player.song.library.SongLibraryNode;

public class TreeSelectedEvent {
    List<SongLibraryNode> nodes;

    public TreeSelectedEvent(List<SongLibraryNode> nodes) {
        this.nodes = nodes;
    }

    public List<SongLibraryNode> getNodes() {
        return nodes;
    }
}
