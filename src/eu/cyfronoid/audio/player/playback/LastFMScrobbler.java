package eu.cyfronoid.audio.player.playback;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;
import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.resources.LastFMSettings;
import eu.cyfronoid.audio.player.resources.Settings;
import eu.cyfronoid.audio.player.song.Song;

public class LastFMScrobbler implements PlaybackListener {
    private static final Logger logger = Logger.getLogger(LastFMScrobbler.class);
    private Optional<Session> session;

    public LastFMScrobbler() {
        Settings settings = PlayerConfigurator.SETTINGS;
        LastFMSettings lastFMSettings = settings.getLastFmSettings();
        if(lastFMSettings == null) {
            session = Optional.absent();
        } else {
            session = Optional.of(Authenticator.getMobileSession(lastFMSettings.getUser(), lastFMSettings.getPassword(), lastFMSettings.getKey(), lastFMSettings.getSecret()));
        }
    }

    @Override
    public void onFinished(Song song) {

    }

    @Override
    public void onStarted(Song song) {
        if(!session.isPresent()) {
            return;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        ScrobbleResult result = Track.scrobble(song.getArtist(), song.getTitle(), now, session.get());
        logger.info("Scrobbling " + song + ": " + (result.isSuccessful() && !result.isIgnored()));
    }

    @Override
    public void process(Song song, byte[] data) {
        if(!session.isPresent()) {
            return;
        }
        Track.updateNowPlaying(song.getArtist(), song.getTitle(), session.get());
    }


}
