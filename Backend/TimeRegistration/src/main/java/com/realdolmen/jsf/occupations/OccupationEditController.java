package com.realdolmen.jsf.occupations;

import com.realdolmen.jsf.Pages;

import javax.faces.view.ViewScoped;
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

    public String saveOccupation() throws IOException {
        Response response = getOccupationEndpoint().update(getEntity().getId(), getEntity());
         if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            return Pages.occupationDetailsFrom(getEntity()).asRedirect();
        } else {
            getToastService().newToast(getLanguage().getString("occupation.name_taken"), 3000);
            return "";
        }
    }
}