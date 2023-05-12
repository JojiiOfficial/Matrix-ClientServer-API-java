package de.jojii.matrixclientserver.Bot;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;

import de.jojii.matrixclientserver.Callbacks.DataCallback;

public class RoomDataCallback implements DataCallback{

	private List<Room> listOfRooms = Collections.emptyList();

	@Override
	public void onData(Object data) throws IOException {
		if (data instanceof String) {
			String dataToParse = (String) data;
			JSONObject jsonObject = new JSONObject(dataToParse);
			listOfRooms = jsonObject.getJSONArray("chunk").toList().stream().map(roomString->Room.buildRoom(roomString)).collect(Collectors.toList());
		}
	}

	public List<Room> getRooms() {
		return listOfRooms;
	}

}