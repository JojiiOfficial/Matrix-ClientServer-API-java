package de.jojii.matrixclientserver.Bot;

import java.util.Map;

public class Room {
    public static String public_chat = "public_chat", private_chat = "private_chat", trusted_private_chat = "trusted_private_chat";
    public static String room_visible = "visible", room_private ="private";
	private String roomId;
	private String name;
	private String canonicalAlias;

	public Room(Map<String,String> aNewRoomMap) {
		this.roomId = aNewRoomMap.get("room_id");
		name = aNewRoomMap.get("name");
		canonicalAlias = aNewRoomMap.get("canonical_alias");
	}

	public static RoomDataCallback buildRooms() {
		return new RoomDataCallback();
	}

	public static Room  buildRoom(Object aNewRoomMap) {
		return new Room((Map<String,String>)aNewRoomMap);
	}

	public String getRoomId() {
		return roomId;
	}

	public String getName() {
		return name;
	}

	public String getCanonicalAlias() {
		return canonicalAlias;
	}
}