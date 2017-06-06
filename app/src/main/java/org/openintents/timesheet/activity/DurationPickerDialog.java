package org.openintents.timesheet.activity;

import android.content.Context;

import org.openintents.timesheet.R;

public class DurationPickerDialog extends TimePickerDialog {
    public DurationPickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute) {
        this(context, R.style.MyThemeDialogAlert, callBack, hourOfDay, minute);
    }

    public DurationPickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute) {
        super(context, theme, callBack, hourOfDay, minute, true);
    }

    protected void updateTitle(int hour, int minute) {
        setTitle(getContext().getString(R.string.duration, new Object[]{String.valueOf(hour), String.valueOf(minute)}));
    }
}
