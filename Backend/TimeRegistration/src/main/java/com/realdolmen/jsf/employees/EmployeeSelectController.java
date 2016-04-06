package com.realdolmen.jsf.employees;

import com.realdolmen.annotations.Filtered;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.occupations.OccupationDetailController;
import com.realdolmen.rest.OccupationEndpoint;
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

    private String occupationId;

    private Project project;

    private FacesContext facesContext;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    public void onOccupationId() {
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
        if (employees == null || project == null) {
            return employees;
        }

        return employees.stream().filter(e -> !project.getEmployees().contains(e)).collect(Collectors.toList());
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
    public String addEmployeeToProject(Employee employee) {
        project.getEmployees().add(employee);
        employee.getMemberProjects().add(project);
        getEntityManager().merge(project);
        getEntityManager().merge(employee);
        return Pages.editProject().param("id", occupationId).asRedirect();
    }

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
        onOccupationId();
    }

    public FacesContext getFacesContext() {
        if (facesContext == null) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }
}
