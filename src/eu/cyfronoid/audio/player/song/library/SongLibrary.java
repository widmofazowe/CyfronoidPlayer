package eu.cyfronoid.audio.player.song.library;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.collect.Multimap;

import eu.cyfronoid.audio.player.playlist.Playlist.MP3FileFilter;
import eu.cyfronoid.framework.util.FileUtil;

public enum SongLibrary {
    INSTANCE;

    private static final String DIRECTORY = "MusicLibrary";

    public DefaultMutableTreeNode buildTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        File startingDirectory = new File(DIRECTORY);
        root.setUserObject(startingDirectory);
        populateChilds(root, startingDirectory);
        return root;
    }

    private void populateChilds(DefaultMutableTreeNode parent, File startingDirectory) {
        Multimap<FileUtil.FileType, File> files = FileUtil.listFilesAndDirectories(startingDirectory, MP3FileFilter.INSTANCE);

        for(File file : files.get(FileUtil.FileType.FILE)) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            node.setUserObject(new SongLibraryElement(file));
            parent.add(node);
        }
        for(File file : files.get(FileUtil.FileType.DIRECTORY)) {
            DefaultMutableTreeNode nodeWithChilds = new DefaultMutableTreeNode();
            nodeWithChilds.setUserObject(new SongLibraryContainer(file));
            populateChilds(nodeWithChilds, file);
            parent.add(nodeWithChilds);
        }
    }
}
