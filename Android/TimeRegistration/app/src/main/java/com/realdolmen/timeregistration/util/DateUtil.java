package com.realdolmen.timeregistration.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by BCCAZ45 on 8/03/2016.
 */
public class DateUtil {

    private static final SimpleDateFormat nameOfDayDateFormat = new SimpleDateFormat("EEEE");

    public static boolean isToday(Date date) {
        return DateUtils.isSameDay(date, new Date());
    }

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

    public static String nameForDate(Date date) {
        if (isToday(date)) {
            return "Today";
        }
        return nameOfDayDateFormat.format(date);
    }
}
