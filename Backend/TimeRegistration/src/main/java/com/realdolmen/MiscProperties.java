package com.realdolmen;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contains a ResourceBundle, retrieved by finding {@code misc.properties}. This class can then be used to access all
 * common configurable properties.
 */
@Stateless
public class MiscProperties {

    private ResourceBundle bundle;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("misc");
    }

    public String getString(String key, Object... parameters) {
        String value = bundle.getString(key);

        for (int i = 0; i < parameters.length; i++) {
            value = value.replaceAll("\\{" + i + "\\}", parameters[i].toString());
        }

        return value;
    }

    public String getString(String key, Map<String, String> replacements) {
        String value = bundle.getString(key);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace("{" + entry.getValue() + "}", entry.getValue());
        }

        return value;
    }

    public boolean hasKey(String key) {
        return bundle.containsKey(key);
    }

    public ResourceBundle getBundle() {
        return bundle;
    }
}
