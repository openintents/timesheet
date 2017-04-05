package org.openintents.timesheet.activity;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import java.util.Calendar;
import org.openintents.util.DateTimeFormater;
import org.openintents.util.DurationFormater;

public class JobListCursorAdapter extends CursorAdapter {
    private static final String TAG = "JobListCursorAdapter";
    private Calendar mCalendar;
    Context mContext;
    StringBuilder mInfo;
    private boolean mShowCustomer;

    public JobListCursorAdapter(Context context, Cursor c, boolean showCustomer) {
        super(context, c);
        this.mCalendar = Calendar.getInstance();
        this.mContext = context;
        this.mInfo = new StringBuilder();
        this.mShowCustomer = showCustomer;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        boolean z;
        JobListItemView cliv = (JobListItemView) view;
        String title = cursor.getString(1);
        String customer = cursor.getString(6);
        long startDate = cursor.getLong(2);
        long endDate = cursor.getLong(3);
        long lastStartBreak = cursor.getLong(4);
        long breakDuration = cursor.getLong(5);
        long lastStartBreak2 = cursor.getLong(12);
        String externalRef = cursor.getString(14);
        if (externalRef != null) {
            if (!externalRef.equals("")) {
                z = true;
                cliv.setIsSyncItem(z);
                cliv.setTitle(title);
                this.mInfo.delete(0, this.mInfo.length());
                if (startDate > 0) {
                    this.mCalendar.setTimeInMillis(startDate);
                    this.mInfo.append(DateTimeFormater.mDateFormater.format(this.mCalendar.getTime()));
                    this.mInfo.append(" ");
                    this.mInfo.append(DateTimeFormater.mTimeFormater.format(this.mCalendar.getTime()));
                    if (this.mShowCustomer && !TextUtils.isEmpty(customer)) {
                        this.mInfo.append(", ");
                        this.mInfo.append(customer);
                    }
                    if (endDate > 0) {
                        this.mInfo.append(", ");
                        this.mInfo.append(DurationFormater.formatDuration(context, (endDate - startDate) - breakDuration, 2));
                    }
                } else if (this.mShowCustomer) {
                    this.mInfo.append(customer);
                }
                cliv.setInfo(this.mInfo.toString());
                if (startDate > 0) {
                    cliv.setStatus(1);
                } else if (endDate > 0) {
                    cliv.setStatus(3);
                } else if (lastStartBreak > 0) {
                    cliv.setStatus(4);
                } else if (lastStartBreak2 <= 0) {
                    cliv.setStatus(5);
                } else {
                    cliv.setStatus(2);
                }
            }
        }
        z = false;
        cliv.setIsSyncItem(z);
        cliv.setTitle(title);
        this.mInfo.delete(0, this.mInfo.length());
        if (startDate > 0) {
            this.mCalendar.setTimeInMillis(startDate);
            this.mInfo.append(DateTimeFormater.mDateFormater.format(this.mCalendar.getTime()));
            this.mInfo.append(" ");
            this.mInfo.append(DateTimeFormater.mTimeFormater.format(this.mCalendar.getTime()));
            this.mInfo.append(", ");
            this.mInfo.append(customer);
            if (endDate > 0) {
                this.mInfo.append(", ");
                this.mInfo.append(DurationFormater.formatDuration(context, (endDate - startDate) - breakDuration, 2));
            }
        } else if (this.mShowCustomer) {
            this.mInfo.append(customer);
        }
        cliv.setInfo(this.mInfo.toString());
        if (startDate > 0) {
            cliv.setStatus(1);
        } else if (endDate > 0) {
            cliv.setStatus(3);
        } else if (lastStartBreak > 0) {
            cliv.setStatus(4);
        } else if (lastStartBreak2 <= 0) {
            cliv.setStatus(2);
        } else {
            cliv.setStatus(5);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new JobListItemView(context);
    }
}
