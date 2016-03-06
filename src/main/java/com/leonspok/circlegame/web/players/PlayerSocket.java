package com.leonspok.circlegame.web.players;

import com.leonspok.circlegame.GamesManager;
import com.leonspok.circlegame.logic.CircleGame;
import com.leonspok.circlegame.logic.CirclePlayer;
import com.leonspok.circlegame.web.GameSocket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

/**
 * Created by igorsavelev on 05/03/16.
 */
@WebSocket
public class PlayerSocket extends GameSocket {
    private CirclePlayer player;

    public CirclePlayer getPlayer() {
        return player;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        super.onOpen(session);
    }

    @OnWebSocketClose
    public void onClose(int i, String s) {
        super.onClose(i, s);
        GamesManager.getSharedInstance().removePlayerSocket(this, this.player);
    }

    @OnWebSocketMessage
    public void onMessage(String s) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(s);
            JSONObject object = (JSONObject)obj;
            String action = (String)object.get("action");
            if (action.equals("auth")) {
                String token = (String)object.get("token");
                CirclePlayer player = GamesManager.getSharedInstance().getCurrentGame().getPlayerWithUID(token);
                if (player != null) {
                    this.player = player;
                    GamesManager.getSharedInstance().addPlayerSocket(this, player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void sendStatus(PlayerStatus status, JSONObject params) {
        if (params == null) {
            params = new JSONObject();
        }

        switch (status) {
            case DISCUSSION:
                params.put("status", "discussion");
                break;
            case VOTING:
                params.put("status", "voting");
                break;
            case VOTED:
                params.put("status", "voted");
                break;
            case KILLED:
                params.put("status", "killed");
                break;
            default:
                return;
        }

        this.sendAction("set_status", params);
    }
}
