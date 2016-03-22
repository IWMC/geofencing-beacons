package com.realdolmen.jsf;

import com.realdolmen.WarFactory;
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
import org.mockito.MockitoAnnotations;

import javax.faces.context.FacesContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class LoginControllerTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Mock
    private UserEndpoint endpoint;

    @Mock
    private Session session;

    @InjectMocks
    private LoginController loginController = new LoginController();

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
        loginController.setUsername(employee.getUsername());
        loginController.setPassword(employee.getPassword());
        String redirect = loginController.doLogin();
        verify(endpoint, times(1)).loginLocal(any());
        verify(session, times(1)).setEmployee(any());
        assertEquals("Correct login should redirect to index page", "/index.xhtml?faces-redirect=true", redirect);
    }

    @Test
    public void testDoLoginWithFalseCredentialsDoesNotRedirect() throws Exception {
        when(endpoint.loginLocal(any(Employee.class))).thenReturn(Response.serverError().build());
        loginController.setUsername(employee.getUsername());
        loginController.setPassword(employee.getPassword() + "bad");
        String redirect = loginController.doLogin();
        verify(endpoint, times(1)).loginLocal(any());
        verify(session, never()).setEmployee(any());
        assertEquals("False login should not redirect to anything", "", redirect);
    }
}