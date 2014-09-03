package eu.cyfronoid.audio.player.dsp;

public interface DigitalSignalProcessor {

    /**
     * Called by the DigitalSignalProcessingAudioDataConsumer.
     *
     * @param leftChannel Audio data for the left channel.
     * @param rightChannel Audio data for the right channel.
     * @param frameRateRatioHint A float value representing the ratio of the current
     *                            frame rate to the desired frame rate. It is used to
     *                            keep DSP animation consistent if the frame rate drop
     *                            below the desired frame rate.
     */
    void process(float[] leftChannel, float[] rightChannel, float frameRateRatioHint);
}