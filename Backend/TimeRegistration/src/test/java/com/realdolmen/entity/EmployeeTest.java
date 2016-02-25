package com.realdolmen.entity;

import com.realdolmen.ArquillianUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Created by BCCAZ45 on 25/02/2016.
 */
@RunWith(Arquillian.class)
public class EmployeeTest {

    @PersistenceContext(unitName = "TimeRegistration-test-persistence-unit")
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private Employee employee;

    private Project simpleProject;

    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        employee = new Employee("Password123", "zout", "", "test@realdolmen.com", "test", "user", "test");
        simpleProject = new Project();
        simpleProject.setProjectNr(9);
        simpleProject.setName("Project 1");
        simpleProject.setStartDate(new Date());
        simpleProject.setDescription("Project 1 description");
        transaction(em::persist, employee);
        transaction(em::persist, simpleProject);
    }

    @Test
    public void testAddNewEmployeeIsPersistedAndConsistent() throws Exception {
        Employee dbEmployee = em.find(Employee.class, employee.getId());
        assertTrue(employee.equals(dbEmployee));
        assertNotNull(dbEmployee);
    }

    @Test
    public void testUpdatedEmployeeIsConsistent() throws Exception {
        transaction(() -> {
            employee.setFirstName("Changed");
            employee.setEmail("testchanged@realdolmen.com");
            employee.getMemberProjects().add(simpleProject);
            simpleProject.getEmployees().add(employee);
            em.merge(employee);
        });

        transaction(() -> {
            Employee dbEmployee = em.find(Employee.class, employee.getId());
            assertNotNull(dbEmployee);
            assertEquals("Employee's name is changed", "Changed", employee.getFirstName());
            assertEquals("Employee's email is changed", "testchanged@realdolmen.com", employee.getEmail());
            assertEquals(dbEmployee, employee);
            assertTrue("Employee should have a project", dbEmployee.getMemberProjects().contains(simpleProject));
            assertTrue("Project should have a employee", simpleProject.getEmployees().contains(dbEmployee));
        });
    }

    @Test
    public void testDeleteEmployeeDoesNotDeleteProject() throws Exception {
        transaction(() -> {
            employee = em.merge(employee);
            em.remove(employee);
        });

        transaction(() -> assertNotNull("Project should still exist", em.find(Project.class, simpleProject.getId())));
    }

    @Test
    public void testDeleteEmployeeRemovesEmployeeFromMemberProjects() throws Exception {
        transaction(() -> {
            employee.getMemberProjects().add(simpleProject);
            simpleProject.getEmployees().add(employee);
        });

        transaction(() -> {
            employee = em.merge(employee);
            Set<Project> projects = em.find(Employee.class, employee.getId()).getMemberProjects();
            em.remove(employee);
            for(Project p : projects) {
                Project dbProject = em.find(Project.class, p.getId());
                assertFalse("A user should be removed from project members when deleted", dbProject.getEmployees().contains(employee));
            }
        });

    }

    private void transaction(Runnable r) throws Exception {
        utx.begin();
        em.joinTransaction();
        r.run();
        utx.commit();
    }

    private <E> void transaction(Consumer<E> consumer, E data) throws Exception {
        transaction(() -> consumer.accept(data));
    }
}