package com.realdolmen.entity.dao;

import com.realdolmen.entity.*;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.entity.validation.New;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Dao class for common CRUD-operations with the {@link Task} entity.
 */
@Stateless
public class TaskDao {

    @Inject
    private Validator<Task> validator;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    public Task findById(long id) {
        return em.find(Task.class, id);
    }

    @Transactional
    public Task findByIdEagerly(long id) {
        Task task = findById(id);
        task.initialize();
        return task;
    }

    public Task findReferenceById(long id) {
        return em.getReference(Task.class, id);
    }

    public TypedQuery<Task> getTasks() {
        return em.createNamedQuery("Task.findAll", Task.class);
    }

    public List<Task> getAllTasks() {
        return getTasks().getResultList();
    }

    public TypedQuery<Task> getTasksByProject(@NotNull String projectNr) {
        return em.createNamedQuery("Task.findByProjectId", Task.class).setParameter("projectId", projectNr);
    }

    @Transactional
    public void addTask(@NotNull Task task) {
        if (task.getProjectId() == 0 && task.getProject() == null) {
            throw new IllegalArgumentException("task should have a project or a project id");
        }

        Project project = task.getProject() != null ? em.find(Project.class, task.getProject().getId())
                : em.find(Project.class, task.getProjectId());

        if (project == null) {
            throw new IllegalArgumentException("project with id " + task.getProjectId() + " does not exist");
        }

        task.setProject(project);

        if (!task.getEmployeeIds().isEmpty()) {
            for (Long id : task.getEmployeeIds()) {
                Employee employee = em.find(Employee.class, id);
                if (employee == null) {
                    throw new IllegalArgumentException("employee with id " + id + "does not exist");
                }

                task.getEmployees().add(employee);
            }
        }

        ValidationResult result = validator.validate(task, New.class);
        if (result.isValid()) {
            em.persist(task);
        }
    }

    @Transactional
    public void removeTask(@NotNull Task task) {
        if (em.contains(task)) {
            em.remove(task);
        } else {
            removeTask(task.getId());
        }
    }

    public Task refresh(Task task) {
        if (!em.contains(task)) {
            return em.find(Task.class, task.getId());
        } else {
            em.refresh(task);
            return task;
        }
    }

    public Task refreshWithReference(Task task) {
        if (!em.contains(task)) {
            return em.getReference(Task.class, task.getId());
        } else {
            em.refresh(task);
            return task;
        }
    }

    @Transactional
    public void removeTask(long taskId) {
        em.createNamedQuery("Task.deleteById").setParameter("id", taskId).executeUpdate();
    }

    public ValidationResult update(Task task) {
        try {
            ValidationResult result = validator.validate(task, Existing.class);

            if (result.isValid()) {
                em.merge(task);
            }

            return result;
        } catch (IllegalArgumentException enfex) {
            throw new IllegalArgumentException("trying to update non-existing task", enfex);
        }
    }

    /**
     * Checks if the given employee is a project manager of the given task.
     *
     * @param task     The task that should be checked
     * @param employee The employee that should be checked
     * @return True if the employee is a project manager of the given task, false otherwise
     */
    public boolean isManagingProjectManager(Task task, Employee employee) {
        if (task == null || employee == null || !(employee instanceof ProjectManager)) {
            return false;
        }

        return refreshWithReference(task).getProject().getEmployees().contains(employee);
    }

    /**
     * Checks if the given employee is a project manager of the project with the given project id.
     *
     * @param projectId The project id of the project
     * @param employee  The employee that should be checked
     * @return True if the employee is a project manager of the project, false otherwise
     */
    public boolean isManagingProjectManager(long projectId, Employee employee) {
        if (employee == null || !(employee instanceof ProjectManager)) {
            return false;
        }

        try {
            return em.getReference(Project.class, projectId).getEmployees().contains(employee);
        } catch (EntityNotFoundException enfex) {
            return false;
        }
    }
}
