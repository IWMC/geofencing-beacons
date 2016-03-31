package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Location;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class ProjectDetailsControllerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private FacesContext facesContext;

    @Mock
    private MapModel model;

    private Project project = new Project("Occupation name", "Occupation description", 15, new Date(), new Date());

    @Mock
    private OccupationEndpoint endpoint = new OccupationEndpoint();

    @InjectMocks
    private ProjectDetailsController controller = new ProjectDetailsController();

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        project.setId(15l);
        controller.setFacesContext(facesContext);
    }

    // TODO: 31/03/2016 Move tests to separate test class

    @Test
    public void testControllerRedirectsWhenNoOccupationId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().noRedirect());
    }

    @Test
    public void testControllerSetsOccupationWhenOccupationIdSet() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(endpoint.findById(project.getId())).thenReturn(Response.ok(project).build());
        controller.setId(String.valueOf(project.getId()));
        controller.onPreRender();
        assertEquals("controller should set the correct active project", project, controller.getEntity());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRedirectsWhenNoOccupationWithOccupationId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(endpoint.findById(project.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        controller.setId(String.valueOf(project.getId()));
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().noRedirect());
    }


    @Test
    public void testGetLocationOrDefaultReturnsFirstLocationFromProject() throws Exception {
        Location location = new Location(10, 10);
        project.getLocations().add(location);
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
    public void testAddMarkerPersistsNewLocation() throws Exception {
        Location location = new Location(10, 10);
        PointSelectEvent event = new PointSelectEvent(mock(UIComponent.class), mock(Behavior.class),
                new LatLng(location.getLatitude(), location.getLongitude()));
        controller.setEntity(project);
        controller.addMarker(event);
        verify(entityManager, times(1)).persist(location);
        verify(entityManager, times(1)).merge(project);
        assertEquals("project should contain a new location", 1, project.getLocations().size());
        assertEquals("project should contain the right location", location, project.getLocations().iterator().next());
    }

    @Test
    public void testAddMarkerAddsMarkerToModel() throws Exception {
        Location location = new Location(10, 10);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        PointSelectEvent event = new PointSelectEvent(mock(UIComponent.class), mock(Behavior.class), latLng);
        controller.setEntity(project);
        controller.addMarker(event);
        verify(model, times(1)).addOverlay(new Marker(latLng));
    }
}
