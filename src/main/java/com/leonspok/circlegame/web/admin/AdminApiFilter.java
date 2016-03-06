package com.leonspok.circlegame.web.admin;

import com.leonspok.circlegame.GamesManager;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class AdminApiFilter implements Filter {
    public static final String COOKIE_NAME = "admin_token";
    private ServletContext sc;

    public void init(FilterConfig filterConfig) throws ServletException {
        sc = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;

            if (httpRequest.getPathInfo().equals("login")) {
                chain.doFilter(request, response);
                return;
            }

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                boolean found = false;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(COOKIE_NAME)) {
                        found = true;
                        String token = cookie.getValue();
                        if (!GamesManager.getSharedInstance().getAdminToken().equals(token)) {
                            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }
                    }
                }
                if (!found) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            chain.doFilter(request, response);
        }
    }

    public void destroy() {

    }
}
