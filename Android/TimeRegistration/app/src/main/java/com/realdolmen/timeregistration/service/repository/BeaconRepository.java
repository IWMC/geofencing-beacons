package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.realdolmen.timeregistration.model.Beacon;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

public class BeaconRepository extends DataRepository<Beacon, Beacon, Beacon> {

	public BeaconRepository(Context context, final LoadCallback callback) {
		reload(context).done(new DoneCallback<BeaconRepository>() {
			@Override
			public void onDone(BeaconRepository result) {
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
	public void save(@NonNull Context context, @NonNull Beacon element, @Nullable ResultCallback<Beacon> callback) {

	}

	@Override
	public void remove(@NonNull Context context, @NonNull Beacon element, @Nullable ResultCallback<Beacon> callback) {

	}

	@Override
	public Promise<BeaconRepository, Throwable, Object> reload(Context context) {
		final Deferred<BeaconRepository, Throwable, Object> def = new DeferredObject<>();
		BackendService.with(context).getBeacons().done(new DoneCallback<List<Beacon>>() {
			@Override
			public void onDone(List<Beacon> result) {
				data.clear();
				data.addAll(result);
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
	public Beacon getById(long id) {
		for (Beacon beacon : data) {
			if(beacon.getId() == id) {
				return beacon;
			}
		}

		return null;
	}
}
