package eu.cyfronoid.audio.player;

public interface PlaybackListener {
    public void onFinished(PlaybackEvent e);
    public void onStarted(PlaybackEvent e);
}
