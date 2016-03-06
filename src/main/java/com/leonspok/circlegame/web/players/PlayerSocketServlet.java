package com.leonspok.circlegame.web.players;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class PlayerSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(PlayerSocket.class);
    }
}
