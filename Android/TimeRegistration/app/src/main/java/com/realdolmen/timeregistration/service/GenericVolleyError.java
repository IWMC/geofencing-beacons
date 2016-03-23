package com.realdolmen.timeregistration.service;

import com.android.volley.VolleyError;

public class GenericVolleyError extends VolleyError {
	public GenericVolleyError(String exceptionMessage) {
		super(exceptionMessage);
	}

	public GenericVolleyError(Throwable t) {
		super(t);
	}
}
