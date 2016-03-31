package com.realdolmen.jsf.occupations;

import com.realdolmen.jsf.Pages;
import org.omnifaces.cdi.ViewScoped;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;

/**
 * A controller for <code>/occupations/occupation-edit.xhtml</code>.
 */
@Named("occupationEdit")
@ViewScoped
public class OccupationEditController extends OccupationDetailController implements Serializable {

    public void saveOccupation() throws IOException {
        Response response = getOccupationEndpoint().update(getEntity().getId(), getEntity());
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            getFacesContext().getExternalContext().redirect(Pages.occupationDetailsFrom(getEntity()).noRedirect());
        } else {
            getToastService().newToast(getLanguage().getString("occupation.name_taken"), 3000);
        }
    }

}