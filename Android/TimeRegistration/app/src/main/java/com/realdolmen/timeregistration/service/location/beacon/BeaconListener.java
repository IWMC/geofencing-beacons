package com.realdolmen.timeregistration.service.location.beacon;

import com.realdolmen.timeregistration.model.Occupation;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class BeaconListener implements RangeNotifier, MonitorNotifier {

	private String id;

	public BeaconListener() {
		id = UUID.randomUUID().toString();
	}

	public BeaconListener(String id) {
		this.id = id;
	}

	@Override
	public void didEnterRegion(Region region) {

	}

	@Override
	public void didExitRegion(Region region) {

	}

	@Override
	public void didDetermineStateForRegion(int i, Region region) {

	}

	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

	}

	public void onEnterOccupation(Occupation occ) {

	}

	public void onExitOccupation(Occupation occ) {

	}

	/**
	 * Default implementation that calls {@link #onEnterOccupation(Occupation)} for every {@link Occupation} in the given list.
	 * @param occupations
	 */
	public void onEnterOccupation(List<Occupation> occupations) {
		for(Occupation occupation : occupations) {
			onEnterOccupation(occupation);
		}
	}

	/**
	 * Default implementation that calls {@link #onExitOccupation(Occupation)} for every {@link Occupation} in the given list.
	 * @param occupations
	 */
	public void onExitOccupation(List<Occupation> occupations) {
		for(Occupation occupation : occupations) {
			onExitOccupation(occupation);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BeaconListener that = (BeaconListener) o;

		return id.equals(that.id);

	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
