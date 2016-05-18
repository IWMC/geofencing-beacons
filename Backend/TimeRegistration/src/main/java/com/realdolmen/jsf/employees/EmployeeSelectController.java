package com.realdolmen.jsf.employees;

import com.realdolmen.annotations.Filtered;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.service.SecurityManager;
import org.jetbrains.annotations.TestOnly;
import org.omnifaces.cdi.ViewScoped;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    private String returnURL;

    private Task task;

    private Project project;

    private FacesContext facesContext;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @Transactional
    public String onParentId() {
        try {
            if (occupationId != null) {
                long id = Long.parseLong(occupationId);
                Response response = occupationEndpoint.findById(id);

                if (response.getEntity() != null && !(response.getEntity() instanceof Project)) {
                    return Pages.detailsOccupation().param("id", occupationId).asRedirect();
                }

                project = response.getStatus() == 200 ? (Project) response.getEntity() : null;
                if (project != null) {
                    return "";
                }
            }

            if (taskId != null) {
                task = taskDao.findByIdEagerly(Long.parseLong(taskId));
                if (task != null) {
                    return "";
                }
            }

            return Pages.searchOccupation().asLocationRedirect();
        } catch (NumberFormatException nfex) {
            return Pages.searchOccupation().asLocationRedirect();
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
    public String addEmployeeToParent(Employee employee) throws UnsupportedEncodingException {
        if (project != null && task == null) {
            project.getEmployees().add(employee);
            employee.getMemberProjects().add(project);
            getEntityManager().merge(project);
            getEntityManager().merge(employee);
            return returnURL == null ? Pages.editProject().param("id", occupationId).asRedirect() : URLDecoder.decode(returnURL, "UTF-8");
        } else if (project == null && task != null) {
            task = taskDao.refresh(task);
            if (taskDao.isManagingProjectManager(task, sm.findEmployee())) {
                task.getEmployees().add(employee);
                taskDao.update(task);
                return returnURL == null ? Pages.detailsTask(task.getId()).asRedirect() : URLDecoder.decode(returnURL, "UTF-8");
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

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }
}
