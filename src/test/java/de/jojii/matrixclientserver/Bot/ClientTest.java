package de.jojii.matrixclientserver.Bot;

import de.jojii.matrixclientserver.Callbacks.DataCallback;
import de.jojii.matrixclientserver.Callbacks.EmptyCallback;
import de.jojii.matrixclientserver.Networking.HttpHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import java.io.IOException;

import static org.mockito.Mockito.*;

class ClientTest {

    private final HttpHelper httpHelper = Mockito.mock(HttpHelper.class);

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
        verify(httpHelper)
                .sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
        callbackCaptor.getValue().onData("test");

        verify(onJoined, times(1)).onData("test");
    }

    @Test
    void joinRoom_callbackIsNull() throws IOException {
        final String expectedURL = "_matrix/client/r0/rooms/myRoom/join";

        final Client client = new Client(httpHelper, true);
        client.joinRoom("myRoom", null);

        final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
        verify(httpHelper)
                .sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
        callbackCaptor.getValue().onData("test");
    }

    @Test
    void leaveRoom_callbackIsNull() throws IOException {
        final String expectedURL = "_matrix/client/r0/rooms/myRoom/leave";

        final Client client = new Client(httpHelper, true);
        client.leaveRoom("myRoom", null);

        final ArgumentCaptor<DataCallback> callbackCaptor = ArgumentCaptor.forClass(DataCallback.class);
        verify(httpHelper)
                .sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
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
        verify(httpHelper)
                .sendRequestAsync(isNull(), eq(expectedURL), isNull(), eq("POST"), callbackCaptor.capture());
        callbackCaptor.getValue().onData("test");

        verify(onGone, times(1)).onRun();
    }
}