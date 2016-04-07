package com.realdolmen.jsf.occupations;

import com.realdolmen.annotations.Filtered;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.material.application.ToastService;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A controller for <code>/occupations/subproject-select.xhtml</code>.
 */
@Named("selectSubproject")
@ViewScoped
@Filtered
public class SubprojectSelectController extends OccupationSearchController implements Serializable {

    private String occupationId;

    private Project project;

    private transient FacesContext facesContext;

    private transient ToastService toastService;

    @Inject
    private Language language;

    public void onPreRender() {
        try {
            if (occupationId != null) {
                long id = Long.parseLong(occupationId);
                Response response = getOccupationEndpoint().findById(id);

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
                getFacesContext().getExternalContext().redirect(Pages.searchOccupation().asLocationRedirect());
            } catch (IOException e) {
                Logger.getLogger(OccupationDetailController.class).error("couldn't redirect with FacesContext", e);
            }
        } catch (IOException e) {
            Logger.getLogger(OccupationDetailController.class).error("couldn't redirect with FacesContext", e);
        }
    }

    public List<Occupation> filterOccupations(List<Occupation> occupations) {
        if (occupations == null || project == null) {
            return occupations;
        }

        return occupations.stream()
                .filter(o -> o instanceof Project && !o.equals(project)
                        && project.getSubProjects() != null && !project.getSubProjects().contains(o)
                        && !((Project) o).getSubProjects().contains(project)) // Avoid cyclic references!
                .collect(Collectors.toList());
    }

    @Override
    public List<Occupation> getOccupations() {
        return filterOccupations(super.getOccupations());
    }

    @Override
    public List<Occupation> getOccupationsWithSearchTerms() {
        return filterOccupations(super.getOccupationsWithSearchTerms());
    }

    @Transactional
    public String addAsSubProject(Project selectedProject) {
        project.getSubProjects().add(selectedProject);
        getEntityManager().merge(project);
        return Pages.editProject().param("id", occupationId).asRedirect();
    }

    public FacesContext getFacesContext() {
        return facesContext;
    }

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        if (facesContext == null) {
            facesContext = FacesContext.getCurrentInstance();
        }

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

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}