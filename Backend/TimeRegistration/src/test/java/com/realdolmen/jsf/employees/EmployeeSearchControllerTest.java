package com.realdolmen.jsf.employees;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.rest.EmployeeEndpoint;
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
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class EmployeeSearchControllerTest {

    @InjectMocks
    private EmployeeSearchController controller = new EmployeeSearchController();

    @Inject
    private EmployeeSearchController realController;

    @Inject
    private UserContext userContext;

    @Inject
    private UserTransaction transaction;

    @Mock
    private EmployeeEndpoint endpoint;

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
    public void testGetEmployeesWithSearchTermsUsesEndpointWhenNoSearchTerms() throws Exception {
        Response responseMock = mock(Response.class);
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(){{
            setId(10l);
        }});

        when(responseMock.getEntity()).thenReturn(employeeList);
        when(endpoint.listAll(any(), any())).thenReturn(responseMock);
        List<Employee> result = controller.getEmployeesWithSearchTerms();
        assertArrayEquals("response should contain list of all employees", employeeList.toArray(), result.toArray());
        verify(responseMock, atLeastOnce()).getEntity();
    }

    @Test
    public void testGetEmployeesWithSearchTermsFiltersOnSearchTerms() throws Exception {
        transaction.begin();
        Employee employee1 = new Employee();
        employee1.setFirstName("First");
        employee1.setLastName("User");
        employee1.setUsername("somename");
        employee1.setEmail("email@email.com");

        ManagementEmployee employee2 = new ManagementEmployee();
        employee2.setFirstName("Last");
        employee2.setLastName("User");
        employee2.setUsername("someothername");
        employee2.setEmail("someotheremail@email.com");

        em.persist(employee1);
        em.persist(employee2);
        transaction.commit();

        realController.setSearchTerms("email@email.com");
        List<Employee> result = realController.getEmployeesWithSearchTerms();
        assertEquals("response should return 1 employee when filtering by a full email", 1, result.size());
        assertEquals(employee1, result.get(0));

        realController.setSearchTerms("somename");
        result = realController.getEmployeesWithSearchTerms();
        assertEquals("response should return 1 employee when filtering by username", 1, result.size());
        assertEquals(employee1, result.get(0));

        realController.setSearchTerms("Last");
        result = realController.getEmployeesWithSearchTerms();
        assertEquals("response should return 1 employee when filtering by first name", 1, result.size());
        assertEquals(employee2, result.get(0));

        realController.setSearchTerms("User");
        result = realController.getEmployeesWithSearchTerms();
        assertEquals("response should return 2 employees when filtering by common last name", 2, result.size());
        assertTrue("response should contain user", result.contains(employee1));
        assertTrue("response should contain user", result.contains(employee2));
    }

    @Test
    public void testGetEmployeesReturnsAllEmployeesUsingEndpoint() throws Exception {
        Response responseMock = mock(Response.class);
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(){{
            setId(10l);
        }});

        when(responseMock.getEntity()).thenReturn(employeeList);
        when(endpoint.listAll(any(), any())).thenReturn(responseMock);
        List<Employee> result = controller.getEmployees();
        assertArrayEquals("response should contain list of all employees", employeeList.toArray(), result.toArray());
        verify(responseMock, atLeastOnce()).getEntity();
    }
}