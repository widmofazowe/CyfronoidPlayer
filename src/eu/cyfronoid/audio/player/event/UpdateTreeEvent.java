package eu.cyfronoid.audio.player.event;

import javax.swing.tree.DefaultMutableTreeNode;

public class UpdateTreeEvent {
    private DefaultMutableTreeNode treeRoot;

    public UpdateTreeEvent(DefaultMutableTreeNode treeRoot) {
        this.treeRoot = treeRoot;
    }

    public DefaultMutableTreeNode getRoot() {
        return treeRoot;
    }

}
