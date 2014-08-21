package eu.cyfronoid.audio.player.providers;

import com.google.inject.Provider;

import eu.cyfronoid.audio.player.CyfronoidPlayer;
import eu.cyfronoid.framework.configuration.ConfigProperties;
import eu.cyfronoid.framework.configuration.adapter.CompositeConfigAdapter;
import eu.cyfronoid.framework.configuration.adapter.PropertiesConfigAdapter;

public class PlayerConfigPropertiesProvider implements Provider<ConfigProperties> {

    @Override
    public ConfigProperties get() {
        CompositeConfigAdapter config = new CompositeConfigAdapter();
        config.addConfigProperties(new PropertiesConfigAdapter("configuration/app.properties"));
        config.addConfigProperties(new PropertiesConfigAdapter(CyfronoidPlayer.class.getResourceAsStream("player.properties")));
        return config;
    }


}
