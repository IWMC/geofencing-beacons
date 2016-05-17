package com.realdolmen.interceptor;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.json.JsonWebToken;
import com.realdolmen.service.SecurityManager;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * Interceptor for checking authorization before accessing certain resources, such as JAX-RS resources.
 * Every method or class in which only {@link UserGroup#EMPLOYEE} is required will go through this interceptor.
 */
@Authorized(UserGroup.EMPLOYEE)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class EmployeeSecurityInterceptor implements Serializable {

    @Inject
    private transient HttpServletRequest request;

    @Inject
    private transient UserContext userContext;

    @Inject
    private transient SecurityManager securityManager;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        if (userContext.getUser() != null) {
            return ctx.proceed();
        } else {
            JsonWebToken jwt = new JsonWebToken(request.getHeader("Authorization"));
            return jwt.getToken() == null || !securityManager.isValidToken(jwt)
                    ? Response.status(Response.Status.FORBIDDEN).build() : ctx.proceed();
        }
    }
}
