package com.realdolmen.config;

import com.realdolmen.entity.*;
import com.realdolmen.service.SecurityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.logging.Logger;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Configuration adding, editing or removing entities that should be present in the database at startup. Should be
 * invoked by {@link StartupConfig} to allow other configurations to work based on previously created results.
 */
@Singleton
public class JPAImportConfig {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager entityManager;

    @Inject
    private SecurityManager securityManager;

    @Transactional
    public void startup() {
        ManagementEmployee employee = new ManagementEmployee();
        employee.setUsername("admin");
        employee.setEmail("admin@realdolmen.com");
        employee.setFirstName("admin");
        employee.setLastName("admin");
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

        entityManager.persist(o);
        entityManager.persist(project);
        entityManager.persist(otherProject);

        createAndPersistOccupations();

        try {
            Employee employee1 = entityManager.createNamedQuery("Employee.findByUsername", Employee.class).setParameter("username", "brentc").getSingleResult();
            employee1.getMemberProjects().add(project);
            entityManager.merge(employee1);
        } catch (NoResultException nrex) {
            Logger.getLogger(JPAImportConfig.class).info("Couldn't find user 'brentc', skipping project assignment");
        }
    }

    private void createAndPersistOccupations() {
        for (int i = 0; i < 15; i++) {
            Project project = new Project();
            project.setProjectNr(i + 9);
            project.setStartDate(new Date());
            project.setName("Project " + (i + 300));
            project.setDescription("The project details from project #" + i);
            project.setEndDate(DateUtils.addMonths(new Date(), 3));
            entityManager.persist(project);
        }
    }
}
