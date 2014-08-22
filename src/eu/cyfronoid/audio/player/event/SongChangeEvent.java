package eu.cyfronoid.audio.player.event;

import eu.cyfronoid.audio.player.song.Song;

public class SongChangeEvent {
    private Song song;
    private boolean autostart;

    public SongChangeEvent(Song song) {
        this.song = song;
        autostart = true;
    }

    public Song getSong() {
        return song;
    }

    public boolean shouldStart() {
        return autostart;
    }
}
