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
		// TODO Auto-generated method stub
		
		if (data instanceof String) {
			String dataToParse = (String) data;
			JSONObject jsonObject = new JSONObject(dataToParse);
			listOfRooms = jsonObject.getJSONArray("chunk").toList().stream().map(roomString->Room.buildRoom(roomString)).collect(Collectors.toList());
			
			System.out.println(jsonObject);
		}

		
	}

	private Function<Map<String, String>,Room> addRoom() {
		
		Function<Map<String, String>, Room> result = new Function<Map<String, String>, Room>() {

			@Override
			public Room apply(Map<String, String> arg0) {
				return new Room(arg0);
			}
		};
		return result ;
	}

	public List<Room> getRooms() {
		// TODO Auto-generated method stub
		return listOfRooms;
	}

}