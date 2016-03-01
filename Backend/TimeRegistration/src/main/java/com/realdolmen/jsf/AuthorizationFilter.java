package com.realdolmen.jsf;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authorization filter that redirects all trafic to login.xhtml if the user is not logged in in the current session.
 */
@WebFilter("*.xhtml")
public class AuthorizationFilter implements Filter {

    public static final String JWT_KEY = "JWT";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) request).getSession();
        String token = (String) session.getAttribute(JWT_KEY);
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
