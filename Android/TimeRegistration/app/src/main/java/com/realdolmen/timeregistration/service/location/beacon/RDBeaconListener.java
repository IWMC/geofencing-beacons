package com.realdolmen.timeregistration.service.location.beacon;

import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.service.repository.Repositories;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class RDBeaconListener extends BeaconListener {

	private BeaconEventHandler eventHandler;

	public RDBeaconListener(BeaconEventHandler eventHandler) {
		super("com.realdolmen.timeregistration.beacon.listener");
		this.eventHandler = eventHandler;
	}

	@Override
	public void didEnterRegion(Region region) {
		BeaconAction action = Repositories.beaconRepository().getByRegion(region);
		if (action != null && eventHandler != null)
			eventHandler.registerEvent(new BeaconEvent(BeaconEvent.BeaconEventType.ENTER, action, region));
	}

	@Override
	public void didExitRegion(Region region) {
		BeaconAction action = Repositories.beaconRepository().getByRegion(region);
		if (action != null && eventHandler != null)
			eventHandler.registerEvent(new BeaconEvent(BeaconEvent.BeaconEventType.EXIT, action, region));
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
		try {
			BeaconAction action = Repositories.beaconRepository().getByRegion(region);
			if (action != null && eventHandler != null)
				eventHandler.registerEvent(new RangeBeaconEvent(BeaconEvent.BeaconEventType.RANGE, action, region, collection));
		} catch (IllegalStateException ise) {
			//Sometimes happens when the beacon library accidentally calls this method one last time while everything is already shut down
		}
	}
}
