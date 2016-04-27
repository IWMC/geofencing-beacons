package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A controller for <code>/tasks/task-details.xhtml</code>.
 */
@Named("taskDetails")
@ViewScoped
public class TaskDetailsController extends DetailController<Task> {

    @Inject
    private TaskDao taskDao;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    public TaskDetailsController() {
        super(Pages.searchOccupation());
    }

    @Override
    public Task loadEntity(long id) {
        Task task = taskDao.findById(id);
        Task.initialize(task);
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
}
