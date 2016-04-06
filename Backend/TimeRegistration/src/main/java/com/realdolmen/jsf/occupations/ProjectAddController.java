package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.validation.ValidationResult;
import org.jetbrains.annotations.TestOnly;
import org.primefaces.material.application.ToastService;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/occupations/occupation-add.xhtml</code>.
 */
@RequestScoped
@Named("projectAdd")
public class ProjectAddController {

    private Project project = new Project();

    @Inject
    private OccupationEndpoint endpoint;

    private FacesContext facesContext = FacesContext.getCurrentInstance();
    private ToastService toastService;

    @Inject
    private Language language;

    public ProjectAddController() {
    }

    public void saveProject() throws IOException {
        Response response = endpoint.addProject(project);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.searchOccupation().asRedirect());
        } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            getToastService().newToast(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN));
        } else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            ValidationResult validationResult = (ValidationResult) response.getEntity();
            assert !validationResult.isValid();
            if (validationResult.getInvalidationTokens().contains(Language.Text.PROJECT_DATE_OUT_OF_BOUNDS)) {
                getToastService().newToast(language.getString(Language.Text.PROJECT_DATE_OUT_OF_BOUNDS));
            }
        }
    }

    public FacesContext getFacesContext() {
        if (facesContext == null || facesContext.isReleased()) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    public ToastService getToastService() {
        if (toastService == null) {
            toastService = ToastService.getInstance();
        }

        return toastService;
    }

    @TestOnly
    public void setToastService(ToastService toastService) {
        this.toastService = toastService;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
