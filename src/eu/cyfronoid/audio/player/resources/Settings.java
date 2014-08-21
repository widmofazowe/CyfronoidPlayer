package eu.cyfronoid.audio.player.resources;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import eu.cyfronoid.audio.player.PlayerConfigurator;

@XmlRootElement
@XmlType
public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class);

    public static Optional<Settings> loadSettings() {
        Settings settings = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Settings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            settings = (Settings) jaxbUnmarshaller.unmarshal(new File(PlayerConfigurator.SETTINGS_FILE));
            logger.debug("Unmarchalled: " + settings);
        } catch (JAXBException e) {
            logger.error(e);
        }
        return Optional.fromNullable(settings);
    }

}
