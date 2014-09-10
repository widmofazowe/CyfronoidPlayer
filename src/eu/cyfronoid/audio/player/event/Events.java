package eu.cyfronoid.audio.player.event;

public class Events {
    public static final PlaylistOpenDialogShowEvent playlistOpenDialog = new PlaylistOpenDialogShowEvent();
    public static final PlaylistSaveDialogShowEvent playlistSaveDialog = new PlaylistSaveDialogShowEvent();
    public static final NewPlaylistEvent newPlaylist = new NewPlaylistEvent();
    public static final ToggleAnalyzerPanel toggleAnalyzerPanel = new ToggleAnalyzerPanel();

    private Events() {

    }

    public static class PlaylistSaveDialogShowEvent {
        private PlaylistSaveDialogShowEvent() {

        }
    }

    public static class PlaylistOpenDialogShowEvent {
        private PlaylistOpenDialogShowEvent() {

        }
    }

    public static class NewPlaylistEvent {
        private NewPlaylistEvent() {

        }
    }

    public static class ToggleAnalyzerPanel {
        private ToggleAnalyzerPanel() {

        }
    }
}
