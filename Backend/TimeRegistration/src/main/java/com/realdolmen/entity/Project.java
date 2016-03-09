package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Project extends Occupation implements Serializable {

    /**
     * Initializes all lazy properties and collections of the entity recursively. Expects to be invoked while still running
     * in a session.
     *
     * @param project the project that should be initialized
     */
    public static void initialize(Project project) {
        Hibernate.initialize(project.getSubProjects());
        project.getSubProjects().forEach(Project::initialize);
    }

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Version
	@Column(name = "version")
	private int version;

	@Column(name = "startDate", nullable = false)
	@Temporal(TemporalType.DATE)
    @Field
    @DateBridge(resolution = Resolution.DAY)
	private Date startDate;

	@Column
	@Temporal(TemporalType.DATE)
    @Field
    @DateBridge(resolution = Resolution.DAY)
	private Date endDate;

	@Column
    @Field
	private int projectNr;

	@OneToMany
	private Set<Project> subProjects = new HashSet<>();

    @ManyToMany(mappedBy = "memberProjects")
    @JsonIgnore
    private Set<Employee> employees = new HashSet<>();

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Project)) {
			return false;
		}
		Project other = (Project) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
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
}