package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * An entity class that represents a single time registration about an {@link Occupation} for a single {@link Employee}.
 */
@Entity
@XmlRootElement
@NamedQueries(
        @NamedQuery(name = "RegisteredOccupation.findOccupationsInRange", query = "SELECT r FROM RegisteredOccupation r WHERE r.registeredStart >= :start AND r.registeredEnd <= :end AND r.registrar.id = :employeeId")
)
public class RegisteredOccupation {

    @ManyToOne
    private Occupation occupation;

    private Date registeredStart;

    private Date registeredEnd;

    @JsonIgnore
    @ManyToOne
    private Employee registrar;

    @Id
    private long id;

    public long getId() {
        return id;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public Date getRegisteredStart() {
        return registeredStart;
    }

    public Date getRegisteredEnd() {
        return registeredEnd;
    }

    public Employee getRegistrar() {
        return registrar;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public void setRegisteredStart(Date registeredStart) {
        this.registeredStart = registeredStart;
    }

    public void setRegisteredEnd(Date registeredEnd) {
        this.registeredEnd = registeredEnd;
    }

    public void setRegistrar(Employee registrar) {
        this.registrar = registrar;
    }

    public void setId(long id) {
        this.id = id;
    }
}