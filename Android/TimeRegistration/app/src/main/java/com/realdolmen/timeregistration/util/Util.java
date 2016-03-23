package com.realdolmen.timeregistration.util;

public class Util {
	public static void requireNonNull(Object o, String message) {
		if (o == null)
			throw new NullPointerException(message);
	}

	public static void requireNonNull(Object o) {
		requireNonNull(o, "NonNull required!");
	}
}
