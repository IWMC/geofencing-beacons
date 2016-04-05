package com.realdolmen.jsf.occupations;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.primefaces.event.map.OverlaySelectEvent;
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

        Project.initialize(project);
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
        setEntity(em.merge(getEntity()));
        marker.setClickable(true);
        getMapModel().addOverlay(marker);
    }

    @Transactional
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    public void removeMarker(OverlaySelectEvent e) {
        if (e.getOverlay() instanceof Marker) {
            Marker marker = (Marker) e.getOverlay();
            Location location = new Location(marker.getLatlng().getLat(), marker.getLatlng().getLng());
            Project project = em.merge(getEntity());
            project.getLocations().removeIf(l -> l.getLatitude() == location.getLatitude() && l.getLongitude() == location.getLongitude());
            setEntity(em.merge(project));
            getMapModel().getMarkers().remove(marker);
        }
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

    /**
     * Remove a subproject from the subproject list from the current project.
     *
     * @param subproject the subproject that should be removed
     */
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    @Transactional
    public void unlinkSubproject(Project subproject) {
        getEntity().getSubProjects().remove(subproject);
        setEntity(em.merge(getEntity()));
    }

    /**
     * Remove an employee from the list of employees from the current project.
     *
     * @param employee the employee that should be removed
     */
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    @Transactional
    public void unlinkEmployee(Employee employee) {
        employee = em.merge(employee);
        getEntity().getEmployees().remove(employee);
        employee.getMemberProjects().remove(getEntity());
        setEntity(em.merge(getEntity()));
    }

    @Override
    public void setEntity(Project entity) {
        Project.initialize(entity);
        super.setEntity(entity);
    }
}
