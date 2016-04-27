package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.realdolmen.entity.validation.Existing;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One of the tasks hold by any {@link Project}.
 */
@Entity
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Task.findAll", query = "SELECT t FROM Task t"),
        @NamedQuery(name = "Task.findByProjectId", query = "SELECT t FROM Task t WHERE t.project.id = :projectId"),
        @NamedQuery(name = "Task.deleteById", query = "DELETE FROM Task t WHERE t.id=:id")
})
public class Task extends Occupation {

    @Min(0)
    private float estimatedHours;

    @ManyToOne
    @NotNull(message = "project.empty", groups = Existing.class)
    @JsonIgnore
    private Project project;

    @ManyToMany
    @JsonIgnore
    private Set<Employee> employees = new HashSet<>();

    @Transient
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long projectId;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Long> employeeIds = new ArrayList<>();

    public float getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(float estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public List<Long> getEmployeeIds() {
        return employeeIds;
    }
}
