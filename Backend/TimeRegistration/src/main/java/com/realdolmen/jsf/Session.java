package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * Used to store data about a JSF session, including authentication data.
 */
@SessionScoped
public class Session implements Serializable {

    private Employee employee;

    public Session() {
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
