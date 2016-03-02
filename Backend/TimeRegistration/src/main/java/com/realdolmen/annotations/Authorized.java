package com.realdolmen.annotations;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Interceptor annotation that validates authentication and authorization when trying to access a secure JAX-RS path,
 * or technically every method accessed or proxied by the container.
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Authorized {

    UserGroup value() default UserGroup.EMPLOYEE;
}
