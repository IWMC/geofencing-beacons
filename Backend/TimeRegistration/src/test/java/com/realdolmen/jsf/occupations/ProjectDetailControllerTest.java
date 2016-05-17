package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.*;
import com.realdolmen.jsf.ControllerTest;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.service.SecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.primefaces.event.map.GeocodeEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.GeocodeResult;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ProjectDetailControllerTest extends ControllerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private MapModel model;

    @Mock
    private OccupationEndpoint endpoint;

    @Mock
    private SecurityManager sm;

    @InjectMocks
    private ProjectDetailController controller = new ProjectDetailController();

    private Language language;

    private UserContext userContext;

    private Project project = new Project("Occupation name", "Occupation description", 15, new Date(), new Date());

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userContext = mock(UserContext.class);
        language = mock(Language.class);
        initForController(controller);
        project.setId(15l);
        controller.setEntity(project);
        controller.setLanguage(language);
        controller.setUserContext(userContext);
    }

    @Test
    public void testLoadReturnsEntityWhenExistingProject() throws Exception {
        when(endpoint.findById(project.getId())).thenReturn(Response.ok(project).build());
        assertEquals(project, controller.loadEntity(project.getId()));
    }

    @Test
    public void testLoadReturnsNullWhenNoProject() throws Exception {
        when(endpoint.findById(project.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        assertNull(controller.loadEntity(project.getId()));
    }

    @Test
    public void testGetLocationOrDefaultReturnsFirstLocationFromProject() throws Exception {
        Location location = new Location(10, 10);
        project.getLocations().add(location);
        controller.setEntity(project);
        assertEquals(location.toString(), controller.getLocationOrDefault());
    }

    @Test
    public void testGetLocationOrDefaultReturnsSearchTermIfNotNull() throws Exception {
        Location location = new Location(10, 10);
        controller.setSearchLocation(location);
        controller.setEntity(project);
        assertEquals(location.toString(), controller.getLocationOrDefault());
    }

    @Test
    public void testGetLocationOrDefaultReturnsDefaultLocationWhenNoProjectLocations() throws Exception {
        project.getLocations().clear();
        controller.setEntity(project);
        assertEquals(Location.REALDOLMEN_HEADQUARTERS.toString(), controller.getLocationOrDefault());
    }

    @Test
    public void testUnlinkSubprojectUnlinksSubprojectIfPresent() throws Exception {
        Project subproject = new Project("Subproject", "Description of subproject", 4, new Date(), new Date());
        project.getSubProjects().add(subproject);
        controller.setEntity(project);
        controller.unlinkSubproject(subproject);
        assertEquals("subproject list should be empty", 0, project.getSubProjects().size());
        verify(entityManager, atLeastOnce()).merge(project);
    }

    @Test
    public void testUnlinkSubprojectIgnoresRequestIfNotPresent() throws Exception {
        Project subproject = new Project("Subproject", "Description of subproject", 4, new Date(), new Date());
        controller.setEntity(project);
        controller.unlinkSubproject(subproject);
        assertEquals("subproject list should be empty", 0, project.getSubProjects().size());
        verify(entityManager, never()).merge(project);
    }

    @Test
    public void testUnlinkEmployeeUnlinksEmployeeIfPresent() throws Exception {
        Employee employee = new Employee(10L, 1, "First", "Last", "Username", "Email", "hash", "salt", "password", new HashSet<>());
        project.getEmployees().add(employee);
        employee.getMemberProjects().add(project);
        controller.setEntity(project);
        when(entityManager.merge(employee)).thenReturn(employee);
        controller.unlinkEmployee(employee);

        assertEquals("employee list should be empty", 0, project.getEmployees().size());
        assertEquals("project list should be empty", 0, employee.getMemberProjects().size());
        verify(entityManager, atLeastOnce()).merge(project);
        verify(entityManager, atLeastOnce()).merge(employee);
    }

    @Test
    public void testUnlinkEmployeeIgnoresRequestIfNotPresent() throws Exception {
        Employee employee = new Employee(10L, 1, "First", "Last", "Username", "Email", "hash", "salt", "password", new HashSet<>());
        controller.setEntity(project);
        when(entityManager.merge(employee)).thenReturn(employee);
        controller.unlinkEmployee(employee);

        verify(entityManager, never()).merge(project);
    }

    @Test
    public void testGeocodeLookupSetsSearchLocationIfEventResults() throws Exception {
        GeocodeResult hqResult = new GeocodeResult("A. Vaucampslaan, Huizingen",
                new LatLng(Location.REALDOLMEN_HEADQUARTERS.getLatitude(), Location.REALDOLMEN_HEADQUARTERS.getLongitude()));
        GeocodeEvent geocodeEvent = mock(GeocodeEvent.class);
        when(geocodeEvent.getResults()).thenReturn(Arrays.asList(hqResult));

        controller.geocodeLookup(geocodeEvent);
        assertEquals(Location.REALDOLMEN_HEADQUARTERS, controller.getSearchLocation());
    }

    @Test
    public void testGeocodeLookupIgnoresEventIfNoEventResults() throws Exception {
        GeocodeEvent event = mock(GeocodeEvent.class);
        when(event.getResults()).thenReturn(new ArrayList<>());
        controller.geocodeLookup(event);

        assertNull(controller.getSearchLocation());
        controller.setSearchLocation(Location.REALDOLMEN_HEADQUARTERS);
        controller.geocodeLookup(event);
        assertEquals(Location.REALDOLMEN_HEADQUARTERS, controller.getSearchLocation());
    }

    @Test
    public void testAddMarkerAddsMarkerToExistingProject() throws Exception {
        PointSelectEvent event = mock(PointSelectEvent.class);
        when(event.getLatLng()).thenReturn(new LatLng(Location.REALDOLMEN_HEADQUARTERS.getLatitude(),
                Location.REALDOLMEN_HEADQUARTERS.getLongitude()));
        controller.addMarker(event);
        assertEquals("project should contain 1 location", 1, project.getLocations().size());
        assertEquals("project should contain the correct location", Location.REALDOLMEN_HEADQUARTERS,
                project.getLocations().iterator().next());
        verify(entityManager, times(1)).persist(Location.REALDOLMEN_HEADQUARTERS);
        verify(entityManager, atLeastOnce()).merge(project);
    }

    @Test
    public void testAddMarkerAddsMarkerAsOverlayToMap() throws Exception {
        PointSelectEvent event = mock(PointSelectEvent.class);
        when(event.getLatLng()).thenReturn(new LatLng(Location.REALDOLMEN_HEADQUARTERS.getLatitude(),
                Location.REALDOLMEN_HEADQUARTERS.getLongitude()));
        controller.addMarker(event);
        verify(model, times(1)).addOverlay(new Marker(event.getLatLng()));
    }

    @Test
    public void testRemoveMarkerRemovesMarkerFromExistingProject() throws Exception {
        when(entityManager.merge(project)).thenReturn(project);
        OverlaySelectEvent event = mock(OverlaySelectEvent.class);
        project.getLocations().add(Location.REALDOLMEN_HEADQUARTERS);
        Marker marker = mock(Marker.class);
        when(marker.getLatlng()).thenReturn(new LatLng(Location.REALDOLMEN_HEADQUARTERS.getLatitude(),
                Location.REALDOLMEN_HEADQUARTERS.getLongitude()));
        when(event.getOverlay()).thenReturn(marker);
        controller.removeMarker(event);
        assertEquals("project should contain 0 locations", 0, project.getLocations().size());
        verify(entityManager, atLeastOnce()).merge(project);
    }

    @Test
    public void testRemoveMarkerRemovesMarkerOverlayFromMap() throws Exception {
        when(entityManager.merge(project)).thenReturn(project);
        OverlaySelectEvent event = mock(OverlaySelectEvent.class);
        project.getLocations().add(Location.REALDOLMEN_HEADQUARTERS);
        Marker marker = mock(Marker.class);
        when(marker.getLatlng()).thenReturn(new LatLng(Location.REALDOLMEN_HEADQUARTERS.getLatitude(),
                Location.REALDOLMEN_HEADQUARTERS.getLongitude()));
        when(event.getOverlay()).thenReturn(marker);
        List<Marker> markers = new ArrayList<>(Arrays.asList(marker));
        when(model.getMarkers()).thenReturn(markers);
        controller.removeMarker(event);
        assertEquals(0, markers.size());
    }

    @Test
    public void testSaveProjectUsesOccupationEndpoint() throws Exception {
        when(endpoint.update(project.getId(), project)).thenReturn(Response.noContent().build());
        controller.saveProject();
        verify(endpoint, atLeastOnce()).update(project.getId(), project);
    }

    @Test
    public void testSaveProjectRedirectsToDetailOnSuccess() throws Exception {
        when(endpoint.update(project.getId(), project)).thenReturn(Response.noContent().build());
        controller.saveProject();
        verify(getExternalContext(), atLeastOnce()).redirect(Pages.occupationDetailsFrom(project).asLocationRedirect());
    }

    @Test
    public void testSaveProjectShowsToastMessageOnFailure() throws Exception {
        String uuid = UUID.randomUUID().toString();
        when(endpoint.update(project.getId(), project)).thenReturn(Response.status(Response.Status.CONFLICT).build());
        when(getLanguage().getString("occupation.name_taken")).thenReturn(uuid);
        controller.saveProject();
        verify(getExternalContext(), never()).redirect(Pages.occupationDetailsFrom(project).asLocationRedirect());
        verify(getToastService(), times(1)).newToast(eq(uuid));
    }

    @Test
    public void testRemoveOccupationDelegatesToEndpoint() throws Exception {
        Project project = new Project();
        project.setId(505L);
        controller.setEntity(project);
        controller.removeProject();
        Mockito.verify(endpoint).removeOccupation(project.getId());
    }

    @Test
    public void testRemoveOccupationRedirectsToOccupationSearch() throws Exception {
        Project project = new Project();
        project.setId(505L);
        controller.setEntity(project);
        controller.removeProject();
        Mockito.verify(getExternalContext()).redirect(Pages.searchOccupation().asLocationRedirect());
    }

    @Test
    public void testShouldShowEditOptionReturnsFalseIfRegularEmployee() throws Exception {
        when(sm.isManagementEmployee()).thenReturn(false);
        when(sm.isProjectManager()).thenReturn(false);
        when(sm.isManagement()).thenReturn(false);
        assertFalse(controller.getShouldShowEditOption());
    }

    @Test
    public void testShouldShowEditOptionReturnsTrueIfManagementEmployee() throws Exception {
        when(sm.isManagementEmployee()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(false);
        when(sm.isManagement()).thenReturn(true);
        assertTrue(controller.getShouldShowEditOption());
    }

    @Test
    public void testShouldShowEditOptionReturnsFalseIfNotManagingProjectManager() throws Exception {
        when(sm.isManagementEmployee()).thenReturn(false);
        when(sm.isProjectManager()).thenReturn(true);
        when(sm.isManagement()).thenReturn(true);
        assertFalse(controller.getShouldShowEditOption());
        when(sm.findEmployee()).thenReturn(new ProjectManager());
        assertFalse(controller.getShouldShowEditOption());
    }

    @Test
    public void testShouldShowEditOptionReturnsTrueIfManagingProjectManager() throws Exception {
        when(sm.isManagementEmployee()).thenReturn(false);
        when(sm.isProjectManager()).thenReturn(true);
        when(sm.isManagement()).thenReturn(true);
        ProjectManager manager = new ProjectManager();
        when(sm.findEmployee()).thenReturn(manager);
        when(userContext.getUser()).thenReturn(manager);
        controller.getEntity().getEmployees().add(manager);
        assertTrue(controller.getShouldShowEditOption());
    }

    public Language getLanguage() {
        return language;
    }

    @Test
    public void testGetEstimatedHoursShowsSimplifiedViewWhenRoundedNumber() throws Exception {
        Task task = new Task();
        task.setEstimatedHours(7);
        final String text = UUID.randomUUID().toString();
        when(language.getString("project.task.hours", 7)).thenReturn(text);
        String result = controller.getEstimatedHours(task);
        assertEquals("result should be retrieved with the correct language bundle key", text, result);
    }

    @Test
    public void testGetEstimatedHoursShowsHoursAndMinutesWhenUnroundedNumber() throws Exception {
        Task task = new Task();
        task.setEstimatedHours(7.5);
        final String text = UUID.randomUUID().toString();
        when(language.getString("project.task.hours_minutes", 7, 30)).thenReturn(text);
        String result = controller.getEstimatedHours(task);
        assertEquals("result should be retrieved with the correct language bundle key", text, result);
    }
}
