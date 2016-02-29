package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.rest.UserEndpoint;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

/**
 * A helper class accessed by JSF that is used as a controller (MVC) for <code>login.xhtml</code>.
 */
@Named
@RequestScoped
public class Login implements Serializable {

    @Inject
    private UserEndpoint endpoint;

    private String username = "";
    private String password = "";

    private UIComponent loginButton;

    public void doLogin() {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(password);
        boolean succeeded = false;

        try {
            succeeded = endpoint.login(employee).getStatus() == 200;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (succeeded) {

            } else {
                FacesContext.getCurrentInstance().addMessage(loginButton.getClientId(),
                        new FacesMessage("Verkeerd gebruikersnaam of wachtwoord"));
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

    public UIComponent getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(UIComponent loginButton) {
        this.loginButton = loginButton;
    }
}
