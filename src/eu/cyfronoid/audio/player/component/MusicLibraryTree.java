package eu.cyfronoid.audio.player.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.component.model.SongTreeModel;
import eu.cyfronoid.audio.player.event.TreeSelectedEvent;
import eu.cyfronoid.audio.player.event.UpdateTreeEvent;
import eu.cyfronoid.audio.player.resources.ActualSelectionSettings;
import eu.cyfronoid.audio.player.song.library.SongLibrary;
import eu.cyfronoid.audio.player.song.library.SongLibraryNode;
import eu.cyfronoid.gui.tree.TreeState;

public class MusicLibraryTree extends JTree implements TreeSelectionListener {
    private static final long serialVersionUID = 5107297933537534482L;
    private static final Logger logger = Logger.getLogger(MusicLibraryTree.class);
    private SongTreeModel model;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private TreeState treeState;

    public MusicLibraryTree() {
        model = new SongTreeModel(SongLibrary.INSTANCE.buildTree());
        setModel(model);
        //setRootVisible(false);
        setDragEnabled(true);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ActualSelectionSettings actualSelections = PlayerConfigurator.SETTINGS.getActualSelections();
                if(actualSelections != null) {
                    TreeState state = new TreeState(getMusicLibraryTree());
                    state.setExpansionState(actualSelections.getExpansionState());
                }
                setSelectionListeners();
            }

        });


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

    private MusicLibraryTree getMusicLibraryTree() {
        return this;
    }

    private void setSelectionListeners() {
        addTreeSelectionListener(this);
    }

    @Subscribe
    public void updateTree(final UpdateTreeEvent event) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String expansionState = treeState.getExpansionState();
                model.setRoot(event.getRoot());
                treeState.setExpansionState(expansionState);
            }

        });
    }

    private static class MusicLibraryTreeTransferHandler extends TransferHandler {
        private static final long serialVersionUID = -7524926382207466058L;

        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        DefaultMutableTreeNode[] nodesToRemove;

        public MusicLibraryTreeTransferHandler() {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                                  ";class=\"" +
                    javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                                  "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch(ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return false;
        }

        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree)c;
            TreePath[] paths = tree.getSelectionPaths();
            if(paths != null) {
                // Make up a node array of copies for transfer and
                // another for/of the nodes that will be removed in
                // exportDone after a successful drop.
                List<DefaultMutableTreeNode> copies =
                    new ArrayList<DefaultMutableTreeNode>();
                List<DefaultMutableTreeNode> toRemove =
                    new ArrayList<DefaultMutableTreeNode>();
                DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)paths[0].getLastPathComponent();
                DefaultMutableTreeNode copy = copy(node);
                copies.add(copy);
                toRemove.add(node);
                for(int i = 1; i < paths.length; i++) {
                    DefaultMutableTreeNode next =
                        (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                    // Do not allow higher level nodes to be added to list.
                    if(next.getLevel() < node.getLevel()) {
                        break;
                    } else if(next.getLevel() > node.getLevel()) {  // child node
                        copy.add(copy(next));
                        // node already contains child
                    } else {                                        // sibling
                        copies.add(copy(next));
                        toRemove.add(next);
                    }
                }
                DefaultMutableTreeNode[] nodes =
                    copies.toArray(new DefaultMutableTreeNode[copies.size()]);
                nodesToRemove =
                    toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
                return new NodesTransferable(nodes);
            }
            return null;
        }

        private DefaultMutableTreeNode copy(TreeNode node) {
            return new DefaultMutableTreeNode(node);
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if((action & MOVE) == MOVE) {
                JTree tree = (JTree)source;
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                // Remove nodes saved in nodesToRemove in createTransferable.
                for(int i = 0; i < nodesToRemove.length; i++) {
                    model.removeNodeFromParent(nodesToRemove[i]);
                }
            }
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public String toString() {
            return getClass().getName();
        }

        public class NodesTransferable implements Transferable {
            DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
             }

            public Object getTransferData(DataFlavor flavor)
                                     throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return nodes;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
}
