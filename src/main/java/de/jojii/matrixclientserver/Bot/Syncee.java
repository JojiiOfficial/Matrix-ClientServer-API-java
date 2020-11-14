package de.jojii.matrixclientserver.Bot;

import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import de.jojii.matrixclientserver.File.FileHelper;
import de.jojii.matrixclientserver.File.Files;
import de.jojii.matrixclientserver.Networking.HttpHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Syncee {

    private static final int LONG_POLLING_TIMEOUT = 40000;

    private Client c;
    private HttpHelper httpHelper;
    private List<RoomEventsCallback> roomEvents = new ArrayList<>();
    private String filterID = null;

    public Syncee(Client c, HttpHelper httpHelper) {
        this.c = c;
        this.httpHelper = httpHelper;
    }

    void addRoomEventListener(RoomEventsCallback callback) {
        roomEvents.add(callback);
    }

    void removeRoomEventListener(RoomEventsCallback callback) {
        roomEvents.remove(callback);
    }

    void startSyncee() {
        if (filterID == null) {
            requestFilterID(data -> {
                this.filterID = (String) data;
                runEventListener(filterID);
            });
        } else {
            runEventListener(filterID);
        }
    }

    private void requestFilterID(DataCallback filterIDResponse) {
        JSONObject object = new JSONObject("{ \"room\": { \"state\": { \"types\": [ \"m.room.*\" ] } }, \"presence\": { \"types\": [ \"m.presence\" ] }, \"event_fields\": [ \"type\", \"content\", \"sender\", \"room_id\", \"event_id\" ] }");
        try {
            httpHelper.sendRequestAsync(c.getHost(), HttpHelper.URLs.user + c.getLoginData().getUser_id() + "/filter", object, data -> {
                try {
                    JSONObject object1data = new JSONObject((String) data);
                    if (object1data.has("filter_id")) {
                        if (filterIDResponse != null) {
                            filterIDResponse.onData(object1data.getString("filter_id"));
                        }
                    } else {
                        System.err.println("Error getting filter!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, true, "POST");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopSyncee() {
        if (eventListenerThread != null) {
            eventListenerThread.stop();
        }
    }

    private Thread eventListenerThread;

    private void runEventListener(String filterID) {
        if (eventListenerThread == null) {
            eventListenerThread = new Thread(() -> {
                String baseurl = HttpHelper.URLs.sync + "?access_token=" + c.getLoginData().getAccess_token() + "&filter=" + filterID + "&timeout=" + LONG_POLLING_TIMEOUT;
                String nextURL = "";
                try {
                    if(!new File(Files.sync_next_batch).exists()){
                        FileHelper.writeFile(Files.sync_next_batch, "");
                    }
                    String file_next_batch = FileHelper.readFile(Files.sync_next_batch);
                    if (file_next_batch.length() > 0) {
                        nextURL = baseurl + "&since=" + file_next_batch;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (nextURL.trim().length() == 0) {
                    nextURL = baseurl;
                }
                while (true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String data;

                    try {
                        data = httpHelper.sendRequest(c.getHost(), nextURL, null, false, "GET");
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (data != null && data.length() > 0) {
                        try {
                            JSONObject syncData = new JSONObject(data);
                            if (syncData.has("next_batch")) {
                                String nextBatch = syncData.getString("next_batch");
                                FileHelper.writeFile(Files.sync_next_batch, nextBatch);

                                List<RoomEvent> roomEvent = RoomEvent.parseAllEvents(syncData);
                                for (RoomEventsCallback roomEventsCallback : roomEvents) {
                                    if (roomEventsCallback != null) {
                                        roomEventsCallback.onEventReceived(roomEvent);
                                    }
                                }

                                nextURL = baseurl + "&since=" + nextBatch;
                            }
                        } catch (JSONException | IOException ea) {
                            ea.printStackTrace();
                            continue;
                        }
                    }
                }
            });
        }

        eventListenerThread.start();
    }

}
