package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.EmployeeEndpoint;
import org.jetbrains.annotations.TestOnly;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.material.application.ToastService;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;

/**
 * A controller for <code>/employees/employee-edit.xhtml</code>.
 */
@ViewScoped
@Named("employeeEdit")
public class EmployeeEditController implements Serializable {

    private String userId;

    private String employeeType;

    @Inject
    private transient EmployeeEndpoint employeeEndpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    private Employee employee;

    private transient FacesContext facesContext = FacesContext.getCurrentInstance();

    private String password;
    private String passwordRepeat;

    @Transactional
    public void onPreRender() throws IOException {
        userId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().getOrDefault("userId", userId);

        try {
            if (userId != null) {
                long id = Long.parseLong(userId);
                Response response = employeeEndpoint.findById(id);
                employee = response.getStatus() == 200 ? (Employee) response.getEntity() : null;
                em.detach(employee);
                if (employee != null) {
                    if (employee instanceof ProjectManager) {
                        employeeType = "2";
                    } else if (employee instanceof ManagementEmployee) {
                        employeeType = "3";
                    } else {
                        employeeType = "1";
                    }

                    return;
                }
            }

            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchEmployee().noRedirect());
        } catch (NumberFormatException nfex) {
            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchEmployee().noRedirect());
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

    public void saveUser() throws IOException {
        Response response = employeeEndpoint.update(employee.getId(), employee);
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            ToastService.getInstance().newToast("Werknemer opgeslagen", 5000);
            FacesContext.getCurrentInstance().getExternalContext().redirect(Pages.searchEmployee().redirect());
        }
    }

    public void changePassword() {

    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}