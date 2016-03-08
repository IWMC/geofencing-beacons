package com.realdolmen.entity;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity for a management employee. It does not hold additional properties by himself, yet it is important to keep this
 * as a separate entity since user rights are different and the differentiation between employees happens by inheritance.
 */
@NamedQueries({
        @NamedQuery(name = "ManagementEmployee.findAll", query = "SELECT me FROM ManagementEmployee me")
})
@Entity
@Indexed
public class ManagementEmployee extends Employee {

    public ManagementEmployee() {
    }

    public ManagementEmployee(Employee employee) {
        super(employee.getId(), employee.getVersion(), employee.getFirstName(), employee.getLastName(), employee.getUsername(),
                employee.getEmail(), employee.getHash(), employee.getSalt(), employee.getPassword(), employee.getMemberProjects());
    }
}
