package com.realdolmen.entity;

/**
 * Interface containing constants representing the persistence units defined in persistence.xml. These can be used in a
 * reusable fashion to provide a persistence unit in {@link javax.persistence.PersistenceContext}.
 */
public interface PersistenceUnit {

    public static final String PRODUCTION_UNIT = "TimeRegistration-persistence-unit";
    public static final String TEST_UNIT = "TimeRegistration-test-persistence-unit";
}
