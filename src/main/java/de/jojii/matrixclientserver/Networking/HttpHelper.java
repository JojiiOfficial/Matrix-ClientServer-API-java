package de.jojii.matrixclientserver.Networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import de.jojii.matrixclientserver.Callbacks.DataCallback;

public class HttpHelper {
    public static class URLs{
        public static String root = "_matrix/";
        public static String client = root+"client/r0/";
        public static String media = root+"media/r0/";

        public static String login = client+"login";
        public static String logout = client+"logout";
        public static String logout_all = client+"logout/all";
        public static String whoami = client+"account/whoami";
        public static String presence = client+"presence/";
        public static String rooms = client+"rooms/";
        public static String sync = client+"sync";
        public static String user  = client+"user/";
        public static String upload = media+"upload/";
    }
    private String access_token;

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String sendRequest(String host, String path, JSONObject data, boolean useAccesstoken, String requestMethod) throws IOException {
    	//TODO accessToken mit ? oder &
        String accessTokenParameter = "";
        
        //TODO Parameter hinzufügen
		accessTokenParameter = accessTokenToAdd(useAccesstoken,path);
		String surl = host+path + accessTokenParameter;
        URL obj = new URL(surl);
        URLConnection con = obj.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod(requestMethod);
        http.setDoOutput(true);
        http.setReadTimeout(60000);

        if(data != null){
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        http.connect();

        if(data != null){
            try(OutputStream os = http.getOutputStream()) {
                os.write(data.toString().getBytes());
            }
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }catch (IOException e){
            return "{\n" +
                    "  \"response\":\"error\",\n" +
                    "  \"code\":"+http.getResponseCode()+"\n" +
                    "}";
        }
    }

	private String accessTokenToAdd(boolean useAccesstoken, String path) {
		if (!useAccesstoken) return "";
		String resultToken ="";
		if (path.contains("?")) {
			resultToken = "&";
		}else {
			resultToken = "?";
		}
		return resultToken + "access_token="+access_token;
	}

    public String sendStream(String host, String path, String contentType, InputStream data, int contentLength, boolean useAccesstoken, String requestMethod) throws IOException {
        String surl = host+path + accessTokenToAdd(useAccesstoken,path);

        URL obj = new URL(surl);
        URLConnection con = obj.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod(requestMethod);
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", contentType);
        http.addRequestProperty("Content-Length", Integer.toString(contentLength));

        try(OutputStream os = http.getOutputStream()) {
            int i = 0;
            int bytes = 0;
            while(bytes != -1) {
                byte []buff = new byte[1024];
                bytes = data.read(buff);
                if (bytes != -1) {
                    os.write(buff, 0, bytes);
                    i += bytes;
                }
            }

            os.flush();
            os.close();
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }catch (IOException e){
            return "{\n" +
                    "  \"response\":\"error\",\n" +
                    "  \"code\":"+http.getResponseCode()+"\n" +
                    "}";
        }
    }

    public void sendStreamAsync(String host, String path, String contentType, int contentLength, InputStream data, boolean useAccesstoken, String requestMethod, DataCallback callback) throws IOException {
        if(callback == null){
            System.err.println("callback must not be null!");
            return;
        }
        new Thread(() -> {
            try {
                String res = sendStream(host,path,contentType,data, contentLength, useAccesstoken,requestMethod);
                callback.onData(res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendRequestAsync(String host, String path, JSONObject data, DataCallback callback) throws IOException {
        sendRequestAsync(host, path, data, callback, access_token != null, "POST");
    }

    public void sendRequestAsync(String host, String path, JSONObject data, String requestMethd, DataCallback callback) throws IOException {
        sendRequestAsync(host, path, data, callback, access_token != null, requestMethd);
    }

    public void sendRequestAsync(String host, String path, JSONObject data, DataCallback callback, boolean useAccesstoken, String requestMethod) throws IOException {
        if(callback == null){
            System.err.println("callback must not be null!");
            return;
        }
        new Thread(() -> {
            try {
                String res = sendRequest(host,path,data,useAccesstoken,requestMethod);
                callback.onData(res);
            } catch (IOException e) {
                System.err.println("Problem at retrieving "+e);
            }
        }).start();
    }

}
