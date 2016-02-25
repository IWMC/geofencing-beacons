package com.realdolmen.validation;

import javax.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of a validation result. If there are validation violations a list will be encapsulated holding a token
 * representing each property that is invalid.
 */
@XmlRootElement
public class ValidationResult {

    private boolean isValid;

    private List<String> invalidationTokens;

    public ValidationResult() {
    }

    public ValidationResult(boolean isValid, List<String> invalidationTokens) {
        this.isValid = isValid;
        this.invalidationTokens = invalidationTokens;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public List<String> getInvalidationTokens() {
        return invalidationTokens;
    }

    public void setInvalidationTokens(List<String> invalidationTokens) {
        this.invalidationTokens = invalidationTokens;
    }

    public static <E> ValidationResult fromConstraintViolations(Set<ConstraintViolation<E>> violations) {
        if (violations == null || violations.isEmpty()) {
            return new ValidationResult(true, new ArrayList<>());
        }

        return new ValidationResult(false, violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList()));
    }
}
