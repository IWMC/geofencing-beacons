package com.realdolmen.annotations;

/**
 * An enum used to define which rights need to be granted to the caller in order for it to pass the security check.
 * It is used in the {@link Authorized} interceptor binding.
 */
public enum UserGroup {
    /**
     * Every registered user in the system.
     */
    EMPLOYEE,

    /**
     * Every project manager in the system, however no regular employee or management employee.
     */
    PROJECT_MANAGER_ONLY,

    /**
     * Every management employee in the system, however no regular employee or project manager.
     */
    MANAGEMENT_EMPLOYEE_ONLY,

    /**
     * Every project manager or management employee, however no regular employee.
     */
    MANAGEMENT
}
