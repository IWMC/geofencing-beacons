package com.realdolmen.timeregistration.service.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.service.repository.Repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener {

	private static final String LOG_TAG = LocationManager.class.getSimpleName();


	private GoogleApiClient mApiClient;
	private Context owner;

	private static LocationManager instance;

	private GeofencingApi geofencingApi = LocationServices.GeofencingApi;

	private Map<Project, List<Geofence>> geofenceMapping = new HashMap<>();

	private PendingIntent pIntent;

	private LocationManager(@NonNull Context context) {
		if (context == null)
			throw new IllegalArgumentException("Given fragment activity cannot be null!");

		owner = context;
		mApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	/**
	 * Creates and automatically connects to necessary services.
	 *
	 * @param context The context to be used when creating the lazy instance.
	 * @return Singleton instance
	 */
	public static LocationManager get(Context context) {
		if (instance == null)
			instance = new LocationManager(context.getApplicationContext()).connect();
		return instance;
	}

	public Location lastKnownLocation() {
		if (ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			throw new IllegalStateException("Cannot access location!");
		}
		return LocationServices.FusedLocationApi.getLastLocation(mApiClient);
	}

	public void addGeofences() {
		if (ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			throw new IllegalStateException("Access to fine location is required!");
		}
		Log.i(LOG_TAG, "Adding " + geofencingRequest().getGeofences().size() + " geofences");
		geofencingApi.addGeofences(mApiClient, geofencingRequest(), getPendingIntent());
	}

	private PendingIntent getPendingIntent() {
		if (pIntent != null) {
			return pIntent;
		}

		Intent intent = new Intent(owner, GeofencingService.class);
		return PendingIntent.getService(owner, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private GeofencingRequest geofencingRequest() {
		List<Geofence> geofences = new ArrayList<>();
		for (Project project : Repositories.occupationRepository().getAllProjects()) {
			geofences.addAll(project.getGeofences());
		}
		return new GeofencingRequest.Builder().addGeofences(geofences).setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build();
	}

	public LocationManager connect() {
		if (!mApiClient.isConnected()) {
			mApiClient.connect();
		}
		return this;
	}

	public GoogleApiClient client() {
		return mApiClient;
	}

	public LocationManager disconnect() {
		if (mApiClient.isConnected()) {
			mApiClient.disconnect();
		}
		return this;
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(LOG_TAG, "Connected!");
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(LOG_TAG, "Connection suspended, " + i);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.d(LOG_TAG, "Connection failed, " + connectionResult.toString());
	}

	public static Geofence createGeofence(Location location) {
		return new Geofence.Builder()
				.setCircularRegion(location.getLatitude(), location.getLongitude(), 8000)
				.setRequestId(UUID.randomUUID().toString())
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
						Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
				.setLoiteringDelay(120000)
				.setExpirationDuration(System.currentTimeMillis())
				.setNotificationResponsiveness(0)
				.build();
	}
}
