package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.occupations.OccupationDetailsController;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;

import javax.enterprise.context.RequestScoped;
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
@RequestScoped
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
                    getFacesContext().getExternalContext().redirect(Pages.detailsProject().param("id", occupationId).redirect());
                }

                project = response.getStatus() == 200 ? (Project) response.getEntity() : null;
                if (project != null) {
                    return;
                }
            }

            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchOccupation().noRedirect());
        } catch (NumberFormatException nfex) {
            try {
                (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                        .getExternalContext().redirect(Pages.searchOccupation().noRedirect());
            } catch (IOException e) {
                Logger.getLogger(OccupationDetailsController.class).error("couldn't redirect with FacesContext", e);
            }
        } catch (IOException e) {
            Logger.getLogger(OccupationDetailsController.class).error("couldn't redirect with FacesContext", e);
        }
    }

    private List<Employee> filterEmployees(List<Employee> employees) {
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
        return Pages.detailsProject().param("id", occupationId).redirect();
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

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }
}
