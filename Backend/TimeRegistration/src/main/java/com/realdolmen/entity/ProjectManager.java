package com.realdolmen.entity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Entity for a project manager. This entity contains all fields of an Employee and some additional properties.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ProjectManager.findAll", query = "SELECT DISTINCT e FROM ProjectManager e ORDER BY e.id")
})
public class ProjectManager extends Employee {

    @OneToMany
    private Set<Project> managedProjects;

    public Set<Project> getManagedProjects() {
        return managedProjects;
    }

    public void setManagedProjects(Set<Project> managedProjects) {
        this.managedProjects = managedProjects;
    }
}
