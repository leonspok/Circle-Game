package com.leonspok.circlegame.web;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class GameSocket {
    protected Session session;

    public Session getSession() {
        return this.session;
    }

    public void onMessage(String s) {

    }

    public void onOpen(Session session) {
        this.session = session;
    }

    public void onClose(int i, String s) {

    }

    public void sendAction(String action, JSONObject params) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", action);
        jsonObject.put("content", params);
        try {
            if (this.session.isOpen()) {
                this.session.getRemote().sendString(jsonObject.toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void close() {
        try {
            this.session.disconnect();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
