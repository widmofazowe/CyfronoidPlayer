package eu.cyfronoid.audio.player.event;

public class NewSamplesEvent {
    private byte[] samples;

    public byte[] getSamples() {
        return samples;
    }

    public NewSamplesEvent(byte[] samples) {
        this.samples = samples;
    }
}
