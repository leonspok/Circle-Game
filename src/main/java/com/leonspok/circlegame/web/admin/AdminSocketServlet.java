package com.leonspok.circlegame.web.admin;

import com.leonspok.circlegame.web.admin.AdminSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class AdminSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(AdminSocket.class);
    }
}
