package eu.cyfronoid.audio.processing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;

import kj.dsp.KJDigitalSignalProcessingAudioDataConsumer;
import kj.dsp.KJDigitalSignalProcessor;
import kj.dsp.KJFFT;

import org.apache.log4j.Logger;

import eu.cyfronoid.framework.builder.BuilderInterface;
import eu.cyfronoid.framework.dsp.Window;
import eu.cyfronoid.framework.exception.NotImplementedException;

public class SpectrumTimeAnalyzer extends JPanel implements KJDigitalSignalProcessor {
    private static Logger logger = Logger.getLogger(SpectrumTimeAnalyzer.class);
    private KJDigitalSignalProcessingAudioDataConsumer dsp;
    private int sampleSize;
    private int fps;
    private boolean dspStarted;
    private SourceDataLine line;
    private static final long serialVersionUID = 7431192235694108570L;
    public static final int DEFAULT_SPECTRUM_ANALYSER_PEAK_DELAY = 20;
    private KJFFT fft;
    private float[] oldFFT;
    private float numberOfBands;
    private int width;
    private int height;
    private float saMultiplier;
    private float decay;
    private Window window;
    private int barOffset = 1;
    private Color[] spectrumAnalyserColors = getDefaultSpectrumAnalyserColors();
    private float colorScale;
    private Color peakColor = null;
    private boolean peaksEnabled = true;
    private int[] peaks;
    private int[] peaksDelay;
    private int peakDelay = DEFAULT_SPECTRUM_ANALYSER_PEAK_DELAY;
    private Image bi;

    private SpectrumTimeAnalyzer(int sampleSize, int noOfBands, int width, int height, float decay, Window window, SourceDataLine line, int fps) {
        this.sampleSize = sampleSize;
        numberOfBands = noOfBands;
        this.width = width;
        this.height = height;
        this.decay = decay;
        this.window = window;
        oldFFT = new float[sampleSize];
        fft = new KJFFT(sampleSize);
        computeMultiplier();
        computeColorScale();
        peaks = new int[noOfBands];
        peaksDelay = new int[noOfBands];
        this.fps = fps;
        this.line = line;
        setSampleSize(sampleSize);
        addDigitalSignalProcessor(this);
        setVisible(true);
        setSize(width, height);
    }

    public void addDigitalSignalProcessor(KJDigitalSignalProcessor processor) {
        dsp.add(processor);
    }

    public void setSampleSize(int size) {
        sampleSize = size;
        dsp = new KJDigitalSignalProcessingAudioDataConsumer(sampleSize, fps);
    }

    public int getFps() {
        return fps;
    }

    public void startDSP() {
        if(line != null) {
            if(dspStarted == true) {
                stopDSP();
            }
            dsp.start(line);
            dspStarted = true;
            logger.debug("DSP started");
        }
    }

    public void stopDSP() {
        if(dsp != null) {
            dsp.stop();
            dspStarted = false;
            logger.debug("DSP stopped");
        }
    }

    @Override
    public synchronized void process(float[] leftChannel, float[] rightChannel, float pFrameRateRatioHint) {
        float c = 0;
        float[] samples = stereoMerge(leftChannel, rightChannel);
        float[] wFFT;
        try {
            wFFT = fft.calculate(window.apply(samples, sampleSize));
        } catch (NotImplementedException e) {
            throw new RuntimeException(e);
        }
        Graphics graphics = getDoubleBuffer().getGraphics();
        float wSadfrr = (decay * pFrameRateRatioHint);
        float bandWidth = ((float) width / (float) numberOfBands);
        for (int a = 0, band = 0; band < numberOfBands; a += saMultiplier, band++) {
            float wFs = 0;
            // -- Average out nearest bands.
            for (int b = 0; b < saMultiplier; b++) {
                wFs += wFFT[a + b];
            }
            // -- Log filter.
            wFs = (wFs * (float) Math.log(band + 2));
            if (wFs > 1.0f) {
                wFs = 1.0f;
            }
            // -- Compute SA decay...
            if (wFs >= (oldFFT[a] - wSadfrr)) {
                oldFFT[a] = wFs;
            } else {
                oldFFT[a] -= wSadfrr;
                if (oldFFT[a] < 0) {
                    oldFFT[a] = 0;
                }
                wFs = oldFFT[a];
            }
            drawSpectrumAnalyserBar(graphics, (int) c, (int) height, (int) bandWidth - 1, (int) (wFs * height), band);
            c += bandWidth;
        }
        if(getGraphics() != null) {
            getGraphics().drawImage(getDoubleBuffer(), 0, 0, null);
        }
    }

