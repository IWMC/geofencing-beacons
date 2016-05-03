package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * A controller for <code>/tasks/task-details.xhtml</code>.
 */
@Named("taskEdit")
@ViewScoped
public class TaskEditController extends TaskDetailsController {

    @Inject
    private TaskDao taskDao;

    @Inject
    private SecurityManager sm;

    @Transactional
    public String saveTask() {
        if (taskDao.isManagingProjectManager(getEntity(), sm.findEmployee())) {
            taskDao.update(getEntity());
        }

        return Pages.detailsTask().param("id", getEntity().getId()).asRedirect();
    }

    @Transactional
    public void removeEmployee(Employee employee) {
        if (taskDao.isManagingProjectManager(getEntity(), sm.findEmployee())) {
            getEntity().getEmployees().remove(employee);
            taskDao.update(getEntity());
        }
    }
}
