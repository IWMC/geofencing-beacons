package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.rest.UserEndpoint;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

/**
 * A controller for <code>login.xhtml</code>.
 */
@Named
@RequestScoped
public class LoginController implements Serializable {

    @Inject
    private UserEndpoint endpoint;

    @Inject
    private Session session;

    private String username = "";
    private String password = "";

    private FacesContext context = FacesContext.getCurrentInstance();

    public String doLogin() {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(password);
        Response response = null;

        try {
            response = endpoint.loginLocal(employee);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (response != null && response.getStatus() == 200) {
                session.setEmployee((Employee) response.getEntity());
                return Pages.index().asRedirect();
            } else {
                String messageText = "Verkeerde gebruikersnaam of wachtwoord";
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageText, messageText);
                context.addMessage(null, message);
                return "";
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
