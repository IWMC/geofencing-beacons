package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Entity to be used in the ORM to store data about an occupation. An occupation
 * can be anything the user can do for a limited period.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(name = "Occupation.findOnlyOccupations", query = "SELECT o FROM Occupation o WHERE TYPE(o) IN (Occupation) ORDER BY o.name"),
        @NamedQuery(name = "Occupation.findAll", query = "SELECT o FROM Occupation o WHERE TYPE(o) IN (Occupation, Project)"),
        @NamedQuery(name = "Occupation.removeById", query = "DELETE FROM Occupation o WHERE o.id = :id"),
//        @NamedQuery(name = "Occupation.findTasksByEmployee", query = "SELECT o FROM Occupation o INNER JOIN Task t ON t.project.id = o.id " +
//                "WHERE :employeeId IN (SELECT td.id from t.employees AS td)")
})
@Indexed
@XmlRootElement
public class Occupation implements Serializable, Initializable {

    /**
     * Initializes all lazy properties and collections of the entity recursively. Expects to be invoked while still running
     * in a session.
     *
     * @param occupation the occupation that should be initialized
     */
    public static void initialize(Occupation occupation) {
        if (occupation instanceof Project) {
            Project.initialize((Project) occupation);
        }

        if (occupation instanceof Task) {
            Hibernate.initialize(((Task) occupation).getEmployees());
            Project.initialize(((Task) occupation).getProject());
        }
    }

    @Min(0)
    private double estimatedHours;

    @Column
    @NotNull(message = "name.empty")
    @Size(min = 1, message = "name.empty")
    @Field
    private String name;

    @Version
    @Column(name = "version")
    private int version;

    @Column(length = 10000)
    @Field
    private String description;

    public Occupation() {
    }

    public Occupation(String name, String description) {
        this.description = description;
        this.name = name;
    }

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

    @Transient
    @JsonProperty("DTYPE")
    private final int DTYPE = 1;

    @Override
    public String toString() {
        String result = getClass().getSimpleName() + " ";
        if (name != null && !name.trim().isEmpty())
            result += "name: " + name;
        if (description != null && !description.trim().isEmpty())
            result += ", description: " + description;
        return result;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Occupation that = (Occupation) o;

        if (version != that.version) return false;
        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;
        return description == null && that.description == null || description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Id
    @DocumentId
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

    @Override
    public void initialize() {
        Occupation.initialize(this);
    }
}