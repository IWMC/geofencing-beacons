package com.realdolmen.entity;

/**
 * A way to initialize some entity, such that no property invocation on that entity will cause any
 * {@link org.hibernate.LazyInitializationException}s.
 */
public interface Initializable {
    void initialize();
}
