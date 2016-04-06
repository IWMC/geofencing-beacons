package com.realdolmen.jsf.employees;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.jsf.Pages;
import com.realdolmen.jsf.Session;
import com.realdolmen.rest.EmployeeEndpoint;
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
import org.primefaces.material.application.ToastService;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class EmployeeEditControllerTest {

    @Mock
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    @Mock
    private ToastService toastService;

    @Mock
    private FacesContext facesContext;

    private Employee employee = new Employee();

    @Inject
    private Session session;

    @Mock
    private EmployeeEndpoint endpoint = new EmployeeEndpoint();

    @InjectMocks
    private EmployeeEditController controller = new EmployeeEditController();

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        employee.setUsername("Employee");
        employee.setFirstName("First");
        employee.setLastName("Last");
        employee.setEmail("email@realdolmen.com");
        employee.setId(1l);
        utx.begin();
        em.persist(employee);
        utx.commit();
        controller.setFacesContext(facesContext);
        session.setEmployee(new ManagementEmployee());
        when(facesContext.isReleased()).thenReturn(false);
    }

    @Test
    public void testControllerUpdatesUser() throws Exception {
        controller.setUserId(employee.getId().toString());
        controller.setEmployee(employee);
        when(endpoint.update(employee.getId(), employee)).thenReturn(Response.noContent().build());
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        controller.saveUser();
        verify(endpoint, times(1)).update(employee.getId(), employee);
    }

    @Test
    public void testControllerRedirectsOnUserUpdate() throws Exception {
        controller.setUserId(employee.getId().toString());
        controller.setEmployee(employee);
        when(endpoint.update(employee.getId(), employee)).thenReturn(Response.noContent().build());
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        controller.saveUser();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchEmployee().asRedirect());
        verify(endpoint, times(1)).update(employee.getId(), employee);
    }

    @Test
    public void testControllerRedirectsWhenNoUserId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        utx.begin();
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchEmployee().asLocationRedirect());
        utx.commit();
    }

    @Test
    public void testControllerSetsEmployeeWhenUserIdSet() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        controller.setUserId(employee.getId().toString());
        when(endpoint.findById(employee.getId())).thenReturn(Response.ok(employee).build());
        controller.onPreRender();
        Assert.assertEquals("controller should set the correct active employee", employee, controller.getEmployee());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRemovesEmployeeAndRedirectsWhenEmployeeIsPresent() throws Exception {
        controller.setUserId(employee.getId().toString());
        controller.setEmployee(employee);
        String result = controller.removeUser();
        verify(endpoint, times(1)).deleteById(employee.getId());
        Assert.assertEquals("controller should redirect to search employees page", Pages.searchEmployee().asRedirect(), result);
    }

    @Test
    public void testControllerRedirectsWhenEmployeeIsNotPresent() throws Exception {
        controller.setUserId(null);
        String result = controller.removeUser();
        verify(endpoint, never()).deleteById(anyLong());
        Assert.assertEquals("controller should redirect to search employees page", Pages.searchEmployee().asRedirect(), result);
    }
}