package eu.cyfronoid.audio.player.playback;

import eu.cyfronoid.audio.player.song.Song;

public interface PlaybackListener {
    public void onFinished(Song song);
    public void onStarted(Song song);
    public void process(Song song, byte[] data);
}
