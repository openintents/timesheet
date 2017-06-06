package org.openintents.distribution;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.Job;

public class LicensePreference extends Preference {
    public LicensePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
    }

    protected void onClick() {
        Intent intent = new Intent(getContext(), LicenseActivity.class);
        intent.putExtra(Job.TITLE, getContext().getString(R.string.license_title));
        getContext().startActivity(intent);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    }

    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
