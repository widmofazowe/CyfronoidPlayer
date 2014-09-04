package eu.cyfronoid.audio.player.playlist;

import java.io.File;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class PlaylistTabParameters {
    public static final PlaylistTabParameters EMPTY_TAB = new PlaylistTabParameters();
    public static final String TREE_SELECTION_LISTENER_NAME = "Selection Listener";

    private Optional<String> name;
    private List<File> files;
    private boolean isTreeSelectionListener;

    public static PlaylistTabParameters createSelectionListener() {
        return new PlaylistTabParameters(Lists.<File>newArrayList(), TREE_SELECTION_LISTENER_NAME, true);
    }

    public PlaylistTabParameters() {
        this(Lists.<File>newArrayList(), null, false);
    }

    public PlaylistTabParameters(List<File> files) {
        this(files, null, false);
    }

    public PlaylistTabParameters(List<File> files, String name) {
        this(files, null, false);
    }

    private PlaylistTabParameters(List<File> files, String name, boolean isTreeSelectionListener) {
        this.name = Optional.fromNullable(name);
        this.files = files;
        this.isTreeSelectionListener = isTreeSelectionListener;
    }

    public Optional<String> getName() {
        return name;
    }

    public void setName(Optional<String> name) {
        this.name = name;
    }

    public List<File> getFiles() {
        if(files == null) {
            files = Lists.newArrayList();
        }
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public boolean isTreeSelectionListener() {
        return isTreeSelectionListener;
    }

}
