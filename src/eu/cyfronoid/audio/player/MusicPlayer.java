package eu.cyfronoid.audio.player;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.event.UpdatePlayingProgressEvent;
import eu.cyfronoid.audio.player.song.Song;

public class MusicPlayer {
    private static final Logger logger = Logger.getLogger(MusicPlayer.class);
    private Song actualSong;
    private boolean isPlaying = false;
    private PlaybackThread playbackThread;
    private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);
    private List<PlaybackListener> playbackListeners = Lists.newArrayList();

    public static void main(String[] argv) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        MusicPlayer player = new MusicPlayer();
        Song song = new Song("Nightmare.mp3");
        player.actualSong = song;
        player.startPlaying();

    }

    public void setSong(Song song) {
        actualSong = song;
        if(playbackThread != null && playbackThread.isAlive()) {
            playbackThread.terminate();
        }
        playbackThread = null;
    }

    public void startPlaying() throws IOException, LineUnavailableException {
        if(canStartPlayback()) {
            rawPlay();
        }
    }

    public void togglePlay() {
        try {
            if(!isPlaying) {
                if(canStartPlayback()) {
                    rawPlay();
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

    private void rawPlay() throws IOException, LineUnavailableException {
        rawplay(actualSong.getFormat(), actualSong.getDecodedAudioInputStream());
    }

    private boolean canStartPlayback() {
        return playbackThread == null || (playbackThread != null && !playbackThread.isRunning);
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
        SourceDataLine line = getLine(targetFormat);
        logger.debug("Starting song " + actualSong.getTitle());
        playbackThread = new PlaybackThread(targetFormat, line, din);
        playbackThread.start();
        isPlaying = true;
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
            line.open(format, BUFFER_SIZE);
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
                isRunning = false;
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

        public void terminate() {
            running = false;
        }
    }

}
