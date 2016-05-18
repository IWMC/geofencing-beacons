package com.realdolmen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
                query = "SELECT r FROM RegisteredOccupation r WHERE YEAR(r.startDate) = :year AND DAY(r.endDate) = :day AND MONTH(r.startDate) = :month AND r.employee.id = :employeeId ORDER BY r.startDate")
        , @NamedQuery(name = "RegisteredOccupation.findOccupationByIdAndUser", query = "SELECT ro FROM RegisteredOccupation ro WHERE ro.employee.id = :userId AND ro.id = :regId")
})
public class RegisteredOccupation implements Initializable {

    @ManyToOne
    @NotNull(message = "occupation.empty")
    private Occupation occupation;

    @NotNull(message = "startDate.empty")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("startDate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("endDate")
    private Date endDate;

    public static void initialize(RegisteredOccupation o) {
        Occupation.initialize(o.occupation);
    }

    @ManyToOne
    @JsonIgnore
    private Employee employee;

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
        return startDate;
    }

    public Date getRegisteredEnd() {
        return endDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public void setRegisteredStart(Date startDate) {
        this.startDate = startDate;
    }

    public void setRegisteredEnd(Date endDate) {
        this.endDate = endDate;
    }

    public void setEmployee(Employee registrar) {
        this.employee = registrar;
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

    @Override
    public void initialize() {
        RegisteredOccupation.initialize(this);
    }
}
