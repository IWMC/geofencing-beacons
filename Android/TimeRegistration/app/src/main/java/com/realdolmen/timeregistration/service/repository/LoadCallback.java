package com.realdolmen.timeregistration.service.repository;

/**
 * Created by BCCAZ45 on 21/03/2016.
 */
public interface LoadCallback {
	enum Result {
		SUCCESS, FAIL
	}

	void onResult(Result result, Throwable error);
}
