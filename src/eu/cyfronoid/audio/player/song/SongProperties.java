package eu.cyfronoid.audio.player.song;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

public class SongProperties {
    private Map<String, Object> properties;
    private Map<String, Object> additionalProperties;

    public SongProperties(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat baseFileFormat = null;
        AudioFormat baseFormat = null;
        baseFileFormat = AudioSystem.getAudioFileFormat(file);
        baseFormat = baseFileFormat.getFormat();
        if (baseFileFormat instanceof TAudioFileFormat) {
            properties = ((TAudioFileFormat)baseFileFormat).properties();
        }
        if (baseFormat instanceof TAudioFormat) {
             additionalProperties = ((TAudioFormat)baseFormat).properties();
        }
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public int getDurationInMiliseconds() {
        return ((Long) get(SongProperty.DURATION)).intValue()/1000;
    }

    public String getTitle() {
        return (String) get(SongProperty.TITLE);
    }

    public int getSizeInBytes() {
        return (int) get(SongProperty.MP3_BYTES);
    }

    public float getFps() {
        return (float) get(SongProperty.MP3_FRAMESIZE_FPS);
    }

    public int getFrameSize() {
        return (int) get(SongProperty.MP3_FRAMESIZE_BYTES);
    }

    public int getFramesNumber() {
        return (int) get(SongProperty.MP3_FRAMES);
    }

    public int getChannelNumber() {
        return (int) get(SongProperty.MP3_CHANNELS);
    }

    public Object get(SongProperty property) {
        return properties.get(property.name);
    }

    public static enum SongProperty {
        DURATION("duration", Long.class), //[Long], duration in microseconds.
        TITLE("title", String.class), //[String], Title of the stream.
        AUTHOR("author", String.class), //[String], Name of the artist of the stream.
        ALBUM("album", String.class), //[String], Name of the album of the stream.
        DATE("date", String.class), //[String], The date (year) of the recording or release of the stream.
        COPYRIGHT("copyright", String.class), //[String], Copyright message of the stream.
        COMMENT("comment", String.class), //[String], Comment of the stream.
        FILE_NAME("file.name", String.class), //[String], Comment of the stream.

        MP3_VERSION_MPEG("mp3.version.mpeg", String.class), //[String], mpeg version"), //1,2 or 2.5
        MP3_VERSION_LAYER("mp3.version.layer", String.class), //[String], layer version 1, 2 or 3
        MP3_VERSION_ENCODING("mp3.version.encoding", String.class), //[String], mpeg encoding"), //MPEG1, MPEG2-LSF, MPEG2.5-LSF
        MP3_CHANNELS("mp3.channels", Integer.class), //[Integer], number of channels 1"), //mono, 2"), //stereo.
        MP3_FREQUENCY("mp3.frequency.hz", Integer.class), //[Integer], sampling rate in hz.
        MP3_BITRATE("mp3.bitrate.nominal.bps", Integer.class), //[Integer], nominal bitrate in bps.
        MP3_BYTES("mp3.length.bytes", Integer.class), //[Integer], length in bytes.
        MP3_FRAMES("mp3.length.frames", Integer.class), //[Integer], length in frames.
        MP3_FRAMESIZE_BYTES("mp3.framesize.bytes", Integer.class), //[Integer], framesize of the first frame. Framesize is not constant for VBR streams.
        MP3_FRAMESIZE_FPS("mp3.framerate.fps", Float.class), //[Float], framerate in frames per seconds.
        MP3_HEADER_POS("mp3.header.pos", Integer.class), //[Integer], position of first audio header (or ID3v2 size).
        MP3_VBR("mp3.vbr", Boolean.class), //[Boolean], vbr flag.
        MP3_VBR_SCALE("mp3.vbr.scale", Integer.class), //[Integer], vbr scale.
        MP3_CRC("mp3.crc", Boolean.class), //[Boolean], crc flag.
        MP3_ORIGINAL("mp3.original", Boolean.class), //[Boolean], original flag.
        MP3_COPYRIGHT("mp3.copyright", Boolean.class), //[Boolean], copyright flag.
        MP3_PADDING("mp3.padding", Boolean.class), //[Boolean], padding flag.
        MP3_MODE("mp3.mode", Integer.class), //[Integer], mode 0:STEREO 1:JOINT_STEREO 2:DUAL_CHANNEL 3:SINGLE_CHANNEL
        MP3_GENRE("mp3.id3tag.genre", String.class), //[String], ID3 tag (v1 or v2) genre.
        MP3_TRACK("mp3.id3tag.track", String.class), //[String], ID3 tag (v1 or v2) track info.
        MP3_V2("mp3.id3tag.v2", InputStream.class), //[InputStream], ID3v2 frames.
        ;

        public final String name;
        public final Class<?> type;

        SongProperty(String name, Class<?> type) {
            this.name= name;
            this.type = type;
        }
    }


}
