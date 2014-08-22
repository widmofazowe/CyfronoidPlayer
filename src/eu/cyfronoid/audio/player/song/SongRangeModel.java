package eu.cyfronoid.audio.player.song;

import javax.swing.DefaultBoundedRangeModel;

public class SongRangeModel extends DefaultBoundedRangeModel {
    private static final long serialVersionUID = -5062633667880580613L;
    private int max;

    public SongRangeModel() {
        super(0, 1, 0, 3700000);
    }

    public void setRange(Song song) {
        int duration = song.getDurationInMiliseconds();
        max = duration;
        setMaximum(duration);
    }

    public int getMax() {
        return max;
    }
}
