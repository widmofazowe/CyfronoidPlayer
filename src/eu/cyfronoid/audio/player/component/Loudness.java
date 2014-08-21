package eu.cyfronoid.audio.player.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.google.common.base.Strings;

public class Loudness extends JPanel {
    private static final long serialVersionUID = 2684811976323749355L;
    private static final int MAX = 0;
    private static final int MIN = -100;
    final JLabel loudnessLabel = new JLabel("0 dB");

    public Loudness() {
        FlowLayout flowLayout = (FlowLayout) getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        final JSlider loudness = new JSlider(JSlider.HORIZONTAL, MIN, MAX, 0);
        Dimension d = loudnessLabel.getPreferredSize();
        loudnessLabel.setPreferredSize(new Dimension(d.width+60,d.height));
        loudness.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                loudnessLabel.setText(formatLabelValue(loudness));
            }

            private String formatLabelValue(final JSlider loudness) {
                return Strings.padStart(Integer.toString(loudness.getValue()), 7, ' ') + " dB";
            }
        });
        add(loudness);
        add(loudnessLabel);
    }
}
