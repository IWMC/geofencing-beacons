package com.realdolmen.config;

import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.service.SecurityManager;
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
import java.util.concurrent.Future;

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
    }
}
