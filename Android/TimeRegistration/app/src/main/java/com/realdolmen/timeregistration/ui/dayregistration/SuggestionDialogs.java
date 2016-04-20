package com.realdolmen.timeregistration.ui.dayregistration;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.repository.OccupationRepository;
import com.realdolmen.timeregistration.service.repository.RegisteredOccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.util.DateUtil;

import org.jdeferred.DoneCallback;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public class SuggestionDialogs {

	private static final String LOG_TAG = SuggestionDialogs.class.getSimpleName();

	private DayRegistrationActivity context;

	SuggestionDialogs(DayRegistrationActivity context) {
		this.context = context;
	}

	private void handleSingleResultEnterSuggestion(final long occId, final Intent intent) {
		Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
			@Override
			public void onDone(OccupationRepository result) {
				Occupation o = result.getById(occId);
				DateTime time = (DateTime) intent.getSerializableExtra(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED);
				if (o == null || time == null) {
					Log.e(LOG_TAG, "Occupation or time is null");
				} else if (!isAllowed(time)) {
					Snackbar.make(context.findViewById(R.id.day_registration_root_view), R.string.suggestion_already_confirmed_day, Snackbar.LENGTH_LONG).show();
				} else if (isAlreadyOngoing(o, time)) {
					Snackbar.make(context.findViewById(R.id.day_registration_root_view), "The occupation is already ongoing and cannot be added again.", Snackbar.LENGTH_LONG).show();
				} else {
					showSingleResultEnterDialog(time, o);
				}
			}
		});
	}

	private boolean isAlreadyOngoing(Occupation occ, DateTime time) {
		return Repositories.registeredOccupationRepository().isAlreadyOngoing(occ, time);
	}

	private void handleSingleResultLeaveSuggestion(final long occId, final Intent intent) {
		Repositories.loadOccupationRepository(context).done(new DoneCallback<OccupationRepository>() {
			@Override
			public void onDone(OccupationRepository result) {
				Occupation o = result.getById(occId);
				DateTime time = (DateTime) intent.getSerializableExtra(RC.actionExtras.fromNotifications.removeSingleResult.TIME_DETECTED);
				if (o == null || time == null) {
					Log.e(LOG_TAG, "An occupation with ID " + occId + " was not found!");
				} else
					showSingleResultLeaveDialog(time, o);
			}
		});
	}

	private void handleMultiResultEnterSuggestion(Intent intent) {
		Intent receivedTemp = (Intent) intent.getSerializableExtra(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT);
		if (receivedTemp == null) {
			Log.e(LOG_TAG, "Received geofence intent is null!");
			return;
		}

		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(receivedTemp);

		if (geofencingEvent.hasError()) {
			Log.e(LOG_TAG, "Geofence event has an error!");
			return;
		}

		List<Project> filteredOccupations = new ArrayList<>(); //only occupations that are not yet ongoing
		for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
			Project p = Repositories.occupationRepository().getByGeofence(geofence);
			if (!isAlreadyOngoing(p, new DateTime(geofencingEvent.getTriggeringLocation().getTime()))) {
				filteredOccupations.add(p);
			}
		}

		if (!filteredOccupations.isEmpty())
			showMultiResultEnterDialog(geofencingEvent, filteredOccupations);
	}

	private void handleMultiResultLeaveSuggestion(List<Geofence> triggeredGeofences, Intent intent) {
		//TODO: make
	}

	private boolean isAllowed(DateTime time) {
		return !Repositories.registeredOccupationRepository().isConfirmed(time.withZone(DateTimeZone.UTC));
	}

	void handleNewIntent(final Intent intent) {
		String action = intent.getAction();

		if (action.equals(RC.action.fromNotification.ADD_SINGLE_RESULT)) {
			final long occId = intent.getLongExtra(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID, 0);
			if (occId == 0)
				Snackbar.make(context.findViewById(R.id.day_registration_root_view), "An error occured! Please manually add your occupation.", Snackbar.LENGTH_LONG).show();
			else
				handleSingleResultEnterSuggestion(occId, intent);


		} else if (action.equals(RC.action.fromNotification.ADD_MULTI_RESULT)) {
			Intent eventIntent = (Intent) intent.getSerializableExtra(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT);
			handleMultiResultEnterSuggestion(eventIntent);

		} else if (action.equals(RC.action.fromNotification.REMOVE_SINGLE_RESULT)) {
			final long occId = intent.getLongExtra(RC.actionExtras.fromNotifications.removeSingleResult.OCCUPATION_ID, 0);
			if (occId == 0) {
				Snackbar.make(context.findViewById(R.id.day_registration_root_view), "An error occured! Please manually complete your occupation.", Snackbar.LENGTH_LONG).show();
			} else {
				handleSingleResultLeaveSuggestion(occId, intent);
			}


		} else if (action.equals(RC.action.fromNotification.REMOVE_MULTI_RESULT)) {
			Intent eventIntent = (Intent) intent.getSerializableExtra(RC.actionExtras.fromNotifications.removeMultiResult.GEOFENCE_EVENT);
			GeofencingEvent event = GeofencingEvent.fromIntent(eventIntent);
			handleMultiResultLeaveSuggestion(event.getTriggeringGeofences(), intent);
		}

	}

	private void showMultiResultEnterDialog(GeofencingEvent geofencingEvent, List<Project> filteredOccupations) {
		//TODO: make
	}

	private void showMultiResultLeaveDialog() {
		//TODO: make
	}

	private void showSingleResultLeaveDialog(final DateTime registeredTime, final Occupation occupation) {
		Repositories.loadRegisteredOccupationRepository(context).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
				List<RegisteredOccupation> ongoingProjects = result.getOngoingOccupations(registeredTime.withZone(DateTimeZone.UTC));
				for (final RegisteredOccupation ongoingProject : ongoingProjects) {
					if (ongoingProject.getOccupation().getId() == occupation.getId()) { //if the ongoing project is the geofencing project
						AlertDialog dialog = new AlertDialog.Builder(context)
								.setMessage(Html.fromHtml(context.getString(
										R.string.notification_remove_single_result_message,
										occupation.getName(),
										DateUtil.formatToHours(registeredTime, DateFormat.is24HourFormat(context.getApplicationContext()))
								)))
								.setPositiveButton(context.getString(R.string.dialog_positive_yes), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										ongoingProject.setRegisteredEnd(registeredTime.withZone(DateTimeZone.UTC));
										context.handleUpdatedRegisteredOccupation(ongoingProject);
									}
								})
								.setNegativeButton(context.getString(R.string.dialog_negative_no), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
								.setTitle(context.getString(R.string.notification_remove_single_result_title, occupation.getName()))
								.create();

						dialog.show();
						break;
					}
				}
			}
		});
	}

	private void showSingleResultEnterDialog(final DateTime registeredTime, final Occupation occupation) {
		AlertDialog dialog = new AlertDialog.Builder(context)
				.setMessage(Html.fromHtml(context.getString(R.string.add_suggested_single_result, occupation.getName(), DateUtil.formatToHours(registeredTime, DateFormat.is24HourFormat(context.getApplicationContext())))))
				.setPositiveButton(context.getString(R.string.add_single_result_positive_button), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						context.handleNewlyRegisteredOccupation(occupation, registeredTime.withZone(DateTimeZone.UTC), null);
					}
				})
				.setNegativeButton(context.getString(R.string.add_single_result_negative_button), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setTitle(context.getString(R.string.add_single_result_title, occupation.getName()))
				.create();

		dialog.show();
	}

}
