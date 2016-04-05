package com.realdolmen.timeregistration.service.location.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.realdolmen.timeregistration.model.Project;

public class GeofenceStore {

	private Context context;

	public GeofenceStore(@NonNull Context context) {
		this.context = context;
	}

	public void saveGeofence(@NonNull Project project, @NonNull Geofence geofence) {

	}

	private String key(String key, long projectId, long locationId) {
		return locationId + "_" + projectId + "_" + key;
	}
}
