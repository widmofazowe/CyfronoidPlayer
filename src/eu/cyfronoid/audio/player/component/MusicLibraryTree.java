package eu.cyfronoid.audio.player.component;

import javax.swing.JTree;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.model.SongTreeModel;

public class MusicLibraryTree extends JTree {
    private static final long serialVersionUID = 5107297933537534482L;
    private SongTreeModel model;

    public MusicLibraryTree() {
        model = PlayerConfigurator.injector.getInstance(SongTreeModel.class);
        setModel(model);
    }
}
