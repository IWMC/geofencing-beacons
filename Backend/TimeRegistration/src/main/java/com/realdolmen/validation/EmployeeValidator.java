package com.realdolmen.validation;

import com.realdolmen.entity.Employee;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.validation.*;
import java.util.Set;

/**
 * Validator used to validate employees.
 */
@Stateless
@Local(Validator.class)
public class EmployeeValidator implements Validator<Employee> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(Employee employee, Class<?>... groups) {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, groups);
        return ValidationResult.fromConstraintViolations(violations);
    }
}
