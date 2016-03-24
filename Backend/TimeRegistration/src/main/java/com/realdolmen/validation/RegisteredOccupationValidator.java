package com.realdolmen.validation;

import com.realdolmen.entity.RegisteredOccupation;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Validator used to validate {@link RegisteredOccupation}.
 */
@Stateless
@Local(Validator.class)
public class RegisteredOccupationValidator implements Validator<RegisteredOccupation> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(RegisteredOccupation registeredOccupation, Class<?>... groups) {
        ValidationResult result = ValidationResult.fromConstraintViolations(validator.validate(registeredOccupation, groups));
        if(registeredOccupation.getRegisteredEnd() != null) {
            if(registeredOccupation.getRegisteredEnd().before(registeredOccupation.getRegisteredStart())) {
                result.setValid(false);
                result.getInvalidationTokens().add("occupation.date.bounds");
            }
        }

        return result;
    }
}
