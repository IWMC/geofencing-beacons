package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
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
        @NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p")
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
        project.getSubProjects().forEach(Project::initialize);
    }

    @Column(name = "startDate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Field
    @DateBridge(resolution = Resolution.DAY)
    private Date startDate;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Field
    @DateBridge(resolution = Resolution.DAY)
    private Date endDate;

    @Column
    @Field
    private int projectNr;

    @Transient
    @JsonProperty("DTYPE")
    private final int DTYPE = 2;

    @OneToMany(cascade = {
            CascadeType.REMOVE
    })
    // TODO: 21/03/2016 Field bridge to allow search on subprojects
    private Set<Project> subProjects = new HashSet<>();

    @ManyToMany(mappedBy = "memberProjects")
    @JsonIgnore
    private Set<Employee> employees = new HashSet<>();

    @OneToMany
    private Set<Location> locations = new HashSet<>();

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
}