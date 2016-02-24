package com.realdolmen.validation;

import javax.ejb.Local;

/**
 * Validator EJB that can validate entities and will return understandable multilingual feedback.
 */
public interface Validator<E> {

    /**
     * Allows something to be validated by checking whether all fields are in a valid state
     * (e.g. not null, passing a certain regex, ...). This could be done using JPA validation,
     * yet is not restricted to it. The {@link ValidationResult} returned allows the
     * integration of the validation in multi-platform and multilingual systems.
     *
     * @param e entity that should be validated
     * @return a positive or negative validation result
     */
    ValidationResult validate(E e, Class<?>... groups);
}