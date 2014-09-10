package eu.cyfronoid.audio.player.playlist;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import eu.cyfronoid.framework.util.ExceptionHelper;

@XmlRootElement
@XmlType(propOrder = {"name", "orderedSongs", "file"})
public class Playlist {
    private final static Logger logger = Logger.getLogger(Playlist.class);
    public static final Playlist EMPTY_PLAYLIST = new Playlist();
    private String name;
    private SortedMap<Integer, File> orderedSongs;
    @XmlTransient
    private File file;

    public Playlist() {
        orderedSongs = Maps.newTreeMap();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedMap<Integer, File> getOrderedSongs() {
        return orderedSongs;
    }

    public void swap(int x, int y) {
        File tempX = orderedSongs.get(x);
        orderedSongs.put(x, orderedSongs.get(y));
        orderedSongs.put(y, tempX);
    }

    @XmlElementWrapper(name="orderedSongs")
    public void setOrderedSongs(SortedMap<Integer, File> orderedSongs) {
        this.orderedSongs = orderedSongs;
    }

    public static Optional<Playlist> loadPlaylist(File file) {
        Playlist playlist = null;
        JAXBContext jaxbContext;
        if(!file.exists() || !file.isFile()) {
            return Optional.absent();
        }
        try {
            jaxbContext = JAXBContext.newInstance(Playlist.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            playlist = (Playlist) jaxbUnmarshaller.unmarshal(file);
            logger.debug("Unmarchalled: " + file.getPath());
        } catch (JAXBException e) {
            logger.error(ExceptionHelper.getStackTrace(e));
        }
        return Optional.fromNullable(playlist);
    }

    public static void main(String[] argv) {
        Playlist playlist = new Playlist();
        playlist.setName("Test");
        SortedMap<Integer, File> sortedMap = new TreeMap<>();
        sortedMap.put(2, new File("MusicLibrary/Disturbed/Disturbed - Stupify.mp3"));
        sortedMap.put(4, new File("MusicLibrary/Black Label/Black Label Society - Fire It Up.mp3"));
        sortedMap.put(3, new File("MusicLibrary/Black Label/Black Label Society - Death March.mp3"));
        sortedMap.put(1, new File("MusicLibrary/Disturbed/Disturbed - The Game.mp3"));
        playlist.setOrderedSongs(sortedMap);
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Playlist.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(playlist, System.out);

        } catch (JAXBException e) {
            logger.error(e);
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
