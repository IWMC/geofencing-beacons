package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * A controller for <code>/tasks/task-details.xhtml</code>.
 */
@Named("taskDetails")
@ViewScoped
public class TaskDetailsController extends DetailController<Task> {

    @Inject
    private SecurityManager sm;

    @Inject
    private TaskDao taskDao;

    public TaskDetailsController() {
        super(Pages.searchOccupation());
    }

    @Override
    public Task loadEntity(long id) {
        Task task = taskDao.findById(id);

        if (task != null) {
            task.initialize();
        }

        return task;
    }

    public String getEstimatedHours() {
        Task task = getEntity();
        if (task.getEstimatedHours() == (int) task.getEstimatedHours()) {
            return getLanguage().getString("project.task.hours", (int) task.getEstimatedHours());
        } else {
            return getLanguage().getString("project.task.hours_minutes",
                    (int) task.getEstimatedHours(),
                    (int) ((task.getEstimatedHours() - Math.floor(task.getEstimatedHours())) * 60));
        }
    }

    @Transactional
    public String removeTask() {
        long id = getEntity().getProject().getId();

        if (taskDao.isManagingProjectManager(getEntity(), sm.findEmployee())) {
            taskDao.removeTask(getEntity());
        }

        return Pages.detailsProject().param("id", id).asRedirect();
    }

    public boolean getShouldShowEditOptions() {
        return taskDao.isManagingProjectManager(getEntity(), sm.findEmployee());
    }
}
