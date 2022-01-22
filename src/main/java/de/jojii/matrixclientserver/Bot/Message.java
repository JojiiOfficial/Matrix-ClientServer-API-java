package de.jojii.matrixclientserver.Bot;

import org.json.JSONObject;

public class Message {

    private JSONObject raw;
    private String type;
    private String body;
    private FileInfo fileInfo;
    private String url;

    public static Message parseMessageContent(JSONObject content) {
        final Message message = new Message();
        message.raw = content;
        if (content.has("msgtype")) {
            message.type = content.getString("msgtype");
        }
        if (content.has("body")) {
            message.body = content.getString("body");
        }
        if (content.has("url")) {
            message.url = content.getString("url");
        }
        if (content.has("info")) {
            JSONObject info = content.getJSONObject("info");
            message.fileInfo = new FileInfo();
            message.fileInfo.blurhash = info.getString("xyz.amorgan.blurhash");
            message.fileInfo.byteSize = info.getInt("size");
            message.fileInfo.width = info.getInt("w");
            message.fileInfo.height = info.getInt("h");
            message.fileInfo.mimetype = info.getString("mimetype");
        }

        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", body='" + body + '\'' +
                ", fileInfo=" + fileInfo +
                ", url='" + url + '\'' +
                '}';
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public String getUrl() {
        return url;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }


    public static class FileInfo {
        String blurhash;
        int byteSize;
        int width;
        int height;
        String mimetype;

        public String getBlurhash() {
            return blurhash;
        }

        public int getByteSize() {
            return byteSize;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getMimetype() {
            return mimetype;
        }


    }
}
