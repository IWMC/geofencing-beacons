package com.realdolmen.timeregistration.service.location.beacon;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.service.data.UserManager;
import com.realdolmen.timeregistration.service.repository.BeaconRepository;
import com.realdolmen.timeregistration.service.repository.RegisteredOccupationRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.ui.login.LoginActivity;
import com.realdolmen.timeregistration.util.exception.MissingTokenException;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

public class BeaconDwellService2 extends Service implements Runnable, BeaconConsumer {

	private static final String TAG = BeaconDwellService2.class.getSimpleName();

	private BackgroundPowerSaver beaconPowerSaver;
	private static final BeaconParser parser = new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24");

	private Handler timerHandler = new Handler();
	private android.os.Binder binder = new Binder();
	private BeaconEventHandler eventHandler;
	private BeaconListener listener;
	private BeaconManager beaconManager;
	private boolean initialized = false;

	private static final int PROCESS_DELAY = 2000;
	private int failedLogins = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void run() {
		eventHandler.process();
		timerHandler.postDelayed(BeaconDwellService2.this, PROCESS_DELAY);
	}

	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setMonitorNotifier(listener);
		beaconManager.setRangeNotifier(listener);
		try {
			for (Region r : repo().getRegionMap().keySet()) {
				beaconManager.stopMonitoringBeaconsInRegion(r);
				beaconManager.stopRangingBeaconsInRegion(r);

				beaconManager.startRangingBeaconsInRegion(r);
				beaconManager.startMonitoringBeaconsInRegion(r);
			}
		} catch (Exception e) {
			Log.e(TAG, "onBeaconServiceConnect: error when trying to add regions to monitors", e);
		}
	}

	private BeaconRepository repo() {
		return Repositories.beaconRepository();
	}

	public void positiveLoginResult() {
		init();
	}

	public class Binder extends android.os.Binder {
		public BeaconDwellService2 getService() {
			return BeaconDwellService2.this;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (RC.beacon.SAVE_POWER) {
			beaconPowerSaver = new BackgroundPowerSaver(getApplicationContext());
			Log.d(TAG, "onCreate: Enabling power saving...");
		}
		init();
	}

	public void init() {
		if (initialized || failedLogins >= 3) {
			return;
		}
		Log.d(TAG, "init: Loading beacon repo");
		loadRepo().done(new DoneCallback<BeaconRepository>() {
			@Override
			public void onDone(BeaconRepository result) {
				failedLogins = 0;
				initialized = true;
				eventHandler = new BeaconEventHandler(getApplicationContext());
				listener = new RDBeaconListener(eventHandler);
				Log.d(TAG, "onDone: Starting beacon monitor");
				beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
				beaconManager.bind(BeaconDwellService2.this);
				if (!beaconManager.getBeaconParsers().contains(parser)) {
					beaconManager.getBeaconParsers().add(parser);
				}
				timerHandler.postDelayed(BeaconDwellService2.this, PROCESS_DELAY);
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				if (result instanceof MissingTokenException) {
					failedLogins += 1;
					tryResolve();
				}
			}
		});
	}

	private void tryResolve() {
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.setAction(RC.action.login.RE_AUTHENTICATION);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		if (beaconManager != null && beaconManager.isAnyConsumerBound()) {
			beaconManager.unbind(this);
		}

		if (eventHandler != null) {
			eventHandler.onDestroy();
		}
		super.onDestroy();
	}

	public Promise<BeaconRepository, Throwable, Void> loadRepo() {
		final Deferred<BeaconRepository, Throwable, Void> def = new DeferredObject<>();
		UserManager.with(getApplicationContext()).checkLocalLogin().done(new DoneCallback<Void>() {
			@Override
			public void onDone(Void result) {
				Repositories.loadBeaconRepository(getApplicationContext()).done(new DoneCallback<BeaconRepository>() {
					@Override
					public void onDone(final BeaconRepository result) {
						Repositories.loadRegisteredOccupationRepository(getApplicationContext()).done(new DoneCallback<RegisteredOccupationRepository>() {
							@Override
							public void onDone(RegisteredOccupationRepository r2) {
								def.resolve(result);
							}
						}).fail(new FailCallback<Object>() {
							@Override
							public void onFail(Object result) {
								def.reject(new IllegalStateException("Could not load registered occupations!"));
							}
						});
					}
				}).fail(new FailCallback<Throwable>() {
					@Override
					public void onFail(Throwable result) {
						def.reject(result);
					}
				});
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				def.reject(result);
			}
		});
		return def.promise();
	}

}

