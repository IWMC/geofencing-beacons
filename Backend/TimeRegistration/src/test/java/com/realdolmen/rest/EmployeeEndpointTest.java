package com.realdolmen.rest;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.jsf.Session;
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
import javax.persistence.*;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class EmployeeEndpointTest {

    @Mock
    private EntityManager manager;

    @InjectMocks
    private EmployeeEndpoint endpoint = new EmployeeEndpoint();

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Inject
    private Session session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        session.setEmployee(new ManagementEmployee());
    }

    private AtomicLong counter = new AtomicLong(1);

    private long createValidIdRequest() {
        long id = counter.getAndIncrement();
        createValidIdRequest(id, new Employee());
        return id;
    }

    private void createValidIdRequest(long id, Employee employee) {
        when(manager.find(Employee.class, id)).thenReturn(employee);
    }

    private long createInvalidIdRequest() {
        long id = counter.getAndIncrement();
        createInvalidIdRequest(id);
        return id;
    }

    private void createInvalidIdRequest(long id) {
        when(manager.find(Employee.class, id)).thenReturn(null);
    }

    @Test
    public void testFindByInvalidIdReturns404() throws Exception {
        Response response = endpoint.findById(createInvalidIdRequest());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testFindByValidIdReturnsEmployeeEntity() throws Exception {
        Employee employee = new Employee();
        employee.setUsername("testFindByValidIdReturnsEmployeeEntity");
        long id = counter.getAndIncrement();
        employee.setId(id);
        createValidIdRequest(id, employee);
        Response response = endpoint.findById(id);
        assertTrue("response returns an employee", response.getEntity() instanceof Employee);
        assertEquals("response should return entity", employee, response.getEntity());
    }

    @Test
    public void testRemoveByInvalidIdReturns404() throws Exception {
        Response response = endpoint.deleteById(createInvalidIdRequest());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testRemoveByValidIdReturns204() throws Exception {
        Employee employee = new Employee();
        employee.setUsername("testFindByValidIdReturnsEmployeeEntity");
        long id = counter.getAndIncrement();
        employee.setId(id);
        createValidIdRequest(id, employee);
        Response response = endpoint.deleteById(id);
        assertEquals("response should return 204", 204, response.getStatus());
    }

    @Test
    public void testFindAllListsAllEntities() throws Exception {
        List<Employee> employees = new ArrayList<>();
        IntStream.range(0, 10).mapToObj(i -> {
            Employee employee = new Employee();
            employee.setId((long) i);
            employee.setUsername("Employee " + i);
            return employee;
        }).forEach(employees::add);
        TypedQuery<Employee> query = when(mock(TypedQuery.class).getResultList()).thenReturn(employees).getMock();
        when(manager.createNamedQuery("Employee.findAll", Employee.class))
                .thenReturn(query);
        Response response = endpoint.listAll(0, 0);
        assertTrue("response should return a list", response.getEntity() instanceof List);
        List<Employee> responseList = (List<Employee>) response.getEntity();
        assertEquals("response should return all employees", employees.size(), responseList.size());
        employees.forEach(e -> {
            assertTrue("response should containg employee", responseList.contains(e));
        });
    }

    @Test
    public void testFindAllWithStartIndexPaginationListsPartialEntities() throws Exception {
        List<Employee> employees = new ArrayList<>();
        IntStream.range(0, 10).mapToObj(i -> {
            Employee employee = new Employee();
            employee.setId((long) i);
            employee.setUsername("Employee " + i);
            return employee;
        }).forEach(employees::add);
        TypedQuery<Employee> query = when(mock(TypedQuery.class).getResultList()).thenReturn(employees).getMock();
        when(manager.createNamedQuery("Employee.findAll", Employee.class))
                .thenReturn(query);
        Response response = endpoint.listAll(10, 0);
        assertTrue("response should return a list", response.getEntity() instanceof List);
        List<Employee> responseList = (List<Employee>) response.getEntity();
        assertEquals("response should return all employees", employees.size(), responseList.size());
        employees.forEach(e -> {
            assertTrue("response should containg employee", responseList.contains(e));
        });

        verify(query, atLeastOnce()).setFirstResult(10);
    }

    @Test
    public void testFindAllWithMaxResultsPaginationListsPartialEntities() throws Exception {
        List<Employee> employees = new ArrayList<>();
        IntStream.range(0, 10).mapToObj(i -> {
            Employee employee = new Employee();
            employee.setId((long) i);
            employee.setUsername("Employee " + i);
            return employee;
        }).forEach(employees::add);
        TypedQuery<Employee> query = when(mock(TypedQuery.class).getResultList()).thenReturn(employees).getMock();
        when(manager.createNamedQuery("Employee.findAll", Employee.class))
                .thenReturn(query);
        Response response = endpoint.listAll(0, 10);
        assertTrue("response should return a list", response.getEntity() instanceof List);
        List<Employee> responseList = (List<Employee>) response.getEntity();
        assertEquals("response should return all employees", employees.size(), responseList.size());
        employees.forEach(e -> {
            assertTrue("response should containg employee", responseList.contains(e));
        });

        verify(query, atLeastOnce()).setMaxResults(10);
    }

    @Test
    public void testFindAllWithFullPaginationListsPartialEntities() throws Exception {
        List<Employee> employees = new ArrayList<>();
        IntStream.range(0, 10).mapToObj(i -> {
            Employee employee = new Employee();
            employee.setId((long) i);
            employee.setUsername("Employee " + i);
            return employee;
        }).forEach(employees::add);
        TypedQuery<Employee> query = when(mock(TypedQuery.class).getResultList()).thenReturn(employees).getMock();
        when(manager.createNamedQuery("Employee.findAll", Employee.class))
                .thenReturn(query);
        Response response = endpoint.listAll(10, 11);
        assertTrue("response should return a list", response.getEntity() instanceof List);
        List<Employee> responseList = (List<Employee>) response.getEntity();
        assertEquals("response should return all employees", employees.size(), responseList.size());
        employees.forEach(e -> {
            assertTrue("response should containg employee", responseList.contains(e));
        });

        verify(query, atLeastOnce()).setFirstResult(10);
        verify(query, atLeastOnce()).setMaxResults(11);
    }

    @Test
    public void testUpdateByInvalidIdReturns404() throws Exception {
        long id = 52;
        createInvalidIdRequest(id);
        Employee employee = new Employee();
        employee.setId(id);
        Response response = endpoint.update(id, employee);
        assertEquals("response should return 404 status code", 404, response.getStatus());
    }

    @Test
    public void testUpdateWithInvalidConfirmationIdReturns409() throws Exception {
        Employee employee = new Employee();
        employee.setId(2l);
        createValidIdRequest(1, new Employee());
        Response response = endpoint.update(1l, employee);
        assertEquals("response should return 409 status code", 409, response.getStatus());
    }

    @Test
    public void testUpdateWithInvalidBodyReturns400() throws Exception {
        Response response = endpoint.update(createValidIdRequest(), null);
        assertEquals("response should return 400 status code", 400, response.getStatus());
    }

    @Test
    public void testUpdateReturns204() throws Exception {
        long id = 10;
        Employee employee = new Employee();
        employee.setFirstName("first name");
        employee.setLastName("last name");
        employee.setUsername("username");
        employee.setId(id);
        employee.setEmail("email@email.com");
        employee.setMemberProjects(new HashSet<>());
        createValidIdRequest(id, new Employee(employee));
        employee.setUsername("another.user");

        Response response = endpoint.update(id, employee);
        assertEquals("response should return 201", 204, response.getStatus());
    }

    @Test
    public void testUpdateOnlyUpdatesNewFields() throws Exception {
        long id = 10;
        Employee employee = new Employee();
        employee.setFirstName("first name");
        employee.setLastName("last name");
        employee.setUsername("username");
        employee.setId(id);
        employee.setEmail("email@email.com");
        employee.setMemberProjects(new HashSet<>());
        createValidIdRequest(id, new Employee(employee));

        Response response = endpoint.update(id, new Employee() {
            { setUsername("another.user"); }
        });

        // TODO: Finish
    }

    @Test
    public void testUpdateChecksInputValidation() throws Exception {

    }

    @Test
    public void testUpdateChecksOnExistingUsername() throws Exception {

    }

    @Test
    public void testUpdateChecksOnExistingEmail() throws Exception {

    }

    @Test
    public void testUpdateMergesNewEntity() throws Exception {

    }

    @Test
    public void testUpgradeToProjectManagerOnInvalidIdReturns404() throws Exception {

    }

    @Test
    public void testUpgradeToProjectManagerReturns204() throws Exception {

    }

    @Test
    public void testUpgradeToProjectManagerUpgradesProjectManager() throws Exception {

    }

    @Test
    public void testUpgradeToManagementEmployeeOnInvalidIdReturns404() throws Exception {

    }

    @Test
    public void testUpgradeToManagementEmployeeReturns204() throws Exception {

    }

    @Test
    public void testUpgradeToManagementEmployeeUpgradesManagementEmployee() throws Exception {

    }
}
