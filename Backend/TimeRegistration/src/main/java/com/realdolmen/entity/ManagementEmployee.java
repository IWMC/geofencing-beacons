package com.realdolmen.entity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Entity for a management employee. It does not hold additional properties by himself, yet it is important to keep this
 * as a separate entity since user rights are different and the differentiation between employees happens by inheritance.
 */
@NamedQueries({
        @NamedQuery(name = "ManagementEmployee.findAll", query = "SELECT me FROM ManagementEmployee me")
})
@Entity
public class ManagementEmployee extends Employee {
}
