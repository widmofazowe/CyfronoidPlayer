package eu.cyfronoid.audio.player.event;

import eu.cyfronoid.audio.player.song.Song;

public class SongFinishedEvent {
    private Song song;

    public SongFinishedEvent(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
}
