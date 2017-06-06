package org.openintents.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.openintents.timesheet.R;
import org.openintents.widget.NumberPicker.OnChangedListener;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode.
 * <p>
 * The hour, each minute digit, and AM/PM (if applicable) can be conrolled by
 * vertical spinners.
 * <p>
 * The hour can be entered by keyboard input. Entering in two digit hours can be
 * accomplished by hitting two digits within a timeout of about a second (e.g.
 * '1' then '2' to select 12).
 * <p>
 * The minutes can be entered by entering single digits.
 * <p>
 * Under AM/PM mode, the user can hit 'a', 'A", 'p' or 'P' to pick.
 * <p>
 * For a dialog using this view, see {@link android.app.TimePickerDialog}.
 */

public class TimePicker extends FrameLayout {

    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        }
    };
    // ui components
    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;
    private final Button mAmPmButton;
    private final String mAmText;
    private final String mPmText;
    // state
    private int mCurrentHour = 0; // 0-23
    private int mCurrentMinute = 0; // 0-59
    private Boolean mIs24HourView = false;
    private boolean mIsAm;
    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.time_picker, this, // we are the parent
                true);

        this.mHourPicker = (NumberPicker) findViewById(R.id.hour);
        this.mHourPicker.setOnChangeListener(new C00502());
        this.mMinutePicker = (NumberPicker) findViewById(R.id.minute);
        this.mMinutePicker.setRange(0, 59);
        this.mMinutePicker.setSpeed(100);
        this.mMinutePicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        this.mMinutePicker.setOnChangeListener(new C00513());
        this.mAmPmButton = (Button) findViewById(R.id.amPm);
        configurePickerRanges();
        Calendar cal = Calendar.getInstance();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(cal.get(Calendar.MINUTE));

        mIsAm = (mCurrentHour < 12);

        String[] dfsAmPm = new DateFormatSymbols().getAmPmStrings();
        this.mAmText = dfsAmPm[Calendar.AM];
        this.mPmText = dfsAmPm[Calendar.PM];
        this.mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        this.mAmPmButton.setOnClickListener(new C00524());
        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mAmPmButton.setEnabled(enabled);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentHour, mCurrentMinute);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(
            OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour (0-23).
     */
    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour;
        updateHourDisplay();
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView != is24HourView) {
            mIs24HourView = is24HourView;
            configurePickerRanges();
            updateHourDisplay();
        }
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline();
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourDisplay() {
        int currentHour = this.mCurrentHour;
        if (!this.mIs24HourView) {
            if (currentHour > 12) {
                currentHour -= 12;
            } else if (currentHour == 0) {
                currentHour = 12;
            }
        }
        this.mHourPicker.setCurrent(currentHour);
        this.mIsAm = this.mCurrentHour < 12;
        this.mAmPmButton.setText(this.mIsAm ? this.mAmText : this.mPmText);
        onTimeChanged();
    }

    private void configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.setRange(0, 23);
            mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
            mAmPmButton.setVisibility(View.GONE);
        } else {
            mHourPicker.setRange(1, 12);
            mHourPicker.setFormatter(null);
            mAmPmButton.setVisibility(View.VISIBLE);
        }
    }

    private void onTimeChanged() {
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(),
                getCurrentMinute());
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay() {
        mMinutePicker.setCurrent(mCurrentMinute);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(),
                getCurrentMinute());
    }

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view      The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute    The current minute.
         */
        void onTimeChanged(TimePicker view, int hourOfDay, int minute);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR;

        static {
            CREATOR = new C00531();
        }

        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.mHour = hour;
            this.mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mHour = in.readInt();
            this.mMinute = in.readInt();
        }

        public int getHour() {
            return this.mHour;
        }

        public int getMinute() {
            return this.mMinute;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mHour);
            dest.writeInt(this.mMinute);
        }

        /* renamed from: org.openintents.widget.TimePicker.SavedState.1 */
        static class C00531 implements Creator<SavedState> {
            C00531() {
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }
    }

    /* renamed from: org.openintents.widget.TimePicker.2 */
    private class C00502 implements OnChangedListener {
        C00502() {
        }

        public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
            TimePicker.this.mCurrentHour = newVal;
            if (!TimePicker.this.mIs24HourView) {
                if (TimePicker.this.mCurrentHour == 12) {
                    TimePicker.this.mCurrentHour = 0;
                }
                if (!TimePicker.this.mIsAm) {
                    TimePicker timePicker = TimePicker.this;
                    timePicker.mCurrentHour = timePicker.mCurrentHour + 12;
                }
            }
            TimePicker.this.onTimeChanged();
        }
    }

    /* renamed from: org.openintents.widget.TimePicker.3 */
    private class C00513 implements OnChangedListener {
        C00513() {
        }

        public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
            TimePicker.this.mCurrentMinute = newVal;
            TimePicker.this.onTimeChanged();
        }
    }

    /* renamed from: org.openintents.widget.TimePicker.4 */
    private class C00524 implements OnClickListener {
        C00524() {
        }

        public void onClick(View v) {
            TimePicker.this.requestFocus();
            TimePicker timePicker;
            if (TimePicker.this.mIsAm) {
                if (TimePicker.this.mCurrentHour < 12) {
                    timePicker = TimePicker.this;
                    timePicker.mCurrentHour = timePicker.mCurrentHour + 12;
                }
            } else if (TimePicker.this.mCurrentHour >= 12) {
                timePicker = TimePicker.this;
                timePicker.mCurrentHour = timePicker.mCurrentHour - 12;
            }
            TimePicker.this.mIsAm = !TimePicker.this.mIsAm;
            TimePicker.this.mAmPmButton.setText(TimePicker.this.mIsAm ? TimePicker.this.mAmText : TimePicker.this.mPmText);
            TimePicker.this.onTimeChanged();
        }
    }
}
