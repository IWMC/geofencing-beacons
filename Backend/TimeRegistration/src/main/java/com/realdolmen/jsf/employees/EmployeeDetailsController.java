package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.EmployeeEndpoint;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/employees/employee-details.xhtml</code>.
 */
@RequestScoped
@Named("employeeDetails")
public class EmployeeDetailsController {

    @ManagedProperty(value = "#{param.userId}")
    private String userId;

    @Inject
    private EmployeeEndpoint employeeEndpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private Employee employee = new Employee();

    private FacesContext facesContext = FacesContext.getCurrentInstance();

    @Transactional
    public void onPreRender() {
        try {
            if (userId != null) {
                long id = Long.parseLong(userId);
                Response response = employeeEndpoint.findById(id);
                employee = response.getStatus() == 200 ? (Employee) response.getEntity() : null;
                if (employee != null) {
                    return;
                }
            }

            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchEmployee().noRedirect());
        } catch (NumberFormatException nfex) {
            try {
                (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                        .getExternalContext().redirect(Pages.searchEmployee().noRedirect());
            } catch (IOException e) {
                Logger.getLogger(EmployeeDetailsController.class).error("couldn't redirect with FacesContext", e);
            }
        } catch (IOException e) {
            Logger.getLogger(EmployeeDetailsController.class).error("couldn't redirect with FacesContext", e);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @TestOnly
    public void setFacesContext(FacesContext context) {
        this.facesContext = context;
    }

    @Transactional
    public String removeUser() {
        if (employee != null) {
            if (userId != null) {
                try {
                    employeeEndpoint.deleteById(Long.parseLong(userId));
                } catch (NumberFormatException nfex) {
                }
            }
        }

        return Pages.searchEmployee().redirect();
    }
}