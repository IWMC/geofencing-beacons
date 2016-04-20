package com.realdolmen.timeregistration.service.location.geofence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
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
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

public class GeofencingBroadcastReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = GeofencingBroadcastReceiver.class.getSimpleName();

	private Context context;
	private Intent broadcastIntent = new Intent();

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
			doNotificationLeave(event, intent, event.getTriggeringGeofences());
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
					showMultiResultEnterNotification(event, geofences);
				}
			});
		}

		return null;
	}

	private void showSingleResultEnterNotification(GeofencingEvent event, Occupation o) {
		DateTime time = new DateTime(event.getTriggeringLocation().getTime());
		if (Repositories.registeredOccupationRepository().isAlreadyOngoing(o, time)) {
			return;
		}
		Intent intent = new Intent(context, DayRegistrationActivity.class)
				.setAction(RC.action.fromNotification.ADD_SINGLE_RESULT)
				.putExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, o.getId())
				.putExtra(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED, time)
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = newNotification(context.getString(R.string.notification_title), context.getString(R.string.notification_enter_single_result, o), pendingIntent);

		if (o instanceof Project) {
			builder.setContentInfo("#" + ((Project) o).getProjectNr());
		}

		notifyUser(1, builder);
	}

	//region Notification Builder methods
	private NotificationCompat.Builder newNotification(@StringRes int title, @StringRes int content, PendingIntent pIntent) {
		return newNotification(title, content, pIntent, !RC.other.KEEP_NOTIFICATIONS);
	}

	private NotificationCompat.Builder newNotification(String title, String content, PendingIntent pIntent) {
		return newNotification(title, content, pIntent, !RC.other.KEEP_NOTIFICATIONS);
	}

	private NotificationCompat.Builder newNotification(@StringRes int title, @StringRes int content, PendingIntent pIntent, boolean autoCancel) {
		return newNotification(context.getString(title), context.getString(content), pIntent, autoCancel);
	}

	private NotificationCompat.Builder newNotification(String title, String content, PendingIntent pIntent, boolean autoCancel) {
		return Injections.getDefaultBuilder(context)
				.setSmallIcon(R.drawable.logo_square)
				.setContentTitle(title)
				.setContentText(content)
				.setLights(0xFFed2b29, 1000, 1000)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setContentIntent(pIntent)
				.setAutoCancel(!RC.other.KEEP_NOTIFICATIONS);
	}
	//endregion

	private void showMultiResultEnterNotification(Intent event, List<Geofence> fences) {
		Intent intent = new Intent(context, DayRegistrationActivity.class)
				.setAction(RC.action.fromNotification.ADD_MULTI_RESULT)
				.putExtra(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT, event)
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notifyUser(1, newNotification(R.string.notification_title, R.string.notification_enter_multiple_results, pendingIntent));
	}

	private void notifyUser(int id, NotificationCompat.Builder builder) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(id, builder.build());
	}


	public Promise doNotificationLeave(final GeofencingEvent geoEvent, final Intent event, final List<Geofence> geofences) {
		if (geofences.size() == 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					Occupation o = result.getByGeofence(geofences.get(0));
					Intent intent = new Intent(context, DayRegistrationActivity.class)
							.setAction(RC.action.fromNotification.REMOVE_SINGLE_RESULT)
							.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.OCCUPATION_ID, o.getId())
							.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.TIME_DETECTED, new DateTime(geoEvent.getTriggeringLocation().getTime()))
							.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					notifyUser(2, newNotification(context.getString(R.string.notification_title), context.getString(R.string.notification_leave_single_result, o), pendingIntent));
				}
			});
		} else if (geofences.size() > 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					Intent intent = new Intent(context, DayRegistrationActivity.class)
							.setAction(RC.action.fromNotification.REMOVE_MULTI_RESULT)
							.putExtra(RC.actionExtras.fromNotifications.removeMultiResult.GEOFENCE_EVENT, event)
							.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					notifyUser(2, newNotification(R.string.notification_remove_multi_results_title, R.string.notification_remove_multi_results, pendingIntent));
				}
			});
		}

		return null;
	}
}
