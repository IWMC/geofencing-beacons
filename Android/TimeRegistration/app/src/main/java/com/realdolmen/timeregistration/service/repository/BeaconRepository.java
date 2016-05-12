package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.service.location.beacon.BeaconListener;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconRepository extends DataRepository<BeaconAction, BeaconAction, BeaconAction> {

	private static final String TAG = BeaconRepository.class.getSimpleName();

	private Region realDolmenRegion;

	private List<BeaconListener> observers = new ArrayList<>();

	private Map<Region, BeaconAction> regionMap = new HashMap<>();

	private BeaconConsumer consumer;
	private Context context;

	private BeaconListener defaultListener = new BeaconListener() {
		@Override
		public void didEnterRegion(Region region) {
			onEnterEvent(region);
		}

		@Override
		public void didExitRegion(Region region) {
			onExitEvent(region);
		}

		@Override
		public void didRangeBeaconsInRegion(Collection<org.altbeacon.beacon.Beacon> collection, Region region) {
			onRangeEvent(region, collection);
		}
	};

	private void initializeBeacons() {
		for (BeaconAction beaconAction : data) {
			Region region = new Region(UUID.randomUUID().toString(), Identifier.parse(RC.beacon.UUID), Identifier.parse(beaconAction.getId().toString()), null);
			regionMap.put(region, beaconAction);
		}
	}

	public Map<Region, BeaconAction> getRegionMap() {
		return regionMap;
	}

	public BeaconManager getBeaconManager() {
		return BeaconManager.getInstanceForApplication(context);
	}

	public void subscribe(BeaconListener listener) {
		if (!observers.contains(listener))
			observers.add(listener);
	}

	public void unsubscribe(BeaconListener listener) {
		if (observers.contains(listener))
			observers.remove(listener);
	}

	@Override
	protected void setup(Context context) {
		this.context = context;
		realDolmenRegion = new Region(UUID.randomUUID().toString(), Identifier.parse(RC.beacon.UUID), null, null);
		getBeaconManager().getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
		initializeBeacons();
	}

	public Region getRegion() {
		return realDolmenRegion;
	}

	public void startMonitor() throws RemoteException {
		Log.d(TAG, "startMonitor: starting beacon monitor");
		getBeaconManager().setMonitorNotifier(defaultListener);
		getBeaconManager().setRangeNotifier(defaultListener);
		for(Region r : regionMap.keySet()) {
			getBeaconManager().startRangingBeaconsInRegion(r);
			getBeaconManager().startMonitoringBeaconsInRegion(r);
		}

	}

	public void stopMonitor() throws RemoteException {
		Log.d(TAG, "stopMonitor: stopping beacon monitor");
		for(Region r : regionMap.keySet()) {
			getBeaconManager().stopRangingBeaconsInRegion(r);
			getBeaconManager().stopMonitoringBeaconsInRegion(r);
		}
	}

	public BeaconRepository(final Context context, final LoadCallback callback) {
		super(context);
		reload(context).done(new DoneCallback<BeaconRepository>() {
			@Override
			public void onDone(BeaconRepository result) {
				setup(context);
				if (callback != null)
					callback.onResult(LoadCallback.Result.SUCCESS, null);

			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				if (callback != null) {
					setLoaded(false, result);
					callback.onResult(LoadCallback.Result.FAIL, result);
				}
			}
		});
	}

	@Override
	public void save(@NonNull Context context, @NonNull BeaconAction element, @Nullable ResultCallback<BeaconAction> callback) {

	}

	@Override
	public void remove(@NonNull Context context, @NonNull BeaconAction element, @Nullable ResultCallback<BeaconAction> callback) {

	}

	@Override
	public void clear() {
		super.clear();
	}


	public Promise onEnterEvent(Region region) {
		final Deferred def = new DeferredObject<>();
		for (BeaconListener l : observers) {
			l.didEnterRegion(region);
		}
		if (regionMap.containsKey(region)) {
			BeaconAction beaconAction = regionMap.get(region);
			for (BeaconListener l : observers) {
				l.onEnterOccupation(new ArrayList<>(beaconAction.getOccupations()));
			}
		}
		return def.promise();
	}

	public Promise onExitEvent(Region region) {
		final Deferred def = new DeferredObject<>();
		for (BeaconListener l : observers) {
			l.didExitRegion(region);
		}

		if (regionMap.containsKey(region)) {
			BeaconAction beaconAction = regionMap.get(region);
			for (BeaconListener l : observers) {
				l.onExitOccupation(new ArrayList<>(beaconAction.getOccupations()));
			}
		}
		return def.promise();
	}

	public Promise onRangeEvent(Region r, Collection<org.altbeacon.beacon.Beacon> beacons) {
		final Deferred def = new DeferredObject<>();
		for (BeaconListener l : observers) {
			l.didRangeBeaconsInRegion(beacons, r);
		}
		return def.promise();
	}

	@Override
	public Promise<BeaconRepository, Throwable, Object> reload(Context context) {
		final Deferred<BeaconRepository, Throwable, Object> def = new DeferredObject<>();
		BackendService.with(context).getBeacons().done(new DoneCallback<List<BeaconAction>>() {
			@Override
			public void onDone(List<BeaconAction> result) {
				clear();
				data.addAll(result);
				setLoaded(true, null);
				def.resolve(BeaconRepository.this);
			}
		}).fail(new FailCallback<Throwable>() {
			@Override
			public void onFail(Throwable result) {
				def.reject(result);
			}
		});
		return def.promise();
	}

	@Override
	public BeaconAction getById(long id) {
		for (BeaconAction beaconAction : data) {
			if (beaconAction.getId() == id) {
				return beaconAction;
			}
		}

		return null;
	}

	public BeaconAction getByRegion(Region region) {
		return regionMap.get(region);
	}
}
