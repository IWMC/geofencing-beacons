package com.realdolmen.interceptor;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
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
 * Every method or class in which {@link UserGroup#MANAGEMENT} authentication is required will go through this interceptor.
 */
@Authorized(UserGroup.MANAGEMENT)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ManagementSecurityInterceptor implements Serializable {

    @Inject
    private transient HttpServletRequest request;

    @Inject
    private transient UserContext userContext;

    @Inject
    private transient SecurityManager securityManager;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        if (userContext.getUser() != null) {
            if (userContext.getUser() instanceof ProjectManager || userContext.getUser() instanceof ManagementEmployee) {
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
