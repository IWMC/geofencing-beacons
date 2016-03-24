package com.realdolmen.validation;


import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.Project;
import com.realdolmen.messages.Language;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Validator used to validate {@link Occupation}s.
 */
@Stateless
@Local(Validator.class)
public class OccupationValidator implements Validator<Occupation> {

    @Resource
    javax.validation.Validator validator;

    @Override
    public ValidationResult validate(Occupation occupation, Class<?>... groups) {
        ValidationResult result = ValidationResult.fromConstraintViolations(validator.validate(occupation, groups));
        if (occupation instanceof Project) {
            Project project = (Project) occupation;
            if (project.getStartDate().after(project.getEndDate())) {
                result.setValid(false);
                result.getInvalidationTokens().add(Language.Text.PROJECT_DATE_OUT_OF_BOUNDS);
            }
        }

        return result;
    }

}
