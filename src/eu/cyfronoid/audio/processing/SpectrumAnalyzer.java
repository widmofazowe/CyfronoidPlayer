package eu.cyfronoid.audio.processing;

import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.base.Optional;

import eu.cyfronoid.framework.Cyfron;
import eu.cyfronoid.framework.dsp.FFTCalculator;
import eu.cyfronoid.framework.dsp.Window;
import eu.cyfronoid.framework.exception.NotImplementedException;
import eu.cyfronoid.framework.util.Complex;

public class SpectrumAnalyzer extends ChartPanel implements Runnable {
    private static final long serialVersionUID = 3585157834555396292L;
    private static final int DEFAULT_SECTIONS_NUMBER = 8;
    private static final Logger logger = Logger.getLogger(SpectrumAnalyzer.class);
    private int sampleSize;
    private FFTCalculator spectrumCalculator;
    private Optional<Window> window = Optional.absent();
    private int sectionsNumber = DEFAULT_SECTIONS_NUMBER; //should be power of two
    private double[] samples;
    private JFreeChart chart;
    private int sampleRate;
    private int numberOfSamples;
    private int segmentSize;
    private int overlap;
    private XYSeries xySeries;
    private int oldestDataIndex;
    private double[] inputData;
    private static MessageFormat messageFormat = new MessageFormat("{0} Hz");
    private DefaultCategoryDataset xySeriesCollection;

    public SpectrumAnalyzer(int sampleSize) {
        super(null, true);
        sampleRate = sampleSize;
        numberOfSamples = sampleSize;
        segmentSize = sampleSize;
        overlap = 0;

        inputData = new double[numberOfSamples];
        oldestDataIndex = 0;

        initChart();
        spectrumCalculator = Cyfron.INSTANCE.injector.getInstance(FFTCalculator.class);
        spectrumCalculator.setSampleSize(sampleSize);
        this.sampleSize = sampleSize;
    }

    private void initChart() {

        xySeriesCollection = new DefaultCategoryDataset();
        chart = ChartFactory.createBarChart(null, "Frequency (Hz)", null,
                xySeriesCollection, PlotOrientation.VERTICAL, false, true, false);
        chart.setAntiAlias(false);

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        NumberAxis xAxis = (NumberAxis) xyPlot.getDomainAxis();
        xAxis.setRange(0, sampleRate / 2);

        setChart(chart);
    }

    public void setSamples(double[] samples) {
        this.samples = samples;
    }

    @Override
    public void run() {
        spectrumCalculator.setSamples(samples);
        if(window.isPresent()) {
            try {
                spectrumCalculator.applyWindow(window.get());
            } catch (NotImplementedException e) {
                logger.warn(e);
            }
        }
        spectrumCalculator.fft();
        Complex[] spectrum = spectrumCalculator.getSpectrum();
        double[] amplitude = calculateAmplitudePerSection(spectrum);
        process(amplitude);
    }

    private void process(double[] amplitude) {
        for(int i = 0; i < amplitude.length; ++i) {
            logger.debug("[" + i + "]: " + amplitude[i]);
        }
    }

    private void updateChart(double[] X, double sampleRate, int segmentSize) {
        xySeriesCollection.setNotify(false);
        xySeriesCollection.clear();
        double period = sampleRate / segmentSize;
        double frequency = 0;
        for (int i = 0; i < X.length; i++) {
            xySeriesCollection.addValue(X[i], "magnitude", messageFormat.format(frequency));
            frequency += period;
        }
        xySeriesCollection.setNotify(true);
    }

    private double[] calculateAmplitudePerSection(Complex[] spectrum) {
        Complex[] amplitude = new Complex[sectionsNumber];
        double[] amplitudes = new double[sectionsNumber];
        Arrays.fill(amplitude, new Complex());
        int samplesPerSection = sampleSize/sectionsNumber;
        double constant = amplitude[0].absolute();
        for(int i = 0; i < sectionsNumber; ++i) {
            for(int j = 0; j < samplesPerSection; ++j) {
                Complex sum = amplitude[i].add(spectrum[i*samplesPerSection+j]);
                amplitude[i] = sum;
            }

            amplitudes[i] = amplitude[i].absolute() - constant;
        }

        return amplitudes;
    }

    public static void main(String[] argv) {
        SpectrumAnalyzer analyzer = new SpectrumAnalyzer(8);
        double[] testSamples = new double[]{0, 1, 2, 1, 0, -1, -2, -1};
        analyzer.setSamples(testSamples);
        analyzer.run();
    }
}
