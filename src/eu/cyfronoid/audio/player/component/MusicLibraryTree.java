package eu.cyfronoid.audio.player.component;

import javax.swing.JTree;

import eu.cyfronoid.audio.player.component.model.SongTreeModel;
import eu.cyfronoid.audio.player.song.library.SongLibrary;

public class MusicLibraryTree extends JTree {
    private static final long serialVersionUID = 5107297933537534482L;
    private SongTreeModel model;

    public MusicLibraryTree() {
        model = new SongTreeModel(SongLibrary.INSTANCE.buildTree());
        setModel(model);
    }
}
