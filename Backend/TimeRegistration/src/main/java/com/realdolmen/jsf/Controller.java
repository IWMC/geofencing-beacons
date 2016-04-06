package com.realdolmen.jsf;

import com.realdolmen.messages.Language;
import org.jetbrains.annotations.TestOnly;
import org.primefaces.material.application.ToastService;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

/**
 * Convenience class containing a {@link javax.faces.context.FacesContext} and a
 * {@link org.primefaces.material.application.ToastService} which is commonly used throughout the website.
 * Its API also allows for an easy injection point for testing.
 */
public class Controller implements Serializable {

    private transient FacesContext facesContext;
    private transient ToastService toastService;

    @Inject
    private transient Language language;

    public FacesContext getFacesContext() {
        if (facesContext == null || facesContext.isReleased()) {
            facesContext = FacesContext.getCurrentInstance();
        }

        return facesContext;
    }

    @TestOnly
    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    public ToastService getToastService() {
        if (toastService == null) {
            toastService = ToastService.getInstance();
        }

        return toastService;
    }

    @TestOnly
    public void setToastService(ToastService toastService) {
        this.toastService = toastService;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void redirect(Pages.Page page) throws IOException {
        getFacesContext().getExternalContext().redirect(page.asLocationRedirect());
    }
}
