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
import com.realdolmen.timeregistration.RC;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils.createGeofencingRequest;

public class GeofenceRequester implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

	private static final String LOG_TAG = GeofenceRequester.class.getSimpleName();

	private PendingIntent mPendingIntent;

	private Activity contextActivity;

	private Context context;

	private boolean connected;

	private GoogleApiClient mApiClient;
	private GeofencingApi geofencingApi;
	private FusedLocationProviderApi fusedLocationProviderApi;

	private boolean pollMode;

	private List<Geofence> geofences = new ArrayList<>();

	Testing testing = new Testing();

	public void disconnect() {
		if (!connected) {
			return;
		}

		connected = false;

		List<String> geofenceIds = new ArrayList<>();
		for (Geofence geofence : geofences) {
			geofenceIds.add(geofence.getRequestId());
		}
		if (geofenceIds != null && !geofenceIds.isEmpty())
			getGeofencingApi().removeGeofences(getGoogleApiClient(), geofenceIds);
		if (pollMode)
			destroyPoll();
		getGoogleApiClient().disconnect();
	}

	public GeofenceRequester(Activity contextActivity) {
		this(contextActivity, false);
	}

	class Testing {

		@TestOnly
		void setConnected(boolean flag) {
			connected = flag;
		}

		@TestOnly
		void setGeofences(List<Geofence> fences) {
			geofences = fences;
		}

		@TestOnly
		void setGoogleApiClient(GoogleApiClient client) {
			mApiClient = client;
		}

		@TestOnly
		void setGeofencingApi(GeofencingApi api) {
			geofencingApi = api;
		}

		@TestOnly
		void setFusedLocationApi(FusedLocationProviderApi api) {
			fusedLocationProviderApi = api;
		}

		@TestOnly
		void setPendingIntent(PendingIntent intent) {
			mPendingIntent = intent;
		}

		@TestOnly
		void setMode(boolean pollMode) {
			GeofenceRequester.this.pollMode = pollMode;
		}

		@TestOnly
		void setActivityContext(Activity ac) {
			contextActivity = ac;
		}
	}

	public GeofenceRequester(Context context, boolean pollMode) {
		this.pollMode = pollMode;
		if (context instanceof Activity) {
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
		if (geofencingApi != null)
			return geofencingApi;
		return geofencingApi = LocationServices.GeofencingApi;
	}

	private PendingIntent createRequestPendingIntent() {
		if (mPendingIntent != null) {
			return mPendingIntent;
		}

		Intent intent = new Intent(RC.geofencing.requests.RECEIVE_GEOFENCE_REQUEST);
		return mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void connect() {
		if (connected) return;
		getGoogleApiClient().connect();
	}

	public void addGeofences(@NonNull List<Geofence> geofences) {
		if (geofences == null) {
			throw new IllegalArgumentException("geofences list cannot be null!");
		}
		this.geofences = geofences;
		if (!connected) {
			connect();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	private void pushGeofences() {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(LOG_TAG, "Required permissions not found when pushing geofences!");
			return;
		}

		if (!geofences.isEmpty()) {
			Log.d(LOG_TAG, "pushGeofences: Adding " + geofences.size() + " geofences");
			getGeofencingApi().removeGeofences(getGoogleApiClient(), createRequestPendingIntent()).setResultCallback(this);
			getGeofencingApi().addGeofences(getGoogleApiClient(), createGeofencingRequest(geofences), createRequestPendingIntent()).setResultCallback(this);
		} else {
			Log.d(LOG_TAG, "pushGeofences: Geofences list is empty");
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(LOG_TAG, "connected in " + (pollMode ? "poll mode" : "regular mode"));
		connected = true;
		if (!pollMode) {
			pushGeofences();
		} else {
			initPoll();
		}
	}

	public FusedLocationProviderApi getFusedLocationApi() {
		if (fusedLocationProviderApi != null)
			return fusedLocationProviderApi;
		return fusedLocationProviderApi = LocationServices.FusedLocationApi;
	}

	private void initPoll() {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(LOG_TAG, "Required permissions not found when applying update request!");
			return;
		}
		getFusedLocationApi().requestLocationUpdates(getGoogleApiClient(), createLocationRequest(), this);
	}

	private void destroyPoll() {
		getFusedLocationApi().removeLocationUpdates(getGoogleApiClient(), this);
	}

	private LocationRequest createLocationRequest() {
		return LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(RC.geofencing.POLL_INTERVAL)
				.setFastestInterval(RC.geofencing.POLL_INTERVAL);
	}

	@Override
	public void onConnectionSuspended(int i) {
		connected = false;
		Log.d(LOG_TAG, "Connection suspended");
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		if (connectionResult.hasResolution() && contextActivity != null) {
			try {
				connectionResult.startResolutionForResult(contextActivity, RC.geofencing.requests.CONNECTION_FAILED_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				Log.e(LOG_TAG, "Connection and resolution failed!", e);
			}
		} else {
			broadcast(RC.geofencing.events.GOOGLE_API_CONNECTION_FAILED);
			Log.e(LOG_TAG, "Broadcast connection failed: " + connectionResult.getErrorMessage());
		}
	}

	@Override
	public void onResult(Status status) {
		Log.d(LOG_TAG, "Geofence add result: " + status);
		if (status.isSuccess()) {
			broadcast(RC.geofencing.events.FENCES_ADD_SUCCESS);
		} else {
			broadcast(RC.geofencing.events.FENCES_ADD_FAIL);
		}
	}

	private void broadcast(String event) {
		if (context != null)
			context.sendBroadcast(new Intent(event));
		else if (contextActivity != null)
			contextActivity.sendBroadcast(new Intent(event));
	}

	@Override
	public void onLocationChanged(Location location) {
		if (RC.other.DEV_MODE)
			Log.d(LOG_TAG, "Polling for location: " + location);
	}
}
