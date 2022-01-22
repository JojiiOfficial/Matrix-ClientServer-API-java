package de.jojii.matrixclientserver.Bot.Events;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoomEvent {
    private final static String[] POINTS = {"timeline", "state", "account_data", "ephemeral", "invite_state"};
    private final static String[] POINTS_TOP = {"join", "invite", "leave"};
    private final JSONObject raw;
    private final String type, event_id, sender, room_id;
    private final JSONObject content;

    public RoomEvent(JSONObject raw, String type, String event_id, String sender, String room_id, JSONObject content) {
        this.raw = raw;
        this.type = type;
        this.event_id = event_id;
        this.sender = sender;
        this.room_id = room_id;
        this.content = content;
    }

    public static String getBodyFromMessageEvent(RoomEvent event) {
        return event.getContent().getString("body");
    }

    public static List<RoomEvent> parseAllEvents(JSONObject object) {
        List<RoomEvent> roomEvents = new ArrayList<>();
        for (String pointTop : POINTS_TOP) {

            if (!object.has("rooms"))
                continue;

            final JSONObject rooms = object.getJSONObject("rooms");

            if (!rooms.has(pointTop))
                continue;

            JSONObject object1 = rooms.getJSONObject(pointTop);
            Iterator<String> keys = object1.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject room = object1.getJSONObject(key);

                for (String point : POINTS) {
                    if (!room.has(point)) {
                        continue;
                    }

                    JSONArray timeline = room.getJSONObject(point).getJSONArray("events");
                    for (int i = 0; i < timeline.length(); i++) {
                        JSONObject event = timeline.getJSONObject(i);
                        roomEvents.add(fetchRoomEvent(event, key));
                    }
                }
            }
        }
        return roomEvents;
    }

    public static RoomEvent fetchRoomEvent(JSONObject event) {
        return fetchRoomEvent(event, "");
    }

    public static RoomEvent fetchRoomEvent(JSONObject event, String key) {
        String event_id = "", sender = "";
        try {
            event_id = event.getString("event_id");
        } catch (Exception ignore) {
        }
        try {
            sender = event.getString("sender");
        } catch (Exception ignore) {
        }

        return new RoomEvent(
                event,
                event.getString("type"),
                event_id,
                sender,
                key,
                event.getJSONObject("content")
        );
    }

    public JSONObject getRaw() {
        return raw;
    }

    public JSONObject getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getEvent_id() {
        return event_id;
    }

    public String getSender() {
        return sender;
    }

    public String getRoom_id() {
        return room_id;
    }

    @Override
    public String toString() {
        return "RoomEvent{" +
                "type='" + type + '\'' +
                ", event_id='" + event_id + '\'' +
                ", sender='" + sender + '\'' +
                ", room_id='" + room_id + '\'' +
                ", content=" + content +
                '}';
    }
}
