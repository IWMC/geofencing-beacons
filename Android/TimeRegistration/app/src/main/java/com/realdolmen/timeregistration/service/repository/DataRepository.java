package com.realdolmen.timeregistration.service.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.realdolmen.timeregistration.service.ResultCallback;

import java.util.List;

/**
 * Interface designed to provide a middleware to communicate between UI and backend/local database.
 */
public interface DataRepository<E> {
	List<E> getAll();
	void save(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<E> callback);
	E get(int index);
	int size();
	void remove(@NonNull Context context, @NonNull E element, @Nullable ResultCallback<E> callback);
	boolean hasLoaded();
}
