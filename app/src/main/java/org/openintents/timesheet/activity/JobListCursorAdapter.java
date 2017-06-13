package org.openintents.timesheet.activity;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import org.openintents.util.DateTimeFormater;
import org.openintents.util.DurationFormater;

import java.util.Calendar;

public class JobListCursorAdapter extends CursorAdapter {
    private static final String TAG = "JobListCursorAdapter";

    Context mContext;
    StringBuilder mInfo;
    /*
	 * private DateFormat mDateFormater = DateFormat.getDateTimeInstance(
	 * DateFormat.SHORT, DateFormat.SHORT);
	 */
    private Calendar mCalendar = Calendar.getInstance();
    private boolean mShowCustomer;

    public JobListCursorAdapter(Context context, Cursor c, boolean showCustomer) {
        super(context, c);
        mContext = context;
        mInfo = new StringBuilder();
        mShowCustomer = showCustomer;

        // don't check preferences, we rely on calling activity.
        // getPreferences(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        JobListItemView cliv = (JobListItemView) view;

        String title = cursor.getString(JobListActivity.COLUMN_INDEX_TITLE);
        String customer = cursor.getString(JobListActivity.COLUMN_INDEX_CUSTOMER);
        long startDate = cursor.getLong(JobListActivity.COLUMN_INDEX_START);
        long endDate = cursor.getLong(JobListActivity.COLUMN_INDEX_END);
        long lastStartBreak = cursor
                .getLong(JobListActivity.COLUMN_INDEX_LAST_START_BREAK);
        long breakDuration = cursor
                .getLong(JobListActivity.COLUMN_INDEX_BREAK_DURATION);
        long lastStartBreak2 = cursor
                .getLong(JobListActivity.COLUMN_INDEX_LAST_START_BREAK2);
        String externalRef = cursor
                .getString(JobListActivity.COLUMN_INDEX_EXTERNAL_REF);
        cliv.setIsSyncItem(externalRef != null && !externalRef.equals(""));

        cliv.setTitle(title);

        mInfo.delete(0, mInfo.length());
        if (startDate > 0) {
            mCalendar.setTimeInMillis(startDate);
            mInfo.append(DateTimeFormater.mDateFormater.format(mCalendar
                    .getTime()));
            mInfo.append(" ");
            mInfo.append(DateTimeFormater.mTimeFormater.format(mCalendar
                    .getTime()));
            if (mShowCustomer) {
                if (!TextUtils.isEmpty(customer)) {
                    mInfo.append(", ");
                    mInfo.append(customer);
                }
            }
            if (endDate > 0) {
                mInfo.append(", ");
                mInfo.append(DurationFormater.formatDuration(context, endDate
                                - startDate - breakDuration,
                        DurationFormater.TYPE_FORMAT_NICE));
            }

        } else {
            if (mShowCustomer) {
                mInfo.append(customer);
            }
        }

        cliv.setInfo(mInfo.toString());

        if (startDate > 0) {
            if (endDate > 0) {
                cliv.setStatus(JobListItemView.STATUS_FINISHED);
            } else {
                if (lastStartBreak > 0) {
                    cliv.setStatus(JobListItemView.STATUS_BREAK);
                } else if (lastStartBreak2 > 0) {
                    cliv.setStatus(JobListItemView.STATUS_BREAK2);
                } else {
                    cliv.setStatus(JobListItemView.STATUS_STARTED);
                }
            }
        } else {
            cliv.setStatus(JobListItemView.STATUS_NEW);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new JobListItemView(context);
    }
}
