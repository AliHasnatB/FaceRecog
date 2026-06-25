package com.backex.faceox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.function.Consumer;

public class FaceWebSocketService extends WebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Consumer<JsonNode> onResult;
    private final Consumer<String>   onStatus;

    public FaceWebSocketService(String url,
                                Consumer<JsonNode> onResult,
                                Consumer<String>   onStatus) throws Exception {
        super(new URI(url));
        this.onResult = onResult;
        this.onStatus = onStatus;
    }

    @Override public void onOpen(ServerHandshake h)            { onStatus.accept("CONNECTED"); }
    @Override public void onClose(int c, String r, boolean re) { onStatus.accept("DISCONNECTED"); }
    @Override public void onError(Exception ex)                { onStatus.accept("ERROR: " + ex.getMessage()); }

    @Override
    public void onMessage(String message) {
        try   { onResult.accept(objectMapper.readTree(message)); }
        catch (Exception e) { onStatus.accept("PARSE_ERROR"); }
    }

    public void sendFrame(byte[] bytes) { if (isOpen()) send(bytes); }
}
