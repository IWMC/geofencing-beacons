package com.realdolmen.jsf;

import com.realdolmen.ArquillianUtil;
import com.realdolmen.entity.Employee;
import com.realdolmen.rest.UserEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.faces.context.FacesContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by BCCAZ45 on 2/03/2016.
 */
@RunWith(Arquillian.class)
public class LoginTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }

    @Mock
    private UserEndpoint endpoint;

    @Mock
    private Session session;

    @InjectMocks
    private Login login = new Login();

    private Employee employee;

    @Mock
    private FacesContext context;

    @Before
    public void setUp() throws Exception {
        employee = new Employee("randomPassword", "asalt", "", "email", "username", "lastName", "firstName");
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoLoginWithCorrectCredentialsRedirectsToIndex() throws Exception {
        when(endpoint.loginLocal(any(Employee.class))).thenReturn(Response.ok().entity(employee).build());
        login.setUsername(employee.getUsername());
        login.setPassword(employee.getPassword());
        String redirect = login.doLogin();
        verify(endpoint, times(1)).loginLocal(any());
        verify(session, times(1)).setEmployee(any());
        assertEquals("Correct login should redirect to index page", "index.xhtml?faces-redirect=true", redirect);
    }

    @Test
    public void testDoLoginWithFalseCredentialsDoesNotRedirect() throws Exception {
        when(endpoint.loginLocal(any(Employee.class))).thenReturn(Response.serverError().build());
        login.setUsername(employee.getUsername());
        login.setPassword(employee.getPassword() + "bad");
        String redirect = login.doLogin();
        verify(endpoint, times(1)).loginLocal(any());
        verify(session, never()).setEmployee(any());
        assertEquals("False login should not redirect to anything", "", redirect);
    }
}