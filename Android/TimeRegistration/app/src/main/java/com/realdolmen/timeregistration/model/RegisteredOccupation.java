package com.realdolmen.timeregistration.model;

import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;

import org.joda.time.DateTime;

/**
 * An entity class that represents a single time registration about an {@link Occupation} for a single Employee.
 */
public class RegisteredOccupation {

	private Occupation occupation;

	@UTC
	private DateTime registeredStart;

	@UTC
	private DateTime registeredEnd;

	private boolean confirmed = false;

	private long id;

	public long getId() {
		return id;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public @UTC DateTime getRegisteredStart() {
		DateUtil.enforceUTC(registeredStart);
		return registeredStart;
	}

	public @UTC DateTime getRegisteredEnd() {
		DateUtil.enforceUTC(registeredEnd);
		return registeredEnd;
	}

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public void setRegisteredStart(@UTC DateTime registeredStart) {
		DateUtil.enforceUTC(registeredStart);
		this.registeredStart = registeredStart;
	}

	public void setRegisteredEnd(@UTC DateTime registeredEnd) {
		DateUtil.enforceUTC(registeredEnd);
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
