package com.realdolmen.timeregistration.service.location.geofence;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeofenceUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void testCreateGeofencingRequestWithNullGeofencesThrowsIllegalArgumentException() throws Exception {
		GeofenceUtils.createGeofencingRequest(null);
	}

	@Test
	public void testCreateGeofencingRequestWithValidListCreatesARequest() throws Exception {
		Geofence geofence = new Geofence.Builder()
				.setRequestId("test")
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
				.setCircularRegion(1, 2, 1000)
				.build();

		GeofencingRequest req = GeofenceUtils.createGeofencingRequest(Arrays.asList(geofence));
		assertNotNull("Generated request should not be null", req);
		assertEquals("Geofence list of the request should be size 1", 1, req.getGeofences().size());
		assertEquals("Initial trigger of the request should be TRANSITION_ENTER", GeofencingRequest.INITIAL_TRIGGER_ENTER, req.getInitialTrigger());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateGeofenceWithNullLocationThrowsIllegalArgumentException() throws Exception {
		GeofenceUtils.createGeofence(1, null, 1000);
	}


}
