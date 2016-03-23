package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jetbrains.annotations.TestOnly;

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

    public ProjectAddController() {
    }

    public void saveProject() throws IOException {
        Response response = endpoint.addProject(project);

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.searchOccupation().redirect());
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
