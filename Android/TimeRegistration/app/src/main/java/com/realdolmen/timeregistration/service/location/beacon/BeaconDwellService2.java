package com.realdolmen.timeregistration.service.location.beacon;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.realdolmen.timeregistration.service.data.UserManager;
import com.realdolmen.timeregistration.service.repository.BeaconRepository;
import com.realdolmen.timeregistration.service.repository.Repositories;
import com.realdolmen.timeregistration.util.exception.MissingTokenException;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

public class BeaconDwellService2 extends Service implements Runnable, BeaconConsumer {

	private static final String TAG = BeaconDwellService.class.getSimpleName();

	private BackgroundPowerSaver beaconPowerSaver = new BackgroundPowerSaver(getApplicationContext());

	private Handler timerHandler = new Handler();
	private android.os.Binder binder = new Binder();
	private BeaconEventHandler eventHandler;
	private BeaconListener listener;
	private BeaconManager beaconManager;
	private boolean initialized = false;

	private static final int PROCESS_DELAY = 2000;

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
		init();
	}

	public void init() {
		if(initialized) {
			return;
		}
		Log.d(TAG, "init: Loading beacon repo");
		loadRepo().done(new DoneCallback<BeaconRepository>() {
			@Override
			public void onDone(BeaconRepository result) {
				initialized = true;
				eventHandler = new BeaconEventHandler(getApplicationContext());
				listener = new RDBeaconListener(eventHandler);
				Log.d(TAG, "onDone: Starting beacon monitor");
				beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
				beaconManager.bind(BeaconDwellService2.this);
				timerHandler.postDelayed(BeaconDwellService2.this, PROCESS_DELAY);
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				if(result instanceof MissingTokenException) {
					tryResolve();
				}
			}
		});
	}

	private void tryResolve() {

	}

	@Override
	public void onDestroy() {
		if(beaconManager != null && beaconManager.isAnyConsumerBound()) {
			beaconManager.unbind(this);
		}

		if(eventHandler != null) {
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
					public void onDone(BeaconRepository result) {
						def.resolve(result);
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

