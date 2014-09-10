package eu.cyfronoid.audio.player.dsp;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.framework.dsp.FFTCalculator;
import eu.cyfronoid.framework.dsp.Window;
import eu.cyfronoid.framework.exception.NotImplementedException;
import eu.cyfronoid.framework.util.ExceptionHelper;

public class LedPartyHardProcessor implements DigitalSignalProcessor {
    private static final int DEFAULT_LED_CHANNEL_NUMBER = 9;
    private static final Logger logger = Logger.getLogger(LedPartyHardProcessor.class);
    private FFTCalculator fftCalculator = PlayerConfigurator.injector.getInstance(FFTCalculator.class);
    private Optional<Window> fftWindow = Optional.absent();
    private int ledChannelNumber = DEFAULT_LED_CHANNEL_NUMBER;

    @Override
    public void process(float[] leftChannel, float[] rightChannel,
            float frameRateRatioHint) {
        //TODO: calculate and send data to usb device
        float[] data = DSPUtils.stereoMerge(leftChannel, rightChannel);
        double[] magnitudes = calculateFFT(data);
    }

    private double[] calculateFFT(float[] data) {
        fftCalculator.setSampleSize(data.length);
        fftCalculator.setSamples(data);
        if(fftWindow.isPresent()) {
            try {
                fftCalculator.applyWindow(fftWindow.get());
            } catch (NotImplementedException e) {
                logger.warn("Cannot apply window " + fftWindow.get() + " because of an error: " + e);
                logger.warn(ExceptionHelper.getStackTrace(e));
            }
        }
        fftCalculator.fft();
        return fftCalculator.getMagnitudes();
    }

}
