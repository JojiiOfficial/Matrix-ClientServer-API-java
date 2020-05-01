package de.jojii.matrixclientserver.Callbacks;

import de.jojii.matrixclientserver.Bot.Events.RoomEvent;

import java.io.IOException;
import java.util.List;

public interface RoomEventsCallback {
    void onEventReceived(List<RoomEvent> roomEvent) throws IOException;
}
