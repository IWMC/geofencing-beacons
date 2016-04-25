package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.ProjectManager;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Used to store data about a JSF session, including authentication data.
 */
@SessionScoped
@Named("userContext")
public class UserContext implements Serializable {

    private Employee employee;

    public UserContext() {
    }

    public Employee getUser() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean getIsProjectManager() {
        return employee != null && employee instanceof ProjectManager;
    }

    public boolean getIsManagementEmployee() {
        return employee != null && employee instanceof ManagementEmployee;
    }

    public boolean getIsManagement() {
        return getIsProjectManager() || getIsManagementEmployee();
    }
}
