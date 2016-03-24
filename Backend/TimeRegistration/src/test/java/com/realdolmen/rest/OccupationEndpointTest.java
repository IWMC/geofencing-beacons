package com.realdolmen.rest;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.*;
import com.realdolmen.jsf.Session;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.transaction.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class OccupationEndpointTest {

    @Mock
    private EntityManager em;

    @Mock
    private SecurityManager sm;

    @Inject
    private Session session;

    @Inject
    private UserTransaction userTransaction;

    @Mock
    private Validator<Occupation> occupationValidator;

    @Mock
    private UserTransaction utx;

    @InjectMocks
    private OccupationEndpoint endpoint;

    private RollbackException existingOccupationException;

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    private List<RegisteredOccupation> occupations;

    @Before
    public void setUp() throws Exception {
        endpoint = new OccupationEndpoint();
        MockitoAnnotations.initMocks(this);
        when(sm.isValidToken(any())).thenReturn(true);
        session.setEmployee(new ManagementEmployee());
        session.getEmployee().setId(1L);
        when(sm.findByJwt(any())).thenReturn(session.getEmployee());
        Occupation oc1 = new Occupation();
        oc1.setName("Lunch");
        Occupation oc2 = new Occupation();
        oc2.setName("Project X");
        RegisteredOccupation ro1 = new RegisteredOccupation();
        ro1.setOccupation(oc1);
        RegisteredOccupation ro2 = new RegisteredOccupation();
        ro2.setOccupation(oc2);

        occupations = new ArrayList<>(Arrays.asList(ro1, ro2));
        existingOccupationException = new RollbackException();
        existingOccupationException.initCause(new PersistenceException(new ConstraintViolationException("", null, "")));
    }

    @Test
    public void testGetOccupationsWithStartDateOnlyReturnsAllOccupationsOfThatDay() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<RegisteredOccupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(occupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class))
                .thenReturn(query);
        Response response = endpoint.getRegisteredOccupations(new Date().getTime());
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testGetOccupationsWithEndDateOnlyReturnsBadRequest() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        Response response = endpoint.getRegisteredOccupations(0);
        assertEquals("Response should be 400 because there is no start time: " + response.getEntity(), 400, response.getStatus());
    }

    @Test
    public void testListAllOccupationsReturnsAllOccupations() {
        List<Occupation> allOccupations = new ArrayList<>(Arrays.asList(new Occupation[]{
                new Occupation("Occupation", "Occupation description"), new Project("Project", "Project description", 5, new Date(), new Date())
        }));
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<Occupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(allOccupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("Occupation.findAll", Occupation.class))
                .thenReturn(query);
        Response response = endpoint.listAll(null, null);
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testListAllOccupationsPassesParameters() {
        List<Occupation> allOccupations = new ArrayList<>(Arrays.asList(new Occupation[]{
                new Occupation("Occupation", "Occupation description"), new Project("Project", "Project description", 5, new Date(), new Date())
        }));
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<Occupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(allOccupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("Occupation.findAll", Occupation.class))
                .thenReturn(query);
        Response response = endpoint.listAll(0, 2);
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        verify(query, atLeastOnce()).setFirstResult(0);
        verify(query, atLeastOnce()).setMaxResults(2);
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testGetOccupationsWithStartAndEndDateReturnsList() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<RegisteredOccupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(occupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class))
                .thenReturn(query);
        Response response = endpoint.getRegisteredOccupations(new Date().getTime());
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testGetOccupationsWithInvalidUserReturnsBadRequest() {
        session.getEmployee().setId(0L);
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        Response response = endpoint.getRegisteredOccupations(new Date().getTime());
        assertEquals("Response should be 400 because the user is invalid (but somehow passed authentication): " + response.getEntity(), 400, response.getStatus());
    }
    private Location location = new Location(10d, 11d);

    private JsonObjectBuilder point = Json.createObjectBuilder().add("long", String.valueOf(location.getLongitude()))
            .add("lat", String.valueOf(location.getLatitude()));

    @Test
    public void testAddLocationPointReturns404OnInvalidId() throws Exception {
        Response response = endpoint.addLocationPoint(null, point.build());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testAddLocationPointReturns400OnNoContent() throws Exception {
        Project existingProject = new Project("Project X", "Description", 10, new Date(), new Date());
        when(em.find(Project.class, existingProject.getId())).thenReturn(existingProject);
        Response response = endpoint.addLocationPoint(existingProject.getId(), null);
        assertEquals("response should have 400 status code", 400, response.getStatus());
    }

    @Test
    public void testAddLocationPointReturns201OnSuccession() throws Exception {
        Project existingProject = new Project("Project X", "Description", 10, new Date(), new Date());
        when(em.find(Project.class, existingProject.getId())).thenReturn(existingProject);
        Response response = endpoint.addLocationPoint(existingProject.getId(), point.build());
        assertEquals("response should have 201 status code", Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddLocationPointPersistsPoint() throws Exception {
        Project existingProject = new Project("Project X", "Description", 10, new Date(), new Date());
        when(em.find(Project.class, existingProject.getId())).thenReturn(existingProject);
        Response response = endpoint.addLocationPoint(existingProject.getId(), point.build());
        verify(em, atLeastOnce()).persist(Matchers.argThat(new BaseMatcher<Location>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof Location) {
                    Location loc = (Location) o;
                    return loc.getLatitude() == location.getLatitude() && loc.getLongitude() == location.getLongitude();
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        }));

        verify(em, atLeastOnce()).merge(existingProject);
    }

    @Test
    public void testAddLocationPointAddsLocationPoint() throws Exception {
        Project existingProject = new Project("Project X", "Description", 10, new Date(), new Date());
        when(em.find(Project.class, existingProject.getId())).thenReturn(existingProject);
        endpoint.addLocationPoint(existingProject.getId(), point.build());
        verify(em, times(1)).persist(location);
    }

    @Test
    public void testSaveOccupationReturns400OnNoRequestContent() throws Exception {
        Response response = endpoint.addOccupation(null);
        assertEquals("response should have 400 status code", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testSaveOccupationSavesOccupationOnNonExistingValidOccupation() throws Exception {
        Occupation occupation = new Occupation("Occupation name", "Occupation description");
        when(occupationValidator.validate(eq(occupation), anyVararg())).thenReturn(new ValidationResult(true));
        endpoint.addOccupation(occupation);
        verify(em, times(1)).persist(occupation);
    }

    @Test
    public void testSaveOccupationReturns201OnNonExistingValidOccupation() throws Exception {
        Occupation occupation = new Occupation("Occupation name", "Occupation description");
        when(occupationValidator.validate(eq(occupation), anyVararg())).thenReturn(new ValidationResult(true));
        Response response = endpoint.addOccupation(occupation);
        assertEquals("response should have 201 status code", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testSaveOccupationReturns400OnNonExistingInvalidOccupation() throws Exception {
        Occupation occupation = new Occupation("", "Occupation description");
        when(occupationValidator.validate(eq(occupation), anyVararg())).thenReturn(new ValidationResult(false));
        Response response = endpoint.addOccupation(occupation);
        assertEquals("response should have 400 status code", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(em, never()).persist(occupation);
    }

    @Test
    public void testSaveOccupationReturns400OnExistingInvalidOccupation() throws Exception {
        Occupation occupation = new Occupation("", "Occupation description");
        Supplier supplier = () -> {
            try {
                utx.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        when(supplier.get()).thenThrow(existingOccupationException);
        when(occupationValidator.validate(eq(occupation), anyVararg())).thenReturn(new ValidationResult(false));
        Response response = endpoint.addOccupation(occupation);
        assertEquals("response should have 400 status code", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(em, never()).persist(occupation);
    }

    @Test
    public void testSaveOccupationReturns400OnExistingValidOccupation() throws Exception {
        Occupation occupation = new Occupation("Occupation name", "Occupation description");
        Supplier supplier = () -> {
            try {
                utx.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        when(supplier.get()).thenThrow(existingOccupationException);
        when(occupationValidator.validate(eq(occupation), anyVararg())).thenReturn(new ValidationResult(true));
        Response response = endpoint.addOccupation(occupation);
        assertEquals("response should have 409 status code", Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }
}