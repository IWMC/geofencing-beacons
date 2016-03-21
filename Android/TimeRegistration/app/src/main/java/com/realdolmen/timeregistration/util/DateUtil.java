package com.realdolmen.timeregistration.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DateUtil {

	private static final DateTimeFormatter nameOfDayDateFormat = DateTimeFormat.forPattern("EEEE");
	private static final DateTimeFormatter hourFormat24Hours = DateTimeFormat.forPattern("HH:mm");
	private static final DateTimeFormatter hourFormatAmPm = DateTimeFormat.forPattern("hh:mm a");
	private static final DateTimeFormatter dayFormat = DateTimeFormat.forPattern("EEEE dd MMMM");

	/**
	 * Uses the current time to check whether the specified date is on the same day.
	 *
	 * @param date The date to check against the current date.
	 * @return true if the specified date is the same as the current date, false if not.
	 */
	public static boolean isToday(@UTC @NonNull DateTime date) {
		DateUtil.enforceUTC(date);
		return date.withTimeAtStartOfDay().isEqual(today().withTimeAtStartOfDay());
	}

	/**
	 * Creates a reverse order list of the last 7 work days (monday-to-friday).
	 *
	 * @return reversed list of work dates.
	 */
	public static List<DateTime> pastWorkWeek() {
		int span = 6;
		List<DateTime> pastWeek = new ArrayList<>();
		for (int i = 0; i < span; i++) {
			DateTime date = today().minusDays(i);
			if(date.get(DateTimeFieldType.dayOfWeek()) == DateTimeConstants.SUNDAY
					|| date.get(DateTimeFieldType.dayOfWeek()) == DateTimeConstants.SATURDAY) {
				span += 1;
				continue;
			}
			pastWeek.add(date);
		}

		Collections.reverse(pastWeek);
		return pastWeek;
	}

	public static String formatToHours(@UTC @NonNull DateTime date) {
		DateUtil.enforceUTC(date);
		return hourFormat24Hours.print(date);
	}

	public static String formatToHours(@UTC @NonNull DateTime date, boolean is24Hours) {
		DateUtil.enforceUTC(date);
		return is24Hours ? hourFormat24Hours.print(date) : hourFormatAmPm.print(date);
	}

	public static String nameForDate(@UTC @NonNull DateTime date) {
		DateUtil.enforceUTC(date);
		if (isToday(date)) {
			return "Today";
		}
		return nameOfDayDateFormat.print(date);
	}

	public static String formatToDay(@UTC DateTime date) {
		DateUtil.enforceUTC(date);
		return dayFormat.print(date);
	}

	public static DateTime today() {
		return DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay();
	}

	public static void enforceUTC(@UTC DateTime date, String message) {
		System.out.println("Date zone: " + date.getZone());
		if(date.getZone() != DateTimeZone.UTC) {
			throw new IllegalStateException(message);
		}
	}

	public static void enforceUTC(@UTC DateTime date) {
		enforceUTC(date, "Date must be in UTC format!");
	}
}
