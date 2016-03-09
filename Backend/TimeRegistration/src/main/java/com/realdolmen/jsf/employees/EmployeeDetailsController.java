package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.jsf.Pages;
import org.jboss.logging.Logger;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.*;

/**
 * A controller for <code>/employees/employee-details.xhtml</code>.
 */
@RequestScoped
@Named("employeeDetails")
public class EmployeeDetailsController {

    @ManagedProperty(value = "#{param.userId}")
    private String userId;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private Employee employee = new Employee();

    @Transactional
    public void onPreRender() {
        try {
            if (userId != null) {
                long id = Long.parseLong(userId);
                employee = em.find(Employee.class, id);
                Employee.initialize(employee);
            } else {
                FacesContext.getCurrentInstance().getExternalContext().redirect(Pages.searchEmployee().noRedirect());
            }
        } catch (NumberFormatException nfex) {
        } catch (Exception e) {
            Logger.getLogger(EmployeeDetailsController.class).error("couldn't initialize lazy collection from employee", e);
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
}
