package com.realdolmen.rest;

import com.realdolmen.entity.Employee;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.EmployeeValidator;
import com.realdolmen.validation.Validator;
import com.sun.security.sasl.ClientFactoryImpl;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStageBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.mockito.*;
import org.mockito.internal.MockitoCore;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class UserEndpointTest {

    @Mock private EntityManager entityManager;

    @Inject
    private UserEndpoint endpoint;
    private Employee employee;

    private TypedQuery<Employee> singleResultFoundQuery;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        Arrays.stream(Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity().asFile()).forEach(archive::addAsLibrary);

        return archive.addAsResource("META-INF/persistence.xml")
                .addPackages(true, "com.realdolmen")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        entityManager = mock(EntityManager.class);
        endpoint.setEntityManager(entityManager);
        final String password = "ThePass123";
        employee = new Employee(password, "1234567", "123456789", "email@email.com", "Test", "Test", "Test");
        singleResultFoundQuery = mockQuerySingleResult(employee);
    }

    private <T> TypedQuery<T> mockQuerySingleResult(T result) {
        TypedQuery<T> query = mock(TypedQuery.class);
        when(query.getSingleResult()).thenReturn(result);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        return query;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRegister() throws Exception {

    }

    @Test
    public void testLoginWithValidCredentialsReturnsJWT(@ArquillianResource URL baseURL) throws Exception {
        final String validToken = "the.jwt.token";
        when(entityManager.createNamedQuery("Employee.findByUsername", Employee.class)).thenReturn(singleResultFoundQuery);
        Response response = endpoint.login(employee);
        assertEquals("response has 200 OK status", 200, response.getStatus());
        assertNotNull("response contains JWT", response.getEntity());
        assertEquals("response returns correct JWT", validToken, response.getEntity());
    }
}