package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.EmployeeEndpoint;
import com.realdolmen.rest.UserEndpoint;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.material.application.ToastService;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * A controller for <code>/employees/employee-add.xhtml</code>.
 */
@ViewScoped
@Named("employeeAdd")
public class EmployeeAddController implements Serializable {

    private String employeeType = EmployeeController.EMPLOYEE_TYPE;

    @Inject
    private transient UserEndpoint userEndpoint;

    @Inject
    private transient EmployeeEndpoint employeeEndpoint;

    @Inject
    private transient Language language;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    private Employee employee = new Employee();

    private transient FacesContext facesContext = FacesContext.getCurrentInstance();
    private transient ToastService toastService = ToastService.getInstance();

    private String password;
    private String passwordRepeat;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ToastService getToastService() {
        return toastService;
    }

    public FacesContext getFacesContext() {
        if (facesContext.isReleased()) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    public void saveUser() throws Exception {
        if (password != null && !password.isEmpty() && !password.equals(passwordRepeat)) {
            getToastService().newToast(language.getLanguageBundle().getString(Language.Text.EMPLOYEE_EDIT_PASSWORD_INVALID), 5000);
            return;
        }

        employee.setPassword(password);
        Response response = userEndpoint.register(employee);
        if (employeeType.equals(EmployeeController.PROJECT_MANAGER_TYPE) && !(employee instanceof ProjectManager)) {
            employeeEndpoint.upgradeProjectManager(employee.getId());
        } else if (employeeType.equals(EmployeeController.MANAGEMENT_EMPLOYEE_TYPE) && !(employee instanceof ManagementEmployee)) {
            employeeEndpoint.upgradeManagementEmployee(employee.getId());
        }

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.searchEmployee().asRedirect());
        }
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
