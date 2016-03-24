package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * An entity class that represents a single time registration about an {@link Occupation} for a single {@link Employee}.
 */
@Entity
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "RegisteredOccupation.findOccupationsInRange",
                query = "SELECT r FROM RegisteredOccupation r WHERE YEAR(r.registeredStart) = :year AND DAY(r.registeredStart) = :day AND MONTH(r.registeredStart) = :month AND r.registrar.id = :employeeId ORDER BY r.registeredStart")
        , @NamedQuery(name = "RegisteredOccupation.findOccupationByIdAndUser", query = "SELECT ro FROM RegisteredOccupation ro WHERE ro.registrar.id = :userId AND ro.id = :regId")
})
public class RegisteredOccupation {

    @ManyToOne
    @NotNull(message = "occupation.empty")
    private Occupation occupation;

    @NotNull(message = "registeredStart.empty")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredStart;

    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredEnd;

    public static void initialize(RegisteredOccupation o) {
        Occupation.initialize(o.occupation);
    }

    @JsonIgnore
    @ManyToOne
    private Employee registrar;

    private boolean confirmed = false;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public void confirm() {
        confirmed = true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

}
