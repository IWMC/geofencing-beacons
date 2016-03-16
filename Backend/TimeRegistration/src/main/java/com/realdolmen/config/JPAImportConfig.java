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
        project.setEndDate(DateUtils.addMonths(new Date(), 3));

        entityManager.persist(o);
        entityManager.persist(project);

        try {
            Employee employee1 = entityManager.createNamedQuery("Employee.findByUsername", Employee.class).setParameter("username", "brentc").getSingleResult();
            employee1.getMemberProjects().add(project);
            entityManager.merge(employee1);
        } catch(NoResultException nrex) {
            Logger.getLogger(JPAImportConfig.class).info("Couldn't find user 'brentc', skipping project assignment");
        }

    }
}
