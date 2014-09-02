package eu.cyfronoid.audio.player.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.cyfronoid.audio.player.song.Song;

@XmlRootElement(namespace = "eu.cyfronoid.audio.player.resources.Settings")
@XmlType(propOrder = { "expansionState", "selectedSong"})
public class ActualSelectionSettings {
    private String expansionState;
    private Song selectedSong;

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
}
