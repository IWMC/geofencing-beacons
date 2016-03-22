package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jetbrains.annotations.TestOnly;

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

    private FacesContext facesContext = FacesContext.getCurrentInstance();

    public OccupationAddController() {
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

    public void saveOccupation() throws IOException {
        Response response = endpoint.addOccupation(occupation);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.searchOccupation().redirect());
        }
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }
}
