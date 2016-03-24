package com.realdolmen.timeregistration.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.realdolmen.timeregistration.R;
import com.realdolmen.timeregistration.service.GenericVolleyError;
import com.realdolmen.timeregistration.service.ResultCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DateUtil {

	private static final DateTimeFormatter nameOfDayDateFormat = DateTimeFormat.forPattern("EEEE");
	private static final DateTimeFormatter hourFormat24Hours = DateTimeFormat.forPattern("HH:mm");
	private static final DateTimeFormatter hourFormatAmPm = DateTimeFormat.forPattern("hh:mm a");
	private static final DateTimeFormatter dayFormat = DateTimeFormat.forPattern("EEEE dd MMMM");
	private static final DateTimeFormatter dayMonthFormat = DateTimeFormat.forPattern("d MMM");

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
			DateTime date = now().minusDays(i);
			if (date.get(DateTimeFieldType.dayOfWeek()) == DateTimeConstants.SUNDAY
					|| date.get(DateTimeFieldType.dayOfWeek()) == DateTimeConstants.SATURDAY) {
				span += 1;
				continue;
			}
			pastWeek.add(date);
		}

		Collections.reverse(pastWeek);
		return pastWeek;
	}

	public static String formatToHours(@NonNull DateTime date) {
		return hourFormat24Hours.print(date);
	}

	public static String formatToHours(@NonNull DateTime date, boolean is24Hours) {
		return is24Hours ? hourFormat24Hours.print(date) : hourFormatAmPm.print(date);
	}

	public static String nameForDate(@NonNull Context context, @NonNull DateTime date) {
		if (isToday(date)) {
			return context.getString(R.string.date_today);
		}
		return nameOfDayDateFormat.print(date) + " " + dayMonthFormat.print(date);
	}

	public static String formatToDay(DateTime date) {
		return dayFormat.print(date);
	}

	public static DateTime today() {
		return now().withTimeAtStartOfDay();
	}

	public static DateTime now() {
		return DateTime.now(DateTimeZone.UTC);
	}

	public static void enforceUTC(@UTC DateTime date, String message) {
		if (date.getZone() != DateTimeZone.UTC) {
			throw new IllegalStateException(message);
		}
	}

	public static void enforceUTC(@UTC DateTime date) {
		enforceUTC(date, "Date must be in UTC format!");
	}

	public static void enforceNotUTC(DateTime date) {
		enforceUTC(date, "Date must not be in UTC format!");
	}

	public static void enforceNotUTC(DateTime date, String message) {
		if (date.getZone() == DateTimeZone.UTC) {
			throw new IllegalStateException(message);
		}
	}

	public static DateTime toLocal(@UTC DateTime date) {
		enforceUTC(date);
		return date.toDateTime(DateTimeZone.getDefault());
	}

	public static DateTime toUTC(DateTime date) {
		return date.toDateTime(DateTimeZone.UTC);
	}

	/**
	 * Makes sure a given date is in UTC format. If a callback is specified it will call it with a FAIL result
	 * and a {@link GenericVolleyError} with cause {@link IllegalStateException}. If none is specified
	 * an {@link IllegalStateException} will be thrown.
	 *
	 * @param date     The date to check
	 * @param message  The message to use in the exception.
	 * @param callback The optional callback that will be called in event of
	 * @return False when the date is UTC. True when it is not and an exception has occured.
	 */
	public static boolean enforceUTC(DateTime date, String message, @Nullable ResultCallback<?> callback) {
		if (date.getZone() != DateTimeZone.UTC) {

			if (callback != null) {
				callback.onResult(ResultCallback.Result.FAIL, null, new GenericVolleyError(new IllegalStateException(message)));
				return true;
			} else {
				throw new IllegalStateException(message);
			}
		}
		return false;
	}

	public static boolean enforceUTC(DateTime date, @Nullable ResultCallback<?> callback) {
		return enforceUTC(date, "Date should be in UTC zone!", callback);
	}
}
