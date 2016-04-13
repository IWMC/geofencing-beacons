package com.realdolmen.timeregistration.service.location.geofence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.service.repository.OccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationActivity;

import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jetbrains.annotations.TestOnly;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

public class GeofencingBroadcastReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = GeofencingBroadcastReceiver.class.getSimpleName();

	private Context context;
	private Intent broadcastIntent = new Intent();
	private boolean testMode;

	@TestOnly
	public void enableTestMode() {
		testMode = true;
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		broadcastIntent.addCategory(RC.geofencing.LOCATION_SERVICES_CATEGORY);

		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

		if (geofencingEvent.hasError()) {
			broadcastError(geofencingEvent);
		} else {
			handleEvent(intent, geofencingEvent);
		}
	}

	private void broadcastError(GeofencingEvent event) {
		Log.e(LOG_TAG, "Geofence error: " + event.getErrorCode());
		context.sendBroadcast(new Intent(RC.geofencing.events.GEOFENCE_ERROR));
	}

	public void handleEvent(Intent intent, GeofencingEvent event) {
		Log.d(LOG_TAG, "Geofence " + event.getGeofenceTransition() + " event received: " + Arrays.toString(event.getTriggeringGeofences().toArray()));
		if (event.getGeofenceTransition() == GeofencingRequest.INITIAL_TRIGGER_ENTER) {
			doNotificationEnter(event, intent, event.getTriggeringGeofences());
		} else if (event.getGeofenceTransition() == GeofencingRequest.INITIAL_TRIGGER_EXIT) {
			doNotificationLeave(event.getTriggeringGeofences());
		}
	}

	public Promise doNotificationEnter(final GeofencingEvent geoEvent, final Intent event, final List<Geofence> geofences) {
		if (geofences.size() == 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					Occupation o = result.getByGeofence(geofences.get(0));
					showSingleResultEnterNotification(geoEvent, o);
				}
			});
		} else if (geofences.size() > 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					String output = context.getString(R.string.notification_enter_multiple_results);
					showMultiResultEnterNotification(event, geofences);
				}
			});
		}

		return null;
	}

	private void showSingleResultEnterNotification(GeofencingEvent event, Occupation o) {
		Intent intent = new Intent(context, DayRegistrationActivity.class);
		intent.setAction(RC.actions.fromNotifications.ADD_SINGLE_RESULT);

		intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, o.getId());
		intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED, new DateTime(event.getTriggeringLocation().getTime()));

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (!testMode) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.logo_square)
					.setContentTitle(context.getString(R.string.notification_title))
					.setContentText(context.getString(R.string.notification_enter_single_result, o))
					.setLights(0xFFed2b29, 1000, 1000)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setContentIntent(pendingIntent);

			if (o instanceof Project) {
				builder.setContentInfo("#" + ((Project) o).getProjectNr());
			}
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, builder.build());
		} else {
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, new Notification());
		}
	}

	private void showMultiResultEnterNotification(Intent event, List<Geofence> fences) {
		Intent intent = new Intent(context, DayRegistrationActivity.class);
		intent.setAction(RC.actions.fromNotifications.ADD_MULTI_RESULT);

		intent.putExtra(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT, event);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (!testMode) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.logo_square)
					.setContentTitle(context.getString(R.string.notification_title))
					.setContentText(context.getString(R.string.notification_enter_multiple_results))
					.setLights(0xFFed2b29, 1000, 1000)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setContentIntent(pendingIntent);

			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, builder.build());
		} else {
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, new Notification());
		}
	}



	public Promise doNotificationLeave(final List<Geofence> geofences) {
		if (geofences.size() == 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					Occupation o = result.getByGeofence(geofences.get(0));
					Intent intent = new Intent();
					intent.setAction(RC.actions.fromNotifications.ADD_SINGLE_RESULT);
					intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, o.getId());
					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
					//TODO: showNotification(context.getString(R.string.notification_leave_single_result, o), null);
				}
			});
		} else if (geofences.size() > 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					//TODO: showNotification(context.getString(R.string.notification_leave_multiple_results), null);
				}
			});
		}

		return null;
	}

/*	public void showNotification(String s, PendingIntent intent) {
		if (!testMode) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.logo_square)
					.setContentTitle(context.getString(R.string.notification_title))
					.setContentText(s)
					.setLights(0xFFed2b29, 1000, 1000)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setContentIntent(intent)
					.setSubText(context.getString(R.string.notification_title))
					.setContentInfo("New occupation suggestion");
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, builder.build());
		} else {
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(1, new Notification());
		}

	}*/
}
