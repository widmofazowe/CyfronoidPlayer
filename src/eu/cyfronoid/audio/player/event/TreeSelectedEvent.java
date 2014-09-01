package eu.cyfronoid.audio.player.event;

import eu.cyfronoid.audio.player.song.library.SongLibraryNode;

public class TreeSelectedEvent {
    SongLibraryNode node;

    public TreeSelectedEvent(SongLibraryNode node) {
        this.node = node;
    }

    public SongLibraryNode getNode() {
        return node;
    }
}
