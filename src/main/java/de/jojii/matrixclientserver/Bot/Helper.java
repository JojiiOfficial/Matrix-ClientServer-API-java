package de.jojii.matrixclientserver.Bot;

import java.util.Random;

import org.json.JSONObject;

public class Helper {
    public static int randomInt(int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

	public static LoginData ofPasswordLoginResponse(String loginResponse) {
		JSONObject _loginResponse = new JSONObject(loginResponse);
		LoginData loginData = new LoginData();
		if (_loginResponse.has("response") && _loginResponse.getString("response").equals("error")
				&& _loginResponse.has("code")) {
			loginData.setSuccess(false);
		} else {
			loginData.setSuccess(true);
		}
		if (loginData.isSuccess()) {
			loginData.setAccess_token(_loginResponse.getString("access_token"));
			loginData.setDevice_id(_loginResponse.getString("device_id"));
			loginData.setHome_server(_loginResponse.getString("home_server"));
			loginData.setUser_id(_loginResponse.getString("user_id"));
		}
		return loginData;
	}

}
