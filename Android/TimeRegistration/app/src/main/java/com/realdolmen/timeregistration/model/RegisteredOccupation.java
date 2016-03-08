package com.realdolmen.timeregistration.model;

import java.util.Date;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
public class RegisteredOccupation {

    private Occupation occupation;

    private Date registeredStart;

    private Date registeredEnd;

    private long id;

    public long getId() {
        return id;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public Date getRegisteredStart() {
        return registeredStart;
    }

    public void setRegisteredStart(Date registeredStart) {
        this.registeredStart = registeredStart;
    }

    public Date getRegisteredEnd() {
        return registeredEnd;
    }

    public void setRegisteredEnd(Date registeredEnd) {
        this.registeredEnd = registeredEnd;
    }

    public void setId(long id) {
        this.id = id;
    }
}
