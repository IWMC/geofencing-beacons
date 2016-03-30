package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

public class OccupationRepository extends DataRepository<Occupation, Occupation, Occupation> {

	OccupationRepository(Context context, final LoadCallback callback) {
		reload(context).done(new DoneCallback<OccupationRepository>() {
			@Override
			public void onDone(OccupationRepository result) {
				if (callback != null)
					callback.onResult(LoadCallback.Result.SUCCESS, null);
			}
		}).fail(new FailCallback<VolleyError>() {
			@Override
			public void onFail(VolleyError result) {
				if (callback != null) {
					setLoaded(false, result);
					callback.onResult(LoadCallback.Result.FAIL, result);
				}
			}
		});
	}

	@Override
	public void save(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be added!");
	}

	@Override
	public void remove(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be removed!");
	}

	@Override
	public Promise<OccupationRepository, VolleyError, Object> reload(Context context) {
		final Deferred<OccupationRepository, VolleyError, Object> def = new DeferredObject<>();
		BackendService.with(context).getRelevantOccupations(new ResultCallback<List<Occupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<Occupation> data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					OccupationRepository.this.data.clear();
					OccupationRepository.this.data.addAll(data);
					setLoaded(true, null);
					def.resolve(OccupationRepository.this);
				} else {
					def.reject(error);
				}
			}
		});
		return def.promise();
	}
}
