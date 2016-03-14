package com.realdolmen.messages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 * Been used to use the selected locale in JSF and possible other systems like JAX-RS resources.
 */
@Named
@SessionScoped
public class Language implements Serializable {


    private Locale defaultLocale;

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale localeCode) {
        this.defaultLocale = localeCode;
    }

    @PostConstruct
    public void init() {
        defaultLocale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale();
    }
}