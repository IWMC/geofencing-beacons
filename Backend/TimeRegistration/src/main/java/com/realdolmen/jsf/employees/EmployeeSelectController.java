package com.realdolmen.jsf.employees;

import com.realdolmen.annotations.Filtered;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.occupations.OccupationDetailController;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.service.SecurityManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;
import org.omnifaces.cdi.ViewScoped;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A controller for <code>/employees/employee-select.xhtml</code>.
 */
@Named("selectEmployee")
@ViewScoped
@Filtered
public class EmployeeSelectController extends EmployeeSearchController {

    @Inject
    private TaskDao taskDao;

    @Inject
    private SecurityManager sm;

    private String occupationId;

    private String taskId;

    private Task task;

    private Project project;

    private FacesContext facesContext;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @Transactional
    public void onParentId() {
        try {
            if (occupationId != null) {
                long id = Long.parseLong(occupationId);
                Response response = occupationEndpoint.findById(id);

                if (response.getEntity() != null && !(response.getEntity() instanceof Project)) {
                    getFacesContext().getExternalContext().redirect(Pages.detailsOccupation().param("id", occupationId).asRedirect());
                }

                project = response.getStatus() == 200 ? (Project) response.getEntity() : null;
                if (project != null) {
                    return;
                }
            }

            if (taskId != null) {
                task = taskDao.findByIdEagerly(Long.parseLong(taskId));
                if (task != null) {
                    return;
                }
            }

            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchOccupation().asLocationRedirect());
        } catch (NumberFormatException nfex) {
            try {
                (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                        .getExternalContext().redirect(Pages.searchOccupation().asLocationRedirect());
            } catch (IOException e) {
                Logger.getLogger(OccupationDetailController.class).error("couldn't redirect with FacesContext", e);
            }
        } catch (IOException e) {
            Logger.getLogger(OccupationDetailController.class).error("couldn't redirect with FacesContext", e);
        }
    }

    public List<Employee> filterEmployees(List<Employee> employees) {
        if (employees == null) {
            return null;
        }

        if (project != null) {
            return employees.stream().filter(e -> !project.getEmployees().contains(e)).collect(Collectors.toList());
        }

        if (task != null) {
            return employees.stream().filter(e -> task.getProject().getEmployees().contains(e)).collect(Collectors.toList());
        }

        return employees;
    }

    @Override
    public List<Employee> getEmployees() {
        return filterEmployees(super.getEmployees());
    }

    @Override
    public List<Employee> getEmployeesWithSearchTerms() {
        return filterEmployees(super.getEmployeesWithSearchTerms());
    }

    @Transactional
    public String addEmployeeToParent(Employee employee) {
        if (project != null && task == null) {
            project.getEmployees().add(employee);
            employee.getMemberProjects().add(project);
            getEntityManager().merge(project);
            getEntityManager().merge(employee);
            return Pages.editProject().param("id", occupationId).asRedirect();
        } else {
            task = taskDao.refresh(task);
            if (taskDao.isManagingProjectManager(task, sm.findEmployee())) {
                task.getEmployees().add(employee);
                taskDao.update(task);
                return Pages.detailsTask(task.getId()).asRedirect();
            }
        }

        return "";
    }

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
        onParentId();
    }

    public FacesContext getFacesContext() {
        if (facesContext == null) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
        onParentId();
    }
}
