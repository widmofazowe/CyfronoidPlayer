package eu.cyfronoid.audio.player.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.event.UpdatePlayingProgressEvent;
import eu.cyfronoid.audio.player.song.SongRangeModel;
import eu.cyfronoid.framework.format.Format;

public class PlayingProgress extends JPanel {
    private static final long serialVersionUID = 7644249089068917323L;
    private SongRangeModel brm;
    private JLabel progressLabel;
    private JSlider progressSlider;

    public PlayingProgress() {
        FlowLayout flowLayout = (FlowLayout) getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        progressLabel = new JLabel("00:00:00");
        Dimension d = progressLabel.getPreferredSize();
        progressLabel.setPreferredSize(new Dimension(d.width+70,d.height));
        brm = new SongRangeModel();
        progressSlider = new JSlider(brm);
        progressSlider.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int value = progressSlider.getValue();
                progressLabel.setText(PlaybackProgressFormatter.INSTANCE.format(value, brm.getMaximum()));
            }
        });
        add(progressSlider);
        add(progressLabel);
    }

    @Subscribe
    public void recieve(UpdatePlayingProgressEvent event) {
        int durationInMiliseconds = event.getDurationInMiliseconds();
        brm.setMaximum(durationInMiliseconds);
        progressLabel.setText(PlaybackProgressFormatter.INSTANCE.format(event.getMilisecondsElapsed(), durationInMiliseconds));
        progressSlider.setValue(event.getMilisecondsElapsed());
    }

    public static enum PlaybackProgressFormatter implements Format {
        INSTANCE;

        private DecimalFormat formatter = new DecimalFormat("00");

        public String format(int actual, int duration) {
            return formatValueToString(actual) + " / " + formatValueToString(duration);
        }

        @Override
        public Object format(Object object) {
            if(object.getClass().equals(Long.class)) {
                return formatValueToString((int)(((Long) object)/1000));
            }
            return formatValueToString((int) object);
        }

        private String formatValueToString(int actual) {
            String hours = getHours(actual);
            String minutes = getMinutes(actual);
            String seconds = getSeconds(actual);
            return hours + ":" + minutes + ":" + seconds;
        }

        private String getHours(int actual) {
            return formatter.format((actual/3600000));
        }

        private String getMinutes(int actual) {
            return formatter.format((actual/60000)%60);
        }

        private String getSeconds(int actual) {
            return formatter.format((actual/1000)%60);
        }


    }
}
