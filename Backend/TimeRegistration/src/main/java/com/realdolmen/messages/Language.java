package com.realdolmen.messages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Been used to use the selected locale in JSF and possible other systems like JAX-RS resources.
 */
@Named
@SessionScoped
public class Language implements Serializable {

    private Locale defaultLocale;
    private ResourceBundle languageBundle;

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

    public String getString(String key, Object... params) {
        String value = getLanguageBundle().getString(key);
        for (int i = 0; i < params.length; i++) {
            value = value.replaceAll("\\{" + i + "\\}", params[i].toString());
        }

        return value;
    }

    public ResourceBundle getLanguageBundle() {
        return FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");
    }

    public static final class Text {

        public static final String EMPLOYEE_EDIT_PASSWORD_INVALID = "employee.edit.password.invalid";
        public static final String EMPLOYEE_EDIT_PASSWORD_SAVED = "employee.edit.password_saved";
        public static final String OCCUPATION_ADD_NAME_TAKEN = "occupation.name_taken";
        public static final String PROJECT_DATE_OUT_OF_BOUNDS = "project.date.bounds";
        public static final String SELECT_SUBPROJECT_ADDED = "project.select_subproject.added";
    }
}