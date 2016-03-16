package com.realdolmen.jsf;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;

/**
 * Util class used to contain simple JSF logic that should not be managed by controllers.
 */
@Named
@RequestScoped
public class Util {

    private FacesContext context = FacesContext.getCurrentInstance();

    public <E> E getOrDefault(List<E> list, int index, E defaultValue) {
        if (list.size() > index) {
            return list.get(index);
        }

        return defaultValue;
    }

    public String getMessageForClient(String clientId) {
        List<FacesMessage> messages = context.getMessageList(clientId);
        if (messages.isEmpty()) {
            return "";
        } else {
            return messages.get(0).getSummary();
        }
    }
}
