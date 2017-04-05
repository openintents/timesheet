package org.openintents.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.openintents.timesheet.PreferenceActivity;

public class DateTimeFormater {
    public static DateFormat mDateFormater;
    public static DateFormat mTimeFormater;
    public static DateFormat mTimeWithSecondsFormater;
    public static boolean mUse24hour;

    static {
        mUse24hour = true;
    }

    public static void getFormatFromPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String dateFormat = prefs.getString(PreferenceActivity.PREFS_EXPORT_DATE_FORMAT, "MM/dd/yyyy");
        String timeFormat = prefs.getString(PreferenceActivity.PREFS_EXPORT_TIME_FORMAT, "HH:mm");
        if (TextUtils.isEmpty(dateFormat)) {
            String androidDateFormat = System.getString(context.getContentResolver(), "date_format");
            if (androidDateFormat != null) {
                dateFormat = androidDateFormat;
            } else {
                dateFormat = "MM/dd/yyyy";
            }
        }
        if (TextUtils.isEmpty(timeFormat)) {
            String androidTimeFormat = System.getString(context.getContentResolver(), "time_12_24");
            if (androidTimeFormat == null || !androidTimeFormat.equals("24")) {
                timeFormat = "hh:mm a";
            } else {
                timeFormat = "HH:mm";
            }
        }
        mDateFormater = new SimpleDateFormat(dateFormat);
        mTimeFormater = new SimpleDateFormat(timeFormat);
        if (timeFormat.equals("hh:mm a")) {
            mTimeWithSecondsFormater = new SimpleDateFormat("hh:mm:ss a");
            mUse24hour = false;
            return;
        }
        mTimeWithSecondsFormater = new SimpleDateFormat("HH:mm:ss");
        mUse24hour = true;
    }
}
