package com.realdolmen.timeregistration.service.location.geofence;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.repository.BackendService;
import com.realdolmen.timeregistration.service.repository.OccupationRepository;
import com.realdolmen.timeregistration.service.repository.RegisteredOccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: 21/04/2016 Rework this test
public class GeofencingBroadcastReceiverTest extends GeofenceTest {

	//region Mocks
	@Mock
	private Context context;

	@Mock
	private Intent broadcastIntent;

	@Mock
	private GeofencingEvent geofencingEvent;

	@Mock
	private Location mockedTriggerLocation;

	@Mock
	private Intent normalIntent;

	@Mock
	private BackendService service;

	@Mock
	private OccupationRepository occupationRepository;

	@Mock
	private RegisteredOccupationRepository registeredOccupationRepository;

	@Mock
	private NotificationManager notificationManager;

	@Mock(answer = Answers.RETURNS_MOCKS)
	private NotificationCompat.Builder notificationBuilder;
	//endregion

	@InjectMocks
	@Spy
	private GeofencingBroadcastReceiver receiver = new GeofencingBroadcastReceiver();

	private List<Geofence> geofenceList;
	private List<RegisteredOccupation> registeredOccupations = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		initRepoMocks();
		geofenceList = new ArrayList<>(Arrays.asList(
				newGeofence("1"),
				newGeofence("2"),
				newGeofence("3")
		));
		registeredOccupations.addAll(Arrays.asList(newRegisteredOcc(), newRegisteredOcc(), newRegisteredOcc(), newRegisteredOcc()));

