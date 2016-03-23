package com.realdolmen.timeregistration.service;

import com.android.volley.VolleyError;

/**
 * Created by Brent on 29/02/2016.
 */
public class GenericVolleyError extends VolleyError {
	public GenericVolleyError(String exceptionMessage) {
		super(exceptionMessage);
	}

	public GenericVolleyError(Throwable t) {
		super(t);
	}
}
