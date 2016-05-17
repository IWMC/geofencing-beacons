package com.realdolmen.jsf.employees;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.EmployeeEndpoint;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class EmployeeSelectControllerTest {

    @Mock
    private EmployeeEndpoint employeeEndpoint;

    @Mock
    private OccupationEndpoint occupationEndpoint;

    @Mock
    private FacesContext facesContext;

    @Mock
    private EntityManager em;

    @InjectMocks
    private EmployeeSelectController controller = new EmployeeSelectController();

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
        controller.onParentId();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().asLocationRedirect());
    }

    @Test
    public void testControllerSetsOccupationWhenOccupationIdSet() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(occupationEndpoint.findById(project.getId())).thenReturn(Response.ok(project).build());
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.onParentId();
        assertEquals("controller should set the correct active occupation", project, controller.getProject());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRedirectsWhenNoOccupationWithOccupationId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(occupationEndpoint.findById(project.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.onParentId();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchOccupation().asLocationRedirect());
    }

    @Test
    public void testEmployeeFilterFiltersByAlreadyLinkedEmployees() throws Exception {
        Employee employee1 = new Employee();
        employee1.setId(10l);
        Employee employee2 = new Employee();
        employee2.setId(11l);
        project.getEmployees().add(employee1);
        controller.setProject(project);
        List<Employee> filteredList = controller.filterEmployees(Arrays.asList(employee1, employee2));
        assertEquals("filtered list should contain 1 value", 1, filteredList.size());
        assertEquals("filtered list should contain the correct employee", employee2, filteredList.get(0));
    }

    @Test
    public void testAddEmployeeToProjectShouldAddEmployeeToProject() throws Exception {
        Employee employee = new Employee();
        employee.setId(10l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));

        controller.addEmployeeToParent(employee);
        assertEquals("project should contain 1 employee", 1, project.getEmployees().size());
        assertEquals("project should contain the correct employee", employee, project.getEmployees().iterator().next());
    }

    @Test
    public void testAddEmployeeToProjectMergesProject() throws Exception {
        Employee employee = new Employee();
        employee.setId(10l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.addEmployeeToParent(employee);
        verify(em, atLeastOnce()).merge(project);
    }

    @Test
    public void testAddEmployeeToProjectMergesEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setId(10l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        controller.addEmployeeToParent(employee);
        verify(em, atLeastOnce()).merge(employee);
    }

    @Test
    public void testAddEmployeeToProjectRedirectsToProjectEdit() throws Exception {
        Employee employee = new Employee();
        employee.setId(10l);
        controller.setProject(project);
        controller.setOccupationId(String.valueOf(project.getId()));
        String response = controller.addEmployeeToParent(employee);
        assertEquals("response should be the correct redirection string",
                Pages.editProject().param("id", String.valueOf(project.getId())).asRedirect(), response);
    }
}
