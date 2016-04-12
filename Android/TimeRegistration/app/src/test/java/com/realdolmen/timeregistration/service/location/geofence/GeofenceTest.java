package com.realdolmen.timeregistration.service.location.geofence;

import com.google.android.gms.location.Geofence;

public abstract class GeofenceTest {
	Geofence newGeofence(String id) {
		return new Geofence.Builder()
				.setRequestId(id)
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
				.setCircularRegion(1, 2, 1000)
				.build();
	}
}
