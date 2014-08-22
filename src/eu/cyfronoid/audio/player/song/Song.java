package eu.cyfronoid.audio.player.song;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Song {
    private SongProperties songProperties;
    private File file;
    private AudioFormat decodedFormat;
    private AudioInputStream in;

    public Song(String fileName) throws UnsupportedAudioFileException, IOException {
        this(new File(fileName));
    }

    public Song(File file) throws UnsupportedAudioFileException, IOException {
        songProperties = new SongProperties(file);
        this.file = file;
        processFile();
    }

    private void processFile() throws UnsupportedAudioFileException, IOException {
        in = AudioSystem.getAudioInputStream(file);
        AudioFormat baseFormat = in.getFormat();
        decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                      baseFormat.getSampleRate(),
                      16,
                      baseFormat.getChannels(),
                      baseFormat.getChannels() * 2,
                      baseFormat.getSampleRate(),
                      false);
    }

    public AudioInputStream getAudioInputStream() {
        return in;
    }

    public AudioInputStream getDecodedAudioInputStream() {
        return AudioSystem.getAudioInputStream(decodedFormat, in);
    }

    public AudioFormat getFormat() {
        return decodedFormat;
    }

    public SongProperties getSongProperties() {
        return songProperties;
    }

    public int getDurationInMiliseconds() {
        return songProperties.getDurationInMiliseconds();
    }

    protected void finalize() throws IOException {
        if(in != null) {
            in.close();
        }
    }

    public String getTitle() {
        return songProperties.getTitle();
    }

    public int getSizeInBytes() {
        return songProperties.getSizeInBytes();
    }

    public float getFps() {
        return songProperties.getFps();
    }

    public int getFrameSize() {
        return songProperties.getFrameSize();
    }

    public int getChannelNumber() {
        return songProperties.getChannelNumber();
    }
}
