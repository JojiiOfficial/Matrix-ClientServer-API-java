package de.jojii.matrixclientserver.Bot;

public class LoginData {
    private boolean success;
    private String user_id, access_token, home_server, device_id;

    public LoginData(boolean success, String user_id, String access_token, String home_server, String device_id) {
        this.success = success;
        this.user_id = user_id;
        this.access_token = access_token;
        this.home_server = home_server;
        this.device_id = device_id;
    }

    public LoginData(){

    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setHome_server(String home_server) {
        this.home_server = home_server;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getHome_server() {
        return home_server;
    }

    public String getDevice_id() {
        return device_id;
    }
}
