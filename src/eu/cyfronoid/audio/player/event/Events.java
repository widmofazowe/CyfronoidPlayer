package eu.cyfronoid.audio.player.event;

public class Events {
    public static final PlaylistOpenDialogShowEvent playlistOpenDialog = new PlaylistOpenDialogShowEvent();
    public static final PlaylistSaveDialogShowEvent playlistSaveDialog = new PlaylistSaveDialogShowEvent();
    public static final NewPlaylistEvent newPlaylist = new NewPlaylistEvent();
    public static final ToggleAnalyzerPanel toggleAnalyzerPanel = new ToggleAnalyzerPanel();
    public static final PlaylistSaveEvent playlistSave = new PlaylistSaveEvent();
    public static final UpdateTabsLabelsEvent updateTabsLabels = new UpdateTabsLabelsEvent();
    public static final ClosePlayerEvent closePlayer = new ClosePlayerEvent();

    private Events() {

    }

    public static class ClosePlayerEvent {
        private ClosePlayerEvent() {

        }
    }

    public static class UpdateTabsLabelsEvent {
        private UpdateTabsLabelsEvent() {

        }
    }

    public static class PlaylistSaveEvent {
        private PlaylistSaveEvent() {

        }
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
