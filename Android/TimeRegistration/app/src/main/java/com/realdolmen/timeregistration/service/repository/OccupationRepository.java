package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;
import com.google.android.gms.location.Geofence;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.List;

public class OccupationRepository extends DataRepository<Occupation, Occupation, Occupation> {

	private static final String TAG = OccupationRepository.class.getSimpleName();

	/**
	 * Calls {@link OccupationRepository#reload(Context)} to initially load the data. Upon completion,
	 * the optional {@link LoadCallback#onResult(LoadCallback.Result, Throwable)} is called.
	 *
	 * @param context  The {@link Context} used with the {@link BackendService}.
	 * @param callback The optional {@link LoadCallback} to be used when data loading is complete.
	 */
	OccupationRepository(@NonNull Context context, final @Nullable LoadCallback callback) {
		super(context);
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

	public List<Project> getAllProjects() {
		List<Project> out = new ArrayList<>();
		for (Occupation occupation : data) {
			if (occupation instanceof Project) {
				out.add((Project) occupation);
			}
		}
		return out;
	}

	/**
	 * The app is not allowed to save occupations to the backend. Therefore it is not implemented.
	 *
	 * @param context  /
	 * @param element  /
	 * @param callback /
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void save(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be added!");
	}

	/**
	 * The app is not allowed to remove occupations from the backend. Therefore it is not implemented.
	 *
	 * @param context  /
	 * @param element  /
	 * @param callback /
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void remove(@NonNull Context context, @NonNull Occupation element, @Nullable ResultCallback<Occupation> callback) {
		throw new UnsupportedOperationException("Occupations cannot be removed!");
	}

	/**
	 * Reloads the data using the backend. <b>Upon a successful retrieval</b> of occupations from
	 * the backend, the backing list will be cleared and all newly retrieved occupations will be
	 * added. After that, the {@link Promise} is resolved.
	 * <p/>
	 * If the <b>retrieval fails</b>, the {@code Promise} will be rejected.
	 *
	 * @param context The {@link Context} to use for the {@link BackendService}.
	 * @return The promise.
	 */
	@Override
	public Promise<OccupationRepository, VolleyError, Object> reload(final Context context) {
		final Deferred<OccupationRepository, VolleyError, Object> def = new DeferredObject<>();
		BackendService.with(context).getRelevantOccupations(new ResultCallback<List<Occupation>>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable List<Occupation> data, @Nullable VolleyError error) {
				if (result == Result.SUCCESS) {
					OccupationRepository.this.data.clear();
					OccupationRepository.this.data.addAll(data);
					setLoaded(true, null);
				} else {
					setLoaded(false, error);
					def.reject(error);
				}
			}
		});
		return def.promise();
	}

	@Override
	public Occupation getById(long id) {
		for(Occupation o : data) {
			if(o.getId() == id) return o;
		}
		return null;
	}

	public Project getByGeofence(Geofence geofence) {
		for (Project p : getAllProjects()) {
			if (p.getGeofences().contains(geofence))
				return p;
		}
		return null;
	}
}
