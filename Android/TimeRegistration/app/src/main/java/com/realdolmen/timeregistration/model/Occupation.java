package com.realdolmen.timeregistration.model;

import android.support.annotation.Nullable;

/**
 * Created by BCCAZ45 on 3/03/2016.
 */
public class Occupation {
    private String name;
    private String description = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Occupation(String name, String description) {
        this.name = name;
        setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        if (description == null) {
            description = "";
            return;
        }
        this.description = description;
    }
}
