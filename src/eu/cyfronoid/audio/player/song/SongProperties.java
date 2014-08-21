package eu.cyfronoid.audio.player.song;

import java.io.File;
import java.io.IOException;
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

    public String getDurationInMiliseconds() {
        return (String) properties.get(SongProperty.DURATION.name);
    }


    public String getTitle() {
        return (String) properties.get(SongProperty.TITLE.name);
    }

    public static enum SongProperty {
        DURATION("duration"), //[Long], duration in microseconds.
        TITLE("title"), //[String], Title of the stream.
        AUTHOR("author"), //[String], Name of the artist of the stream.
        ALBUM("album"), //[String], Name of the album of the stream.
        DATE("date"), //[String], The date (year) of the recording or release of the stream.
        COPYRIGHT("copyright"), //[String], Copyright message of the stream.
        COMMENT("comment"), //[String], Comment of the stream.


        MP3_VERSION_MPEG("mp3.version.mpeg"), //[String], mpeg version"), //1,2 or 2.5
        MP3_VERSION_LAYER("mp3.version.layer"), //[String], layer version 1, 2 or 3
        MP3_VERSION_ENCODING("mp3.version.encoding"), //[String], mpeg encoding"), //MPEG1, MPEG2-LSF, MPEG2.5-LSF
        MP3_CHANNELS("mp3.channels"), //[Integer], number of channels 1"), //mono, 2"), //stereo.
        MP3_FREQUENCY("mp3.frequency.hz"), //[Integer], sampling rate in hz.
        MP3_BITRATE("mp3.bitrate.nominal.bps"), //[Integer], nominal bitrate in bps.
        MP3_BYTES("mp3.length.bytes"), //[Integer], length in bytes.
        MP3_FRAMES("mp3.length.frames"), //[Integer], length in frames.
        MP3_FRAMESIZE_BYTES("mp3.framesize.bytes"), //[Integer], framesize of the first frame. Framesize is not constant for VBR streams.
        MP3_FRAMESIZE_FPS("mp3.framerate.fps"), //[Float], framerate in frames per seconds.
        MP3_HEADER_POS("mp3.header.pos"), //[Integer], position of first audio header (or ID3v2 size).
        MP3_VBR("mp3.vbr"), //[Boolean], vbr flag.
        MP3_VBR_SCALE("mp3.vbr.scale"), //[Integer], vbr scale.
        MP3_CRC("mp3.crc"), //[Boolean], crc flag.
        MP3_ORIGINAL("mp3.original"), //[Boolean], original flag.
        MP3_COPYRIGHT("mp3.copyright"), //[Boolean], copyright flag.
        MP3_PADDING("mp3.padding"), //[Boolean], padding flag.
        MP3_MODE("mp3.mode"), //[Integer], mode 0:STEREO 1:JOINT_STEREO 2:DUAL_CHANNEL 3:SINGLE_CHANNEL
        MP3_GENRE("mp3.id3tag.genre"), //[String], ID3 tag (v1 or v2) genre.
        MP3_TRACK("mp3.id3tag.track"), //[String], ID3 tag (v1 or v2) track info.
        MP3_V2("mp3.id3tag.v2"), //[InputStream], ID3v2 frames.
        ;

        public final String name;

        SongProperty(String name) {
            this.name= name;
        }
    }

}
