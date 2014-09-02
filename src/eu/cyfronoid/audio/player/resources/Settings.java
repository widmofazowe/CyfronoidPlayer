package eu.cyfronoid.audio.player.resources;

import java.awt.Dimension;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import eu.cyfronoid.audio.player.PlayerConfigurator;

@XmlRootElement
@XmlType(propOrder = {"windowDimension", "musicLibraryDirectories", "gain", "actualSelections"})
public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class);

    private List<String> musicLibraryDirectories;
    private double gain;
    private ActualSelectionSettings actualSelections;
    private Dimension windowDimension;

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
        settings.setActualSelections(new ActualSelectionSettings());
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

    @XmlElement
    public void setGain(double gain) {
        this.gain = gain;
    }

    public ActualSelectionSettings getActualSelections() {
        return actualSelections;
    }

    @XmlElement
    public void setActualSelections(ActualSelectionSettings actualSelections) {
        this.actualSelections = actualSelections;
    }

    public Dimension getWindowDimension() {
        return windowDimension;
    }

    @XmlElement
    @XmlJavaTypeAdapter(DimensionAdapter.class)
    public void setWindowDimension(Dimension windowDimension) {
        this.windowDimension = windowDimension;
    }

}
