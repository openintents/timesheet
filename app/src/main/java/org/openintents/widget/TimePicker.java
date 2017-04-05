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

public class TimePicker extends FrameLayout {
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER;
    private final Button mAmPmButton;
    private final String mAmText;
    private int mCurrentHour;
    private int mCurrentMinute;
    private final NumberPicker mHourPicker;
    private Boolean mIs24HourView;
    private boolean mIsAm;
    private final NumberPicker mMinutePicker;
    private OnTimeChangedListener mOnTimeChangedListener;
    private final String mPmText;

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    /* renamed from: org.openintents.widget.TimePicker.1 */
    public static class C00491 implements OnTimeChangedListener {
        C00491() {
        }

        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        }
    }

    /* renamed from: org.openintents.widget.TimePicker.2 */
    class C00502 implements OnChangedListener {
        C00502() {
        }

        public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
            TimePicker.this.mCurrentHour = newVal;
            if (!TimePicker.this.mIs24HourView.booleanValue()) {
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
    class C00513 implements OnChangedListener {
        C00513() {
        }

        public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
            TimePicker.this.mCurrentMinute = newVal;
            TimePicker.this.onTimeChanged();
        }
    }

    /* renamed from: org.openintents.widget.TimePicker.4 */
    class C00524 implements OnClickListener {
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

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR;
        private final int mHour;
        private final int mMinute;

        /* renamed from: org.openintents.widget.TimePicker.SavedState.1 */
        public static class C00531 implements Creator<SavedState> {
            C00531() {
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

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

        static {
            CREATOR = new C00531();
        }
    }

    static {
        NO_OP_CHANGE_LISTENER = new C00491();
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        boolean z;
        this.mCurrentHour = 0;
        this.mCurrentMinute = 0;
        this.mIs24HourView = Boolean.valueOf(false);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.time_picker, this, true);
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
        setCurrentHour(Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
        setCurrentMinute(Integer.valueOf(cal.get(Calendar.MINUTE)));
        if (this.mCurrentHour < 12) {
            z = true;
        } else {
            z = false;
        }
        this.mIsAm = z;
        String[] dfsAmPm = new DateFormatSymbols().getAmPmStrings();
        this.mAmText = dfsAmPm[0];
        this.mPmText = dfsAmPm[1];
        this.mAmPmButton.setText(this.mIsAm ? this.mAmText : this.mPmText);
        this.mAmPmButton.setOnClickListener(new C00524());
        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mMinutePicker.setEnabled(enabled);
        this.mHourPicker.setEnabled(enabled);
        this.mAmPmButton.setEnabled(enabled);
    }

    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        return new SavedState(null, this.mCurrentHour, this.mCurrentMinute);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(Integer.valueOf(ss.getHour()));
        setCurrentMinute(Integer.valueOf(ss.getMinute()));
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
    }

    public Integer getCurrentHour() {
        return Integer.valueOf(this.mCurrentHour);
    }

    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour.intValue();
        updateHourDisplay();
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.mIs24HourView != is24HourView) {
            this.mIs24HourView = is24HourView;
            configurePickerRanges();
            updateHourDisplay();
        }
    }

    public boolean is24HourView() {
        return this.mIs24HourView.booleanValue();
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mCurrentMinute);
    }

    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute.intValue();
        updateMinuteDisplay();
    }

    public int getBaseline() {
        return this.mHourPicker.getBaseline();
    }

    private void updateHourDisplay() {
        int currentHour = this.mCurrentHour;
        if (!this.mIs24HourView.booleanValue()) {
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
        if (this.mIs24HourView.booleanValue()) {
            this.mHourPicker.setRange(0, 23);
            this.mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
            this.mAmPmButton.setVisibility(GONE);
            return;
        }
        this.mHourPicker.setRange(1, 12);
        this.mHourPicker.setFormatter(null);
        this.mAmPmButton.setVisibility(VISIBLE);
    }

    private void onTimeChanged() {
        this.mOnTimeChangedListener.onTimeChanged(this, getCurrentHour().intValue(), getCurrentMinute().intValue());
    }

    private void updateMinuteDisplay() {
        this.mMinutePicker.setCurrent(this.mCurrentMinute);
        this.mOnTimeChangedListener.onTimeChanged(this, getCurrentHour().intValue(), getCurrentMinute().intValue());
    }
}
