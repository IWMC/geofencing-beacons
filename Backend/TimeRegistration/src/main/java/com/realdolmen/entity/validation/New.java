package com.realdolmen.entity.validation;

import javax.validation.groups.Default;

/**
 * <b>Interface used as a group in bean validation.</b>
 * This group is used for bean validation on entity beans that are new to the system. For example an employee that is
 * registering an account.
 */
public interface New extends Default {
}
