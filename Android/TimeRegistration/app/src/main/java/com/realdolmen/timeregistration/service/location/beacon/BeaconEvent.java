package com.realdolmen.timeregistration.service.location.beacon;

import com.realdolmen.timeregistration.model.BeaconAction;

import org.altbeacon.beacon.Region;

import java.io.Serializable;

public class BeaconEvent implements Serializable {

	public enum BeaconEventType {
		ENTER, EXIT, RANGE
	}

	private int ageTreshold = 0;
	private BeaconEventType type;
	private BeaconAction action;
	private Region region;

	public BeaconEvent(BeaconEventType type, BeaconAction action, Region region) {
		this.type = type;
		this.action = action;
		this.region = region;
	}

	public Region getRegion() {
		return region;
	}

	/**
	 * @return age treshold in seconds
	 */
	public int getAgeTreshold() {
		return ageTreshold;
	}

	public BeaconEventType getType() {
		return type;
	}

	public BeaconAction getAction() {
		return action;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BeaconEvent that = (BeaconEvent) o;

		if (type != that.type) return false;
		return action.equals(that.action);

	}

	public void setAgeTreshold(int ageTreshold) {
		this.ageTreshold = ageTreshold;
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + action.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "BeaconEvent{" +
				"ageTreshold=" + ageTreshold +
				", type=" + type +
				", action=" + action +
				'}';
	}
}
