package eu.cyfronoid.audio.player.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import eu.cyfronoid.audio.player.PlayerConfigurator;

@XmlRootElement
@XmlType
public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class);

    private List<String> musicLibraryDirectories;
    private double gain;

    public List<String> getMusicLibraryDirectories() {
        return musicLibraryDirectories;
    }

    @XmlElementWrapper(name="musicLibraryDirectories")
    @XmlElement(name="directory")
    public void setMusicLibraryDirectories(List<String> musicLibraryDirectories) {
        this.musicLibraryDirectories = musicLibraryDirectories;
    }

    public static Optional<Settings> loadSettings() {
        Settings settings = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Settings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            settings = (Settings) jaxbUnmarshaller.unmarshal(new File(PlayerConfigurator.SETTINGS_FILE));
            logger.debug("Unmarchalled: " + PlayerConfigurator.SETTINGS_FILE);
        } catch (JAXBException e) {
            logger.error(e);
        }
        return Optional.fromNullable(settings);
    }

    public static void main(String[] argv) {
        Settings settings = loadSettings().get();
        List<String> directories = new ArrayList<>();
        directories.add("MusicLibrary");
        settings.setMusicLibraryDirectories(directories);
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Settings.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(settings, System.out);
        } catch (JAXBException e) {
            logger.error(e);
        }
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

}
