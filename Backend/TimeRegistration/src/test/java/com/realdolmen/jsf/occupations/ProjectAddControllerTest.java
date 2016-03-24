package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Project;
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
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class ProjectAddControllerTest {

    @Mock
    private Language language;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ToastService toastService;

    @Mock
    private OccupationEndpoint endpoint;

    @InjectMocks
    private ProjectAddController controller = new ProjectAddController();

    @Mock
    private ExternalContext externalContext;

    private Project project = new Project("Occupation name", "Occupation description", 10, new Date(), new Date());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        project.setId(123l);
        controller.setProject(project);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Test
    public void testSaveProjectCallsEndpoint() throws Exception {
        when(endpoint.addProject(any())).thenReturn(Response.created(
                UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(project.getId())).build());
        controller.saveProject();
        verify(endpoint, times(1)).addProject(project);
    }

    @Test
    public void testSaveProjectRedirectsToSearchOccupationOnSuccess() throws Exception {
        when(endpoint.addProject(any())).thenReturn(Response.created(
                UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(project.getId())).build());
        controller.saveProject();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().redirect());
    }

    @Test
    public void testSaveProjectDoesNothingOnGeneric400() throws Exception {
        ValidationResult result = new ValidationResult(false, new ArrayList<>());
        when(endpoint.addProject(any())).thenReturn(Response.status(Response.Status.BAD_REQUEST).entity(result).build());
        controller.saveProject();
        verify(externalContext, never()).redirect(anyString());
    }

    @Test
    public void testSaveProjectShowsToastOnNameConflict() throws Exception {
        when(endpoint.addProject(any())).thenReturn(Response.status(Response.Status.CONFLICT).build());
        when(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN)).thenReturn("Occupation add name taken message");
        controller.saveProject();
        verify(toastService, atLeastOnce()).newToast(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN));
    }

    @Test
    public void testSaveProjectShowsToastOnStartDateAfterEndDate() throws Exception {
        ValidationResult result = new ValidationResult(false, new ArrayList<>());
        result.getInvalidationTokens().add(Language.Text.PROJECT_DATE_OUT_OF_BOUNDS);
        when(endpoint.addProject(any())).thenReturn(Response.status(Response.Status.BAD_REQUEST).entity(result).build());
        controller.saveProject();
        verify(toastService, atLeastOnce()).newToast(language.getString(Language.Text.PROJECT_DATE_OUT_OF_BOUNDS));
    }
}
