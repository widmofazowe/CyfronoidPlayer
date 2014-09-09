package eu.cyfronoid.audio.player.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.collect.Lists;
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
    private SongTreeModel model;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private TreeState treeState;
    public static final DataFlavor NODES_FLAVOR;
    private static DataFlavor[] flavors;

    static {
        String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                ";class=\"" + DefaultMutableTreeNode[].class.getName() +
                    "\"";
        try {
            NODES_FLAVOR = new DataFlavor(mimeType);
            flavors = new DataFlavor[]{NODES_FLAVOR};
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MusicLibraryTree() {
        model = new SongTreeModel(SongLibrary.INSTANCE.buildTree());
        setModel(model);
        //setRootVisible(false);

        setTransferHandler(new MusicLibraryTreeTransferHandler());
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


    public class MusicLibraryTreeTransferHandler extends TransferHandler {
        private static final long serialVersionUID = -7524926382207466058L;

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            return true;
        }

        @Override
        public boolean canImport(JComponent dest, DataFlavor[] flavors) {
            return true;
        }

        @Override
        public int getSourceActions(JComponent comp) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree)c;
            TreePath[] paths = tree.getSelectionPaths();
            if(paths != null) {
                List<DefaultMutableTreeNode> selected = Lists.newArrayList();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
                selected.add(node);
                for(int i = 1; i < paths.length; i++) {
                    DefaultMutableTreeNode next = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                    selected.add(next);
                }
                DefaultMutableTreeNode[] nodes = selected.toArray(new DefaultMutableTreeNode[selected.size()]);
                return new NodesTransferable(nodes);
            }
            return null;
        }

        public class NodesTransferable implements Transferable {
            private DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                                     throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return nodes;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return NODES_FLAVOR.equals(flavor);
            }
        }
    }

}
