package com.leonspok.circlegame.web.admin;

import com.leonspok.circlegame.GamesManager;
import com.leonspok.circlegame.web.GameSocket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;

/**
 * Created by igorsavelev on 05/03/16.
 */
@WebSocket
public class AdminSocket extends GameSocket {

    @Override
    @OnWebSocketMessage
    public void onMessage(String s) {
        super.onMessage(s);
    }

    @Override
    @OnWebSocketConnect
    public void onOpen(Session session) {
        super.onOpen(session);
        GamesManager.getSharedInstance().setAdminSocket(this);
    }

    @Override
    @OnWebSocketClose
    public void onClose(int i, String s) {
        super.onClose(i, s);
        GamesManager.getSharedInstance().removeAdminSocket();
    }

    @Override
    public void sendAction(String action, JSONObject params) {
        super.sendAction(action, params);
    }

    public void sendStatus(AdminStatus status, JSONObject params) {
        if (params == null) {
            params = new JSONObject();
        }

        switch (status) {
            case NO_GAME:
                params.put("status", "no_game");
                break;
            case REGISTRATION:
                params.put("status", "registration");
                break;
            case DISCUSSION:
                params.put("status", "discussion");
                break;
            case VOTING:
                params.put("status", "voting");
                break;
            case VOTED:
                params.put("status", "voted");
                break;
            case FINISHED:
                params.put("status", "finished");
                break;
            default:
                return;
        }

        this.sendAction("set_status", params);
    }
}
