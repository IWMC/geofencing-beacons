package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.entity.validation.New;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One of the tasks hold by any {@link Project}.
 */
@Entity
@XmlRootElement(name = "task")
@NamedQueries({
        @NamedQuery(name = "Task.findAll", query = "SELECT t FROM Task t"),
        @NamedQuery(name = "Task.findByProjectId", query = "SELECT t FROM Task t WHERE t.project.projectNr = :projectId"),
        @NamedQuery(name = "Task.deleteById", query = "DELETE FROM Task t WHERE t.id=:id")
})
@Indexed
public class Task extends Occupation {

    @Min(0)
    private double estimatedHours;

    @ManyToOne
    @NotNull(message = "project.empty", groups = Existing.class)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Project project;

    @ManyToMany
    @JsonIgnore
    @IndexedEmbedded
    private Set<Employee> employees = new HashSet<>();

    @Transient
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Min(value = 1, message = "project_id.invalid", groups = New.class)
    private long projectId;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<Long> employeeIds = new ArrayList<>();

    public Task() {
    }

    public Task(String name, String description, double estimatedHours, Project project) {
        super(name, description);
        this.estimatedHours = estimatedHours;
        this.project = project;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    @XmlTransient
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
