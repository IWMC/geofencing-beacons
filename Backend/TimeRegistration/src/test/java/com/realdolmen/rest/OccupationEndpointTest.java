package com.realdolmen.rest;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.RegisteredOccupation;
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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by BCCAZ45 on 9/03/2016.
 */
@RunWith(Arquillian.class)
public class OccupationEndpointTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private OccupationEndpoint endpoint;

    @Mock
    private SecurityManager sm;

    @Inject
    private Session session;

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
    }

    @Test
    public void testGetOccupationsWithStartDateOnlyReturnsAllOccupationsOfThatDay() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<RegisteredOccupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(occupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class))
                .thenReturn(query);
        Response response = endpoint.getOccupations(new Date().getTime(), 0);
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testGetOccupationsWithEndDateOnlyReturnsBadRequest() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        Response response = endpoint.getOccupations(0, new Date().getTime());
        assertEquals("Response should be 400 because there is no start time: " + response.getEntity(), 400, response.getStatus());
    }

    @Test
    public void testGetOccupationsWithStartAndEndDateReturnsList() {
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        TypedQuery<RegisteredOccupation> query = when(mock(TypedQuery.class).getResultList()).thenReturn(occupations).getMock();
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class))
                .thenReturn(query);
        Response response = endpoint.getOccupations(new Date().getTime(), new Date().getTime());
        assertEquals("Should be 200 OK: " + response.getEntity(), 200, response.getStatus());
        List<RegisteredOccupation> returnedOccupations = (List<RegisteredOccupation>) response.getEntity();
        returnedOccupations.equals(occupations);
    }

    @Test
    public void testGetOccupationsWithInvalidUserReturnsBadRequest() {
        session.getEmployee().setId(0L);
        when(sm.findEmployee()).thenReturn(session.getEmployee());
        Response response = endpoint.getOccupations(new Date().getTime(), new Date().getTime());
        assertEquals("Response should be 400 because the user is invalid (but somehow passed authentication): " + response.getEntity(), 400, response.getStatus());
    }
}