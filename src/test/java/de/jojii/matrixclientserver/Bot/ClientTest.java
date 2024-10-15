package de.jojii.matrixclientserver.Bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.Callbacks.EmptyCallback;
import de.jojii.matrixclientserver.Networking.HttpHelper;

class ClientTest {

	private final HttpHelper httpHelper = Mockito.mock(HttpHelper.class, withSettings().verboseLogging());

	@Test
	void joinRoom_notLoggedIn() throws IOException {
		Client client = new Client(httpHelper, false);
		DataCallback callback = Mockito.mock(DataCallback.class);

		client.joinRoom("myRoom", callback);

		verify(callback, never()).onData(Any.ANY);
	}

	@Test
	void joinRoom() throws IOException {
		final String expectedURL = "_matrix/client/r0/rooms/myRoom/join";

		final Client client = new Client(httpHelper, true);
		final DataCallback onJoined = Mockito.mock(DataCallback.class);

		// invoke method
		client.joinRoom("myRoom", onJoined);

		final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
		verify(httpHelper).sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
		callbackCaptor.getValue().onData("test");

		verify(onJoined, times(1)).onData("test");
	}

	@Test
	void joinRoom_callbackIsNull() throws IOException {
		final String expectedURL = "_matrix/client/r0/rooms/myRoom/join";

		final Client client = new Client(httpHelper, true);
		client.joinRoom("myRoom", null);

		final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
		verify(httpHelper).sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
		callbackCaptor.getValue().onData("test");
	}

	@Test
	void leaveRoom_callbackIsNull() throws IOException {
		final String expectedURL = "_matrix/client/r0/rooms/myRoom/leave";

		final Client client = new Client(httpHelper, true);
		client.leaveRoom("myRoom", null);

		final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
		verify(httpHelper).sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
		callbackCaptor.getValue().onData("test");
	}

	@Test
	void leaveRoom() throws IOException {
		final String expectedURL = "_matrix/client/r0/rooms/familyChat/leave";

		final Client client = new Client(httpHelper, true);
		final EmptyCallback onGone = Mockito.mock(EmptyCallback.class);

		// invoke method
		client.leaveRoom("familyChat", onGone);

		final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
		verify(httpHelper).sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
		callbackCaptor.getValue().onData("test");

		verify(onGone, times(1)).onRun();
	}

	@Test
	void resolveRoomAliasSync() throws IOException {

		final String expectedURL = "_matrix/client/r0/directory/room";

		final Client client = new Client(httpHelper, true);

		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("room_id", "!hCBUUsLgnlXIvwmnkT:starship-enterprise.com");
		JSONArray servers = new JSONArray();
		servers.put("starship-enterprise.com");
		jsonResponse.put("servers", servers);

		when(httpHelper.sendRequest(isNull(), startsWith(expectedURL), isNull(), eq(true), eq("GET")))
				.thenReturn(jsonResponse.toString());

		String resolvedId = client.resolveRoomAliasSync("#holodeck:starship-enterprise.com");
		assertEquals("!hCBUUsLgnlXIvwmnkT:starship-enterprise.com", resolvedId);
	}

	@Test
	void getDirectRoomMapSync() throws IOException {
		final Client client = new Client(httpHelper, true);
		performLogin(client);

		final String expectedURL = "_matrix/client/r0/user/@data:starship-enterprise.com/account_data/m.direct";

		JSONObject jsonResponse = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put("!rGkalNKwzOdTviJzbY:starship-enterprise.com");
		jsonResponse.put("@picard:starship-enterprise.com", jsonArray);

		when(httpHelper.sendRequest(isNull(), startsWith(expectedURL), isNull(), eq(true), eq("GET")))
				.thenReturn(jsonResponse.toString());

		Map<String, List<String>> directRoomMap = client.getDirectChatRoomsMapSync();
		List<String> list = directRoomMap.get("@picard:starship-enterprise.com");
		assertEquals("!rGkalNKwzOdTviJzbY:starship-enterprise.com", list.get(0));
	}

	@Test
	void getOrCreateDirectChatRoomSync() throws IOException {
		final Client client = new Client(httpHelper, true);
		performLogin(client);

		// no direct message room for @geordi found, will need to create
		String expectedURL = "_matrix/client/r0/user/@data:starship-enterprise.com/account_data/m.direct";
		when(httpHelper.sendRequest(isNull(), startsWith(expectedURL), isNull(), eq(true), eq("GET")))
				.thenReturn(new JSONObject().toString());
		// TODO do not deliver empty but other, then test if correct update

		expectedURL = "_matrix/client/r0/createRoom";
		when(httpHelper.sendRequest(isNull(), startsWith(expectedURL), isNotNull(), eq(true), eq("POST"), eq(true)))
				.thenReturn("{\"room_id\":\"!geordihere:starship-enterprise.com\"}");

		String roomId = client.getOrCreateDirectChatRoomSync("@geordi:starship-enterprise.com");
		assertEquals("!geordihere:starship-enterprise.com", roomId);
	}

	private void performLogin(Client client) throws IOException {
		JSONObject loginResponse = new JSONObject();
		loginResponse.put("user_id", "@data:starship-enterprise.com");
		loginResponse.put("access_token", "syt_c2VydmljZS1hY2NvdW50LWVsZXhpcy1zZXJ2ZXI_ZwTAYorZbOeMzLGRBDTt_3ltbrk");
		loginResponse.put("home_server", "starship-enterprise.com");
		loginResponse.put("device_id", "mockito");

		when(httpHelper.sendRequest(isNull(), eq(HttpHelper.URLs.login), isNotNull(), eq(false), eq("POST")))
				.thenReturn(loginResponse.toString());
		client.loginSync("@data:starship-enterprise.com", "doesnotcompute");
		assertTrue(client.isLoggedIn());
	}

}