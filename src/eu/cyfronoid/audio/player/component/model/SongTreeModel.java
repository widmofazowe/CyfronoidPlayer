package eu.cyfronoid.audio.player.component.model;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.google.inject.Inject;

public class SongTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = 5848931979263323655L;

    @Inject
    public SongTreeModel(TreeNode root) {
        super(root);
    }

}
