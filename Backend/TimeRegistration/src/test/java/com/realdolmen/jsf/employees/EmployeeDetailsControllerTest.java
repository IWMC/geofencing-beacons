package com.realdolmen.jsf.employees;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
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
import org.mockito.Spy;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import static org.mockito.Mockito.*;


@RunWith(Arquillian.class)
public class EmployeeDetailsControllerTest {
    
    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private EmployeeDetailsController controller;
    
    @Inject
    private UserTransaction utx;

    @Spy
    private FacesContext facesContext;
    
    private Employee employee = new Employee();

    @Inject
    private Session session;

    @Mock
    private EmployeeEndpoint endpoint = new EmployeeEndpoint();

    @InjectMocks
    private EmployeeDetailsController notInjectedController = new EmployeeDetailsController();

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
        utx.begin();
        em.persist(employee);
        utx.commit();
        controller.setFacesContext(facesContext);
        session.setEmployee(new ManagementEmployee());
    }

    @Test
    public void testControllerRedirectsWhenNoUserId() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        utx.begin();
        controller.onPreRender();
        verify(externalContext, atLeastOnce()).redirect(Pages.searchEmployee().noRedirect());
        utx.commit();
    }

    @Test
    public void testControllerSetsEmployeeWhenUserIdSet() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        controller.setUserId(employee.getId().toString());
        controller.onPreRender();
        Assert.assertEquals("controller should set the correct active employee", employee, controller.getEmployee());
        verify(externalContext, never()).redirect(any());
    }

    @Test
    public void testControllerRemovesEmployeeAndRedirectsWhenEmployeeIsPresent() throws Exception {
        notInjectedController.setUserId(employee.getId().toString());
        String result = notInjectedController.removeUser();
        verify(endpoint, times(1)).deleteById(employee.getId());
        Assert.assertEquals("controller should redirect to search employees page", Pages.searchEmployee().redirect(), result);
    }

    @Test
    public void testControllerRedirectsWhenEmployeeIsNotPresent() throws Exception {
        notInjectedController.setUserId(null);
        String result = notInjectedController.removeUser();
        verify(endpoint, never()).deleteById(anyLong());
        Assert.assertEquals("controller should redirect to search employees page", Pages.searchEmployee().redirect(), result);
    }
}
