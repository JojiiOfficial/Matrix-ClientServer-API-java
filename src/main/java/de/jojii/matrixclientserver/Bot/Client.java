package de.jojii.matrixclientserver.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.Callbacks.EmptyCallback;
import de.jojii.matrixclientserver.Callbacks.LoginCallback;
import de.jojii.matrixclientserver.Callbacks.MemberCallback;
import de.jojii.matrixclientserver.Callbacks.RoomEventCallback;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import de.jojii.matrixclientserver.Networking.HttpHelper;

public class Client {
    private String host;
    private LoginData loginData;
    private final HttpHelper httpHelper;
    private Syncee syncee;

    public void login(String username, String password, LoginCallback onResponse) throws IOException {
        JSONObject object = new JSONObject();
        object.put("type", "m.login.password");
        object.put("user", username);
        object.put("password", password);
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.login, object, data -> {
			LoginData loginData = Helper.ofPasswordLoginResponse((String) data);
			if (loginData.isSuccess()) {
				this.loginData = loginData;
				syncee.startSyncee();
			}
            if (onResponse != null) {
                onResponse.onResponse(loginData);
            }
        });
    }

	public void loginSync(String username, String password) throws IOException {
		JSONObject object = new JSONObject();
		object.put("type", "m.login.password");
		object.put("user", username);
		object.put("password", password);
		String loginResponse = httpHelper.sendRequest(host, HttpHelper.URLs.login, object, false, "POST");
		LoginData loginData = Helper.ofPasswordLoginResponse(loginResponse);
		if (loginData.isSuccess()) {
			this.loginData = loginData;
			syncee.startSyncee();
		}
	}

	/**
	 * Login using a matrix <a href=
	 * "https://spec.matrix.org/v1.11/client-server-api/#using-access-tokens">access
	 * token</a>.
	 * 
	 * @param userToken
	 * @param onResponse
	 * @throws IOException
	 */
    public void login(String userToken, LoginCallback onResponse) throws IOException {
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.whoami, null, "GET", data -> {
			this.loginData = null;

            JSONObject object = new JSONObject((String) data);
            LoginData loginData = new LoginData();
            if (object.has("user_id")) {
                loginData.setUser_id(object.getString("user_id"));
                loginData.setHome_server(host);
                loginData.setAccess_token(userToken);
                loginData.setSuccess(true);
                this.loginData = loginData;
                syncee.startSyncee();
            } else {
                loginData.setSuccess(false);
            }
            if (onResponse != null) {
                onResponse.onResponse(loginData);
            }
        });
	}

	/**
	 * Perform a synchronous login using a JWT token. The matrix server has to
	 * support this authentication method, else it will fail.
	 * 
	 * @param jwtToken
	 * @param deviceId this token is allocated to
	 * @throws IOException on technical error, or
	 * @see https://element-hq.github.io/synapse/latest/usage/configuration/config_documentation.html#jwt_config
	 */
	public void loginWithJWTSync(String jwtToken, @Nullable String deviceId) throws IOException {
		JSONObject object = new JSONObject();
		object.put("type", "org.matrix.login.jwt");
		object.put("token", jwtToken);
		if (deviceId != null) {
			object.put("device_id", deviceId);
		}
		String loginResponse = httpHelper.sendRequest(host, HttpHelper.URLs.login, object, false, "POST", true);
		JSONObject _loginResponse = new JSONObject(loginResponse);
		LoginData loginData = new LoginData();
		if (_loginResponse.has("user_id")) {
			loginData.setUser_id(_loginResponse.getString("user_id"));
			loginData.setHome_server(_loginResponse.getString("home_server"));
			loginData.setAccess_token(_loginResponse.getString("access_token"));
			loginData.setDevice_id(_loginResponse.getString("device_id"));
			loginData.setSuccess(true);
			this.loginData = loginData;
//			syncee.startSyncee();
		} else {
			loginData.setSuccess(false);
		}
    }

    public void registerRoomEventListener(RoomEventsCallback event) {
        syncee.addRoomEventListener(event);
    }

    public void removeRoomEventListener(RoomEventsCallback event) {
        syncee.removeRoomEventListener(event);
    }

    public void logout(EmptyCallback onLoggedOut) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.logout, null, data -> {
			this.loginData = null;
            if (onLoggedOut != null) {
                onLoggedOut.onRun();
            }
        });
    }

    public void logoutAll(EmptyCallback onLoggedOut) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.logout_all, null, data -> {
			this.loginData = null;
            if (onLoggedOut != null) {
                onLoggedOut.onRun();
            }
        });
    }

    public void whoami(DataCallback iam) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.whoami, null, "GET", data -> {
            if (iam != null) {
                JSONObject object = new JSONObject((String) data);
                if (object.has("user_id")) {
                    iam.onData(object.getString("user_id"));
                }
            }
        });
    }

    public void setPresence(String presence, String msg, EmptyCallback onStateChanged) throws IOException {
        setPresence(getLoginData().getUser_id(), presence, msg, onStateChanged);
    }

    public void setPresence(String userid, String presence, String msg, EmptyCallback onStateChanged) throws
            IOException {
				if (!isLoggedIn())
            return;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("presence", presence);
        jsonObject.put("status_msg", msg);

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.presence + userid + "/status", jsonObject, "PUT", data -> {
            if (onStateChanged != null) {
                onStateChanged.onRun();
            }
        });
    }

    public void joinRoom(String roomID, DataCallback onJoined) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/join", null, "POST", data -> {
            if (onJoined != null) {
                onJoined.onData(data);
            }
        });
    }

    public void leaveRoom(String roomID, EmptyCallback onGone) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/leave", null, "POST", data -> {
            if (onGone != null) {
                onGone.onRun();
            }
        });
    }

	/**
	 * Get or create a direct chat room with a user. If created the room will be
	 * registered in <code>m.direct</code> account data, as required in the <a href=
	 * "https://spec.matrix.org/v1.11/client-server-api/#client-behaviour-21">specification</a>.
	 * 
	 * @param userID the user id to create the direct chat room with
	 * @return <code>null</code> if not logged in otherwise the direct chat room id
	 * @throws IOException
	 */
	public String getOrCreateDirectChatRoomSync(String userID) throws IOException {
		if (!isLoggedIn()) {
			return null;
		}

		Map<String, List<String>> directChatRooms = getDirectChatRoomsMapSync();
		List<String> list = directChatRooms.get(userID);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}

		// create the direct chat room
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("is_direct", Boolean.TRUE);
		jsonObject.put("preset", "trusted_private_chat");
		jsonObject.put("invite", new JSONArray().put(userID));

		String response = httpHelper.sendRequest(host, HttpHelper.URLs.client + "createRoom", jsonObject, true, "POST",
				true);
		JSONObject object = new JSONObject(response);
		String roomID = object.getString("room_id");

		// register in users account_data m.direct
		directChatRooms.put(userID, Collections.singletonList(roomID));
		JSONObject mDirect = new JSONObject(directChatRooms);
		httpHelper.sendRequest(host, HttpHelper.URLs.user + loginData.getUser_id() + "/account_data/m.direct", mDirect,
				true, "PUT");

		return roomID;
	}

	/**
	 * Returns the map of the registered user's direct chat rooms.
	 * 
	 * @return <code>null</code> if not logged in, otherwise a map containing the
	 *         users id as keys and the respective direct chat room ids.
	 * @throws IOException
	 * @see https://spec.matrix.org/v1.11/client-server-api/#mdirect
	 */
	public Map<String, List<String>> getDirectChatRoomsMapSync() throws IOException {
		if (loginData == null) {
			return null;
		}
		String response = httpHelper.sendRequest(host,
				HttpHelper.URLs.user + loginData.getUser_id() + "/account_data/m.direct", null, true, "GET");
		JSONObject jsonObject = new JSONObject(response);
		return jsonObject.keySet().stream()
				.collect(Collectors.toMap(key -> (String) key,
						key -> IntStream.range(0, jsonObject.getJSONArray((String) key).length())
								.mapToObj(i -> jsonObject.getJSONArray((String) key).getString(i))
								.collect(Collectors.toList())));
	}

	/**
	 * Requests that the server resolve a room alias to a room ID.
	 * 
	 * @param roomID
	 * @return the resolved room id or <code>null</code> if no room_id value was
	 *         found
	 * @throws IOException
	 * @see https://spec.matrix.org/v1.11/client-server-api/#get_matrixclientv3directoryroomroomalias
	 */
	public String resolveRoomAliasSync(String roomID) throws IOException {
		if (roomID.startsWith("#")) {
			String response = httpHelper.sendRequest(host, HttpHelper.URLs.directory + "room/" + roomID, null, true,
					"GET");
			JSONObject object = new JSONObject(response);
			if (object.has("room_id")) {
				return object.getString("room_id");
			} else {
				return null;
			}
		}
		return roomID;
	}

    public void sendText(String roomID, String message, DataCallback response) throws IOException {
        sendText(roomID, message, false, "", response);
    }

    public void sendText(String roomID, String message, boolean formatted, String formattedMessage, DataCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject data = new JSONObject();
        data.put("msgtype", "m.text");
        data.put("body", message);
        if (formatted) {
            data.put("formatted_body", formattedMessage);
            data.put("format", "org.matrix.custom.html");
        }

        sendRoomEvent("m.room.message", roomID, data, response);
    }

    public void sendMessage(String roomID, JSONObject messageObject, DataCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        sendRoomEvent("m.room.message", roomID, messageObject, response);
    }

    public void sendRoomEvent(String event, String roomID, JSONObject content, DataCallback response) throws
            IOException {
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/send/" + event + "/" + System.currentTimeMillis(), content, "PUT", data -> {
            if (response != null) {
                response.onData(data);
            }
        });
    }

    public void kickUser(String roomID, String userID, String reason, DataCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject ob = new JSONObject();
        ob.put("reason", reason);
        ob.put("user_id", userID);

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/kick", ob, "POST", data -> {
            if (response != null) {
                response.onData(data);
            }
        });
    }

    public void banUser(String roomID, String userID, String reason, DataCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject ob = new JSONObject();
        ob.put("reason", reason);
        ob.put("user_id", userID);

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/ban", ob, "POST", data -> {
            if (response != null) {
                response.onData(data);
            }
        });
    }

    public void unbanUser(String roomID, String userID, DataCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject ob = new JSONObject();
        ob.put("user_id", userID);

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/unban", ob, "POST", data -> {
            if (response != null) {
                response.onData(data);
            }
        });
    }

    public void sendReadReceipt(String roomID, String eventID, String receiptType, DataCallback response) throws
            IOException {
				if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/receipt/" + receiptType + "/" + eventID, null, "POST", data -> {
            if (response != null) {
                response.onData(data);
            }
        });
    }

    public void setTyping(boolean typing, String roomID, int timeout, EmptyCallback response) throws IOException {
        setTyping(typing, getLoginData().getUser_id(), roomID, timeout, response);
    }

    public void setTyping(boolean typing, String userid, String roomID, int timeout, EmptyCallback response) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject object = new JSONObject();
        object.put("typing", String.valueOf(typing));
        object.put("timeout", timeout);

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/typing/" + userid, object, "PUT", data -> {
            if (response != null) {
                response.onRun();
            }
        });
    }

    public void getRoomMembers(String roomID, MemberCallback memberCallback) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/joined_members", null, "GET", data -> {
            if (memberCallback != null) {
                try {
                    JSONObject object = new JSONObject((String) data).getJSONObject("joined");
                    Iterator<String> keys = object.keys();
                    List<Member> members = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject user = object.getJSONObject(key);
                        String avatar = "";
                        if (user.has("avatar_url") && user.get("avatar_url") != null && user.get("avatar_url") instanceof String) {
                            avatar = user.getString("avatar_url");
                        }
                        members.add(new Member(
                                key,
                                user.getString("display_name"),
                                avatar
                        ));
                    }
                    memberCallback.onResponse(members);
                } catch (JSONException e) {
                    e.printStackTrace();
                    memberCallback.onResponse(null);
                }
            }
        });
    }

    public void getRoomEventFromId(String roomID, String eventID, RoomEventCallback callback) throws IOException {
		if (!isLoggedIn())
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + URLEncoder.encode(roomID) + "/event/" + URLEncoder.encode(eventID), null, "GET", data -> {
            if (callback != null) {
                try {
                    JSONObject object = new JSONObject((String) data);
                    System.err.println(object.toString());
                    callback.onEventReceived(RoomEvent.fetchRoomEvent(object));
                } catch (JSONException ee) {
                    ee.printStackTrace();
                }
            }
        });
    }


    public void createRoom(String preset, String visibility, @Nullable String alias, String name, @Nullable String topic, @Nullable List<String> invitations, @Nullable String roomVersion, DataCallback callback) throws IOException {
		if (!isLoggedIn())
            return;

        JSONObject object = new JSONObject();
        object.put("preset", preset);
        object.put("visibility", visibility);
        if(alias != null){
            object.put("room_alias_name", alias);
        }
        if(topic != null){
            object.put("topic", topic);
        }
        if(roomVersion != null){
            object.put("room_version", roomVersion);
        }
        object.put("name", name);
        if(invitations != null){
            JSONArray inviteUser = new JSONArray();
            for(String user : invitations){
                inviteUser.put(user);
            }
            object.put("invite", inviteUser);
        }
        createRoom(object, callback);
    }

    public void createRoom(JSONObject data, DataCallback callback) throws IOException {
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.client + "createRoom", data, "POST", responsedata -> {
            if (callback != null) {
                try {
                    JSONObject object = new JSONObject((String) responsedata);
                    if(object.has("room_id")){
                        callback.onData(object.getString("room_id"));
                    }else{
                        callback.onData(object);
                    }
                } catch (JSONException ee) {
                    ee.printStackTrace();
                }
            }
        });
    }

    public void sendFile(String contentType, int contentLength, InputStream data, DataCallback callback) throws IOException {
        httpHelper.sendStreamAsync(host, HttpHelper.URLs.upload,contentType,contentLength,data,true,"POST", responsedata -> {
            if (callback != null) {
                try {
                    JSONObject object = new JSONObject((String) responsedata);
                    if(object.has("content_uri")){
                        callback.onData(object.getString("content_uri"));
                    }else{
                        callback.onData(object);
                    }
                } catch (JSONException ee) {
                    ee.printStackTrace();
                }
            }
        });
    }



        public static class Room {
        public static String public_chat = "public_chat", private_chat = "private_chat", trusted_private_chat = "trusted_private_chat";
        public static String room_visible = "visible", room_private ="private";
    }

    public Client(String host) {
        this.host = host;
		this.httpHelper = new HttpHelper(() -> {
			return loginData != null ? loginData.getAccess_token() : null;
		});
        this.syncee = new Syncee(this, httpHelper);
        if (!host.endsWith("/"))
            this.host += "/";
    }

    /**
     * For testing only.
     */
    public Client(HttpHelper httpHelper, boolean isLoggedIn) {
        this.httpHelper = httpHelper;
        this.syncee = new Syncee(this, httpHelper);
		if (isLoggedIn) {
			LoginData loginData = new LoginData();
			loginData.setSuccess(true);
			loginData.setUser_id("@data:starship-enterprise.com");
			this.loginData = loginData;
		}
    }

    public String getHost() {
        return host;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public boolean isLoggedIn() {
		return loginData != null ? loginData.isSuccess() : false;
    }
}