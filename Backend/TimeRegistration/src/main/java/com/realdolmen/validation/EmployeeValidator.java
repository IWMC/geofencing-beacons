package com.realdolmen.validation;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.validation.Existing;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.validation.*;
import java.util.Arrays;
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

        ValidationResult result = ValidationResult.fromConstraintViolations(violations);

        if (Arrays.asList(groups).contains(Existing.class)) {
            if (employee.getPassword() != null) {
                int length = employee.getPassword().length();
                final String token = "password.length";
                if (length != 0 && (length < 6 || length > 15) && !result.getInvalidationTokens().contains(token)) {
                    result.setValid(false);
                    result.getInvalidationTokens().add(token);
                }
            }
        }

        return result;
    }
}
