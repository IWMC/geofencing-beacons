package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class OccupationDetailsControllerTest {

    @Mock
    private FacesContext facesContext;

    private Occupation occupation = new Occupation("Occupation name", "Occupation description");

    @Mock
    private OccupationEndpoint endpoint = new OccupationEndpoint();

    @InjectMocks
    private OccupationDetailsController controller = new OccupationDetailsController();

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        occupation.setId(15l);
        controller.setFacesContext(facesContext);
    }

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
        when(endpoint.findById(occupation.getId())).thenReturn(Response.ok(occupation).build());
        controller.setOccupationId(String.valueOf(occupation.getId()));
        controller.onPreRender();
        Assert.assertEquals("controller should set the correct active occupation", occupation, controller.getOccupation());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRedirectsWhenNoOccupationWithOccupationId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(endpoint.findById(occupation.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        controller.setOccupationId(String.valueOf(occupation.getId()));
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().noRedirect());
    }
}
