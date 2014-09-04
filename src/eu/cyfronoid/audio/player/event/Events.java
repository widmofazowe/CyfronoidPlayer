package eu.cyfronoid.audio.player.event;

public class Events {
    public static final PlaylistOpenDialogShowEvent playlistOpenDialog = new PlaylistOpenDialogShowEvent();
    public static final PlaylistSaveDialogShowEvent playlistSaveDialog = new PlaylistSaveDialogShowEvent();

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
}
