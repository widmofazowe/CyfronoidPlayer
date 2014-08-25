package eu.cyfronoid.audio.player.song;

import javax.swing.tree.DefaultMutableTreeNode;

import eu.cyfronoid.framework.util.FileUtil;

public class SongLibrary {
    private static final String DIRECTORY = "MusicLibrary";

    public void buildTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

    }

    private void listFilesByDIrectory(DefaultMutableTreeNode parent, File startingDirectory) {
        Map<FileUtil.FileType, File> files = FileUtil.listFilesAndDirectories(startingDirectory);
    }
}
