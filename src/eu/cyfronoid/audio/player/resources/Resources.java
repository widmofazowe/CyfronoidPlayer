package eu.cyfronoid.audio.player.resources;

import java.awt.Image;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.framework.configuration.ConfigProperties;
import eu.cyfronoid.gui.about.dependency.ThirdPartyDependency;

public enum Resources {
    PLAYER
    ;

    private static final Logger logger = Logger.getLogger(Resources.class);
    private ConfigProperties configPorperties = PlayerConfigurator.injector.getInstance(ConfigProperties.class);

    public String get(String name, Object... objects) {
        logger.debug("Getting property " + name + ((objects.length > 0) ? " with arguments: " + Joiner.on(",").join(objects) : ""));
        String pattern = configPorperties.get(name);
        if(pattern == null) {
            logger.warn("There is no property '" + name + "'");
            return name;
        }
        return MessageFormat.format(pattern, objects);
    }

    public static class PropertyKey {
        public static final String FILE_MENU = "file_menu";
        public static final String HELP_MENU = "help_menu";
        public static final String EXIT= "exit";
        public static final String ABOUT = "about";
        public static final String ABOUT_TEXT = "about_text";
        public static final String APP_TEXT = "app_text";
        public static final String DEPENDENCIES = "dependencies";
        public static final String PREVIOUS = "previous";
        public static final String NEXT = "next";
        public static final String PLAY = "play";
        public static final String PAUSE = "pause";
        public static final String OPEN = "open";
        public static final String NEW = "new";
        public static final String VIEW_MENU = "view";
        public static final String TOGGLE_SPECTRUM = "toggle_spectrum";
        public static final String SAVE = "save";
        public static final String EXIT_WITH_UNSAVED = "exit_with_unsaved";
        public static final String SAVING_DIALOG_TITLE = "saving_dialog_title";
        public static final String REMOVE = "remove";
        public static final String CLOSE = "close";
        public static final String CLOSE_ALL = "close_all";
        public static final String RENAME = "rename";
        public static final String NEW_NAME = "new_name";
    }

    public static Collection<ThirdPartyDependency> THIRD_PARTY_DEPENDENCIES = Collections.emptyList();

    public static enum Icons {
        APP_ICON(""),
        LEFT_ARROW("rewind12.png"),
        RIGHT_ARROW("rewind13.png"),
        PLAY_ARROW("play13.png"),
        PAUSE_ARROW("pause7.png"),
        ;

        private static final String DIR = "/resources/icons/";
        private final String path;
        private final ImageIcon imageIcon;

        Icons(String iconName) {
            path = DIR + iconName;
            imageIcon = new ImageIcon(Resources.class.getResource(path));
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
