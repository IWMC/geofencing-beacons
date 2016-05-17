package com.realdolmen.timeregistration.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.service.location.geofence.Injections;

public class Util {

	public enum BluetoothState {
		UNSUPPORTED, OFF, ON
	}

	public static void requireNonNull(Object o, String message) {
		if (o == null)
			throw new NullPointerException(message);
	}

	public static void requireNonNull(Object o) {
		requireNonNull(o, "NonNull required!");
	}

	public static BluetoothState getBluetoothState() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			return BluetoothState.UNSUPPORTED;
		} else if (!mBluetoothAdapter.isEnabled()) {
			return BluetoothState.OFF;
		}
		return BluetoothState.ON;
	}

	public static boolean isConnectedToInternet(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getActiveNetworkInfo();
		return mWifi != null && (mWifi.isConnected() || mWifi.isRoaming());
	}

	public static NotificationCompat.Builder newNotification(Context context, @StringRes int title, @StringRes int content, PendingIntent pIntent) {
		return newNotification(context, title, content, pIntent, !RC.other.KEEP_NOTIFICATIONS);
	}

	public static NotificationCompat.Builder newNotification(Context context, String title, String content, PendingIntent pIntent) {
		return newNotification(context, title, content, pIntent, !RC.other.KEEP_NOTIFICATIONS);
	}

	public static NotificationCompat.Builder newNotification(Context context, @StringRes int title, @StringRes int content, PendingIntent pIntent, boolean autoCancel) {
		return newNotification(context, context.getString(title), context.getString(content), pIntent, autoCancel);
	}

	public static NotificationCompat.Builder newNotification(Context context, String title, String content, PendingIntent pIntent, boolean autoCancel) {
		return Injections.getDefaultBuilder(context)
				.setSmallIcon(R.drawable.logo_square)
				.setContentTitle(title)
				.setContentText(content)
				.setLights(0xFFed2b29, 1000, 1000)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setContentIntent(pIntent)
				.setAutoCancel(autoCancel);
	}

	public static void notifyUser(Context context, int id, NotificationCompat.Builder builder) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(id, builder.build());
	}

}
