package com.realdolmen.timeregistration.service.location.geofence;

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
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

import static com.realdolmen.timeregistration.util.Util.newNotification;
import static com.realdolmen.timeregistration.util.Util.notifyUser;

public class GeofencingBroadcastReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = GeofencingBroadcastReceiver.class.getSimpleName();

	private Context context;
	private Intent broadcastIntent = new Intent();

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		broadcastIntent.addCategory(RC.geofencing.LOCATION_SERVICES_CATEGORY);

		GeofencingEvent geofencingEvent = toGeoEvent(intent);

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

	GeofencingEvent toGeoEvent(Intent intent) {
		return GeofencingEvent.fromIntent(intent);
	}

	void showSingleResultEnterNotification(GeofencingEvent event, Occupation o) {
		DateTime time = new DateTime(event.getTriggeringLocation().getTime());
		if (Repositories.registeredOccupationRepository().isAlreadyOngoing(o, time)) {
			return;
		}
		Intent intent = newIntent(DayRegistrationActivity.class);
		intent.setAction(RC.action.fromNotification.ADD_SINGLE_RESULT);
		intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, o.getId());
		intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED, time);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = newNotification(context, context.getString(R.string.notification_title), context.getString(R.string.notification_enter_single_result, o), pendingIntent);

		if (o instanceof Project) {
			builder.setContentInfo("#" + ((Project) o).getProjectNr());
		}

		notifyUser(context, 1, builder);
	}

	Intent newIntent(Class c) {
		return new Intent(context, c);
	}

	void showMultiResultEnterNotification(Intent event, List<Geofence> fences) {
		Intent intent = newIntent(DayRegistrationActivity.class);
		intent.setAction(RC.action.fromNotification.ADD_MULTI_RESULT);
		intent.putExtra(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT, event);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notifyUser(context, 1, newNotification(context, R.string.notification_title, R.string.notification_enter_multiple_results, pendingIntent));
	}



	void showSingleResultLeaveNotification(GeofencingEvent geoEvent, Occupation o) {
		Intent intent = newIntent(DayRegistrationActivity.class);
		intent.setAction(RC.action.fromNotification.REMOVE_SINGLE_RESULT);
		intent.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.OCCUPATION_ID, o.getId());
		intent.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.TIME_DETECTED, new DateTime(geoEvent.getTriggeringLocation().getTime()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notifyUser(context, 2, newNotification(context, context.getString(R.string.notification_title), context.getString(R.string.notification_leave_single_result, o), pendingIntent));

	}

	void showMultiResultLeaveNotification(Intent event) {
		Intent intent = newIntent(DayRegistrationActivity.class);
		intent.setAction(RC.action.fromNotification.REMOVE_MULTI_RESULT);
		intent.putExtra(RC.actionExtras.fromNotifications.removeMultiResult.GEOFENCE_EVENT, event);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notifyUser(context, 2, newNotification(context, R.string.notification_remove_multi_results_title, R.string.notification_remove_multi_results, pendingIntent));

	}

	Promise doNotificationLeave(final GeofencingEvent geoEvent, final Intent event, final List<Geofence> geofences) {
		if (geofences.size() == 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					Occupation o = result.getByGeofence(geofences.get(0));
					showSingleResultLeaveNotification(geoEvent, o);
				}
			});
		} else if (geofences.size() > 1) {
			return Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
				@Override
				public void onDone(OccupationRepository result) {
					showMultiResultLeaveNotification(event);
				}
			});
		}

		return null;
	}
}
