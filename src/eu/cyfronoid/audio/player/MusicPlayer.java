package eu.cyfronoid.audio.player;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.cyfronoid.audio.player.event.ChangeGainEvent;
import eu.cyfronoid.audio.player.event.SongChangeEvent;
import eu.cyfronoid.audio.player.event.SongFinishedEvent;
import eu.cyfronoid.audio.player.event.UpdatePlayingProgressEvent;
import eu.cyfronoid.audio.player.song.Song;

public class MusicPlayer {
    private static final Logger logger = Logger.getLogger(MusicPlayer.class);
    private Song actualSong;
    private boolean isPlaying = false;
    private PlaybackThread playbackThread;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private List<PlaybackListener> playbackListeners = Lists.newArrayList();
    private SourceDataLine line;
    private FloatControl gainControl;

    public void setSong(Song song) throws IOException {
        stop();
        if(actualSong != null) {
            actualSong.close();
        }
        actualSong = song;
    }

    public Optional<Song> getActualSong() {
        return Optional.fromNullable(actualSong);
    }

    private void stop() {
        if(playbackThread != null && playbackThread.isRuning()) {
            playbackThread.terminate();
        }
        gainControl = null;
        playbackThread = null;
    }

    public void startPlaying() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if(canStartPlayback()) {
            rawplay();
        }
    }

    public void togglePlay() {
        try {
            if(!isPlaying) {
                if(canStartPlayback()) {
                    rawplay();
                } else {
                    isPlaying = true;
                    resume();
                }
            } else {
                isPlaying = false;
                pause();
            }
        } catch (Exception e) {
            logger.error("Cannot play song because of an error", e);
        }
    }

    private void rawplay() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        rawplay(actualSong.getFormat(), actualSong.getDecodedAudioInputStream());
    }

    private boolean canStartPlayback() {
        return playbackThread == null || (playbackThread != null && !playbackThread.isRunning);
    }

    @Subscribe
    public void changeSong(SongChangeEvent event) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        setSong(event.getSong());
        rawplay();
    }

    private void resume() {
        playbackThread.resumeThread();
        logger.debug("Resumed song " + actualSong.getTitle());
    }

    private void pause() throws InterruptedException {
        playbackThread.pauseThread();
        logger.debug("Paused song " + actualSong.getTitle());
    }

    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        line = getLine(targetFormat);
        logger.debug("Starting song " + actualSong.getTitle());
        playbackThread = new PlaybackThread(targetFormat, line, din);
        playbackThread.start();
        setGain(PlayerConfigurator.SETTINGS.getGain());
        isPlaying = true;
    }

    @Subscribe
    public void recive(ChangeGainEvent event) {
        double gain = event.getGain();
        PlayerConfigurator.SETTINGS.setGain(gain);
        setGain(gain);
    }

    private void setGain(double paramDouble) {
        if(hasGainControl()) {
            double d4 = calculateGain(paramDouble);
            logger.debug("Gain: " + d4);
            this.gainControl.setValue((float)d4);
        } else {
            logger.error("Gain control not supported");
        }
    }

    private double calculateGain(double paramDouble) {
        double d1 = getMinimumGain();
        double d2 = 0.5F * getMaximumGain() - getMinimumGain();
        double d3 = Math.log(10.0D) / 20.0D;
        double d4 = d1 + 1.0D / d3 * Math.log(1.0D + (Math.exp(d3 * d2) - 1.0D) * paramDouble);
        return d4;
    }

    private float getMaximumGain() {
        if (hasGainControl()) {
            return this.gainControl.getMaximum();
        }
        return 0.0F;
    }

    private float getMinimumGain() {
        if (hasGainControl()) {
            return this.gainControl.getMinimum();
        }
        return 0.0F;
    }

    private boolean hasGainControl() {
        if((this.gainControl == null) && (this.line != null) && (this.line.isControlSupported(FloatControl.Type.MASTER_GAIN))) {
            this.gainControl = ((FloatControl)this.line.getControl(FloatControl.Type.MASTER_GAIN));
        }
        return this.gainControl != null;
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        return res;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void addPlaybackListener(PlaybackListener listener) {
        playbackListeners.add(listener);
    }

    private class PlaybackThread extends Thread {
        private static final int BUFFER_SIZE = 4096;
        private final Object THREAD_MONITOR = new Object();
        private int nBytesWritten;
        private AudioInputStream din;
        private SourceDataLine line;
        private boolean pauseThreadFlag;
        private boolean isRunning = true;
        private boolean running = true;
        private AudioFormat format;

        public PlaybackThread(AudioFormat targetFormat, SourceDataLine line, AudioInputStream din) throws LineUnavailableException {
            this.line = line;
            this.din = din;
            this.format = targetFormat;
            line.open(format);
            line.start();
        }

        @Override
        public void run() {
            byte[] data = new byte[BUFFER_SIZE];
            try {
                int nBytesRead = 0;
                nBytesWritten = 0;
                isRunning = true;

                while(nBytesRead != -1 && running) {

                    checkForPaused();
                    nBytesRead = din.read(data, 0, data.length);

                    if(nBytesRead != -1) {
                        nBytesWritten = line.write(data, 0, nBytesRead);
                        eventBus.post(new UpdatePlayingProgressEvent(actualSong.getSongProperties(), (int)line.getMicrosecondPosition()/1000));
                    }
                }


                line.drain();
                line.stop();
                line.close();
                din.close();
                isRunning = false;
                if(running) {
                    eventBus.post(new SongFinishedEvent(actualSong));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void checkForPaused() {
            synchronized (THREAD_MONITOR) {
                while(pauseThreadFlag) {
                    try {
                        THREAD_MONITOR.wait();
                    } catch (Exception e) {
                        logger.warn(e);
                    }
                }
            }
        }

        public void pauseThread() throws InterruptedException {
            pauseThreadFlag = true;
        }

        public void resumeThread() {
            synchronized (THREAD_MONITOR) {
                pauseThreadFlag = false;
                THREAD_MONITOR.notify();
            }
        }

        public boolean isRuning() {
            return running;
        }

        public void terminate() {
            running = false;
        }
    }

}
