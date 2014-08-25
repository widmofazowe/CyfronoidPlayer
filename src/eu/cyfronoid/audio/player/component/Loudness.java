package eu.cyfronoid.audio.player.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.event.ChangeGainEvent;

public class Loudness extends JPanel {
    private static final long serialVersionUID = 2684811976323749355L;
    private static final int MAX = 100;
    private static final int MIN = 0;
    final JLabel loudnessLabel = new JLabel("  100%");
    private EventBus eventBus;

    public Loudness() {
        FlowLayout flowLayout = (FlowLayout) getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        final JSlider loudness = new JSlider(JSlider.HORIZONTAL, MIN, MAX, MAX);
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
                eventBus.post(new ChangeGainEvent((double)loudness.getValue()/100.0d));
                return Strings.padStart(Integer.toString(loudness.getValue()), 5, ' ') + "%";
            }
        });
        add(loudness);
        add(loudnessLabel);
        eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    }
}
