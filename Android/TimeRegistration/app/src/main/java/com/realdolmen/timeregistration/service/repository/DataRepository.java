package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Abstract class designed to provide a middleware to communicate between UI and backend/local database.
 */
public abstract class DataRepository<E, RM_OUT, SAVE_OUT> {

	protected final List<E> data = new ArrayList<>();

	private boolean loaded;

	private Queue<LoadCallback> callbacksOnLoaded = new LinkedList<>();

	public List<E> getAll() {
		return Collections.unmodifiableList(data);
	}

	public abstract void save(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<SAVE_OUT> callback);

	public E get(int index) {
		return data.get(index);
	}

	public int size() {
		return data.size();
	}

	public abstract void remove(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<RM_OUT> callback);

	public boolean isLoaded() {
		return loaded;
	}

	public void addOnLoadCallback(LoadCallback callback) {
		if (isLoaded()) {
			callback.onResult(LoadCallback.Result.SUCCESS, null);
		} else {
			callbacksOnLoaded.add(callback);
		}
	}

	protected void setLoaded(boolean isLoaded, @Nullable Throwable throwable) {
		loaded = isLoaded;
		while (!callbacksOnLoaded.isEmpty()) {
			callbacksOnLoaded.poll().onResult(isLoaded ? LoadCallback.Result.SUCCESS : LoadCallback.Result.FAIL, throwable);
		}
	}

	public Promise<RM_OUT, VolleyError, Object> remove(@NonNull Context context, E element) {
		final Deferred<RM_OUT, VolleyError, Object> def = new DeferredObject<>();
		remove(context, element, new ResultCallback<RM_OUT>() {
			@Override
			public void onResult(@NonNull Result result, @Nullable RM_OUT data, @Nullable VolleyError error) {
				if(result == Result.SUCCESS)
					def.resolve(data);
				else
					def.reject(error);
			}
		});
		return def.promise();
	}

	public abstract Promise<? extends DataRepository, VolleyError, Object> reload(Context context);

	public abstract E getById(long id);

	public void clear() {
		setLoaded(false, null);
		data.clear();
	}
}
