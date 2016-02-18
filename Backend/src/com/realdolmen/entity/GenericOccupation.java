package com.realdolmen.entity;

import javax.persistence.Entity;

/**
 * An occupation that an Employee can register, yet it is not a project. For example:
 * <ul>
 *     <li>Eating</li>
 *     <li>Off work</li>
 *     <li>...</li>
 * </ul>
 */
@Entity
public class GenericOccupation extends Occupation {
}
