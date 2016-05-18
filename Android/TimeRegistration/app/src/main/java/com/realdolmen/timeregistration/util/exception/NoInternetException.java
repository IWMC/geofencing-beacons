package com.realdolmen.timeregistration.util.exception;

public class NoInternetException extends Exception {
	public NoInternetException() {
	}

	public NoInternetException(String detailMessage) {
		super(detailMessage);
	}

	public NoInternetException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
