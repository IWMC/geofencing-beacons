package com.realdolmen.jsf.occupations;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Occupation;
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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class SubprojectSelectControllerTest {

    @Mock
    private OccupationEndpoint occupationEndpoint;

    @Mock
    private FacesContext facesContext;

    @Mock
    private EntityManager em;

    @InjectMocks
    private SubprojectSelectController controller = new SubprojectSelectController();

    private Project project = new Project("Occupation name", "Occupation description", 15, new Date(), new Date());

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void init() {
        controller.setFacesContext(facesContext);
        MockitoAnnotations.initMocks(this);
        when(occupationEndpoint.findById(project.getId())).thenReturn(Response.ok(project).build());
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
        when(occupationEndpoint.findById(project.getId())).thenReturn(Response.ok(project).build());
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.onPreRender();
        assertEquals("controller should set the correct active occupation", project, controller.getProject());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRedirectsWhenNoOccupationWithOccupationId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(occupationEndpoint.findById(project.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().noRedirect());
    }

    @Test
    public void testOccupationFilterFiltersByAlreadyLinkedSubProjects() throws Exception {
        Project project1 = new Project();
        project1.setId(11l);
        Project project2 = new Project();
        project2.setId(12l);
        project.getSubProjects().add(project1);

        controller.setProject(project);
        List<Occupation> filteredList = controller.filterOccupations(Arrays.asList(project1, project2));
        assertEquals("filtered list should contain 1 value", 1, filteredList.size());
        assertEquals("filtered list should contain the correct occupation", project2, filteredList.get(0));
    }

    @Test
    public void testOccupationFilterFiltersByAlreadyLinkedParentProjects() throws Exception {
        Project project1 = new Project();
        project1.setId(11l);
        Project project2 = new Project();
        project2.setId(12l);
        project.getSubProjects().add(project1);

        controller.setProject(project);
        List<Occupation> filteredList = controller.filterOccupations(Arrays.asList(project1, project2));
        assertEquals("filtered list should contain 1 value", 1, filteredList.size());
        assertEquals("filtered list should contain the correct occupation", project2, filteredList.get(0));
    }

    @Test
    public void testAddProjectToProjectShouldAddProjectToProject() throws Exception {
        Project project1 = new Project();
        project1.setId(11l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.addAsSubProject(project1);

        assertEquals("project should contain 1 subproject", 1, project.getSubProjects().size());
        assertEquals("project should contain the correct subproject", project1, project.getSubProjects().iterator().next());
    }

    @Test
    public void testAddProjectToProjectMergesParentProject() throws Exception {
        Project project1 = new Project();
        project1.setId(11l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.addAsSubProject(project1);

        verify(em, atLeastOnce()).merge(project);
    }

    @Test
    public void testAddProjectToProjectRedirectsToProjectDetails() throws Exception {
        Project project1 = new Project();
        project1.setId(11l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        String response = controller.addAsSubProject(project1);

        assertEquals("response should be the correct redirection string",
                Pages.detailsProject().param("id", String.valueOf(project.getId())).redirect(), response);
    }

}
