package com.realdolmen.timeregistration.service.location.beacon;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.dayregistration.DayRegistrationActivity;
import com.realdolmen.timeregistration.util.Util;

import org.altbeacon.beacon.Beacon;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconDwellManager extends Service implements Runnable {

	private static final String TAG = BeaconDwellManager.class.getSimpleName();

	private Handler timerHandler = new Handler();
	private Map<BeaconEvent, DateTime> registeredEvents = new HashMap<>();
	private Map<BeaconEvent, Double> recordedDistances = new HashMap<>();

	private android.os.Binder binder = new Binder();

	public class Binder extends android.os.Binder {
		public BeaconDwellManager getService() {
			return BeaconDwellManager.this;
		}
	}

	public BeaconDwellManager() {
		timerHandler.postDelayed(this, 1000);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void registerEvent(BeaconEvent event) {
		if (event.getType() == BeaconEvent.BeaconEventType.EXIT) {
			BeaconEvent matching = getMatchingEnterEvent(event);
			if (matching != null) {
				if (recordedDistances.containsKey(matching)) {
					if (recordedDistances.get(matching) > matching.getAction().getMode().getMeters()) {
						//ignore
					} else {
						registeredEvents.remove(matching);
					}
				} else {
					registeredEvents.remove(matching);
				}
			} else {
				registeredEvents.put(event, new DateTime());
			}
		} else if (event.getType() == BeaconEvent.BeaconEventType.ENTER) {
			registeredEvents.put(event, new DateTime());
		} else if (event.getType() == BeaconEvent.BeaconEventType.RANGE) {
			//find enter event for region
			if (!RC.beacon.MEASURE_RANGE) {
				return;
			}
			BeaconEvent enterEvent = getMatchingEnterEvent(event);
			if (enterEvent != null) {
				//set last recorded distance
				if (enterEvent.getAction().getMode().isRangeMode()) {
					List<Beacon> beacons = ((RangeBeaconEvent) event).getBeacons();
					if (beacons.size() > 0)
						recordedDistances.put(enterEvent, beacons.get(0).getDistance());
				}
			}
		}
	}

	private BeaconEvent getMatchingEnterEvent(BeaconEvent source) {
		BeaconEvent match = null;
		for (BeaconEvent event : registeredEvents.keySet()) {
			if (event.getType() == BeaconEvent.BeaconEventType.ENTER && event.getRegion().equals(source.getRegion())) {
				match = event;
				break;
			}
		}
		return match;
	}

	@Override
	public void run() {

		List<BeaconEvent> triggeredEvents = new ArrayList<>();
		List<BeaconEvent> silentRemove = new ArrayList<>();

		for (Map.Entry<BeaconEvent, DateTime> entry : registeredEvents.entrySet()) {
			int tresholdInSeconds = entry.getKey().getAgeTreshold();
			int secondsDifference = Seconds.secondsBetween(entry.getValue(), new DateTime()).getSeconds();

			if (secondsDifference >= tresholdInSeconds) {
				if (recordedDistances.containsKey(entry.getKey())) {
					if (entry.getKey().getAction().getMode().getMeters() >= recordedDistances.get(entry.getKey())) {
						triggeredEvents.add(entry.getKey());
					} else {
						//distance greater
						if (entry.getKey().getType() == BeaconEvent.BeaconEventType.ENTER) {
							triggeredEvents.add(new BeaconEvent(BeaconEvent.BeaconEventType.EXIT, entry.getKey().getAction(), entry.getKey().getRegion()));
							silentRemove.add(entry.getKey());
						}
					}
				} else {
					triggeredEvents.add(entry.getKey());
				}
			}
		}

		for (BeaconEvent event : triggeredEvents) {
			DateTime time = registeredEvents.remove(event);
			trigger(event, time);
		}

		for (BeaconEvent silent : silentRemove) {
			registeredEvents.remove(silent);
			recordedDistances.remove(silent);
		}

		timerHandler.postDelayed(this, 1000);
	}

	private void trigger(BeaconEvent event, DateTime time) {

		Intent intent = new Intent(getApplicationContext(), DayRegistrationActivity.class);
		if (event.getAction().getOccupations().size() == 1 && event.getType() == BeaconEvent.BeaconEventType.ENTER) {
			Occupation o = (Occupation) event.getAction().getOccupations().toArray()[0];
			if (Repositories.registeredOccupationRepository().isAlreadyOngoing(o, time.withZone(DateTimeZone.UTC))) {
				return;
			}
			intent.setAction(RC.action.fromNotification.ADD_SINGLE_RESULT);
			intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, o.getId());
			intent.putExtra(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED, time);
		}

		if (event.getAction().getOccupations().size() == 1 && event.getType() == BeaconEvent.BeaconEventType.EXIT) {
			Occupation o = (Occupation) event.getAction().getOccupations().toArray()[0];
			if (!Repositories.registeredOccupationRepository().isAlreadyOngoing(o, time.withZone(DateTimeZone.UTC))) {
				return;
			}
			intent.setAction(RC.action.fromNotification.REMOVE_SINGLE_RESULT);
			intent.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.OCCUPATION_ID, o.getId());
			intent.putExtra(RC.actionExtras.fromNotifications.removeSingleResult.TIME_DETECTED, time);
		}

		if (event.getAction().getOccupations().size() > 1 && event.getType() == BeaconEvent.BeaconEventType.ENTER) {
			intent.setAction(RC.action.fromNotification.ADD_MULTI_RESULT);
			intent.putExtra(RC.actionExtras.fromNotifications.addMultiResult.BEACON_EVENT, event);
			intent.putExtra(RC.actionExtras.fromNotifications.addMultiResult.TIME_DETECTED, time);
		}

		if (event.getAction().getOccupations().size() > 1 && event.getType() == BeaconEvent.BeaconEventType.ENTER) {
			intent.setAction(RC.action.fromNotification.REMOVE_MULTI_RESULT);
			intent.putExtra(RC.actionExtras.fromNotifications.removeMultiResult.BEACON_EVENT, event);
			intent.putExtra(RC.actionExtras.fromNotifications.removeMultiResult.TIME_DETECTED, time);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		String content = "";
		if (event.getType() == BeaconEvent.BeaconEventType.ENTER) {
			if (event.getAction().getOccupations().size() == 1) {
				content = getString(R.string.notification_beacon_add_single_result, event.getAction().getOccupations().toArray()[0]);
			} else if (event.getAction().getOccupations().size() > 1) {
				content = getString(R.string.notification_beacon_add_multiple_results);
			}
		} else {
			if (event.getAction().getOccupations().size() == 1) {
				content = getString(R.string.notification_beacon_remove_single_result, event.getAction().getOccupations().toArray()[0]);
			} else if (event.getAction().getOccupations().size() > 1) {
				content = getString(R.string.notification_beacon_remove_multiple_results);
			}
		}
		NotificationCompat.Builder builder = Util.newNotification(getApplicationContext(), getString(R.string.notification_title), content, pendingIntent);
		Util.notifyUser(getApplicationContext(), event.getRegion().getId2().toInt(), builder);
	}
}
