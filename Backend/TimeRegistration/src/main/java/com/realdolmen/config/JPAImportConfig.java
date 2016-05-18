package com.realdolmen.config;

import com.realdolmen.entity.*;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.service.SecurityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Configuration adding, editing or removing entities that should be present in the database at startup. Should be
 * invoked by {@link StartupConfig} to allow other configurations to work based on previously created results.
 */
@Singleton
public class JPAImportConfig {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager entityManager;

    @Inject
    private TaskDao taskDao;

    @Inject
    private SecurityManager securityManager;

    @Transactional
    public void startup() {
        ManagementEmployee employee = new ManagementEmployee();
        employee.setUsername("admin");
        employee.setEmail("admin@realdolmen.com");
        employee.setFirstName("admin");
        employee.setLastName("admin");
        employee.setJobFunction("3");

        String salt = null;
        try {
            salt = securityManager.randomSalt();
            employee.setSalt(salt);
            employee.setHash(securityManager.generateHash(salt, "Bla123"));
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(JPAImportConfig.class).error("Could not add admin account", e);
        }

        entityManager.persist(employee);
        Logger.getLogger(JPAImportConfig.class).info("Succesfully added admin account: 'admin'");

        Occupation o = new Occupation();
        o.setName("Lunch");
        o.setDescription("Lunch time");
        o.setEstimatedHours(0.5);

        Project project = new Project();
        project.setProjectNr(8);
        project.setStartDate(new Date());

        project.setName("Project X");
        project.setDescription("The super secret project no one can know about");
        project.setEndDate(DateUtils.addMonths(new Date(), 3));

        Project otherProject = new Project();
        otherProject.setProjectNr(100);
        otherProject.setStartDate(new Date());
        otherProject.setName("Project 100");
        otherProject.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam aliquam nisl at erat malesuada sodales. Sed semper metus vitae efficitur finibus. Morbi justo metus, sagittis eget ultricies et, accumsan quis lorem. Aliquam ut lacinia ante, ut semper quam.Donec a odio felis. Vivamus tempus mi arcu, a volutpat mauris feugiat ac. Etiam diam enim, malesuada id tortor ac, tincidunt pharetra turpis. Cras eu augue sit amet arcu porta feugiat. Phasellus lobortis ultricies ultricies. Mauris eleifend urna sed ipsum dapibus, eget feugiat nisi semper. Morbi fringilla viverra massa sit amet dapibus. Integer faucibus odio vel justo interdum maximus. Sed ut imperdiet risus, egestas bibendum odio. Vestibulum dapibus nec est at imperdiet.");
        otherProject.setEndDate(DateUtils.addMonths(new Date(), 3));
        for (int i = 0; i < 20; i++) {
            Occupation oo = new Occupation();
            oo.setName("Occupation " + i);
            if (i % 4 == 0) {
                oo.setDescription("Description for #" + i);
            }
            entityManager.persist(oo);
        }

        Beacon b = new Beacon();
        b.getOccupations().add(project);
        b.setMode(new BeaconMode(true, 1));

        entityManager.persist(o);
        entityManager.persist(project);
        entityManager.persist(b);
        entityManager.persist(otherProject);

        createAndPersistOccupations();

        try {
            Employee employee1 = entityManager.createNamedQuery("Employee.findByUsername", Employee.class).setParameter("username", "brentc").getSingleResult();
            employee1.getMemberProjects().add(project);
            entityManager.merge(employee1);
            Project p = entityManager.merge(project);

            Task javadocTask = new Task("Javadoc", "Omschrijving van javadoc", 8.5, p);
            project.getTasks().add(javadocTask);
            javadocTask.getEmployeeIds().add(2L);
            taskDao.addTask(javadocTask);
        } catch (NoResultException nrex) {
            Logger.getLogger(JPAImportConfig.class).info("Couldn't find user 'brentc', skipping project assignment");
        }
    }

    private void createAndPersistOccupations() {
        final Random random = new Random();
        for (int i = 0; i < 3; i++) {
            Project project = new Project();
            project.setProjectNr(i + 9);
            DateTime date = DateTime.now().withYear(random.nextInt(3) + 2014).withDayOfYear(1 + random.nextInt(365));
            project.setStartDate(date.toDate());
            project.setName("Project " + (i + 300));
            project.setDescription("The project details from project #" + i);
            project.setEndDate(date.plusMonths(random.nextInt(20)).toDate());
            entityManager.persist(project);
            final int taskCount = random.nextInt(4);
            final List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class).getResultList();

            for (int j = 0; j < taskCount; j++) {
                final String name = "Opdracht " + project.getProjectNr() + " - " + (j + 1);
                Task task = new Task(name, "Omschrijving van " + name, random.nextDouble() * 6, project);
                project.getTasks().add(task);
                entityManager.persist(task);

                final int registrationCount = random.nextInt(3);
                for (int r = 0; r < registrationCount; r++) {
                    RegisteredOccupation occupation = new RegisteredOccupation();
                    occupation.setOccupation(task);
                    final DateTime registeredOccupationDate = DateTime.now().withDayOfYear(random.nextInt(365) + 1);
                    occupation.setRegisteredStart(registeredOccupationDate.toDate());
                    occupation.setRegisteredEnd(registeredOccupationDate.plusDays(random.nextInt(5)).toDate());
                    occupation.setEmployee(employeeList.get(random.nextInt(employeeList.size())));

                    if (random.nextBoolean()) {
                        occupation.confirm();
                    }

                    entityManager.persist(occupation);
                }
            }
        }
    }
}
