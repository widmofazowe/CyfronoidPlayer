package eu.cyfronoid.audio.player.resources;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Resources {

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
