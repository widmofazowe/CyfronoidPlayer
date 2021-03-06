package eu.cyfronoid.audio.player.song;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import eu.cyfronoid.audio.player.playlist.PlaylistTableModel;
import eu.cyfronoid.gui.tableModel.TableElement;

public class Song implements TableElement {
    private SongProperties songProperties;
    private File file;
    private AudioFormat decodedFormat;
    private AudioInputStream in;
    private Map<String, Object> tableProperties = new HashMap<>();

    public Song(String fileName) throws UnsupportedAudioFileException, IOException {
        this(new File(fileName));
    }

    public Song(File file) throws UnsupportedAudioFileException, IOException {
        songProperties = new SongProperties(file);
        this.file = file;
        populateTableProperties();
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    private void populateTableProperties() {
        for(PlaylistTableModel.Column column : PlaylistTableModel.Column.values()) {
            tableProperties.put(column.columnName, column.format(songProperties.get(column.property)));
        }
    }

    public AudioInputStream getDecodedAudioInputStream() throws UnsupportedAudioFileException, IOException {
        processFile();
        return AudioSystem.getAudioInputStream(decodedFormat, in);
    }

    private void processFile() throws UnsupportedAudioFileException, IOException {
        close();
        in = getAudioInputStream();
        AudioFormat baseFormat = in.getFormat();
        decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                      baseFormat.getSampleRate(),
                      16,
                      baseFormat.getChannels(),
                      baseFormat.getChannels() * 2,
                      baseFormat.getSampleRate(),
                      false);
    }

    private AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioInputStream(file);
    }

    public AudioFormat getFormat() throws UnsupportedAudioFileException, IOException {
        processFile();
        return decodedFormat;
    }

    public SongProperties getSongProperties() {
        return songProperties;
    }

    public int getDurationInMiliseconds() {
        return songProperties.getDurationInMiliseconds();
    }

    public String getTitle() {
        return songProperties.getTitle();
    }

    public String getArtist() {
        return songProperties.getAuthor();
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

    @Override
    public Object getAttributeValue(String columnName) {
        if(columnName.equals(PlaylistTableModel.Column.FILE_NAME.columnName)) {
            return file.getName();
        }
        return tableProperties.get(columnName);
    }

    @Override
    public String toString() {
        return songProperties.getAuthor() + " - " + songProperties.getAlbum() + " - " + songProperties.getTitle();
    }

    public File getFile() {
        return file;
    }

    public void close() throws IOException {
        if(in != null) {
            in.close();
            in = null;
        }
    }

}
