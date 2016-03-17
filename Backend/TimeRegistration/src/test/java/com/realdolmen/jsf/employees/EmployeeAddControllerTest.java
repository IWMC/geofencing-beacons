package com.realdolmen.jsf.employees;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.EmployeeEndpoint;
import com.realdolmen.rest.UserEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.primefaces.material.application.ToastService;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Arquillian.class)
public class EmployeeAddControllerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ToastService toastService;

    @Mock
    private UserEndpoint userEndpoint;

    @Mock
    private EmployeeEndpoint employeeEndpoint;

    @Mock
    private Language language;

    @InjectMocks
    private EmployeeAddController controller = new EmployeeAddController();

    @Mock
    private ExternalContext externalContext;

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    private Employee validEmployee = new Employee(10L, 0, "First name", "Last name", "Username", "employeeaddcontrollertest@realdolmen.com"
            , "1234567", "1234567", "Bla123", new HashSet<>());

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(facesContext.isReleased()).thenReturn(false);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(userEndpoint.register(any())).thenReturn(Response.created(UriBuilder.fromResource(EmployeeEndpoint.class)
                .path(String.valueOf(validEmployee.getId())).build()).build());
        when(employeeEndpoint.downgradeEmployee(validEmployee.getId())).thenReturn(Response.noContent().build());
        when(employeeEndpoint.upgradeManagementEmployee(validEmployee.getId())).thenReturn(Response.noContent().build());
        when(employeeEndpoint.upgradeProjectManager(validEmployee.getId())).thenReturn(Response.noContent().build());
        ResourceBundle bundle = new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return "";
            }

            @Override
            public @NotNull Enumeration<String> getKeys() {
                return new Vector<String>().elements();
            }
        };
        when(language.getLanguageBundle()).thenReturn(bundle);
    }

    @Test
    public void testSaveUserUsesEndpoint() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        verify(userEndpoint, atLeastOnce()).register(validEmployee);
    }

    @Test
    public void testSaveSetsPasswordWhenValid() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        validEmployee.setPassword("");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        assertEquals("password should be set in employee", "Bla123", validEmployee.getPassword());
    }

    @Test
    public void testSaveUserRedirectsToSearchPage() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        verify(externalContext, atLeastOnce()).redirect(Pages.searchEmployee().redirect());
    }

    @Test
    public void testSaveUserUpgradesEmployeeToProjectManager() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        controller.setEmployee(validEmployee);
        controller.setEmployeeType(EmployeeController.PROJECT_MANAGER_TYPE);
        controller.saveUser();

        verify(employeeEndpoint, never()).upgradeManagementEmployee(validEmployee.getId());
        verify(employeeEndpoint, atLeastOnce()).upgradeProjectManager(validEmployee.getId());
    }

    @Test
    public void testSaveUserUpgradesEmployeeToManagementEmployee() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        controller.setEmployee(validEmployee);
        controller.setEmployeeType(EmployeeController.MANAGEMENT_EMPLOYEE_TYPE);
        controller.saveUser();

        verify(employeeEndpoint, atLeastOnce()).upgradeManagementEmployee(validEmployee.getId());
        verify(employeeEndpoint, never()).upgradeProjectManager(validEmployee.getId());
    }

    @Test
    public void testSaveUserDoesNotUpgradeOnEmployeeType() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla123");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        verify(employeeEndpoint, never()).upgradeProjectManager(validEmployee.getId());
        verify(employeeEndpoint, never()).upgradeManagementEmployee(validEmployee.getId());
    }

    @Test
    public void testSaveUserChecksPasswordAndPasswordRepeat() throws Exception {
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla1234");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        verify(userEndpoint, never()).register(any());
    }

    @Test
    public void testSaveUserDoesNotRedirectOnInvalidEndpointResult() throws Exception {
        when(userEndpoint.register(validEmployee)).thenReturn(Response.status(Response.Status.BAD_REQUEST).build());
        controller.setPassword("Bla123");
        controller.setPasswordRepeat("Bla1234");
        controller.setEmployee(validEmployee);
        controller.saveUser();

        verify(externalContext, never()).redirect(anyString());
    }
}
