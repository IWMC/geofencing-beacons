package com.realdolmen.timeregistration.model;

		import java.util.Date;

/**
 * An entity class that represents a single time registration about an {@link Occupation} for a single Employee.
 */
public class RegisteredOccupation {

	private Occupation occupation;

	private Date registeredStart;

	private Date registeredEnd;

	private boolean confirmed = false;

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

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public void setRegisteredStart(Date registeredStart) {
		this.registeredStart = registeredStart;
	}

	public void setRegisteredEnd(Date registeredEnd) {
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
