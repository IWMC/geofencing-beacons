package com.realdolmen.timeregistration.service.location.beacon;

import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.service.repository.Repositories;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class RDBeaconListener extends BeaconListener {

	private BeaconDwellManager dwellManager;

	public RDBeaconListener(BeaconDwellManager dwellManager) {
		super("rdbeaconlistener");
		this.dwellManager = dwellManager;
	}

	@Override
	public void didEnterRegion(Region region) {
		BeaconAction action = Repositories.beaconRepository().getByRegion(region);
		if (action != null && dwellManager != null)
			dwellManager.registerEvent(new BeaconEvent(BeaconEvent.BeaconEventType.ENTER, action, region));
	}

	@Override
	public void didExitRegion(Region region) {
		BeaconAction action = Repositories.beaconRepository().getByRegion(region);
		if (action != null && dwellManager != null)
			dwellManager.registerEvent(new BeaconEvent(BeaconEvent.BeaconEventType.EXIT, action, region));
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
		BeaconAction action = Repositories.beaconRepository().getByRegion(region);
		if (action != null && dwellManager != null)
			dwellManager.registerEvent(new RangeBeaconEvent(BeaconEvent.BeaconEventType.RANGE, action, region, collection));
	}
}
