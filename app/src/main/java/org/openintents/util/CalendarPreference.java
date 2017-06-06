package org.openintents.util;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.database.Cursor;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.Calendars;
import org.openintents.timesheet.activity.JobActivity;

public class CalendarPreference extends ListPreference {
    public CalendarPreference(Context context) {
        super(context);
    }

    public CalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void createEntries(int calendarAuthority) {
        Cursor cursor;
        CharSequence[] mEntries;
        CharSequence[] mEntryValues;
        if (calendarAuthority == 1) {
            cursor = getContext().getContentResolver().query(Calendars.CONTENT_URI_1, Calendars.NAME_PROJECTION, null, null, null);
        } else if (calendarAuthority == 2) {
            cursor = getContext().getContentResolver().query(Calendars.CONTENT_URI_2, Calendars.NAME_PROJECTION, null, null, null);
        } else {
            Toast.makeText(getContext(), R.string.calendar_missing, Toast.LENGTH_SHORT).show();
            cursor = null;
        }
        if (cursor != null) {
            mEntries = new CharSequence[cursor.getCount()];
            mEntryValues = new CharSequence[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                mEntryValues[i] = cursor.getString(0);
                mEntries[i] = cursor.getString(1);
                i++;
            }
        } else {
            mEntries = new CharSequence[0];
            mEntryValues = new CharSequence[0];
        }
        setEntries(mEntries);
        setEntryValues(mEntryValues);
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        createEntries(JobActivity.getCalendarAuthority(getContext()));
        super.onPrepareDialogBuilder(builder);
    }
}