    private float[] stereoMerge(float[] leftSamples, float[] rightSamples) {
        float[] merged = new float[leftSamples.length];
        for (int a = 0; a < leftSamples.length; a++) {
            merged[a] = (leftSamples[a] + rightSamples[a]) / 2.0f;
        }
        return merged;
    }

    private void computeMultiplier() {
        saMultiplier = (sampleSize / 2) / numberOfBands;
    }

    private void drawSpectrumAnalyserBar(Graphics graphics, int x, int y, int width, int height, int band) {
        float c = 0;
        for (int a = y; a >= y - height; a -= barOffset)
        {
            c += colorScale;
            if (c < spectrumAnalyserColors.length)
            {
                graphics.setColor(spectrumAnalyserColors[(int) c]);
            }
            graphics.fillRect(x, a, width, 1);
        }
        if ((peakColor != null) && (peaksEnabled == true))
        {
            graphics.setColor(peakColor);
            if (height > peaks[band])
            {
                peaks[band] = height;
                peaksDelay[band] = peakDelay;
            }
            else
            {
                peaksDelay[band]--;
                if (peaksDelay[band] < 0) peaks[band]--;
                if (peaks[band] < 0) peaks[band] = 0;
            }
            graphics.fillRect(x, y - peaks[band], width, 1);
        }
    }

    private synchronized Image getDoubleBuffer() {
        if(bi == null || (bi.getWidth(null) != getSize().width || bi.getHeight(null) != getSize().height)){
            width = getSize().width;
            height = getSize().height;
            computeColorScale();
            bi = getGraphicsConfiguration().createCompatibleVolatileImage(width, height);
        }
        return bi;
    }

    private void computeColorScale() {
        colorScale = ((float) spectrumAnalyserColors.length / height) * barOffset * 1.0f;
    }

    public static Color[] getDefaultSpectrumAnalyserColors() {
        Color[] wColors = new Color[256];
        for (int a = 0; a < 128; a++) {
            wColors[a] = new Color(0, (a >> 1) + 192, 0);
        }
        for (int a = 0; a < 64; a++) {
            wColors[a + 128] = new Color(a << 2, 255, 0);
        }
        for (int a = 0; a < 64; a++) {
            wColors[a + 192] = new Color(255, 255 - (a << 2), 0);
        }
        return wColors;
    }

    public static Order create() {
        return new Builder();
    }

    private static class Builder implements Order, Attributes {
        private int sampleSize;
        private int noOfBands;
        private int width;
        private int height;
        private float decay;
        private Window window;
        private SourceDataLine line;
        private int fps;

        @Override
        public AttrNoOfBands setSampleSize(int sampleSize) {
            this.sampleSize = sampleSize;
            return this;
        }

        @Override
        public AttrWidth setNoOfBands(int noOfBands) {
            this.noOfBands = noOfBands;
            return this;
        }

        @Override
        public AttrHeight setWidth(int width) {
            this.width = width;
            return this;
        }

        @Override
        public AttrDecay setHeight(int height) {
            this.height = height;
            return this;
        }

        @Override
        public AttrWindow setDecay(float decay) {
            this.decay = decay;
            return this;
        }

        @Override
        public SpectrumTimeAnalyzer build() {
            return new SpectrumTimeAnalyzer(sampleSize, noOfBands, width, height, decay, window, line, fps);
        }

        @Override
        public AttrLine noWindowing() {
            window = Window.RECTANGULAR;
            return this;
        }

        @Override
        public AttrLine setWindowing(Window window) {
            this.window = window;
            return this;
        }

        @Override
        public AttrFPS setLine(SourceDataLine line) {
            this.line = line;
            return this;
        }

        @Override
        public BuilderInterface<SpectrumTimeAnalyzer> setFPS(int fps) {
            this.fps = fps;
            return this;
        }

    }

    public interface Order extends AttrSampleSize{}
    private interface Attributes extends AttrNoOfBands, AttrWidth, AttrHeight, AttrDecay, AttrWindow, AttrLine, AttrFPS, BuilderInterface<SpectrumTimeAnalyzer>{}

    public interface AttrSampleSize {
        public AttrNoOfBands setSampleSize(int sampleSize);
    }

    public interface AttrNoOfBands {
        public AttrWidth setNoOfBands(int noOfBands);
    }

    public interface AttrWidth {
        public AttrHeight setWidth(int width);
    }

    public interface AttrHeight {
        public AttrDecay setHeight(int height);
    }

    public interface AttrDecay {
        public AttrWindow setDecay(float decay);
    }

    public interface AttrWindow {
        public AttrLine noWindowing();
        public AttrLine setWindowing(Window window);
    }

    public interface AttrLine {
        public AttrFPS setLine(SourceDataLine line);
    }

    public interface AttrFPS {
        public BuilderInterface<SpectrumTimeAnalyzer> setFPS(int fps);
    }

}
