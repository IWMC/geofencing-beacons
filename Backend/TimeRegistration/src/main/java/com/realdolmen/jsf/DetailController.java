package com.realdolmen.jsf;

import javax.faces.bean.ManagedProperty;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Serializable;

/**
 * A common class used by every controller that uses query parameters to retrieve an entity.
 */
public abstract class DetailController<E> extends Controller implements Serializable {

    @ManagedProperty(value = "#{param.id}")
    private String id;

    private E entity;

    private Pages.Page errorPage;

    /**
     * The only constructor requiring an error page if something would go wrong.
     *
     * @param errorPage     the page the system should redirect to if the id is not given as a query parameter.
     */
    public DetailController(Pages.Page errorPage) {
        this.errorPage = errorPage;
    }

    public void redirectToErrorPage() throws IOException {
        // Is it due to redirection to another page?
        if (!getFacesContext().getExternalContext().isResponseCommitted()) {
            getFacesContext().getExternalContext().redirect(errorPage.noRedirect());
        }
    }

    @Transactional
    public void onPreRender() throws IOException {
        if (entity != null) {
            return;
        }

        if (id == null || id.equals(0)) {
            redirectToErrorPage();
            return;
        }

        long id = 0;

        try {
            id = Long.parseLong(getId());
        } catch (NumberFormatException nfex) {
            redirectToErrorPage();
            return;
        }

        entity = loadEntity(id);
        if (entity == null) {
            redirectToErrorPage();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public E getEntity() {
        if (entity == null) {
            try {
                onPreRender();
            } catch (IOException ex) {
            }
        }

        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    /**
     * The strategy to convert the id to an entity. This method will therefore be used when the id is set by JSF.
     *
     * @param id the id of the entity that should be loaded
     * @return the entity, or null if something goes wrong
     */
    public abstract E loadEntity(long id);
}
