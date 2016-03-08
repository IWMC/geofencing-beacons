package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
@Entity
@XmlRootElement
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
