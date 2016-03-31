package com.realdolmen.timeregistration.service.location;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

public class GeofencingService extends IntentService {

	private static final String LOG_TAG = GeofencingService.class.getSimpleName();


	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public GeofencingService(String name) {
		super(name);
	}

	public GeofencingService() {
		this("com.realdolmen.geofencing");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent event = GeofencingEvent.fromIntent(intent);
		if (event.hasError()) {
			Log.e(LOG_TAG, "Geofencing error! -> " + event.getErrorCode());
		}

		switch (event.getGeofenceTransition()) {
			case GeofencingRequest.INITIAL_TRIGGER_ENTER:
				Log.i(LOG_TAG, "You have entered " + event.getTriggeringLocation());
				break;
			case GeofencingRequest.INITIAL_TRIGGER_EXIT:
				Log.i(LOG_TAG, "You have exited " + event.getTriggeringLocation());
				break;
		}
	}
}
