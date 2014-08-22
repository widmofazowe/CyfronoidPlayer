package eu.cyfronoid.audio.player.event;

import eu.cyfronoid.audio.player.song.SongProperties;

public class UpdatePlayingProgressEvent {
    private int microsecondsPosition;
    private SongProperties songProperties;

    public UpdatePlayingProgressEvent(SongProperties songProperties, int microsecondsPosition) {
        this.songProperties = songProperties;
        this.microsecondsPosition = microsecondsPosition;
    }

    public int getMicrosecondsPosition() {
        return microsecondsPosition;
    }

    public int getSongSize() {
        return songProperties.getSizeInBytes();
    }

    public int getDurationInMiliseconds() {
        return songProperties.getDurationInMiliseconds();
    }

    public float getFps() {
        return songProperties.getFps();
    }

    public int getFrameSize() {
        return songProperties.getFrameSize();
    }

    public int getMilisecondsElapsed() {
        return microsecondsPosition;
    }

}
