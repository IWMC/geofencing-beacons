package com.realdolmen.entity.dao;

import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.validation.New;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

/**
 * Dao class for common CRUD-operations with the {@link Task} entity.
 */
@Stateless
public class TaskDao {

    @Inject
    private Validator<Task> validator;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    public TypedQuery<Task> getTasks() {
        return em.createNamedQuery("Task.findAll", Task.class);
    }

    public TypedQuery<Task> getTasksByProject(@NotNull String projectNr) {
        return em.createNamedQuery("Task.findByProjectId", Task.class).setParameter("projectId", projectNr);
    }

    @Transactional
    public void addTask(@NotNull Task task) {
        if (task.getProjectId() > 0) {
            Project project = em.find(Project.class, task.getProjectId());

            if (project == null) {
                throw new IllegalArgumentException("project cannot be null");
            }

            task.setProject(project);
        }

        ValidationResult result = validator.validate(task, New.class);
        if (result.isValid()) {
            em.persist(task);
        }
    }

    @Transactional
    public void removeTask(@NotNull Task task) {
        em.remove(task);
    }

    public void removeTask(long taskId) {
        em.createNamedQuery("Task.deleteById").setParameter("id", taskId).executeUpdate();
    }
}
