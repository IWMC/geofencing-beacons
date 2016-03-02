package com.realdolmen.interceptor;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.jsf.Session;
import com.realdolmen.json.JsonWebToken;
import com.realdolmen.service.SecurityManager;

import javax.annotation.Priority;
import javax.annotation.Resource;
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
 * Every method or class in which only {@link UserGroup#EMPLOYEE} is required will go through this interceptor.
 */
@Authorized(UserGroup.EMPLOYEE)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class EmployeeSecurityInterceptor {

    @Inject
    private HttpServletRequest request;

    @Inject
    private Session session;

    @Inject
    private SecurityManager securityManager;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        if (session.getEmployee() != null) {
            return ctx.proceed();
        } else {
            JsonWebToken jwt = new JsonWebToken(request.getHeader("Authorization"));
            return jwt.getToken() == null ? Response.status(Response.Status.FORBIDDEN).build() : ctx.proceed();
        }
    }
}
