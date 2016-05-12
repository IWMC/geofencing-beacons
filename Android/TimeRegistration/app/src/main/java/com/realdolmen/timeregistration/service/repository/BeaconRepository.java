package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.BeaconAction;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconRepository extends DataRepository<BeaconAction, BeaconAction, BeaconAction> {

	private static final String TAG = BeaconRepository.class.getSimpleName();

	private Map<Region, BeaconAction> regionMap = new HashMap<>();

	private Context context;

	private void initializeBeacons() {
		for (BeaconAction beaconAction : data) {
			Region region = new Region(UUID.randomUUID().toString(), Identifier.parse(RC.beacon.UUID), Identifier.parse(beaconAction.getId().toString()), null);
			regionMap.put(region, beaconAction);
		}
	}

	public Map<Region, BeaconAction> getRegionMap() {
		return regionMap;
	}

	@Override
	protected void setup(Context context) {
		this.context = context;
		initializeBeacons();
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
