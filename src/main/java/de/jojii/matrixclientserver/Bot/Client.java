package de.jojii.matrixclientserver.Bot;

import org.jetbrains.annotations.Nullable;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.*;
import de.jojii.matrixclientserver.Networking.HttpHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Client {
    private String host;
    private LoginData loginData;
    private boolean isLoggedIn = false;
    private final HttpHelper httpHelper;
    private Syncee syncee;

    public void login(String username, String password, LoginCallback onResponse) throws IOException {
        JSONObject object = new JSONObject();
        object.put("type", "m.login.password");
        object.put("user", username);
        object.put("password", password);
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.login, object, data -> {
            JSONObject object1 = new JSONObject((String) data);
            LoginData loginData = new LoginData();
            if (object1.has("response") && object1.getString("response").equals("error") && object1.has("code")) {
                loginData.setSuccess(false);
            } else {
                loginData.setSuccess(true);
                isLoggedIn = true;
            }
            if (loginData.isSuccess()) {
                loginData.setAccess_token(object1.getString("access_token"));
                loginData.setDevice_id(object1.getString("device_id"));
                loginData.setHome_server(object1.getString("home_server"));
                loginData.setUser_id(object1.getString("user_id"));
                this.loginData = loginData;
                httpHelper.setAccess_token(loginData.getAccess_token());
                syncee.startSyncee();
            }
            if (onResponse != null) {
                onResponse.onResponse(loginData);
            }
        });
    }

    public void login(String userToken, LoginCallback onResponse) throws IOException {
        httpHelper.setAccess_token(userToken);
        httpHelper.sendRequestAsync(host, HttpHelper.URLs.whoami, null, "GET", data -> {
            this.isLoggedIn = false;

            JSONObject object = new JSONObject((String) data);
            LoginData loginData = new LoginData();
            if (object.has("user_id")) {
                loginData.setUser_id(object.getString("user_id"));
                loginData.setHome_server(host);
                loginData.setAccess_token(userToken);
                loginData.setSuccess(true);
                isLoggedIn = true;
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

    public void registerRoomEventListener(RoomEventsCallback event) {
        syncee.addRoomEventListener(event);
    }

    public void removeRoomEventListener(RoomEventsCallback event) {
        syncee.removeRoomEventListener(event);
    }

    public void logout(EmptyCallback onLoggedOut) throws IOException {
        if (!isLoggedIn)
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.logout, null, data -> {
            this.isLoggedIn = false;
            if (onLoggedOut != null) {
                onLoggedOut.onRun();
            }
        });
    }

    public void logoutAll(EmptyCallback onLoggedOut) throws IOException {
        if (!isLoggedIn)
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.logout_all, null, data -> {
            this.isLoggedIn = false;
            if (onLoggedOut != null) {
                onLoggedOut.onRun();
            }
        });
    }

    public void whoami(DataCallback iam) throws IOException {
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/join", null, "POST", data -> {
            if (onJoined != null) {
                onJoined.onData(data);
            }
        });
    }

    public void leaveRoom(String roomID, EmptyCallback onGone) throws IOException {
        if (!isLoggedIn)
            return;

        httpHelper.sendRequestAsync(host, HttpHelper.URLs.rooms + roomID + "/leave", null, "POST", data -> {
            if (onGone != null) {
                onGone.onRun();
            }
        });
    }

    public void sendText(String roomID, String message, DataCallback response) throws IOException {
        sendText(roomID, message, false, "", response);
    }

    public void sendText(String roomID, String message, boolean formatted, String formattedMessage, DataCallback response) throws IOException {
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        if (!isLoggedIn)
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
        this.httpHelper = new HttpHelper();
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
        this.isLoggedIn = isLoggedIn;
    }

    public String getHost() {
        return host;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}