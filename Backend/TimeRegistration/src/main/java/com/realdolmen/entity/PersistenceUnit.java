package com.realdolmen.entity;

/**
 * Interface containing constants representing the persistence units defined in persistence.xml. These can be used in a
 * reusable fashion to provide a persistence unit in {@link javax.persistence.PersistenceContext}.
 */
public interface PersistenceUnit {

    String PRODUCTION = "TimeRegistration-persistence-unit";
    String TEST = "TimeRegistration-test-persistence-unit";
}
