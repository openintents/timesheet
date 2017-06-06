package org.openintents.timesheet.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.LayoutInflater;
import android.view.View;

import org.openintents.timesheet.R;
import org.openintents.widget.TimePicker;
import org.openintents.widget.TimePicker.OnTimeChangedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimePickerDialog extends AlertDialog implements OnClickListener, OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private final Calendar mCalendar;
    private final OnTimeSetListener mCallback;
    private final DateFormat mDateFormat;
    private final TimePicker mTimePicker;
    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;

    public TimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        this(context, R.style.MyThemeDialogAlert, callBack, hourOfDay, minute, is24HourView);
    }

    public TimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        this.mCallback = callBack;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        this.mDateFormat = getTimeFormat(context);
        this.mCalendar = Calendar.getInstance();
        updateTitle(this.mInitialHourOfDay, this.mInitialMinute);
        setButton(context.getText(R.string.date_time_set), this);
        setButton2(context.getText(android.R.string.cancel), (OnClickListener) null);
        setIcon(R.drawable.ic_dialog_time);
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.time_picker_dialog, null);
        setView(view);
        this.mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        this.mTimePicker.setCurrentHour(Integer.valueOf(this.mInitialHourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mInitialMinute));
        this.mTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mTimePicker.setOnTimeChangedListener(this);
    }

    private DateFormat getTimeFormat(Context context) {
        String value = System.getString(context.getContentResolver(), "time_12_24");
        boolean b24 = (value == null || value.equals("12")) ? false : true;
        return new SimpleDateFormat(b24 ? "H:mm" : "h:mm a");
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallback != null) {
            this.mTimePicker.clearFocus();
            this.mCallback.onTimeSet(this.mTimePicker, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
        }
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        updateTitle(hourOfDay, minute);
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        this.mTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minutOfHour));
    }

    protected void updateTitle(int hour, int minute) {
        this.mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        this.mCalendar.set(Calendar.MINUTE, minute);
        setTitle(this.mDateFormat.format(this.mCalendar.getTime()));
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, this.mTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
        this.mTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mTimePicker.setOnTimeChangedListener(this);
        updateTitle(hour, minute);
    }

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker timePicker, int i, int i2);
    }
}
