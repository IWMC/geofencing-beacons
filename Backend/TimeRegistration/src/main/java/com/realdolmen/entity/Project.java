package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * An occupation that contains extra data concerning the project.
 */
@Entity
@XmlRootElement
@Indexed
@NamedQueries({
        @NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p"),
        @NamedQuery(name = "Project.findProjectWithEmployeesById", query = "SELECT p FROM Project p JOIN FETCH p.employees where p.id=:id")
})
public class Project extends Occupation implements Serializable {

    /**
     * Initializes all lazy properties and collections of the entity recursively. Expects to be invoked while still running
     * in a session.
     *
     * @param project the project that should be initialized
     */
    public static void initialize(Project project) {
        Hibernate.initialize(project.getLocations());
        Hibernate.initialize(project.getSubProjects());
        Hibernate.initialize(project.getTasks());
        project.getSubProjects().forEach(Initializable::initialize);
        project.getEmployees().forEach(Hibernate::initialize);
    }

    @Column(name = "startDate", nullable = false)
    @Temporal(TemporalType.DATE)
    @Field
    @DateBridge(resolution = Resolution.DAY)
    @NotNull(message = "startDate.empty")
    private Date startDate;

    @Column
    @Temporal(TemporalType.DATE)
    @Field
    @DateBridge(resolution = Resolution.DAY)
    @NotNull(message = "endDate.empty")
    private Date endDate;

    @Column
    @Field
    @Min(value = 0, message = "projectNr.bounds")
    private int projectNr;

    @Transient
    @JsonProperty("DTYPE")
    private final int DTYPE = 2;

    @Column(name = "subProjects")
    @OneToMany(cascade = {
            CascadeType.REMOVE
    })
    //@JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonIgnore
    // TODO: 21/03/2016 Field bridge to allow search on subprojects
    private Set<Project> subProjects = new HashSet<>();

    @ManyToMany(mappedBy = "memberProjects")
    @JsonIgnore
    //@JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Set<Employee> employees = new HashSet<>();

    @OneToMany
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Set<Location> locations = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    @XmlTransient
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    public Project() {
    }

    public Project(String name, String description, int projectNr, Date startDate, Date endDate) {
        super(name, description);
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectNr = projectNr;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getProjectNr() {
        return projectNr;
    }

    public void setProjectNr(int projectNr) {
        this.projectNr = projectNr;
    }

    public Set<Project> getSubProjects() {
        return subProjects;
    }

    public void setSubProjects(Set<Project> subProjects) {
        this.subProjects = subProjects;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    @XmlTransient
    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        String result = getClass().getSimpleName() + " ";
        result += "projectNr: " + projectNr;
        return result;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    @Override
    public void initialize() {
        Project.initialize(this);
    }
}