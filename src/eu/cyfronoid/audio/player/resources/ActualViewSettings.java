package eu.cyfronoid.audio.player.resources;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.cyfronoid.audio.player.song.Song;

@XmlRootElement(namespace = "eu.cyfronoid.audio.player.resources.Settings")
@XmlType(propOrder = { "expansionState", "selectedSong", "openedPlaylists", "selectedTab"})
public class ActualViewSettings {
    private String expansionState;
    private Song selectedSong;
    private List<String> openedPlaylist;
    private int selectedTab;

//    public TreePath getSelectedTreePath() {
//        return selectedTreePath;
//    }
//
//    @XmlElement
//    @XmlJavaTypeAdapter(SelectedTreePathAdapter.class)
//    public void setSelectedTreePath(TreePath selectedTreePath) {
//        this.selectedTreePath = selectedTreePath;
//    }

    public Song getSelectedSong() {
        return selectedSong;
    }

    @XmlElement
    @XmlJavaTypeAdapter(SongAdapter.class)
    public void setSelectedSong(Song selectedSong) {
        this.selectedSong = selectedSong;
    }

    public String getExpansionState() {
        return expansionState;
    }

    public void setExpansionState(String expansionState) {
        this.expansionState = expansionState;
    }

    @XmlElementWrapper(name="openedPlaylists")
    @XmlElement(name="openedPlaylist")
    public void setOpenedPlaylists(List<String> files) {
        this.openedPlaylist = files;
    }

    public List<String> getOpenedPlaylists() {
        return openedPlaylist;
    }

    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
    }

    public int getSelectedTab() {
        return selectedTab;
    }
}
