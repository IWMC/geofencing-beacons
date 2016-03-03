package com.realdolmen.interceptor;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.jsf.Session;
import com.realdolmen.json.JsonWebToken;
import com.realdolmen.service.SecurityManager;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Interceptor for checking authorization before accessing certain resources, such as JAX-RS resources.
 * Every method or class in which {@link UserGroup#MANAGEMENT} authentication is required will go through this interceptor.
 */
@Authorized(UserGroup.MANAGEMENT)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ManagementSecurityInterceptor {

    @Inject
    private HttpServletRequest request;

    @Inject
    private Session session;

    @Inject
    private SecurityManager securityManager;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        if (session.getEmployee() != null) {
            if (session.getEmployee() instanceof ProjectManager || session.getEmployee() instanceof ManagementEmployee) {
                return ctx.proceed();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } else {
            JsonWebToken jwt = new JsonWebToken(request.getHeader("Authorization"));
            if (jwt.getToken() != null) {
                Employee employee = securityManager.findByJwt(jwt);
                if (employee != null && (employee instanceof ProjectManager || employee instanceof ManagementEmployee)) {
                    return ctx.proceed();
                }
            }

            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}
