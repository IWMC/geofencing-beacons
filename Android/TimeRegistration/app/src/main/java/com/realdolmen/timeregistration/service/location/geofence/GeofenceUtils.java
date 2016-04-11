package com.realdolmen.timeregistration.service.location.geofence;


import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

public class GeofenceUtils {
	public static final boolean DEV_MODE = true;
	public static final int CONNECTION_FAILED_RESOLUTION_REQUEST = 9000;
	public static final String RECEIVE_GEOFENCE_REQUEST = "com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE";
	public static final String LOCATION_SERVICES_CATEGORY = "com.realdolmen.location.CATEGORY";

	public static final int POLL_INTERVAL = DEV_MODE ? 5000 : 10 * 60 * 1000;

	static GeofencingRequest createGeofencingRequest(@NonNull List<Geofence> geofences) {
		if (geofences == null)
			throw new IllegalArgumentException("Geofences list cannot be null");
		return new GeofencingRequest.Builder()
				.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
				.addGeofences(geofences).build();
	}

	/**
	 * Creates a geofence from a given id, location and radius.
	 * <p/>
	 * Transition types of the geofence are {@link Geofence#GEOFENCE_TRANSITION_ENTER} and
	 * {@link Geofence#GEOFENCE_TRANSITION_EXIT}. The expiration duration is set to
	 * {@link Geofence#NEVER_EXPIRE}.
	 *
	 * @param id       The geofence request id
	 * @param location Represents longitude and latitude
	 * @param radius   Radius in meters
	 * @return
	 */
	public static Geofence createGeofence(long id, @NonNull Location location, float radius) {
		if (location == null)
			throw new IllegalArgumentException("Location cannot be null");
		return new Geofence.Builder()
				.setCircularRegion(location.getLatitude(), location.getLongitude(), radius)
				.setRequestId(id + "/" + location.toString())
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.build();
	}
}
