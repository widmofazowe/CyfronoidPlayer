package eu.cyfronoid.audio.player;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.cyfronoid.audio.player.providers.PlayerConfigPropertiesProvider;
import eu.cyfronoid.audio.player.resources.Resources;
import eu.cyfronoid.audio.player.resources.Resources.Icons;
import eu.cyfronoid.audio.player.resources.Settings;
import eu.cyfronoid.framework.configuration.ConfigProperties;
import eu.cyfronoid.gui.about.AboutDialog;
import eu.cyfronoid.gui.image.TransparentImage;

public class PlayerConfigurator extends AbstractModule {
    private static final Logger logger = Logger.getLogger(PlayerConfigurator.class);
    public static final String SETTINGS_FILE = "settings.xml";
    public static final Settings SETTINGS;
    public static final Injector injector = Guice.createInjector(new PlayerConfigurator());
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final Locale LOCALE = new Locale("pl", "PL");
    public static final ImageIcon APPLICATION_ICON = new ImageIcon(TransparentImage.getTransparentIcon(Icons.APP_ICON.getImage(), Color.white));
    public static final AboutDialog ABOUT_DIALOG = new AboutDialog(APPLICATION_ICON, "", "", Resources.THIRD_PARTY_DEPENDENCIES, "", "");

    @Override
    protected void configure() {
        bind(ConfigProperties.class).toProvider(PlayerConfigPropertiesProvider.class);
    }

    private PlayerConfigurator() {
        DOMConfigurator.configure("configuration/log4j.xml");
    }

    static {
        Optional<Settings> loadedSettings = Settings.loadSettings();
        if(!loadedSettings.isPresent()) {
            String errorMessage = "There is no " + SETTINGS_FILE + " file.";
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        SETTINGS = loadedSettings.get();
    }

    public static void saveSettings() {
        File file = new File(SETTINGS_FILE);
        logger.debug("Saving " + SETTINGS_FILE + " file.");
        JAXBContext jaxbContext;
        try {
            file.createNewFile();
            jaxbContext = JAXBContext.newInstance(Settings.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(SETTINGS, file);
        } catch (IOException | JAXBException e) {
            logger.error(e);
        }
    }

}
