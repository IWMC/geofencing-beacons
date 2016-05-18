package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.DetailController;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/occupations/occupation-details.xhtml</code>.
 */
@Named("occupationDetails")
@ViewScoped
public class OccupationDetailController extends DetailController<Occupation> {

    @Inject
    private transient OccupationEndpoint occupationEndpoint;

    public OccupationDetailController() {
        super(Pages.searchOccupation());
    }

    @Override
    public Occupation loadEntity(long id) {
        Response response = occupationEndpoint.findById(id);
        return response.getStatus() == 200 ? (Occupation) response.getEntity() : null;
    }

    public OccupationEndpoint getOccupationEndpoint() {
        return occupationEndpoint;
    }

    public String removeOccupation() throws IOException {
        getOccupationEndpoint().removeOccupation(getEntity().getId());
        return redirect(Pages.searchOccupation());
    }
}
