package eu.cyfronoid.audio.player.resources;

import java.awt.Dimension;

public enum DefaultSettings {
    INSTANCE;

    private Dimension dimension = new Dimension(800, 600);

    public Dimension getDimension() {
        return dimension ;
    }
}
