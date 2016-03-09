package com.realdolmen.timeregistration.util;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DateUtil {

	private static final SimpleDateFormat nameOfDayDateFormat = new SimpleDateFormat("EEEE");
	private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

	/**
	 * Uses the current time to check whether the specified date is on the same day.
	 *
	 * @param date The date to check against the current date.
	 * @return true if the specified date is the same as the current date, false if not.
	 */
	public static boolean isToday(@NonNull Date date) {
		return DateUtils.isSameDay(date, new Date());
	}

	/**
	 * Creates a reverse order list of the last 7 work days (monday-to-friday).
	 *
	 * @return reversed list of work dates.
	 */
	public static List<Date> pastWorkWeek() {
		int span = 6;
		List<Date> pastWeek = new ArrayList<>();
		for (int i = 0; i < span; i++) {
			Date date = DateUtils.addDays(new Date(), -i);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			int type = c.get(Calendar.DAY_OF_WEEK);
			if (type == Calendar.SATURDAY || type == Calendar.SUNDAY) {
				span += 1;
				continue;
			}
			pastWeek.add(date);
		}

		Collections.reverse(pastWeek);
		return pastWeek;
	}

	public static String formatToHours(@NonNull Date date) {
		return hourFormat.format(date);
	}

	public static String nameForDate(@NonNull Date date) {
		if (isToday(date)) {
			return "Today";
		}
		return nameOfDayDateFormat.format(date);
	}
}
