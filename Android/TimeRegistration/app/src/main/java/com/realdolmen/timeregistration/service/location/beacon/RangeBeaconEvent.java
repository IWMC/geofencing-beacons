package com.realdolmen.timeregistration.service.location.beacon;

import com.realdolmen.timeregistration.model.BeaconAction;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RangeBeaconEvent extends BeaconEvent {

	private List<Beacon> beacons;

	public RangeBeaconEvent(BeaconEventType type, BeaconAction action, Region region, Collection<Beacon> beacons) {
		super(type, action, region);
		this.beacons = new ArrayList<>(beacons);
	}

	public List<Beacon> getBeacons() {
		return beacons;
	}

	@Override
	public String toString() {
		return "RangeBeaconEvent{" +
				"beacons=" + beacons +
				", " + super.toString() +
				'}';
	}
}
