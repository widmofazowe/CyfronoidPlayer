package eu.cyfronoid.audio.player.dsp;

public interface AudioDataConsumer {
    void writeAudioData(byte[] audioData);
    void writeAudioData(byte[] audioData, int offset, int length);
}
