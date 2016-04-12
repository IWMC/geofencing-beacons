package com.realdolmen.timeregistration.service.location.geofence;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class GeoService extends Service {

	private final Binder binder = new GeoBinder();

	private GeofenceRequester requester;

	public class GeoBinder extends Binder {
		GeoService getService() {
			return GeoService.this;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		requester = new GeofenceRequester(getApplicationContext(), true);
		requester.connect();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		requester.disconnect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


}
