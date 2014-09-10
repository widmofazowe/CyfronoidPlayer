package eu.cyfronoid.audio.player.resources;

import static de.umass.util.StringUtilities.isMD5;
import de.umass.util.StringUtilities;

public class LastFMSettings {
    private String user;
    private String password;
    private String key;
    private String secret;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = (!isMD5(password)) ? StringUtilities.md5(password) : password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
