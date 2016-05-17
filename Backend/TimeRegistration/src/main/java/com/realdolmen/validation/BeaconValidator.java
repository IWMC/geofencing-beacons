package com.realdolmen.validation;

import com.realdolmen.entity.Beacon;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local(Validator.class)
public class BeaconValidator implements Validator<Beacon> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(Beacon beacon, Class<?>... groups) {
        return ValidationResult.fromConstraintViolations(validator.validate(beacon, groups));
    }

}
