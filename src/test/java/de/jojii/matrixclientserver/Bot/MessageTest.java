package de.jojii.matrixclientserver.Bot;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageTest {

    @Test
    void parseMessageContent_text() {
        final JSONObject json = new JSONObject("{\"body\":\"Hello, world!\",\"msgtype\":\"m.text\"}");

        final Message result = Message.parseMessageContent(json);

        assertNotNull(result);
        assertEquals("m.text", result.getType());
        assertEquals("Hello, world!", result.getBody());
    }

    @Test
    void parseMessageContent_image() {
        final JSONObject json = new JSONObject("{\"body\":\"red.png\",\"msgtype\":\"m.image\",\"url\":\"mxc://example.org/dQRxjroKFYddMfTrpUYrXMRP\",\"info\":{\"xyz.amorgan.blurhash\":\"U#Q[S_xa%~%2r?jZozj[.mkCR5j[%Mj[ayj[\",\"size\":98,\"w\":30,\"h\":20,\"mimetype\":\"image/png\"}}");

        final Message result = Message.parseMessageContent(json);

        assertNotNull(result);
        assertEquals("m.image", result.getType());
        assertEquals("red.png", result.getBody());
        assertEquals("mxc://example.org/dQRxjroKFYddMfTrpUYrXMRP", result.getUrl());
        Message.FileInfo fileInfo = result.getFileInfo();
        assertNotNull(fileInfo);
        assertEquals("U#Q[S_xa%~%2r?jZozj[.mkCR5j[%Mj[ayj[", fileInfo.getBlurhash());
        assertEquals(98, fileInfo.getByteSize());
        assertEquals(20, fileInfo.getHeight());
        assertEquals(30, fileInfo.getWidth());
        assertEquals("image/png", fileInfo.getMimetype());
    }
}