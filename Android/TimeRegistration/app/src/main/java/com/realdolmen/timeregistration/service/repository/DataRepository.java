package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.realdolmen.timeregistration.service.ResultCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Abstract class designed to provide a middleware to communicate between UI and backend/local database.
 */
public abstract class DataRepository<E> {

	protected final List<E> data = new ArrayList<>();

	private boolean loaded;

	private Queue<LoadCallback> callbacksOnLoaded = new LinkedList<>();

	public List<E> getAll() {
		return Collections.unmodifiableList(data);
	}

	public abstract void save(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<E> callback);

	public E get(int index) {
		return data.get(index);
	}

	public int size() {
		return data.size();
	}

	public abstract void remove(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<E> callback);

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
}
