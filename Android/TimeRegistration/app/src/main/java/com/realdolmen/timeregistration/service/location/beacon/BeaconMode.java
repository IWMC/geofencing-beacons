package com.realdolmen.timeregistration.service.location.beacon;

import java.io.Serializable;

public class BeaconMode implements Serializable {

	private boolean rangeMode;
	private double meters;

	public BeaconMode() {
	}

	public BeaconMode(boolean rangeMode) {
		this.rangeMode = rangeMode;
	}

	public BeaconMode(boolean rangeMode, double meters) {
		this.meters = meters;
		this.rangeMode = rangeMode;
	}

	public boolean isRangeMode() {
		return rangeMode;
	}

	public void setRangeMode(boolean rangeMode) {
		this.rangeMode = rangeMode;
	}

	public double getMeters() {
		return meters;
	}

	public void setMeters(double meters) {
		this.meters = meters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BeaconMode)) return false;

		BeaconMode that = (BeaconMode) o;

		if (rangeMode != that.rangeMode) return false;
		return Double.compare(that.meters, meters) == 0;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = (rangeMode ? 1 : 0);
		temp = Double.doubleToLongBits(meters);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "BeaconMode{" +
				"rangeMode=" + rangeMode +
				", meters=" + meters +
				'}';
	}
}
