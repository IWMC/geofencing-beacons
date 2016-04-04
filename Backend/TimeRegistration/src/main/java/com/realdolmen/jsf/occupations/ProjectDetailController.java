package com.realdolmen.jsf.occupations;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.Location;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.faces.view.ViewScoped;
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
public class ProjectDetailController extends DetailController<Project> implements Serializable {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    @Inject
    private transient OccupationEndpoint occupationEndpoint;

    private transient MapModel mapModel = new DefaultMapModel();

    public ProjectDetailController() {
        super(Pages.searchOccupation());
    }

    @Override
    public Project loadEntity(long id) {
        Response response = occupationEndpoint.findById(id);
        Project project = response.getStatus() == 200 ? (Project) response.getEntity() : null;
        if (project != null) {
            project.getLocations().stream().map(l -> new Marker(new LatLng(l.getLatitude(), l.getLongitude())))
                    .forEach(mapModel::addOverlay);
        }

        return project;
    }

    public String getLocationOrDefault() {
        return getEntity().getLocations() != null && !getEntity().getLocations().isEmpty() ?
                getEntity().getLocations().iterator().next().toString() : Location.REALDOLMEN_HEADQUARTERS.toString();
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    @Transactional
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    public void addMarker(PointSelectEvent psev) {
        if (psev == null || psev.getLatLng() == null) {
            return;
        }

        LatLng latLng = psev.getLatLng();
        Marker marker = new Marker(latLng);
        Location location = new Location(latLng.getLat(), latLng.getLng());
        getEntity().getLocations().add(location);
        em.persist(location);
        em.merge(getEntity());
        getMapModel().addOverlay(marker);
    }

    public void saveProject() throws IOException {
        Response response = getOccupationEndpoint().update(getEntity().getId(), getEntity());
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            redirect(Pages.occupationDetailsFrom(getEntity()));
        } else {
            getToastService().newToast(getLanguage().getString("occupation.name_taken"));
        }
    }

    public OccupationEndpoint getOccupationEndpoint() {
        return occupationEndpoint;
    }
}
