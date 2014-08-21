package eu.cyfronoid.audio.player;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

import eu.cyfronoid.audio.player.song.Song;

public class MusicPlayer {
    private static final Logger logger = Logger.getLogger(MusicPlayer.class);
    private Song actualSong;
    private boolean isPlaying = false;
    private PlaybackThread playbackThread;

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
        line.start();
        playbackThread = new PlaybackThread(line, din);
        playbackThread.start();
        isPlaying = true;
//        try {
//            Thread.sleep(1);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private static class PlaybackThread extends Thread {
        private static final Object THREAD_MONITOR = new Object();
        private int nBytesWritten;
        private AudioInputStream din;
        private SourceDataLine line;
        private boolean pauseThreadFlag;
        private boolean isRunning = true;
        private boolean running = true;

        public PlaybackThread(SourceDataLine line, AudioInputStream din) {
            this.line = line;
            this.din = din;
        }

        @Override
        public void run() {
            byte[] data = new byte[4096];
            try {
                int nBytesRead = 0;
                nBytesWritten = 0;
                isRunning = true;
                while(nBytesRead != -1 && running) {

                    checkForPaused();
                    nBytesRead = din.read(data, 0, data.length);

                    if(nBytesRead != -1) {
                        nBytesWritten = line.write(data, 0, nBytesRead);
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
