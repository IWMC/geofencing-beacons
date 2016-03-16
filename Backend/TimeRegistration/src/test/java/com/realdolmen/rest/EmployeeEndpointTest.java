package com.realdolmen.rest;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.jsf.Session;
import com.realdolmen.validation.EmployeePasswordCredentialsValidator;
import com.realdolmen.validation.EmployeeValidator;
import com.realdolmen.validation.ValidationResult;
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
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class EmployeeEndpointTest {

    @Mock
    private EntityManager manager;

    @Mock
    private EmployeeValidator employeeValidator;

    @Mock
    private EmployeePasswordCredentialsValidator employeePasswordCredentialsValidator;

    @InjectMocks
    private EmployeeEndpoint endpoint = new EmployeeEndpoint();

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Inject
    private Session session;
    
    private Employee employee1 = new Employee();
    private Employee employee2 = new Employee();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        session.setEmployee(new ManagementEmployee());
        Employee employee = new Employee();
        employee1.setFirstName("first name");
        employee1.setLastName("last name");
        employee1.setUsername("username");
        employee1.setId(10l);
        employee1.setEmail("email@email.com");
        employee1.setMemberProjects(new HashSet<>());
        employee2.setUsername("another.user");
        employee2.setPassword("new.password");
        employee2.setFirstName("another first name");
        employee2.setLastName("another last name");
        employee2.setEmail("anotheremail@email.com");
        employee2.setId(employee1.getId());
    }

    private AtomicLong counter = new AtomicLong(1);

    private long createValidIdRequest() {
        long id = counter.getAndIncrement();
        createValidIdRequest(id, new Employee());
        return id;
    }
    
    private TypedQuery<Employee> createValidUsernameRequest(String username, Employee employee) {
        TypedQuery<Employee> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(employee);
        when(manager.createNamedQuery("Employee.findByUsername", Employee.class)).thenReturn(query);
        return query;
    }

    private TypedQuery<Employee> createValidEmailRequest(String email, Employee employee) {
        TypedQuery<Employee> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(employee);
        when(manager.createNamedQuery("Employee.findByEmail", Employee.class)).thenReturn(query);
        return query;
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
        createValidIdRequest(id, new Employee(employee1));
        employee1.setUsername("another.user");

        Response response = endpoint.update(id, employee1);
        assertEquals("response should return 201", 204, response.getStatus());
    }

    @Test
    public void testUpdateOnlyUpdatesAllFields() throws Exception {
        createValidIdRequest(employee1.getId(), new Employee(employee1));
        endpoint.update(employee1.getId(), employee2);
        verify(manager, times(1)).merge(employee2);
    }

    @Test
    public void testUpdateChecksInputValidation() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        ValidationResult validationResult = new ValidationResult(false, new ArrayList<>());
        when(employeeValidator.validate(employee2, Existing.class)).thenReturn(validationResult);
        Response response = endpoint.update(employee1.getId(), employee2);
        verify(employeeValidator, atLeastOnce()).validate(eq(employee2), anyVararg());
        assertEquals("response should contain validation result", validationResult, response.getEntity());
    }

    @Test
    public void testUpdateChecksOnExistingUsername() throws Exception {
        employee2.setId(employee1.getId() + 1);
        when(manager.find(Employee.class, employee2.getId())).thenReturn(employee2);
        when(manager.merge(employee2)).thenThrow(NonUniqueResultException.class);
        TypedQuery<Employee> query = createValidUsernameRequest(employee1.getUsername(), employee1);
        employee2.setUsername(employee1.getUsername());
        Response response = endpoint.update(employee2.getId(), employee2);
        assertEquals("response should have status code 409", 409, response.getStatus());
    }

    @Test
    public void testUpdateChecksOnExistingEmail() throws Exception {
        employee2.setId(employee1.getId() + 1);
        when(manager.find(Employee.class, employee2.getId())).thenReturn(employee2);
        when(manager.merge(employee2)).thenThrow(NonUniqueResultException.class);
        TypedQuery<Employee> query = createValidEmailRequest(employee1.getEmail(), employee1);
        employee2.setEmail(employee1.getEmail());
        Response response = endpoint.update(employee2.getId(), employee2);
        assertEquals("response should have status code 409", 409, response.getStatus());
    }

    @Test
    public void testUpdateMergesNewEntity() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        endpoint.update(employee1.getId(), employee1);
        verify(manager, times(1)).merge(employee1);
    }

    @Test
    public void testUpgradeToProjectManagerOnInvalidIdReturns404() throws Exception {
        Response response = endpoint.upgradeProjectManager(employee1.getId());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testUpgradeToProjectManagerReturns204() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        Response response = endpoint.upgradeProjectManager(employee1.getId());
        assertEquals("response should have 204 status code", 204, response.getStatus());
    }

    @Test
    public void testUpgradeToProjectManagerUpgradesProjectManager() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        endpoint.upgradeProjectManager(employee1.getId());
        verify(manager, atLeastOnce()).remove(employee1);
        verify(manager, atLeastOnce()).persist(new ProjectManager(employee1));
    }

    @Test
    public void testUpgradeToManagementEmployeeOnInvalidIdReturns404() throws Exception {
        Response response = endpoint.upgradeManagementEmployee(employee1.getId());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testUpgradeToManagementEmployeeReturns204() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        Response response = endpoint.upgradeManagementEmployee(employee1.getId());
        assertEquals("response should have 204 status code", 204, response.getStatus());
    }

    @Test
    public void testUpgradeToManagementEmployeeUpgradesManagementEmployee() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        endpoint.upgradeProjectManager(employee1.getId());
        verify(manager, atLeastOnce()).remove(employee1);
        verify(manager, atLeastOnce()).persist(new ManagementEmployee(employee1));
    }

    @Test
    public void testDowngradeToEmployeeOnInvalidIdReturns404() throws Exception {
        Response response = endpoint.downgradeEmployee(employee1.getId());
        assertEquals("response should have 404 status code", 404, response.getStatus());
    }

    @Test
    public void testDowngradeToEmployeeReturns204() throws Exception {
        when(manager.find(Employee.class, employee1.getId())).thenReturn(employee1);
        Response response = endpoint.downgradeEmployee(employee1.getId());
        assertEquals("response should have 204 status code", 204, response.getStatus());
    }

    @Test
    public void testDowngradeToEmployeeUpgradesManagementEmployee() throws Exception {
        ProjectManager projectManager = new ProjectManager(employee1);
        when(manager.find(Employee.class, projectManager.getId())).thenReturn(projectManager);
        endpoint.downgradeEmployee(projectManager.getId());
        verify(manager, atLeastOnce()).remove(projectManager);
        verify(manager, atLeastOnce()).persist(employee1);
    }
}
