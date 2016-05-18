package com.realdolmen.jsf.occupations;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.service.SecurityManager;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jetbrains.annotations.TestOnly;
import org.primefaces.event.map.GeocodeEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A controller for <code>/occupations/project-details.xhtml</code>.
 */
@Named("projectDetails")
@ViewScoped
public class ProjectDetailController extends DetailController<Project> implements Serializable {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    @Inject
    private TaskDao taskDao;

    @Inject
    private SecurityManager sm;

    @Inject
    private UserContext userContext;

    @Inject
    private transient OccupationEndpoint occupationEndpoint;

    private Location searchLocation;

    private transient MapModel mapModel = new DefaultMapModel();

    private String taskSearchTerms;

    public ProjectDetailController() {
        super(Pages.searchOccupation());
    }

    @Override
    @Transactional
    public Project loadEntity(long id) {
        Response response = occupationEndpoint.findById(id);
        Project project = response.getStatus() == 200 ? (Project) response.getEntity() : null;
        if (project != null) {
            project.getLocations().stream().map(l -> new Marker(new LatLng(l.getLatitude(), l.getLongitude())))
                    .forEach(mapModel::addOverlay);
            project.initialize();
        }

        return project;
    }

    public String getLocationOrDefault() {
        if (searchLocation != null) {
            return searchLocation.toString();
        }

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

    public String saveProject() throws IOException {
        Response response = getOccupationEndpoint().update(getEntity().getId(), getEntity());
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            return redirect(Pages.occupationDetailsFrom(getEntity()));
        } else if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
            return redirectToErrorPage();
        } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            getToastService().newToast(getLanguage().getString("occupation.name_taken"));
        }

        return "";
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
        if (!getEntity().getSubProjects().contains(subproject)) {
            return;
        }

        getEntity().getSubProjects().remove(subproject);
        setEntity(em.merge(getEntity()));
    }

    /**
     * Remove an employee from the list of employees from the current project.
     *
     * @param employee the employee that should be removed
     */
    @Authorized(UserGroup.MANAGEMENT)
    @Transactional
    public void unlinkEmployee(Employee employee) {
        employee = em.merge(employee);
        if (!getEntity().getEmployees().contains(employee)) {
            return;
        }

        getEntity().getEmployees().remove(employee);
        employee.getMemberProjects().remove(getEntity());
        setEntity(em.merge(getEntity()));
    }

    public void geocodeLookup(GeocodeEvent e) {
        if (e.getResults() == null || e.getResults().isEmpty()) {
            return;
        }

        LatLng location = e.getResults().get(0).getLatLng();
        searchLocation = new Location(location.getLat(), location.getLng());
    }

    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    public String removeProject() throws IOException {
        getOccupationEndpoint().removeOccupation(getEntity().getId());
        return redirect(Pages.searchOccupation());
    }

    @Override
    public void setEntity(Project entity) {
        if (entity != null) {
            Project.initialize(entity);
        }

        super.setEntity(entity);
    }

    public Location getSearchLocation() {
        return searchLocation;
    }

    public void setSearchLocation(Location searchLocation) {
        this.searchLocation = searchLocation;
    }

    public boolean getShouldShowEditOption() {
        return sm.isProjectManager() && getEntity().getEmployees().contains(userContext.getUser());
    }

    public boolean getShouldShowTaskEditOption() {
        return sm.isProjectManager() && getEntity().getEmployees().contains(userContext.getUser());
    }

    public String getEstimatedHours(Task task) {
        if (task.getEstimatedHours() == (int) task.getEstimatedHours()) {
            return getLanguage().getString("project.task.hours", (int) task.getEstimatedHours());
        } else {
            return getLanguage().getString("project.task.hours_minutes",
                    (int) task.getEstimatedHours(),
                    (int) ((task.getEstimatedHours() - Math.floor(task.getEstimatedHours())) * 60));
        }
    }

    // TODO: 3/05/2016 Test Lucene
    public List<Task> getTasks() {
        if (taskSearchTerms == null || taskSearchTerms.trim().isEmpty()) {
            return new ArrayList<>(getEntity().getTasks());
        }

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Task.class).get();
        org.apache.lucene.search.Query luceneQuery = qb
                .bool().should(qb.keyword().onFields("employees.firstName", "employees.lastName", "employees.email", "name", "description")
                        .matching(taskSearchTerms).createQuery())
                .createQuery();

        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Task.class);
        return (List<Task>) jpaQuery.getResultList().stream()
                .filter(getEntity().getTasks()::contains)
                .collect(Collectors.toList());
    }

    public String getTaskSearchTerms() {
        return taskSearchTerms;
    }

    public void setTaskSearchTerms(String taskSearchTerms) {
        this.taskSearchTerms = taskSearchTerms;
    }

    @TestOnly
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public String getEmployeeJobFunction(Employee employee) {
        return getLanguage().getString("employee.jobtitle." + employee.getJobFunction());
    }
}