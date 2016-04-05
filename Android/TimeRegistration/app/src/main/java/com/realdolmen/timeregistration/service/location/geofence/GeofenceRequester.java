package com.realdolmen.timeregistration.service.location.geofence;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils.CONNECTION_FAILED_RESOLUTION_REQUEST;
import static com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils.POLL_INTERVAL;
import static com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils.RECEIVE_GEOFENCE_REQUEST;
import static com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils.createGeofencingRequest;

public class GeofenceRequester implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

	private static final String LOG_TAG = GeofenceRequester.class.getSimpleName();

	private PendingIntent mPendingIntent;

	private Activity contextActivity;

	private Context context;

	private boolean connected;

	private GoogleApiClient mApiClient;

	private boolean pollMode;

	private List<Geofence> geofences = new ArrayList<>();

	public void disconnect() {
		List<String> geofenceIds = new ArrayList<>();
		for (Geofence geofence : geofences) {
			geofenceIds.add(geofence.getRequestId());
		}
		getGeofencingApi().removeGeofences(getGoogleApiClient(), geofenceIds);
		getFusedLocationApi().removeLocationUpdates(getGoogleApiClient(), this);
		connected = false;
		getGoogleApiClient().disconnect();
	}

	public GeofenceRequester(Activity contextActivity) {
		this(contextActivity, false);
	}


	public GeofenceRequester(Context context, boolean pollMode) {
		this.pollMode = pollMode;
		if(context instanceof Activity) {
			contextActivity = (Activity) context;
		}
		this.context = context;
	}

	public GoogleApiClient getGoogleApiClient() {
		if (mApiClient == null)
			mApiClient = new GoogleApiClient.Builder(context)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();

		return mApiClient;
	}

	public GeofencingApi getGeofencingApi() {
		return LocationServices.GeofencingApi;
	}

	private PendingIntent createRequestPendingIntent() {
		if (mPendingIntent != null) {
			return mPendingIntent;
		}

		Intent intent = new Intent(RECEIVE_GEOFENCE_REQUEST);
		return mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void connect() {
		getGoogleApiClient().connect();
	}

	public void addGeofences(@NonNull List<Geofence> geofences) {
		this.geofences = geofences;
		if (!connected) {
			connect();
		}
	}

	private void pushGeofences() {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			Log.e(LOG_TAG, "Required permissions not found when pushing geofences!");
			return;
		}
		if (!geofences.isEmpty())
			getGeofencingApi().addGeofences(getGoogleApiClient(), createGeofencingRequest(geofences), createRequestPendingIntent()).setResultCallback(this);
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(LOG_TAG, "connected in " + (pollMode ? "poll mode": "regular mode"));
		connected = true;
		if (!pollMode) {
			pushGeofences();
		} else {
			initPoll();
		}
	}

	public FusedLocationProviderApi getFusedLocationApi() {
		return LocationServices.FusedLocationApi;
	}

	private void initPoll() {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			Log.e(LOG_TAG, "Required permissions not found when applying update request!");
			return;
		}
		getFusedLocationApi().requestLocationUpdates(getGoogleApiClient(), createLocationRequest(), this);
	}

	private LocationRequest createLocationRequest() {
		return LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(POLL_INTERVAL)
				.setFastestInterval(POLL_INTERVAL);
	}

	@Override
	public void onConnectionSuspended(int i) {
		connected = false;
		Log.d(LOG_TAG, "Connection suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution() && contextActivity != null) {
			try {
				connectionResult.startResolutionForResult(contextActivity, CONNECTION_FAILED_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				Log.e(LOG_TAG, "Connection and resolution failed!", e);
			}
		} else {
			//TODO: Broadcast connection failed event so the activity can show feedback
			Log.e(LOG_TAG, "Broadcast connection failed: " + connectionResult.getErrorMessage());
		}
	}

	@Override
	public void onResult(Status status) {
		Log.d(LOG_TAG, "Geofence add result: " + status);
		if (status.isSuccess()) {
			//TODO: Broadcast success event of adding geofences
		} else {
			//TODO: Broadcast fail event of adding geofences
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(LOG_TAG, "Polling for location: " + location);
	}
}
