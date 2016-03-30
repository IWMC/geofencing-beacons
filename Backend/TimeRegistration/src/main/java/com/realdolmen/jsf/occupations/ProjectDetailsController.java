package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Location;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;

/**
 * A controller for <code>/occupations/project-details.xhtml</code>.
 */
@Named("projectDetails")
@ViewScoped
public class ProjectDetailsController implements Serializable {

    @ManagedProperty(value = "#{param.occupationId}")
    private String occupationId;

    @Inject
    private transient OccupationEndpoint occupationEndpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    private transient MapModel mapModel = new DefaultMapModel();

    private Project project;

    private transient FacesContext facesContext = FacesContext.getCurrentInstance();

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
                    project.getLocations().stream().map(l -> new Marker(new LatLng(l.getLatitude(), l.getLongitude())))
                            .forEach(mapModel::addOverlay);
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

    public String getLocationOrDefault() {
        return project.getLocations() != null && !project.getLocations().isEmpty() ?
                project.getLocations().iterator().next().toString() : Location.REALDOLMEN_HEADQUARTERS.toString();
    }

    @Transactional
    public void addMarker(PointSelectEvent psev) {
        if (psev == null || psev.getLatLng() == null) {
            return;
        }

        LatLng latLng = psev.getLatLng();
        Marker marker = new Marker(latLng);
        Location location = new Location(latLng.getLat(), latLng.getLng());
        project.getLocations().add(location);
        em.persist(location);
        em.merge(project);
        mapModel.addOverlay(marker);
    }

    public MapModel getMapModel() {
        return mapModel;
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
