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

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisteredOccupationRepository extends DataRepository<RegisteredOccupation, Integer, Long> {

	private static final String LOG_TAG = RegisteredOccupationRepository.class.getSimpleName();

	@UTC
	private final Map<DateTime, List<RegisteredOccupation>> dataByDate = new HashMap<>();

	/**
	 * Calls {@link RegisteredOccupationRepository#reload(Context)} to initially load the data. Upon completion,
	 * the optional {@link LoadCallback#onResult(LoadCallback.Result, Throwable)} is called.
	 *
	 * @param context  The {@link Context} used with the {@link BackendService}.
	 * @param callback The optional {@link LoadCallback} to be used when data loading is complete.
	 */
	RegisteredOccupationRepository(@NonNull Context context, @Nullable final LoadCallback callback) {
		reload(context).done(new DoneCallback<RegisteredOccupationRepository>() {
			@Override
			public void onDone(RegisteredOccupationRepository result) {
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

	/**
	 * Saves a {@link RegisteredOccupation} to the backend. If the {@code RegisteredOccupation} does
	 * not yet exist, it will be persisted as a new one. If it does, it is merged with the remote version.
	 * <p/>
	 * If the request succeeds, the {@code element}'s ID is filled in with the returned ID. Afterwards
	 * it is added to the backing data list and {@link #invalidateData()} is called.
	 *
	 * @param context  The {@link Context} to use with {@link BackendService}.
	 * @param element  The {@link RegisteredOccupation} to save to the backend.
	 * @param callback The optional {@link ResultCallback<Long>} called upon completion.
	 */
	@Override
	public void save(@NonNull Context context, @NonNull final RegisteredOccupation element, @Nullable final ResultCallback<Long> callback) {
		BackendService.with(context).saveOccupation(data.contains(element), element, new ResultCallback<Long>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Long data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					if (data != null) {
						element.setId(data);
						RegisteredOccupationRepository.this.data.add(element);
					}

					invalidateData();
				}
				if (callback != null)
					callback.onResult(result, data, error);
			}
		});
		//TODO: Make use of local database
	}

	/**
	 * Clears the lazy caching map used to group {@link RegisteredOccupation}s by {@link DateTime}.
	 * This should only be called internally.
	 */
	private void invalidateData() {
		dataByDate.clear();
	}

	/**
	 * Retrieves the {@link RegisteredOccupation}s who fall in the same day as the given {@code date}.
	 * <p/>
	 * After the first calculation of this subset it is stored in a map by the given date.
	 *
	 * @param date The {@code DateTime} in {@link org.joda.time.DateTimeZone#UTC} format.
	 * @return The resulting unmodifiable subset of {@code RegisteredOccupation}s grouped by {@code date}.
	 */
	public List<RegisteredOccupation> getAll(@UTC @NonNull DateTime date) {
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
	 * If the HTTP status code is {@code 204 NO CONTENT} the {@code element} is removed
	 * from the backing list and {@link #invalidateData()} is called.
	 *
	 * @param context  The {@link Context} used with the {@link BackendService}.
	 * @param element  The {@link RegisteredOccupation} to remove.
	 * @param callback The optional {@link LoadCallback} to be used when the operation is complete.
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

					if (callback != null)
						callback.onResult(Result.SUCCESS, resultData, null);
				} else {
					if (callback != null)
						callback.onResult(Result.FAIL, resultData, error);
				}
			}
		});
	}

	/**
	 * Reloads the data using the backend. <b>Upon a successful retrieval</b> of registered occupations from
	 * the backend, the backing list will be cleared and all newly retrieved registrations will be
	 * added. After that, the {@link Promise} is resolved.
	 * <p/>
	 * If the <b>retrieval fails</b>, the {@code Promise} will be rejected.
	 *
	 * @param context The {@link Context} to use for the {@link BackendService}.
	 * @return The promise.
	 */
	@Override
	public Promise<RegisteredOccupationRepository, VolleyError, Object> reload(Context context) {
		final Deferred<RegisteredOccupationRepository, VolleyError, Object> def = new DeferredObject<>();
		BackendService.with(context).getRegisteredOccupationsRangeUntilNow(DateUtil.today(), 7, new ResultCallback<List<RegisteredOccupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<RegisteredOccupation> data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					RegisteredOccupationRepository.this.data.clear();
					invalidateData();
					RegisteredOccupationRepository.this.data.addAll(data);
					setLoaded(true, null);
					def.resolve(RegisteredOccupationRepository.this);
				} else {
					def.reject(error);
				}
			}
		});
		return def.promise();
	}

	public Promise<Integer, VolleyError, Object> confirmDate(@NonNull Context context, @UTC final DateTime date) {
		final Deferred<Integer, VolleyError, Object> def = new DeferredObject<>();
		BackendService.with(context).confirmOccupations(date, new ResultCallback<Integer>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable Integer data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					for (RegisteredOccupation ro : getAll(date)) {
						ro.confirm();
					}
					def.resolve(data);
				} else
					def.reject(error);
			}
		});
		return def.promise();
	}

	public RegisteredOccupation getById(long id) {
		for(RegisteredOccupation o : data) {
			if(o.getId() == id) return o;
		}
		return null;
	}
}
