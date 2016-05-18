package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A controller for <code>/tasks/task-add.xhtml</code>.
 */
@Named("taskAdd")
@ViewScoped
public class TaskAddController extends TaskEditController {

    @Inject
    private TaskDao taskDao;

    @Inject
    private SecurityManager sm;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Override
    public Task loadEntity(long id) {
        Task task = new Task();
        task.setProjectId(id);
        task.setProject(em.find(Project.class, id));
        return task;
    }

    public String returnToProject() {
        return Pages.detailsProject(Long.parseLong(getId())).asRedirect();
    }

    @Override
    public String saveTask() {
        if (taskDao.isManagingProjectManager(getEntity().getProjectId(), sm.findEmployee())) {
            taskDao.addTask(getEntity());
        }

        return Pages.detailsTask(getEntity().getId()).asRedirect();
    }
}
