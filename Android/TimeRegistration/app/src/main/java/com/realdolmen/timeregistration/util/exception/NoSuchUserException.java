package com.realdolmen.timeregistration.util.exception;

public class NoSuchUserException extends IllegalStateException {
	public NoSuchUserException(String detailMessage) {
		super(detailMessage);
	}

	public NoSuchUserException() {
	}
}
