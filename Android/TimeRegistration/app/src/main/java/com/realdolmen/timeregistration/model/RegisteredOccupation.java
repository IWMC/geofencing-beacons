package com.realdolmen.timeregistration.model;

import org.joda.time.DateTime;

/**
 * An entity class that represents a single time registration about an {@link Occupation} for a single Employee.
 */
public class RegisteredOccupation {

	private Occupation occupation;

	private DateTime registeredStart;

	private DateTime registeredEnd;

	private boolean confirmed = false;

	private long id;

	public long getId() {
		return id;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public DateTime getRegisteredStart() {
		return registeredStart;
	}

	public DateTime getRegisteredEnd() {
		return registeredEnd;
	}

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public void setRegisteredStart(DateTime registeredStart) {
		this.registeredStart = registeredStart;
	}

	public void setRegisteredEnd(DateTime registeredEnd) {
		this.registeredEnd = registeredEnd;
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
