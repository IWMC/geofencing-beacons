package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.RegisteredOccupation;
import com.realdolmen.timeregistration.service.ResultCallback;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisteredOccupationRepository extends DataRepository<RegisteredOccupation, Integer, Long> {

	private static final String LOG_TAG = RegisteredOccupationRepository.class.getSimpleName();


	private final
	@UTC
	Map<DateTime, List<RegisteredOccupation>> dataByDate = new HashMap<>();

	RegisteredOccupationRepository(@NonNull Context context, @Nullable final LoadCallback callback) {
		BackendService.with(context).getRegisteredOccupationsRangeUntilNow(DateUtil.today(), 7, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					RegisteredOccupationRepository.this.data.addAll(data);
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
	public void save(@NonNull Context context, @NonNull final RegisteredOccupation element, @Nullable final ResultCallback<Long> callback) {
		data.add(element);
		invalidateData();
		BackendService.with(context).registerOccupation(element, new ResultCallback<Long>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Long data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					element.setId(data);
				}
				callback.onResult(result, data, error);
			}
		});
		//TODO: Make use of local database
	}

	private void invalidateData() {
		dataByDate.clear();
	}

	public List<RegisteredOccupation> getAll(@UTC DateTime date) {
		DateUtil.enforceUTC(date);
		if (dataByDate.containsKey(date.withTimeAtStartOfDay())) {
			return Collections.unmodifiableList(dataByDate.get(date.withTimeAtStartOfDay()));
		}

		List<RegisteredOccupation> filtered = new ArrayList<>();
		for (RegisteredOccupation occ : data) {
			if (occ.getRegisteredStart().withTimeAtStartOfDay().isEqual(date.withTimeAtStartOfDay())) {
				filtered.add(occ);
			}
		}

		dataByDate.put(date.withTimeAtStartOfDay(), filtered);
		return Collections.unmodifiableList(filtered);
	}

	/**
	 * Removes a {@link RegisteredOccupation} from the database.
	 *
	 * @param context
	 * @param element
	 * @param callback
	 */
	@Override
	public void remove(@NonNull Context context, @NonNull final RegisteredOccupation element, @Nullable final ResultCallback<Integer> callback) {
		//TODO: Make use of local database
		BackendService.with(context).removeRegisteredOccupation(element.getId(), new ResultCallback<Integer>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Integer resultData, @Nullable VolleyError error) {
				Log.d(LOG_TAG, "Remove result is " + resultData + " and error: " + error);
				if (result == Result.SUCCESS) {
					if (resultData == 204) {
						data.remove(element);
						invalidateData();
					}
					callback.onResult(Result.SUCCESS, resultData, null);
				} else {
					callback.onResult(Result.FAIL, resultData, error);
				}
			}
		});
	}
}
