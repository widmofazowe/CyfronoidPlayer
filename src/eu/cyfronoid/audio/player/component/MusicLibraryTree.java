package eu.cyfronoid.audio.player.component;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.model.SongTreeModel;
import eu.cyfronoid.audio.player.event.TreeSelectedEvent;
import eu.cyfronoid.audio.player.song.library.SongLibrary;
import eu.cyfronoid.audio.player.song.library.SongLibraryNode;

public class MusicLibraryTree extends JTree implements TreeSelectionListener {
    private static final long serialVersionUID = 5107297933537534482L;
    private SongTreeModel model;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);

    public MusicLibraryTree() {
        model = new SongTreeModel(SongLibrary.INSTANCE.buildTree());
        setModel(model);
        addTreeSelectionListener(this);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        SongLibraryNode nodeObject = (SongLibraryNode) node.getUserObject();
        eventBus.post(new TreeSelectedEvent(nodeObject));
    }

}
