package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.OccupationEndpoint;
import com.realdolmen.validation.ValidationResult;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.primefaces.material.application.ToastService;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class OccupationAddControllerTest {

    @Mock
    private Language language;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ToastService toastService;

    @Mock
    private OccupationEndpoint endpoint;

    @InjectMocks
    private OccupationAddController controller = new OccupationAddController();

    @Mock
    private ExternalContext externalContext;

    private Occupation occupation = new Occupation("Occupation name", "Occupation description");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        occupation.setId(123l);
        controller.setOccupation(occupation);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Test
    public void testSaveOccupationCallsEndpoint() throws Exception {
        when(endpoint.addOccupation(any())).thenReturn(Response.created(
                UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(occupation.getId())).build());
        controller.saveOccupation();
        verify(endpoint, times(1)).addOccupation(occupation);
    }

    @Test
    public void testSaveOccupationRedirectsToSearchOccupationOnSuccess() throws Exception {
        when(endpoint.addOccupation(any())).thenReturn(Response.created(
                UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(occupation.getId())).build());
        controller.saveOccupation();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().asRedirect());
    }

    @Test
    public void testSaveOccupationDoesNothingOn400() throws Exception {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        when(endpoint.addOccupation(any())).thenReturn(Response.status(Response.Status.BAD_REQUEST).entity(result).build());
        controller.saveOccupation();
        verify(externalContext, never()).redirect(anyString());
    }

    @Test
    public void testSaveOccupationShowsToastOnNameConflict() throws Exception {
        when(endpoint.addOccupation(any())).thenReturn(Response.status(Response.Status.CONFLICT).build());
        when(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN)).thenReturn("Occupation add name taken message");
        controller.saveOccupation();
        verify(toastService, atLeastOnce()).newToast(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN));
    }
}