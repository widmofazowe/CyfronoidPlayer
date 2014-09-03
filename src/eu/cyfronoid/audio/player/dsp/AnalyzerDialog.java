package eu.cyfronoid.audio.player.dsp;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.event.NewSamplesEvent;

public class AnalyzerDialog extends JDialog {
    private static final long serialVersionUID = -8337104359634688814L;
    private static final Logger logger = Logger.getLogger(AnalyzerDialog.class);
    private boolean dspEnabled = true;
    private SpectrumTimeAnalyzer analyzer;

    public static AnalyzerDialog open() {
        AnalyzerDialog dialog = new AnalyzerDialog();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        return dialog;
    }

    private AnalyzerDialog() {
        setBounds(100, 100, 450, 300);
        setAnalyzerPanel();
        add(analyzer);
    }

    public void startDSP(SourceDataLine line) {
        analyzer.startDSP(line);
    }

    @Subscribe
    public void reciveSamples(NewSamplesEvent event) {
        byte[] pcmdata = event.getSamples();
        analyzer.writeDSP(pcmdata);
    }

    private void setAnalyzerPanel() {
        String javaVersion = System.getProperty("java.version");
        if ((javaVersion != null) && ((javaVersion.startsWith("1.3"))) || (javaVersion.startsWith("1.4")))
        {
            logger.info("DSP disabled for JRE " + javaVersion);
        } else if (!dspEnabled) {
            logger.info("DSP disabled");
        } else {
            if (analyzer == null) {
                analyzer = new SpectrumTimeAnalyzer();
            }
            String visualMode = "spectrum";
            if ((visualMode != null) && (visualMode.length() > 0)) {
                if(visualMode.equalsIgnoreCase("off")) {
                    analyzer.setDisplayMode(SpectrumTimeAnalyzer.DISPLAY_MODE_OFF);
                } else if(visualMode.equalsIgnoreCase("oscillo")) {
                    analyzer.setDisplayMode(SpectrumTimeAnalyzer.DISPLAY_MODE_SCOPE);
                } else {
                    analyzer.setDisplayMode(SpectrumTimeAnalyzer.DISPLAY_MODE_SPECTRUM_ANALYSER);
                }
            }
            else analyzer.setDisplayMode(SpectrumTimeAnalyzer.DISPLAY_MODE_SPECTRUM_ANALYSER);
            analyzer.setSpectrumAnalyserBandCount(19);
            analyzer.setSize(400, 300);
            analyzer.setSpectrumAnalyserDecay(0.05f);
            int fps = SpectrumTimeAnalyzer.DEFAULT_FPS;
            analyzer.setFps(fps);
            analyzer.setPeakDelay((int) (fps * SpectrumTimeAnalyzer.DEFAULT_SPECTRUM_ANALYSER_PEAK_DELAY_FPS_RATIO));
            analyzer.setToolTipText("Spectrum Analyzer");
        }
    }
}
