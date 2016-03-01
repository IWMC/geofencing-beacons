package com.realdolmen.rest;

import com.realdolmen.ArquillianUtil;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.validation.New;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class UserEndpointTest {

    @Spy private SecurityManager securityManager;
    @Mock private EntityManager entityManager;
    @Mock private Validator<Employee> validator;

    @InjectMocks
    private UserEndpoint endpoint = new UserEndpoint();
    private Employee employee;

    private TypedQuery<Employee> singleResultFoundQuery;

    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        final String password = "ThePass123";
        employee = new Employee(password, "1234567", "123456789", "email@email.com", "Test", "Test", "Test");
        singleResultFoundQuery = mockQuerySingleResult(employee);
        when(securityManager.randomSalt()).thenReturn("1234567");
        when(entityManager.createNamedQuery("Employee.findByUsername", Employee.class)).thenReturn(singleResultFoundQuery);
        when(validator.validate(eq(employee), anyObject())).thenReturn(new ValidationResult(true, new ArrayList<>()));
    }

    private <T> TypedQuery<T> mockQuerySingleResult(T result) {
        TypedQuery<T> query = mock(TypedQuery.class);
        when(query.getSingleResult()).thenReturn(result);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        return query;
    }

    private <T> TypedQuery<T> mockQueryNoResult() {
        TypedQuery<T> query = mock(TypedQuery.class);
        when(query.getSingleResult()).thenThrow(NoResultException.class);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        return query;
    }

    @Test
    public void testRegisterInvalidData() throws Exception {
        ValidationResult result = new ValidationResult(false, Arrays.asList(new String[] { "invalidation1", "invalidation2" }));
        when(validator.validate(employee, New.class)).thenReturn(result);
        Response response = endpoint.register(employee);
        verify(validator, atLeastOnce()).validate(employee, New.class);
        assertEquals("response returns 400 code", 400, response.getStatus());
        assertEquals("response returns validation violations", result, response.getEntity());
    }

    @Test
    public void testRegisterValidData() throws Exception {
        Response response = endpoint.register(employee);
        assertEquals("valid registration returns 201 code", 201, response.getStatus());
        verify(validator, atLeastOnce()).validate(employee, New.class);
    }

    @Test
    public void testLoginWithValidCredentialsReturnsJWT() throws Exception {
        final String validToken = "the.jwt.token";
        when(securityManager.generateToken(employee)).thenReturn(validToken);
        when(securityManager.checkPassword(employee, employee.getPassword())).thenReturn(true);
        Response response = endpoint.login(employee);
        assertEquals("response has 200 OK status", 200, response.getStatus());
        assertNotNull("response contains JWT_KEY", response.getEntity());
        assertEquals("response returns correct JWT_KEY", validToken, response.getEntity());
        verify(singleResultFoundQuery, atLeastOnce()).setParameter("username", employee.getUsername());
    }

    @Test
    public void testLoginWithInvalidCredentialsReturns400() throws Exception {
        TypedQuery<Employee> query = mockQueryNoResult();
        when(entityManager.createNamedQuery("Employee.findByUsername", Employee.class)).thenReturn(query);
        Response response = endpoint.login(employee);
        assertEquals("response has 400 status", 400, response.getStatus());
        assertNull("response does not contain JWT_KEY", response.getEntity());
    }
}