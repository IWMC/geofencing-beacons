package com.realdolmen.jsf;

import org.jetbrains.annotations.Nullable;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public <E> List<E> setToList(@Nullable Set<E> set) {
        if (set == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(set);
        }
    }
}