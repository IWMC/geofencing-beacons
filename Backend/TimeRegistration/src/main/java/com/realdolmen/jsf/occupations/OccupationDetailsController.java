package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.jsf.Pages;
import com.realdolmen.rest.OccupationEndpoint;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.TestOnly;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A controller for <code>/occupations/occupation-details.xhtml</code>.
 */
@Named("occupationDetails")
@RequestScoped
public class OccupationDetailsController {

    @ManagedProperty(value = "#{param.occupationId}")
    private String occupationId;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private Occupation occupation;

    private FacesContext facesContext = FacesContext.getCurrentInstance();

    @Transactional
    public void onPreRender() {
        try {
            if (occupationId != null) {
                long id = Long.parseLong(occupationId);
                Response response = occupationEndpoint.findById(id);
                occupation = response.getStatus() == 200 ? (Occupation) response.getEntity() : null;
                if (occupation != null) {
                    return;
                }
            }

            (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                    .getExternalContext().redirect(Pages.searchOccupation().noRedirect());
        } catch (NumberFormatException nfex) {
            try {
                (facesContext == null ? FacesContext.getCurrentInstance() : facesContext)
                        .getExternalContext().redirect(Pages.searchOccupation().noRedirect());
            } catch (IOException e) {
                Logger.getLogger(OccupationDetailsController.class).error("couldn't redirect with FacesContext", e);
            }
        } catch (IOException e) {
            Logger.getLogger(OccupationDetailsController.class).error("couldn't redirect with FacesContext", e);
        }
    }

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    @TestOnly
    public void setFacesContext(FacesContext context) {
        this.facesContext = context;
    }
}
