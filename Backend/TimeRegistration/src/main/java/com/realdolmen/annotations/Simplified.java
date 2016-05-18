package com.realdolmen.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation always used in combination with {@link javax.inject.Inject} to inject a simplified version of something.
 * When annotating this with a {@link com.realdolmen.service.ReportsQueryBuilder} a
 * {@link com.realdolmen.service.SimplifiedReportsQueryBuilder} will be injected, that supports a simplified query language.
 */
@Target({TYPE, METHOD, PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface Simplified {
}
