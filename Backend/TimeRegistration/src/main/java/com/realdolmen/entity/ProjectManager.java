package com.realdolmen.entity;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Entity for a project manager. This entity contains all fields of an Employee and some additional properties.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ProjectManager.findAll", query = "SELECT DISTINCT e FROM ProjectManager e ORDER BY e.id")
})
@Indexed
@Table(name = "projectmanager")
public class ProjectManager extends Employee {

    public ProjectManager() {
    }

    public ProjectManager(Employee employee) {
        super(employee.getId(), employee.getVersion(), employee.getFirstName(), employee.getLastName(), employee.getUsername(),
                employee.getEmail(), employee.getHash(), employee.getSalt(), employee.getPassword(), employee.getMemberProjects());
    }

    @OneToMany
    private Set<Project> managedProjects;

    public Set<Project> getManagedProjects() {
        return managedProjects;
    }

    public void setManagedProjects(Set<Project> managedProjects) {
        this.managedProjects = managedProjects;
    }
}