		BackendService.Testing.setBackendService(context, service);
		when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager);

		Injections.setDefaultBuilder(notificationBuilder);

		when(mockedTriggerLocation.getTime()).thenReturn(System.currentTimeMillis());
		when(geofencingEvent.getTriggeringLocation()).thenReturn(mockedTriggerLocation);

		when(receiver.newIntent(any(Class.class))).thenReturn(normalIntent);
		when(receiver.toGeoEvent(any(Intent.class))).thenReturn(geofencingEvent);
	}

	private RegisteredOccupation newRegisteredOcc() {
		Random r = new Random();
		RegisteredOccupation regOcc = new RegisteredOccupation();
		regOcc.setOccupation(new Occupation(UUID.randomUUID().toString()));
		regOcc.setId(r.nextLong());
		regOcc.setRegisteredStart(new DateTime(DateTimeZone.UTC));
		regOcc.setRegisteredEnd(r.nextBoolean() ? new DateTime(DateTimeZone.UTC) : null);
		return regOcc;
	}

	private void initRepoMocks() {
		Repositories.Testing.setOccupationRepository(occupationRepository);
		Repositories.Testing.setRegisteredOccupationRepository(registeredOccupationRepository);
		when(occupationRepository.isLoaded()).thenReturn(true);
		when(occupationRepository.getByGeofence((Geofence) any())).thenReturn(new Project("", "", 0, null, null));
		when(registeredOccupationRepository.isLoaded()).thenReturn(true);
		when(registeredOccupationRepository.getOngoingOccupations(any(DateTime.class))).thenReturn(registeredOccupations);
	}

	@Test
	public void testHandleEventWithTriggerEnterDelegatesToDoNotificationEnter() throws Exception {
		when(geofencingEvent.getGeofenceTransition()).thenReturn(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		when(geofencingEvent.getTriggeringGeofences()).thenReturn(geofenceList);
		receiver.handleEvent(normalIntent, geofencingEvent);
		verify(receiver).doNotificationEnter(geofencingEvent, normalIntent, geofenceList);
	}

	@Test
	public void testHandleEventWithTriggerExitDelegatesToDoNotifcationLeave() throws Exception {
		when(geofencingEvent.getGeofenceTransition()).thenReturn(GeofencingRequest.INITIAL_TRIGGER_EXIT);
		when(geofencingEvent.getTriggeringGeofences()).thenReturn(geofenceList);
		receiver.handleEvent(normalIntent, geofencingEvent);
		verify(receiver).doNotificationLeave(geofencingEvent, normalIntent, geofenceList);
	}

	@Test
	public void testDoNotificationEnterWithSingleResultShowsCorrectNotification() throws Exception {
		when(context.getString(eq(R.string.notification_enter_single_result), anyVararg())).thenReturn("single_result");
		when(context.getString(eq(R.string.notification_enter_multiple_results))).thenReturn("multiple_results");
		receiver.doNotificationEnter(geofencingEvent, normalIntent, Arrays.asList(newGeofence("1")));
		verify(receiver).showSingleResultEnterNotification(eq(geofencingEvent), any(Occupation.class));
	}

	@Test
	public void testDoNotificationEnterWithMultipleResultsShowsCorrectNotification() throws Exception {
		when(context.getString(eq(R.string.notification_enter_single_result), anyVararg())).thenReturn("single_result");
		when(context.getString(eq(R.string.notification_enter_multiple_results))).thenReturn("multiple_results");
		geofenceList.add(newGeofence("2"));
		receiver.doNotificationEnter(geofencingEvent, normalIntent, geofenceList);
		verify(receiver).showMultiResultEnterNotification(any(Intent.class), anyListOf(Geofence.class));
	}

	@Test
	public void testDoNotificationLeaveWithSingleResultShowsCorrectNotification() throws Exception {
		when(context.getString(eq(R.string.notification_enter_single_result), anyVararg())).thenReturn("single_result");
		when(context.getString(eq(R.string.notification_enter_multiple_results))).thenReturn("multiple_results");
		receiver.doNotificationLeave(geofencingEvent, normalIntent, Arrays.asList(newGeofence("1")));
		verify(receiver).showSingleResultLeaveNotification(eq(geofencingEvent), any(Occupation.class));
	}

	@Test
	public void testDoNotificationLeaveWithMultipleResultsShowsCorrectNotification() throws Exception {
		when(context.getString(eq(R.string.notification_enter_single_result), anyVararg())).thenReturn("single_result");
		when(context.getString(eq(R.string.notification_enter_multiple_results))).thenReturn("multiple_results");
		geofenceList.add(newGeofence("2"));
		receiver.doNotificationLeave(geofencingEvent, normalIntent, geofenceList);
		verify(receiver).showMultiResultLeaveNotification(any(Intent.class));
	}

	@Test
	public void testShowNotificationNotifiesWithSameIdEveryTime() throws Exception {

		when(geofencingEvent.getTriggeringLocation()).thenReturn(mockedTriggerLocation);
		when(mockedTriggerLocation.getTime()).thenReturn(System.currentTimeMillis());

		//Enter event should be id 1
		receiver.doNotificationEnter(geofencingEvent, normalIntent, geofenceList);
		verify(receiver).notifyUser(eq(1), any(NotificationCompat.Builder.class));

		//leave event should be id 2
		receiver.doNotificationLeave(geofencingEvent, normalIntent, geofenceList);
		verify(receiver).notifyUser(eq(2), any(NotificationCompat.Builder.class));
	}

	@Test
	public void testShowSingleResultEnterNotificationWithAlreadyOngoingProjectsIgnoresNotification() throws Exception {
		Occupation o = new Occupation("occ");
		when(registeredOccupationRepository.isAlreadyOngoing(eq(o), any(DateTime.class))).thenReturn(true);
		receiver.showSingleResultEnterNotification(geofencingEvent, o);
		verify(receiver, never()).newIntent(any(Class.class));
	}

	@Test
	public void testShowSingleResultEnterNotificationProducesCorrectIntent() throws Exception {
		Occupation o = new Occupation("occ");
		receiver.showSingleResultEnterNotification(geofencingEvent, o);
		verify(normalIntent).setAction(RC.action.fromNotification.ADD_SINGLE_RESULT);
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.addSingleResult.OCCUPATION_ID), anyLong());
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.addSingleResult.TIME_DETECTED), any(Serializable.class));
		verify(normalIntent).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@Test
	public void testShowMultiResultEnterNotificationProducesCorrectIntent() throws Exception {
		receiver.showMultiResultEnterNotification(normalIntent, geofenceList);
		verify(normalIntent).setAction(RC.action.fromNotification.ADD_MULTI_RESULT);
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.addMultiResult.GEOFENCE_EVENT), any(Parcelable.class));
		verify(normalIntent).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@Test
	public void testShowSingleResultLeaveNotificationProducesCorrectIntent() throws Exception {
		Occupation o = new Occupation("occ");
		receiver.showSingleResultLeaveNotification(geofencingEvent, o);
		verify(normalIntent).setAction(RC.action.fromNotification.REMOVE_SINGLE_RESULT);
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.removeSingleResult.OCCUPATION_ID), anyLong());
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.removeSingleResult.TIME_DETECTED), any(Serializable.class));
		verify(normalIntent).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@Test
	public void testShowMultiResultLeaveNotificationProducesCorrectIntent() throws Exception {
		receiver.showMultiResultLeaveNotification(normalIntent);
		verify(normalIntent).setAction(RC.action.fromNotification.REMOVE_MULTI_RESULT);
		verify(normalIntent).putExtra(eq(RC.actionExtras.fromNotifications.removeMultiResult.GEOFENCE_EVENT), any(Parcelable.class));
		verify(normalIntent).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}
}