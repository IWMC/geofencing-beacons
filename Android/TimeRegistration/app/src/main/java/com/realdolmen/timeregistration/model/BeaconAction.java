package com.realdolmen.timeregistration.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.realdolmen.timeregistration.service.location.beacon.BeaconMode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@DatabaseTable
public class BeaconAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(foreign = true)
	private Collection<Occupation> occupations = new HashSet<>();

	@DatabaseField
	private String region;

	@DatabaseField(foreign = true)
	private BeaconMode mode;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Collection<Occupation> getOccupations() {
		return this.occupations;
	}

	public void setOccupations(final Set<Occupation> occupations) {
		this.occupations = occupations;
	}

	public BeaconMode getMode() {
		return mode;
	}

	public void setMode(BeaconMode mode) {
		this.mode = mode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BeaconAction)) {
			return false;
		}
		BeaconAction other = (BeaconAction) obj;
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
		return "BeaconAction{" +
				"id=" + id +
				", occupations=" + occupations +
				", mode=" + mode +
				'}';
	}
}
