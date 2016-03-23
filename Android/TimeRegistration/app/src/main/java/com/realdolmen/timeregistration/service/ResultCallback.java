package com.realdolmen.timeregistration.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;

/**
 * Interface designed to implement callbacks for the UI to respond to network events.
 *
 * @param <E> Generic type of the onSuccess data parameter.
 */
public interface ResultCallback<E> {

	enum Result {
		SUCCESS, FAIL
	}

	/**
	 * @param result {@link Result#SUCCESS} when the operation succeeded.
	 * @param data   The produced data.
	 * @param error  The error that is thrown when the operation fails. This field will be null if the operation succeeds.
	 */
	void onResult(@NonNull Result result, @Nullable E data, @Nullable VolleyError error);

}
