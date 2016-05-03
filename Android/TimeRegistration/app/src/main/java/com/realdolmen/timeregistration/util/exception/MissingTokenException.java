package com.realdolmen.timeregistration.util.exception;

public class MissingTokenException extends IllegalStateException {
	public MissingTokenException(String detailMessage) {
		super(detailMessage);
	}

	public MissingTokenException() {
	}
}
