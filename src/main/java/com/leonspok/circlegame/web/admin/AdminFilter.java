package com.leonspok.circlegame.web.admin;

import com.leonspok.circlegame.GamesManager;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by igorsavelev on 05/03/16.
 */
public class AdminFilter implements Filter {
    private ServletContext sc;

    public void init(FilterConfig filterConfig) throws ServletException {
        sc = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest &&
                response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                boolean found = false;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(AdminApiFilter.COOKIE_NAME)) {
                        found = true;
                        String token = cookie.getValue();
                        if (!token.equals(GamesManager.getSharedInstance().getAdminToken())) {
                            httpResponse.sendRedirect(httpRequest.getContextPath()+"admin_login.html");
                            return;
                        }
                    }
                }
                if (!found) {
                    httpResponse.sendRedirect(httpRequest.getContextPath()+"admin_login.html");
                    return;
                }
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath()+"admin_login.html");
                return;
            }

            chain.doFilter(request, response);
        }
    }

    public void destroy() {

    }
}
