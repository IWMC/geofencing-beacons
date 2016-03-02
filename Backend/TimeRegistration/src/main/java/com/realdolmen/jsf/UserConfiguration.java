package com.realdolmen.jsf;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 * Web Bean containing client preferences.
 */
@ManagedBean(name = "userConfiguration", eager = true)
@SessionScoped
public class UserConfiguration implements Serializable {

    private String locale = "nl";

    public UserConfiguration() {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(locale));
    }
}
