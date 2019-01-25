package com.apptech.android.bushero;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DepartureTimeParser {
    // We convert the Transport API "hh:mm" time into a timestamp we can actually use.
    public static long getTime(Bus bus) {
        String time = bus.getBestDepartureEstimate();

        int index = time.indexOf(":");
        if (index == -1) {
            return 0;
        }

        // get hours and minutes from string.
        String hoursStr = time.substring(0, index);
        String minutesStr = time.substring(index + 1);
        int hours = Integer.parseInt(hoursStr);
        int minutes = Integer.parseInt(minutesStr);

        // set this current date/time.
        Calendar now = GregorianCalendar.getInstance();

        // if this bus is due on a different date then transport api gives us a date object,
        // otherwise it just defaults to today.
        String date = bus.getDate();
        if (date != null && date.length() > 0) {
            String[] tokens = date.split("-");
            if (tokens.length == 3) {
                try {
                    int year = Integer.parseInt(tokens[0]);
                    int month = Integer.parseInt(tokens[1]) - 1; // months are indexed from 0
                    int day = Integer.parseInt(tokens[2]);

                    now.set(Calendar.YEAR, year);
                    now.set(Calendar.MONTH, month);
                    now.set(Calendar.DAY_OF_MONTH, day);
                } catch (NumberFormatException e) {
                    // well, let's just not do that then...
                }
            }
        }

        // set bus due time.
        now.set(Calendar.HOUR_OF_DAY, hours);
        now.set(Calendar.MINUTE, minutes);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        return now.getTimeInMillis();
    }

}
