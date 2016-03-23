package com.realdolmen.timeregistration.service.repository;

public interface LoadCallback {
	enum Result {
		SUCCESS, FAIL
	}

	void onResult(Result result, Throwable error);
}
