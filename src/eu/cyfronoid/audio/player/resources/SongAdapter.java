package eu.cyfronoid.audio.player.resources;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.cyfronoid.audio.player.song.Song;

public class SongAdapter extends XmlAdapter<String, Song> {

    @Override
    public String marshal(Song song) throws Exception {
        return song.getPath();
    }

    @Override
    public Song unmarshal(String path) throws Exception {
        return new Song(path);
    }

}
