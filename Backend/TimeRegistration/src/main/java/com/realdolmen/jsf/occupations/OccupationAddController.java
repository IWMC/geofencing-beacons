package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.OccupationEndpoint;
import org.jetbrains.annotations.TestOnly;
import org.primefaces.material.application.ToastService;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/occupations/occupation-add.xhtml</code>.
 */
@Named("occupationAdd")
@RequestScoped
public class OccupationAddController {

    private Occupation occupation = new Occupation();

    @Inject
    private OccupationEndpoint endpoint;

    @Inject
    private Language language;

    private FacesContext facesContext = FacesContext.getCurrentInstance();
    private ToastService toastService;

    public OccupationAddController() {
    }

    public void saveOccupation() throws IOException {
        Response response = endpoint.addOccupation(occupation);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.searchOccupation().redirect());
        } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            getToastService().newToast(language.getString(Language.Text.OCCUPATION_ADD_NAME_TAKEN));
        }
    }

    public ToastService getToastService() {
        if (toastService == null) {
            toastService = ToastService.getInstance();
        }

        return toastService;
    }

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

    @TestOnly
    public void setToastService(ToastService toastService) {
        this.toastService = toastService;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }
}
