package com.realdolmen.config;

import com.realdolmen.entity.*;
import com.realdolmen.service.SecurityManager;
import org.apache.commons.lang3.time.DateUtils;
import org.infinispan.persistence.manager.PersistenceManager;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

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

        Occupation o = new Occupation();
        o.setName("Lunch");
        o.setDescription("Lunch time");

        Project project = new Project();
        project.setProjectNr(8);
        project.setStartDate(new Date());
        project.setName("Project X");
        project.setEndDate(DateUtils.addMonths(new Date(), 3));

        for (int i = 0; i < 20; i++) {
            Occupation oo = new Occupation();
            oo.setName("Occupation " + ThreadLocalRandom.current().nextInt(100));
            if(i % 4 == 0) {
                oo.setDescription("Description for #" + i);
            }
            entityManager.persist(oo);
        }

        entityManager.persist(o);
        entityManager.persist(project);

        Employee employee1 = entityManager.createNamedQuery("Employee.findByUsername", Employee.class).setParameter("username", "brentc").getSingleResult();
        employee1.getMemberProjects().add(project);
        entityManager.merge(employee1);

        Logger.getLogger(JPAImportConfig.class).info("Succesfully added admin account: 'admin'");
    }
}
