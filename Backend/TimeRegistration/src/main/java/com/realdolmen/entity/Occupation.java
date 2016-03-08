package com.realdolmen.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Entity to be used in the ORM to store data about an occupation. An occupation
 * can be anything the user can do for a limited period. Occupations can be
 * divided into two classes: {@link Project}s and {@link GenericOccupation}s.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Occupation {

    @Column
    @NotNull(message = "name")
    private String name;

    @Version
    @Column(name = "version")
    private int version;

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

    private transient final int DTYPE = 1;

    @Override
    public String toString() {
        String result = getClass().getSimpleName() + " ";
        if (name != null && !name.trim().isEmpty())
            result += "name: " + name;
        if (description != null && !description.trim().isEmpty())
            result += ", description: " + description;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Occupation)) return false;

        Occupation that = (Occupation) o;

        if (version != that.version) return false;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}