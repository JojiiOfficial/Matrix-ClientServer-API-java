package de.jojii.matrixclientserver.Callbacks;

import de.jojii.matrixclientserver.Bot.LoginData;

import java.io.IOException;

public interface LoginCallback {
    void onResponse(LoginData data) throws IOException;
}
