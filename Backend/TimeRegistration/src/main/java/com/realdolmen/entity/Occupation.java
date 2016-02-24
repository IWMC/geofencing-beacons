package com.realdolmen.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;

/**
 * Entity to be used in the ORM to store data about an occupation. An occupation
 * can be anything the user can do for a limited period. Occupations can be
 * divided into two classes: {@link Project}s and {@link GenericOccupation}s.
 */
@MappedSuperclass
public class Occupation {

    @Column
    @NotNull(message = "name")
    private String name;

    @Column
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        String result = getClass().getSimpleName() + " ";
        if (name != null && !name.trim().isEmpty())
            result += "name: " + name;
        if (description != null && !description.trim().isEmpty())
            result += ", description: " + description;
        return result;
    }
}