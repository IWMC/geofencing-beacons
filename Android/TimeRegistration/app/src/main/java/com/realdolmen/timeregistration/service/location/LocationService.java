package com.realdolmen.timeregistration.service.location;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

public class LocationService extends IntentService {

	private static final String LOG_TAG = LocationService.class.getSimpleName();


	public static final String STARTUP_ACTION = "com.realdolmen.location.service.STARTUP";
	public static final String SHUTDOWN_ACTION = "com.realdolmen.location.service.SHUTDOWN";

	/**
	 * The interval to poll for location updates in seconds
	 */
	public static int POLL_INTERVAL = 5 * 1000;

	private LocationManager lm;

	public LocationService(String name) {
		super(name);
		Log.d(LOG_TAG, "Starting service (" + name + ")");
	}

	public LocationService() {
		this("com.realdolmen.location.service");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println(intent);
		doStartup(intent);
		handleGeofencingEvent(GeofencingEvent.fromIntent(intent));
		if (true) return;
		switch (intent.getAction()) {
			case STARTUP_ACTION:
				doStartup(intent);
				return;
			case SHUTDOWN_ACTION:
				doShutdown(intent);
				return;
			default:

		}
	}

	private void handleGeofencingEvent(GeofencingEvent event) {
		if (event.hasError()) {
			return;
		}

		Log.d(LOG_TAG, "Received geofencing event: " + event);
		switch (event.getGeofenceTransition()) {
			case GeofencingRequest.INITIAL_TRIGGER_ENTER:
				Log.i(LOG_TAG, "You have entered " + event.getTriggeringLocation());
				break;
			case GeofencingRequest.INITIAL_TRIGGER_EXIT:
				Log.i(LOG_TAG, "You have exited " + event.getTriggeringLocation());
				break;
		}
	}

	private void doShutdown(Intent intent) {
		if (lm == null)
			return;

		lm.removeGeofences();
		lm.disconnect();
		stopSelf();
	}

	private void doStartup(Intent intent) {
		if (lm != null)
			return;

		lm = LocationManager.get(getApplicationContext());
		lm.requestLocationUpdates();
	}
}
