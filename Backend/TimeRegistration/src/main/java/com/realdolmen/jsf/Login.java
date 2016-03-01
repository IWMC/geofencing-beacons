package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.rest.UserEndpoint;
import org.primefaces.material.application.ToastService;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    public String doLogin() {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(password);
        Response jwt = null;

        try {
            jwt = endpoint.login(employee);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (jwt != null && jwt.getStatus() == 200) {
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
                HttpSession httpSession = request.getSession(false);
                httpSession.setAttribute(AuthorizationFilter.JWT_KEY, jwt.getEntity());
                return Pages.index().redirect();
            } else {
                String messageText = "Verkeerde gebruikersnaam of wachtwoord";
                ToastService.getInstance().newToast(messageText);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageText, messageText);
                FacesContext.getCurrentInstance().addMessage(null, message);
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

    public UIComponent getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(UIComponent loginButton) {
        this.loginButton = loginButton;
    }
}
