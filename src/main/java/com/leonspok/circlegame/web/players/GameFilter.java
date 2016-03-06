package com.leonspok.circlegame.web.players;

import com.leonspok.circlegame.GamesManager;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class GameFilter implements Filter {
    private ServletContext sc;

    public void init(FilterConfig filterConfig) throws ServletException {
        sc = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest &&
                response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;

            if (GamesManager.getSharedInstance().getCurrentGame() == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath()+"no_game.html");
                return;
            }

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                boolean found = false;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(ApiFilter.COOKIE_NAME)) {
                        found = true;
                        String token = cookie.getValue();
                        if (!GamesManager.getSharedInstance().userExistsWithToken(token)) {
                            httpResponse.sendRedirect(httpRequest.getContextPath()+"login.html");
                            return;
                        }
                    }
                }
                if (!found) {
                    httpResponse.sendRedirect(httpRequest.getContextPath()+"login.html");
                    return;
                }
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath()+"login.html");
                return;
            }

            chain.doFilter(request, response);
        }
    }

    public void destroy() {

    }
}
