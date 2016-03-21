package com.realdolmen.timeregistration.util;

/**
 * Created by BCCAZ45 on 21/03/2016.
 */
public class Util {
	public static void requireNonNull(Object o, String message) {
		if (o == null)
			throw new NullPointerException(message);
	}

	public static void requireNonNull(Object o) {
		requireNonNull(o, "NonNull required!");
	}
}
