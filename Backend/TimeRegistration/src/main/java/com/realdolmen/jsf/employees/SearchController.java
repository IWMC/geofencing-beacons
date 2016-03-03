package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.rest.EmployeeEndpoint;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * A controller for <code>/employees/search-employees.xhtml</code>.
 */
@Named("employeeSearch")
@RequestScoped
public class SearchController implements Serializable {

    @Inject
    private EmployeeEndpoint endpoint;

    private List<Employee> employees;

    @PostConstruct
    public void init() {
        employees = (List<Employee>) endpoint.listAll(0, 0).getEntity();
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}
