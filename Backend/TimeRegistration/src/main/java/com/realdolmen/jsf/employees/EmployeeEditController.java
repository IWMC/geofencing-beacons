package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.jsf.Pages;
import com.realdolmen.json.EmployeePasswordCredentials;
import com.realdolmen.messages.Language;
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
import java.security.NoSuchAlgorithmException;

import static com.realdolmen.jsf.Pages.searchEmployee;

/**
 * A controller for <code>/employees/employee-edit.xhtml</code>.
 */
@Named("employeeEdit")
@ViewScoped
public class EmployeeEditController implements Serializable {

    private String userId;

    private String employeeType = EmployeeController.EMPLOYEE_TYPE;

    @Inject
    private transient EmployeeEndpoint employeeEndpoint;

    @Inject
    private transient Language language;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private transient EntityManager em;

    private Employee employee;

    private transient FacesContext facesContext = FacesContext.getCurrentInstance();

    private transient ToastService toastService = ToastService.getInstance();

    private String password;
    private String passwordRepeat;

    @Transactional
    public String onPreRender() throws IOException {
        userId = getFacesContext().getExternalContext().getRequestParameterMap().getOrDefault("userId", userId);

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
                    } else if (employee.getJobFunction() == null || employee.getJobFunction().isEmpty()){
                        employeeType = "1";
                    } else {
                        employeeType = employee.getJobFunction() == null || employee.getJobFunction().isEmpty() ? "1" : employee.getJobFunction();
                    }

                    return "";
                }
            }

            return searchEmployee().asLocationRedirect();
        } catch (NumberFormatException nfex) {
            return searchEmployee().asLocationRedirect();
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

    public FacesContext getFacesContext() {
        if (facesContext.isReleased()) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    @TestOnly
    public void setFacesContext(FacesContext context) {
        this.facesContext = context;
    }

    @Transactional
    public String removeUser() {
        if (employee != null && userId != null) {
            try {
                employeeEndpoint.deleteById(Long.parseLong(userId));
            } catch (NumberFormatException nfex) {
            }
        }

        return searchEmployee().asRedirect();
    }

    public String saveUser() throws Exception {
//        if (employeeType.equals(EmployeeController.EMPLOYEE_TYPE)) {
//            employee.setJobFunction(language.getString("employee.jobtitle.employee"));
//        } else if (employeeType.equals(EmployeeController.MANAGEMENT_EMPLOYEE_TYPE)) {
//            employee.setJobFunction(language.getString("employee.jobtitle.management"));
//        } else if (employeeType.equals(EmployeeController.PROJECT_MANAGER_TYPE)) {
//            employee.setJobFunction(language.getString("employee.jobtitle.project_manager"));
//        } else {
//            employee.setJobFunction(language.getString("employee.jobtitle." + employeeType));
//        }
        employee.setJobFunction(employeeType);

        Response response = employeeEndpoint.update(employee.getId(), employee);
        if (employeeType.equals(EmployeeController.PROJECT_MANAGER_TYPE) && !(employee instanceof ProjectManager)) {
            employeeEndpoint.upgradeProjectManager(employee.getId());
        } else if (employeeType.equals(EmployeeController.MANAGEMENT_EMPLOYEE_TYPE) && !(employee instanceof ManagementEmployee)) {
            employeeEndpoint.upgradeManagementEmployee(employee.getId());
        } else if (employeeType.equals(EmployeeController.EMPLOYEE_TYPE) && (employee instanceof ProjectManager || employee instanceof ManagementEmployee)) {
            employeeEndpoint.downgradeEmployee(employee.getId());
        }

        return response.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ? Pages.searchEmployee().asRedirect() : "";
    }

    public long getIdAsLong() {
        if (userId == null) {
            return 0;
        } else {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException nfex) {
                return 0;
            }
        }
    }

    public void changePassword() throws NoSuchAlgorithmException {
        Response response = employeeEndpoint.updatePassword(getIdAsLong(),
                new EmployeePasswordCredentials(getPassword(), getPasswordRepeat(), getIdAsLong()));
        String message = language.getLanguageBundle().getString(response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()
                ? Language.Text.EMPLOYEE_EDIT_PASSWORD_INVALID : Language.Text.EMPLOYEE_EDIT_PASSWORD_SAVED);
        ToastService.getInstance().newToast(message, 5000);
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