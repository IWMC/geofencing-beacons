package com.realdolmen.config;

import com.realdolmen.entity.PersistenceUnit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Executes all necessary operations during application startup and shutdown.
 */
@Startup
@Singleton
public class StartupConfig {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager entityManager;

    @Inject
    private JPAImportConfig importConfig;

    @PostConstruct
    @Asynchronous
    public void startup() {
        importConfig.startup();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        try {
            fullTextEntityManager.createIndexer().startAndWait();
            Logger.getLogger(StartupConfig.class).info("Created Lucene index from existing database");
        } catch (InterruptedException e) {
            Logger.getLogger(StartupConfig.class).error("Could not create Lucene index from existing database", e);
        }
    }

    @PreDestroy
    public void shutdown() {

    }
}
