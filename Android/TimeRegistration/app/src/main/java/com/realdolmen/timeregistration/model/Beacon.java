package com.realdolmen.timeregistration.model;

import com.j256.ormlite.field.DatabaseField;
import com.realdolmen.timeregistration.service.location.beacon.BeaconMode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Beacon implements Serializable {

	private static final long serialVersionUID = 1L;

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField
	private Set<Occupation> occupation = new HashSet<>();

	private BeaconMode mode;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Set<Occupation> getOccupation() {
		return this.occupation;
	}

	public void setOccupation(final Set<Occupation> occupation) {
		this.occupation = occupation;
	}

	public BeaconMode getMode() {
		return mode;
	}

	public void setMode(BeaconMode mode) {
		this.mode = mode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Beacon)) {
			return false;
		}
		Beacon other = (Beacon) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Beacon{" +
				"id=" + id +
				", occupation=" + occupation +
				", mode=" + mode +
				'}';
	}
}
