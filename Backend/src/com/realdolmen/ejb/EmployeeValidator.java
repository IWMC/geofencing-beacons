package com.realdolmen.ejb;

import com.realdolmen.entity.Employee;

import javax.ejb.Stateless;
import java.util.List;

/**
 * A {@link Validator} used to validate {@link Employee}s.
 */
@Stateless
public class EmployeeValidator implements Validator<Employee> {

    @Override
    public List<Validation> validate(Employee employee) {
        return null;
    }
}
