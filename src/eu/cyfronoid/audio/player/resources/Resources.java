package eu.cyfronoid.audio.player.resources;

import java.awt.Image;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ImageIcon;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.framework.configuration.ConfigProperties;
import eu.cyfronoid.gui.about.dependency.ThirdPartyDependency;

public enum Resources {
    PLAYER
    ;

    private ConfigProperties configPorperties = PlayerConfigurator.injector.getInstance(ConfigProperties.class);

    public String get(String name, Object... objects) {
        return MessageFormat.format(configPorperties.get(name), objects);
    }

    public static class PropertyKey {
        public static final String FILE_MENU = "file_menu";
        public static final String HELP_MENU = "help_menu";
        public static final String EXIT= "exit";
        public static final String ABOUT = "about";
    }

    public static Collection<ThirdPartyDependency> THIRD_PARTY_DEPENDENCIES = Collections.emptyList();

    public static enum Icons {
        APP_ICON("");

        private static final String DIR = "resources/icons/";
        private final String path;
        private final ImageIcon imageIcon;

        Icons(String iconName) {
            path = DIR + iconName;
            imageIcon = new ImageIcon(path);
        }

        public ImageIcon getImageIcon() {
            return imageIcon;
        }

        public String getPath() {
            return path;
        }

        public Image getImage() {
            return imageIcon.getImage();
        }
    }
}
