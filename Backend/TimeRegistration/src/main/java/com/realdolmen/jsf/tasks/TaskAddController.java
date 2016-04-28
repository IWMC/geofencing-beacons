package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

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

    @Override
    public Task loadEntity(long id) {
        Task task = new Task();
        task.setProjectId(id);
        return task;
    }

    @Override
    public String saveTask() {
        if (taskDao.isManagingProjectManager(getEntity().getProjectId(), sm.findEmployee())) {
            taskDao.addTask(getEntity());
        }

        return Pages.editTask(getEntity().getId()).asRedirect();
    }
}
