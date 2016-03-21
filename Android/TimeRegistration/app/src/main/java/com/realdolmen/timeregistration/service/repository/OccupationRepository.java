package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.service.ResultCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BCCAZ45 on 21/03/2016.
 */
public class OccupationRepository implements DataRepository<Occupation> {

	private List<Occupation> data = new ArrayList<>();
	private boolean loaded;

	OccupationRepository(Context context, final LoadCallback callback) {
		BackendService.with(context).getRelevantOccupations(new ResultCallback<List<Occupation>>() {
			@Override
			public void onSuccess(List<Occupation> data) {
				OccupationRepository.this.data.clear();
				OccupationRepository.this.data.addAll(data);
				if (callback != null)
					callback.onResult(LoadCallback.Result.SUCCESS, null);
				loaded = true;
			}

			@Override
			public void onError(VolleyError error) {
				if (callback != null)
					callback.onResult(LoadCallback.Result.FAIL, error);
			}
		});
	}

	@Override
	public List<Occupation> getAll() {
		return Collections.unmodifiableList(data);
	}

	@Override
	public void save(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be added!");
	}

	@Override
	public Occupation get(int index) {
		return data.get(index);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public void remove(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be removed!");
	}

	@Override
	public boolean hasLoaded() {
		return loaded;
	}

}
