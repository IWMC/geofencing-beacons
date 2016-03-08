package com.realdolmen.validation;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.entity.validation.New;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class EmployeeValidatorTest {

    @Inject
    private Validator<Employee> validator;

    private Employee validExistingEmployee = new Employee("", "1234567", "1234567", "email@email.com", "Username", "Lastname", "Firstname");
    private Employee validNewEmployee = new Employee("Abc123", "", "", "email@email.com", "Username", "Lastname", "Firstname");
    private Employee invalidPasswordEmployee = new Employee("abc123", "", "", "email@email.com", "Username", "Lastname", "Firstname");
    private Employee tooLongPasswordEmployee = new Employee("Ab23456789123456", "", "", "email@email.com", "Username", "Lastname", "Firstname");
    private Employee noDataEmployee = new Employee("", "", "", "", "", "", "");
    private Employee invalidEmailEmployee = new Employee("Abc123", "", "", "email@email.1com", "Username", "Lastname", "Firstname");

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    private void assertNoValidationExceptions(ValidationResult result) {
        assertTrue("validation should be valid", result.isValid());
        assertTrue("validation result should contain no validation tokens", result.getInvalidationTokens().isEmpty());
    }

    private <E> void assertContainsAll(List<E> list, E... values) {
        Arrays.stream(values).forEach(v -> assertTrue(v + " should be in list", list.contains(v)));
        List<E> valuesList = Arrays.asList(values);
        list.forEach(v -> assertTrue(v + " should not be in list", valuesList.contains(v)));
    }

    @Test
    public void testValidateSucceedsOnNewValidEmployee() throws Exception {
        assertNoValidationExceptions(validator.validate(validNewEmployee, New.class));
    }

    @Test
    public void testValidateSucceedsOnExistingValidEmployee() throws Exception {
        assertNoValidationExceptions(validator.validate(validExistingEmployee, Existing.class));
    }

    @Test
    public void testValidateFailsOnNewEmptyEmployee() {
        ValidationResult result = validator.validate(noDataEmployee, New.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "password.length", "username.length", "firstName.length", "lastName.length",
                "email.pattern");
    }

    @Test
    public void testValidateFailsOnExistingEmptyEmployee() {
        ValidationResult result = validator.validate(noDataEmployee, Existing.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "username.length", "firstName.length", "lastName.length",
                "email.pattern");
    }

    @Test
    public void testValidateFailsOnWrongNewPasswordEmployee() {
        ValidationResult result = validator.validate(invalidPasswordEmployee, New.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "password.pattern");
    }

    @Test
    public void testValidateFailsOnWrongExistingPasswordEmployee() {
        ValidationResult result = validator.validate(invalidPasswordEmployee, Existing.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "password.pattern");
    }

    @Test
    public void testValidateFailsOnTooLongNewPasswordEmployee() {
        ValidationResult result = validator.validate(tooLongPasswordEmployee, New.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "password.length");
    }

    @Test
    public void testValidateFailsOnTooLongExistingPasswordEmployee() {
        ValidationResult result = validator.validate(tooLongPasswordEmployee, Existing.class);
        assertFalse("validation is invalid", result.isValid());
        assertContainsAll(result.getInvalidationTokens(), "password.length");
    }
}