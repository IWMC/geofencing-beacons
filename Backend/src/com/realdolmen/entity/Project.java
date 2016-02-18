package com.realdolmen.entity;

import javax.persistence.Entity;
import java.sql.Date;

/**
 * An occupation that contains extra data concerning the project.
 */
@Entity
public class Project extends Occupation {

    private Date startDate;
    private Date endDate;

    public Project() {
    }

    public Project(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
