package com.realdolmen.timeregistration.service.location.geofence;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.jetbrains.annotations.TestOnly;

/**
 * Created by BCCAZ45 on 20/04/2016.
 */
public class Injections {

	private static NotificationCompat.Builder defaultBuilder;

	public static NotificationCompat.Builder getDefaultBuilder(Context context) {
		if (defaultBuilder == null) {
			return new NotificationCompat.Builder(context);
		} else {
			return defaultBuilder;
		}
	}

	@TestOnly
	public static void setDefaultBuilder(NotificationCompat.Builder defaultBuilder) {
		Injections.defaultBuilder = defaultBuilder;
	}
}
