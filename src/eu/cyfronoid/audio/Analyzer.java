package eu.cyfronoid.audio;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import eu.cyfronoid.audio.processing.SpectrumTimeAnalyzer;
import eu.cyfronoid.framework.audio.AudioFormatBuilder;

public class Analyzer {

    private JFrame frame;
    private SpectrumTimeAnalyzer analyzer;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Analyzer window = new Analyzer();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws LineUnavailableException
     */
    public Analyzer() throws LineUnavailableException {
        initialize();
        analyzer.startDSP();
    }

    /**
     * Initialize the contents of the frame.
     * @throws LineUnavailableException
     */
    private void initialize() throws LineUnavailableException {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        analyzer = SpectrumTimeAnalyzer.create()
                .setSampleSize(2048)
                .setNoOfBands(20)
                .setWidth(800)
                .setHeight(600)
                .setDecay(0.05f)
                .noWindowing()
                .setLine(AudioSystem.getSourceDataLine(AudioFormatBuilder.create()
                        .rate44100()
                        .sixteenBits()
                        .twoChannels()
                        .signed()
                        .littleEndian()
                        .build()))
                .setFPS(60)
                .build();

        frame.getContentPane().add(analyzer, BorderLayout.CENTER);
    }

}
