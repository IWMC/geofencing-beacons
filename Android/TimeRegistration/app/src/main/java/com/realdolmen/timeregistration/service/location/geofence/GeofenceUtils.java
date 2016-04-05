package com.realdolmen.timeregistration.service.location.geofence;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

public class GeofenceUtils {
	public static final boolean DEV_MODE = true;
	public static final int CONNECTION_FAILED_RESOLUTION_REQUEST = 9000;
	public static final String RECEIVE_GEOFENCE_REQUEST = "com.aol.android.geofence.ACTION_RECEIVE_GEOFENCE";
	public static final String LOCATION_SERVICES_CATEGORY = "com.realdolmen.location.CATEGORY";

	public static final String GEOFENCE_STORE_SHARED_PREFERENCES = "RealDolmenGeofencesStore";

	public static final int POLL_INTERVAL = DEV_MODE ? 5000 : 15 * 60 * 1000;

	public interface StoreKeys {
		int KEY_PROJECT_ID = 1;
		int KEY_LATITUDE = 2;
		int KEY_LONGITUDE = 3;

	}

	static GeofencingRequest createGeofencingRequest(List<Geofence> geofences) {
		return new GeofencingRequest.Builder()
				.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
				.addGeofences(geofences).build();
	}
}
