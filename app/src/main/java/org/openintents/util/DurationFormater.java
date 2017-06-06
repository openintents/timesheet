package org.openintents.util;

import android.content.Context;

import org.openintents.timesheet.R;

public class DurationFormater {
    public static final int TYPE_FORMAT_DONT_UPDATE_INPUT_BOX = 3;
    public static final int TYPE_FORMAT_NICE = 2;
    public static final int TYPE_FORMAT_SECONDS = 1;

    public static String formatDuration(Context context, long durationMillis, int type) {
        if (type == TYPE_FORMAT_SECONDS) {
            return formatDurationSeconds(context, durationMillis);
        }
        return formatDurationNice(context, durationMillis);
    }

    public static String formatDurationSeconds(Context context, long durationMillis) {
        String sign = "";
        if (durationMillis < 0) {
            durationMillis = -durationMillis;
            sign = "-";
        }
        int seconds = (int) (durationMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        StringBuilder str = new StringBuilder();
        if (hours == 0 && minutes == 0 && seconds == 0) {
            sign = "";
        }
        str.append(sign);
        str.append(hours);
        str.append(":");
        if (minutes < 10) {
            str.append("0");
        }
        str.append(minutes);
        str.append(":");
        if (seconds < 10) {
            str.append("0");
        }
        str.append(seconds);
        return str.toString();
    }

    public static String formatDurationNice(Context context, long durationMillis) {
        String sign = "";
        if (durationMillis < 0) {
            durationMillis = -durationMillis;
            sign = "-";
        }
        int seconds = (int) (durationMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        StringBuilder str = new StringBuilder();
        if (hours == 0 && minutes == 0) {
            sign = "";
        }
        str.append(sign);
        if (hours != 0) {
            str.append(hours);
            str.append(" ");
            str.append(context.getString(R.string.hour_short));
            str.append(" ");
        }
        if (minutes != 0 || hours == 0) {
            str.append(minutes);
            str.append(" ");
            str.append(context.getString(R.string.minute_short));
        }
        return str.toString();
    }
}
