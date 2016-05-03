package com.realdolmen.validation;

import com.realdolmen.entity.Task;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Validator used to validate {@link com.realdolmen.entity.Task}s.
 */
@Stateless
@Local(Validator.class)
public class TaskValidator implements Validator<Task> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(Task task, Class<?>... groups) {
        return ValidationResult.fromConstraintViolations(validator.validate(task, groups));
    }

}
