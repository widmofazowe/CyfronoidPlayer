package eu.cyfronoid.audio.player.event;

public class ChangeGainEvent {
    double gain;

    public ChangeGainEvent(double gain) {
        this.gain = gain;
    }

    public double getGain() {
        return gain;
    }
}
