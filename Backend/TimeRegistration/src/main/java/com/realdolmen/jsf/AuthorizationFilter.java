package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.json.JsonWebToken;
import com.realdolmen.service.SecurityManager;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Authorization filter that redirects all traffic to login.xhtml if the user is not logged in in the current session.
 */
@ApplicationScoped
@WebFilter(urlPatterns = "*.xhtml", initParams = {
        @WebInitParam(name = AuthorizationFilter.INCLUDED_WEB_INIT_PARAM, value = "/index.xhtml;/employees/.*")
})
public class AuthorizationFilter implements Filter {

    public static final String JWT_KEY = "JWT";
    public static final String INCLUDED_WEB_INIT_PARAM = "included";

    @Inject
    private Session session;

    private List<String> includedUrls = new ArrayList<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Employee employee = session.getEmployee();
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (includedUrls.stream().anyMatch(httpRequest.getRequestURI()::matches) && employee == null) {
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.sendRedirect(Pages.login().noRedirect());
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Stream.of(filterConfig.getInitParameter(INCLUDED_WEB_INIT_PARAM).split(";")).forEach(includedUrls::add);
    }

    @Override
    public void destroy() {
    }

}
