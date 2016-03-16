package com.realdolmen.validation;

import com.realdolmen.json.EmployeePasswordCredentials;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Validator used to validate employees.
 */
@Stateless
@Local(Validator.class)
public class EmployeePasswordCredentialsValidator implements Validator<EmployeePasswordCredentials> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(EmployeePasswordCredentials credentials, Class<?>... groups) {
        Set<ConstraintViolation<EmployeePasswordCredentials>> violations = validator.validate(credentials, groups);
        ValidationResult result = ValidationResult.fromConstraintViolations(violations);

        if (result.isValid() && !credentials.getPassword().equals(credentials.getPasswordRepeat())) {
            result.setValid(false);
            result.getInvalidationTokens().add("password_repeat.wrong");
        }

        return result;
    }
}
