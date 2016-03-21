package com.realdolmen.timeregistration.service;

import com.android.volley.VolleyError;

/**
 * Interface designed to implement callbacks for the UI to respond to network events.
 *
 * @param <E> Generic type of the onSuccess data parameter.
 */
public interface ResultCallback<E> {
	/**
	 * Called when the request succeeded. A request succeeds when the http response code is in the error range.
	 *
	 * @param data The data produced from the successful response.
	 */
	void onSuccess(E data);

	/**
	 * Called when the request fails. A request can fail if the http response code is in the
	 * error range or the response is invalid.
	 *
	 * @param error The error produces by Volley. There is also a {@link GenericVolleyError} for
	 *              custom errors in case of invalid responses.
	 */
	void onError(VolleyError error);
}
