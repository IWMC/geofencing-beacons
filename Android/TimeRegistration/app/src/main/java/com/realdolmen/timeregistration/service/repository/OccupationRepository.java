package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.ResultCallback;

import java.util.List;

public class OccupationRepository extends DataRepository<Occupation, Occupation, Occupation> {

	OccupationRepository(Context context, final LoadCallback callback) {
		BackendService.with(context).getRelevantOccupations(new ResultCallback<List<Occupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<Occupation> data, @Nullable VolleyError error) {
				if(result == Result.SUCCESS) {
					OccupationRepository.this.data.clear();
					OccupationRepository.this.data.addAll(data);
					setLoaded(true, null);
					if (callback != null)
						callback.onResult(LoadCallback.Result.SUCCESS, null);
				} else if (callback != null) {
					setLoaded(false, error);
					callback.onResult(LoadCallback.Result.FAIL, error);
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

}
