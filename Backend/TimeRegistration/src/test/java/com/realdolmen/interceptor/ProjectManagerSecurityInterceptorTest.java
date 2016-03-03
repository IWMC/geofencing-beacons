package com.realdolmen.interceptor;

import com.realdolmen.ArquillianUtil;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.jsf.Session;
import com.realdolmen.service.SecurityManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class ProjectManagerSecurityInterceptorTest {

    @InjectMocks
    private ProjectManagerSecurityInterceptor interceptor = new ProjectManagerSecurityInterceptor();

    @Mock
    private InvocationContext context;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityManager securityManager;

    @Mock
    private Session session;

    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private void invokeAndCheckDenial() throws Exception {
        Object objectResponse = interceptor.manageTransaction(context);
        assertTrue("response should be of type " + Response.class.getCanonicalName(), objectResponse instanceof Response);
        Response response = (Response) objectResponse;
        assertEquals("response should return status code 403", 403, response.getStatus());
        verify(context, never()).proceed();
    }

    private void invokeAndCheckAcceptance() throws Exception {
        interceptor.manageTransaction(context);
        verify(context, times(1)).proceed();
    }

    private void configureJWT(boolean valid) {
        when(request.getHeader("Authorization")).thenReturn("a.jwt.token");
        when(securityManager.isValidToken(any())).thenReturn(valid);
    }

    @Test
    public void testInterceptorDeniesOnNoCredentials() throws Exception {
        invokeAndCheckDenial();
    }

    @Test
    public void testInterceptorAcceptsOnProjectManagerCredentialsJWT() throws Exception {
        configureJWT(true);
        when(securityManager.findByJwt(any())).thenReturn(new ProjectManager());
        invokeAndCheckAcceptance();
    }

    @Test
    public void testInterceptorAcceptsOnProjectManagerCredentialsJSF() throws Exception {
        when(session.getEmployee()).thenReturn(new ProjectManager());
        invokeAndCheckAcceptance();
    }

    @Test
    public void testInterceptorDeniesOnManagementEmployeeCredentialsJWT() throws Exception {
        configureJWT(true);
        when(securityManager.findByJwt(any())).thenReturn(new ManagementEmployee());
        invokeAndCheckDenial();
    }

    @Test
    public void testInterceptorDeniesOnManagementEmployeeCredentialsJSF() throws Exception {
        when(session.getEmployee()).thenReturn(new ManagementEmployee());
        invokeAndCheckDenial();
    }

    @Test
    public void testInterceptorDeniesOnInvalidCredentialsJWT() throws Exception {
        configureJWT(false);
        when(securityManager.findByJwt(any())).thenReturn(null);
        invokeAndCheckDenial();
    }

    @Test
    public void testInterceptorDeniesOnEmployeeCredentialsJSF() throws Exception {
        when(session.getEmployee()).thenReturn(new Employee());
        invokeAndCheckDenial();
    }

    @Test
    public void testInterceptorDeniesOnEmployeeCredentialsJWT() throws Exception {
        configureJWT(true);
        when(securityManager.findByJwt(any())).thenReturn(new Employee());
        invokeAndCheckDenial();
    }
}