package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realdolmen.entity.validation.Existing;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

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
    private Project project;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long projectId;

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
}
