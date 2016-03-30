package com.realdolmen.timeregistration.service.repository;

public interface LoadCallback {

	enum Result {
		SUCCESS, FAIL
	}

	/**
	 * Callback method.
	 *
	 * @param result When the operation has a positive result, {@link Result#SUCCESS} should be used.
	 *               Otherwise {@link Result#FAIL} should be used.
	 * @param error  When the result is {@link Result#SUCCESS} the error is {@code null}. Otherwise it should be filled in.
	 */
	void onResult(Result result, Throwable error);
}
