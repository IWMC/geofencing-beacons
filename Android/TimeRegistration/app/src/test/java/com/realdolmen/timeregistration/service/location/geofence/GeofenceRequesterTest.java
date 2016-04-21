package com.realdolmen.timeregistration.service.location.geofence;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GeofenceRequesterTest extends GeofenceTest {

	@Mock private Context context;
	@Mock private GoogleApiClient googleApiClient;
	@Mock private PendingIntent pendingIntent;
	@Mock private GeofencingApi geofencingApi;
	@Mock private FusedLocationProviderApi fusedLocationApi;
	@Mock private PendingResult<Status> pendingResult;
	@Mock private Activity mockedActivity;

	private GeofenceRequester geofenceRequester;
	private List<Geofence> geofences = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		geofenceRequester = new GeofenceRequester(context, false);
		geofenceRequester.testing.setGoogleApiClient(googleApiClient);
		geofenceRequester.testing.setGeofencingApi(geofencingApi);
		geofenceRequester.testing.setFusedLocationApi(fusedLocationApi);
		geofences.addAll(Arrays.asList(
				newGeofence("1"),
				newGeofence("2"),
				newGeofence("3")
		));
	}

	@Test
	public void testDisconnectDoesNothingWhenNotConnected() throws Exception {
		geofenceRequester.disconnect();
		verify(geofencingApi, never()).removeGeofences(eq(googleApiClient), anyListOf(String.class));
		verify(fusedLocationApi, never()).removeLocationUpdates(googleApiClient, geofenceRequester);
	}

	@Test
	public void testDisconnectRemovesRegisteredGeofences() throws Exception {
		geofenceRequester.testing.setConnected(true);
		geofenceRequester.testing.setGeofences(geofences);
		geofenceRequester.disconnect();
		verify(geofencingApi).removeGeofences(eq(googleApiClient), anyListOf(String.class));
		verify(fusedLocationApi).removeLocationUpdates(googleApiClient, geofenceRequester);
		verify(googleApiClient).disconnect();
	}

	@Test
	public void testConnectDoesNothingWhenAlreadyConnected() throws Exception {
		geofenceRequester.testing.setConnected(true);
		geofenceRequester.connect();
		verify(googleApiClient, never()).connect();
	}

	@Test
	public void testConnectConnectsToGoogleApiWhenNotConnected() throws Exception {
		geofenceRequester.connect();
		verify(googleApiClient).connect();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddGeofencesWithNullListThrowsIllegalArgumentException() throws Exception {
		geofenceRequester.addGeofences(null);
	}

	@Test
	public void testAddGeofencesWhileNotConnectedAlsoConnects() throws Exception {
		geofenceRequester.addGeofences(geofences);
		verify(googleApiClient).connect();
	}

	@SuppressWarnings("MissingPermission")
	@Test
	public void testOnConnectedInRegularModePushesGeofencesIfGeofencesListIsNotEmpty() throws Exception {
		geofenceRequester.testing.setGeofences(geofences);
		geofenceRequester.testing.setPendingIntent(pendingIntent);
		when(geofencingApi.removeGeofences(eq(googleApiClient), any(PendingIntent.class))).thenReturn(pendingResult);
		when(geofencingApi.addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent))).thenReturn(pendingResult);
		geofenceRequester.onConnected(null);
		verify(geofencingApi).removeGeofences(googleApiClient, pendingIntent);
		verify(geofencingApi).addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent));
		verify(pendingResult, times(2)).setResultCallback(geofenceRequester);
	}

	@SuppressWarnings("MissingPermission")
	@Test
	public void testOnConnectedInRegularModeDoesNothingWhenGeofencingListIsEmpty() throws Exception {
		geofenceRequester.testing.setGeofences(new ArrayList<Geofence>());
		geofenceRequester.testing.setPendingIntent(pendingIntent);
		when(geofencingApi.removeGeofences(eq(googleApiClient), any(PendingIntent.class))).thenReturn(pendingResult);
		when(geofencingApi.addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent))).thenReturn(pendingResult);
		geofenceRequester.onConnected(null);
		verify(geofencingApi, never()).removeGeofences(googleApiClient, pendingIntent);
		verify(geofencingApi, never()).addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent));
		verify(pendingResult, never()).setResultCallback(geofenceRequester);
	}

	@SuppressWarnings("MissingPermission")
	@Test
	public void testOnConnectedInPollModeCreatesUpdateRequest() throws Exception {
		geofenceRequester.testing.setGeofences(new ArrayList<Geofence>());
		geofenceRequester.testing.setPendingIntent(pendingIntent);
		geofenceRequester.testing.setMode(true);
		when(geofencingApi.removeGeofences(eq(googleApiClient), any(PendingIntent.class))).thenReturn(pendingResult);
		when(geofencingApi.addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent))).thenReturn(pendingResult);
		geofenceRequester.onConnected(null);
		verify(geofencingApi, never()).removeGeofences(googleApiClient, pendingIntent);
		verify(geofencingApi, never()).addGeofences(eq(googleApiClient), any(GeofencingRequest.class), eq(pendingIntent));
		verify(pendingResult, never()).setResultCallback(geofenceRequester);
		verify(fusedLocationApi).requestLocationUpdates(eq(googleApiClient), any(LocationRequest.class), eq(geofenceRequester));
	}

	//These tests cannot be run due to the inability to mock or extend ConnectionResult.
	/*@Test
	public void testOnConnectionFailedWithExistingActivityContextTriesResolvingAutomatically() throws Exception {
		geofenceRequester.testing.setActivityContext(mockedActivity);
		ConnectionResult result = mock(ConnectionResult.class);
		when(result.hasResolution()).thenReturn(true);
		geofenceRequester.onConnectionFailed(result);
		verify(result).startResolutionForResult(mockedActivity, GeofenceUtils.CONNECTION_FAILED_RESOLUTION_REQUEST);
	}

	@Test
	public void testOnConnectionFailedWithNoExistingActivityContextBroadcastsErrorWithoutResolution() throws Exception {
		geofenceRequester.testing.setActivityContext(null);
		ConnectionResult result = mock(ConnectionResult.class);
		when(result.hasResolution()).thenReturn(true);
		geofenceRequester.onConnectionFailed(result);
		verify(result, never()).startResolutionForResult(mockedActivity, GeofenceUtils.CONNECTION_FAILED_RESOLUTION_REQUEST);
		//TODO: check if error broadcast is done
	}*/
}
