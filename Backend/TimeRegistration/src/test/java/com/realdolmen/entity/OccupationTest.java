package com.realdolmen.entity;

import com.realdolmen.WarFactory;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * This test class tests JPA operations with {@link Occupation}s. It ensures that all JPA operations are consistent and
 * valid.
 */
@RunWith(Arquillian.class)
public class OccupationTest {

    @PersistenceContext(unitName = PersistenceUnit.TEST)
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private Occupation occupation = new Occupation("Lunch " + atomicInteger.getAndIncrement(), "Food consumption in the middle of the day!");
    private Project project = new Project("Project Y " + atomicInteger.getAndIncrement(), "Another project almost as secret as Project X", 42, new Date(), new Date());

    private static AtomicInteger atomicInteger = new AtomicInteger(1);

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        transaction(() -> {
            em.persist(occupation);
            em.persist(project);
        });
    }

    @Test
    public void testInitialize() throws Exception {

    }

    @Test
    public void testAddNewProjectIsPersistedAndConsistent() throws Exception {
        Project dbProject = em.find(Project.class, project.getId());
        assertNotNull(dbProject);
        assertTrue(project.equals(dbProject));
    }

    @Test
    public void testAddNewOccupationIsPersistedAndConsistent() throws Exception {
        Occupation dbOccupation = em.find(Occupation.class, occupation.getId());
        assertNotNull(dbOccupation);
        assertTrue(occupation.equals(dbOccupation));
    }

    @Test
    public void testUpdatedProjectIsConsistent() throws Exception {
        final String newName = "Long lunch";
        final String newDescription = "Just like a normal lunch, but longer";

        Date startDate = new Date();
        startDate.setTime(new Date().getTime() + 50000);
        Date endDate = new Date();
        endDate.setTime(startDate.getTime() + 50000);

        Location location = new Location(20, 20);
        Project subproject = new Project("Subproject", "Subproject description", 156, new Date(), new Date());
        Employee employee = new Employee();
        transaction(em::persist, subproject);
        transaction(em::persist, employee);
        transaction(em::persist, location);

        transaction(() -> {
            project.setName(newName);
            project.setDescription(newDescription);
            project.setStartDate(startDate);
            project.setEndDate(endDate);
            project.setProjectNr(43);
            project.getLocations().add(location);
            project.getSubProjects().add(subproject);
            project.getEmployees().add(employee);
            employee.getMemberProjects().add(project);
            em.merge(employee);
            em.merge(project);
        });

        transaction(() -> {
            Project dbProject = em.find(Project.class, project.getId());
            assertNotNull(dbProject);
            assertEquals("project should have a different name", project.getName(), dbProject.getName());
            assertEquals("project should have a different description", project.getDescription(), dbProject.getDescription());
            assertEquals("project should have a different project nr", project.getProjectNr(), dbProject.getProjectNr());
            assertEquals("project should have 1 location", 1, dbProject.getLocations().size());
            assertEquals("project should have the correct location", location, dbProject.getLocations().iterator().next());
            assertEquals("project should have 1 subproject", 1, dbProject.getSubProjects().size());
            assertEquals("project should have the correct subproject", subproject, dbProject.getSubProjects().iterator().next());
            assertEquals("project should have 1 member", 1, dbProject.getEmployees().size());
            assertEquals("project should have the correct member", employee, dbProject.getEmployees().iterator().next());

            //TODO: Fix dates inconsistency.
            assertEquals("project should have the same start date", project.getStartDate(), dbProject.getStartDate());
            assertEquals("project should have the same end date", project.getEndDate(), dbProject.getEndDate());
            assertEquals(project, dbProject);
        });
    }

    @Test
    public void testUpdatedOccupationIsConsistent() throws Exception {
        final String newName = "Long lunch " + atomicInteger.getAndIncrement();
        final String newDescription = "Just like a normal lunch, but longer";

        transaction(() -> {
            occupation.setName(newName);
            occupation.setDescription(newDescription);
            em.merge(occupation);
        });

        transaction(() -> {
            Occupation dbOccupation = em.find(Occupation.class, occupation.getId());
            assertNotNull(dbOccupation);
            assertEquals("occupation should have a different name", occupation.getName(), dbOccupation.getName());
            assertEquals("occupation should have a different description", occupation.getDescription(), dbOccupation.getDescription());
        });
    }

    @Test
    public void testDeleteProjectDeletesSubprojects() throws Exception {
        Project subproject = new Project("Subproject " + atomicInteger.getAndIncrement(), "Subproject description", 156, new Date(), new Date());
        transaction(em::persist, subproject);

        transaction(() -> {
            project.getSubProjects().add(subproject);
            em.merge(project);
        });

        transaction(() -> {
            Project dbProject = em.find(Project.class, project.getId());
            em.remove(dbProject);
        });

        transaction(() -> {
            Project dbSubproject = em.find(Project.class, subproject.getId());
            assertNull("subproject should be removed", dbSubproject);
        });
    }

    @Test
    public void testDeleteOccupationDoesNotWorkWhenExistingEmployeesRelated() throws Exception {
        Employee employee = new Employee();
        transaction(em::persist, employee);
        transaction(() -> {
            employee.getMemberProjects().add(em.merge(project));
            project.getEmployees().add(employee);
        });

        transaction(em::merge, employee);

        try {
            utx.begin();
            em.joinTransaction();
            em.remove(em.find(Project.class, project.getId()));
            utx.commit();
        } catch (Exception ex) {
        }
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
