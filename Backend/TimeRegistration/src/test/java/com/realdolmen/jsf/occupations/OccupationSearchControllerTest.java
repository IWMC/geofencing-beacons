package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.rest.OccupationEndpoint;
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
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class OccupationSearchControllerTest {

    @InjectMocks
    private OccupationSearchController controller = new OccupationSearchController();

    @Inject
    private OccupationSearchController realController;

    @Inject
    private UserContext userContext;

    @Inject
    private UserTransaction transaction;

    @Mock
    private OccupationEndpoint endpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void init() {
        userContext.setEmployee(new ManagementEmployee());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Transactional
    public void testGetOccupationsWithSearchTermsUsesEndpointWhenNoSearchTerms() throws Exception {
        Response responseMock = mock(Response.class);
        List<Occupation> occupationList = new ArrayList<>();
        occupationList.add(new Occupation(){{
            setId(10l);
        }});

        when(responseMock.getEntity()).thenReturn(occupationList);
        when(endpoint.listAll(any(), any())).thenReturn(responseMock);
        List<Occupation> result = controller.getOccupationsWithSearchTerms();
        assertArrayEquals("response should contain list of all occupations", occupationList.toArray(), result.toArray());
        verify(responseMock, atLeastOnce()).getEntity();
    }

    @Test
    public void testGetOccupationsWithSearchTermsFiltersOnSearchTerms() throws Exception {
        transaction.begin();
        em.createQuery("DELETE FROM Beacon").executeUpdate();
        em.createQuery("DELETE FROM RegisteredOccupation").executeUpdate();
        transaction.commit();
        transaction.begin();
        em.createQuery("DELETE FROM Occupation").executeUpdate();
        transaction.commit();

        transaction.begin();
        Occupation occupation = new Occupation("Occupation", "Description for lunch");
        Project project = new Project("Project X", "Some description", 10, new Date(), new Date());

        em.persist(occupation);
        em.persist(project);
        transaction.commit();

        realController.setSearchTerms("X");
        List<Occupation> result = realController.getOccupationsWithSearchTerms();
        assertEquals("response should return 1 occupation when filtering by a name", 1, result.size());
        assertEquals(project, result.get(0));

        realController.setSearchTerms("lunch");
        result = realController.getOccupationsWithSearchTerms();
        assertEquals("response should return 1 occupation when filtering by a description", 1, result.size());
        assertEquals(occupation, result.get(0));

        realController.setSearchTerms("description");
        result = realController.getOccupationsWithSearchTerms();
        assertEquals("response should return 2 occupations when filtering by a description", 2, result.size());
        assertTrue("response should contain occupation", result.contains(occupation));
        assertTrue("response should contain project", result.contains(project));
    }

    @Test
    public void testGetOccupationsReturnsAllOccupationsUsingEndpoint() throws Exception {
        Response responseMock = mock(Response.class);
        List<Occupation> occupationList = new ArrayList<>();
        occupationList.add(new Occupation(){{
            setId(10l);
        }});

        when(responseMock.getEntity()).thenReturn(occupationList);
        when(endpoint.listAll(any(), any())).thenReturn(responseMock);
        List<Occupation> result = controller.getOccupations();
        assertArrayEquals("response should contain list of all occupations", occupationList.toArray(), result.toArray());
        verify(responseMock, atLeastOnce()).getEntity();
    }

}