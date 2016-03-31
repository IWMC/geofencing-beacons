package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Location;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * A controller for <code>/occupations/project-details.xhtml</code>.
 */
@Named("projectDetails")
@ViewScoped
public class ProjectDetailsController extends DetailController<Project> implements Serializable {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private transient OccupationEndpoint occupationEndpoint;

    private transient MapModel mapModel = new DefaultMapModel();

    public ProjectDetailsController() {
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

    @Transactional
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
        mapModel.addOverlay(marker);
    }

    public MapModel getMapModel() {
        return mapModel;
    }
}
