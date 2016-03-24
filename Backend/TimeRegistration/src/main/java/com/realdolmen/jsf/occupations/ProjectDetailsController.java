package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/occupations/project-details.xhtml</code>.
 */
@Named("projectDetails")
@RequestScoped
public class ProjectDetailsController {

    @ManagedProperty(value = "#{param.occupationId}")
    private String occupationId;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private Project project;

    private FacesContext facesContext = FacesContext.getCurrentInstance();

    @Transactional
    public void onPreRender() {
        try {
            if (occupationId != null) {
                long id = Long.parseLong(occupationId);
                Response response = occupationEndpoint.findById(id);

                if (response.getEntity() != null && !(response.getEntity() instanceof Project)) {
                    facesContext.getExternalContext().redirect(Pages.detailsProject().param("id", occupationId).redirect());
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

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
    }

    public Project getProject() {
        return project;
    }

    public void setOccupation(Project project) {
        this.project = project;
    }

    @TestOnly
    public void setFacesContext(FacesContext context) {
        this.facesContext = context;
    }
}
