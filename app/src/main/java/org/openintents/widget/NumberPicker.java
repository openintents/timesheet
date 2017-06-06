package org.openintents.widget;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openintents.timesheet.R;

public class NumberPicker extends LinearLayout implements OnClickListener, OnFocusChangeListener, OnLongClickListener {
    public static final Formatter TWO_DIGIT_FORMATTER;
    private static final char[] DIGIT_CHARACTERS;

    static {
        TWO_DIGIT_FORMATTER = new C00471();
        DIGIT_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    }

    private final Handler mHandler;
    private final LayoutInflater mInflater;
    private final InputFilter mInputFilter;
    private final InputFilter mNumberInputFilter;
    private final Runnable mRunnable;
    private final Animation mSlideDownInAnimation;
    private final Animation mSlideDownOutAnimation;
    private final Animation mSlideUpInAnimation;
    private final Animation mSlideUpOutAnimation;
    private final TextView mText;
    private int mCurrent;
    private boolean mDecrement;
    private NumberPickerButton mDecrementButton;
    private String[] mDisplayedValues;
    private int mEnd;
    private Formatter mFormatter;
    private boolean mIncrement;
    private NumberPickerButton mIncrementButton;
    private OnChangedListener mListener;
    private int mPrevious;
    private long mSpeed;
    private int mStart;
    private int mStep;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mRunnable = new C00482();
        this.mSpeed = 300;
        this.mStep = 1;
        setOrientation(VERTICAL);
        this.mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mInflater.inflate(R.layout.number_picker, this, true);
        this.mHandler = new Handler();
        this.mInputFilter = new NumberPickerInputFilter();
        this.mNumberInputFilter = new NumberRangeKeyListener();
        this.mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
        this.mIncrementButton.setOnClickListener(this);
        this.mIncrementButton.setOnLongClickListener(this);
        this.mIncrementButton.setNumberPicker(this);
        this.mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
        this.mDecrementButton.setOnClickListener(this);
        this.mDecrementButton.setOnLongClickListener(this);
        this.mDecrementButton.setNumberPicker(this);
        this.mText = (TextView) findViewById(R.id.timepicker_input);
        this.mText.setOnFocusChangeListener(this);
        this.mText.setFilters(new InputFilter[]{this.mInputFilter});
        this.mSlideUpOutAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, -100.0f);
        this.mSlideUpOutAnimation.setDuration(200);
        this.mSlideUpInAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 100.0f, 1, 0.0f);
        this.mSlideUpInAnimation.setDuration(200);
        this.mSlideDownOutAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, 100.0f);
        this.mSlideDownOutAnimation.setDuration(200);
        this.mSlideDownInAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, -100.0f, 1, 0.0f);
        this.mSlideDownInAnimation.setDuration(200);
        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIncrementButton.setEnabled(enabled);
        this.mDecrementButton.setEnabled(enabled);
        this.mText.setEnabled(enabled);
    }

    public void setOnChangeListener(OnChangedListener listener) {
        this.mListener = listener;
    }

    public void setFormatter(Formatter formatter) {
        this.mFormatter = formatter;
    }

    public void setRange(int start, int end) {
        this.mStart = start;
        this.mEnd = end;
        this.mCurrent = start;
        updateView();
    }

    public void setRange(int start, int end, String[] displayedValues) {
        this.mDisplayedValues = displayedValues;
        this.mStart = start;
        this.mEnd = end;
        this.mCurrent = start;
        updateView();
    }

    public void setCurrent(int current) {
        this.mCurrent = current;
        updateView();
    }

    public void setSpeed(long speed) {
        this.mSpeed = speed;
    }

    public void onClick(View v) {
        this.mText.clearFocus();
        if (R.id.increment == v.getId()) {
            changeCurrent(this.mCurrent + (this.mStep - (this.mCurrent % this.mStep)), this.mSlideUpInAnimation, this.mSlideUpOutAnimation);
        } else if (R.id.decrement == v.getId()) {
            int step = this.mCurrent % this.mStep;
            if (step == 0) {
                step = this.mStep;
            }
            changeCurrent(this.mCurrent - step, this.mSlideDownInAnimation, this.mSlideDownOutAnimation);
        }
    }

    private String formatNumber(int value) {
        if (this.mFormatter != null) {
            return this.mFormatter.toString(value);
        }
        return String.valueOf(value);
    }

    private void changeCurrent(int current, Animation in, Animation out) {
        if (current > this.mEnd) {
            current = ((current - this.mStart) % ((this.mEnd + 1) - this.mStart)) + this.mStart;
        } else if (current < this.mStart) {
            current = (((current - this.mStart) % ((this.mEnd + 1) - this.mStart)) + this.mEnd) + 1;
        }
        this.mPrevious = this.mCurrent;
        this.mCurrent = current;
        notifyChange();
        updateView();
    }

    private void notifyChange() {
        if (this.mListener != null) {
            this.mListener.onChanged(this, this.mPrevious, this.mCurrent);
        }
    }

    private void updateView() {
        if (this.mDisplayedValues == null) {
            this.mText.setText(formatNumber(this.mCurrent));
        } else {
            this.mText.setText(this.mDisplayedValues[this.mCurrent - this.mStart]);
        }
    }

    private void validateCurrentView(CharSequence str) {
        int val = getSelectedPos(str.toString());
        if (val >= this.mStart && val <= this.mEnd) {
            this.mPrevious = this.mCurrent;
            this.mCurrent = val;
            notifyChange();
        }
        updateView();
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String str = String.valueOf(((TextView) v).getText());
            if ("".equals(str)) {
                updateView();
            } else {
                validateCurrentView(str);
            }
        }
    }

    public boolean onLongClick(View v) {
        this.mText.clearFocus();
        if (R.id.increment == v.getId()) {
            this.mIncrement = true;
            this.mHandler.post(this.mRunnable);
        } else if (R.id.decrement == v.getId()) {
            this.mDecrement = true;
            this.mHandler.post(this.mRunnable);
        }
        return true;
    }

    public void cancelIncrement() {
        this.mIncrement = false;
    }

    public void cancelDecrement() {
        this.mDecrement = false;
    }

    private int getSelectedPos(String str) {
        if (this.mDisplayedValues == null) {
            return Integer.parseInt(str);
        }
        for (int i = 0; i < this.mDisplayedValues.length; i++) {
            str = str.toLowerCase();
            if (this.mDisplayedValues[i].toLowerCase().startsWith(str)) {
                return this.mStart + i;
            }
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return this.mStart;
        }
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public interface Formatter {
        String toString(int i);
    }

    public interface OnChangedListener {
        void onChanged(NumberPicker numberPicker, int i, int i2);
    }

    /* renamed from: org.openintents.widget.NumberPicker.1 */
    public static class C00471 implements Formatter {
        final Object[] mArgs;
        final StringBuilder mBuilder;
        final java.util.Formatter mFmt;

        C00471() {
            this.mBuilder = new StringBuilder();
            this.mFmt = new java.util.Formatter(this.mBuilder);
            this.mArgs = new Object[1];
        }

        public String toString(int value) {
            this.mArgs[0] = Integer.valueOf(value);
            this.mBuilder.delete(0, this.mBuilder.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }
    }

    /* renamed from: org.openintents.widget.NumberPicker.2 */
    public class C00482 implements Runnable {
        C00482() {
        }

        public void run() {
            if (NumberPicker.this.mIncrement) {
                NumberPicker.this.changeCurrent(NumberPicker.this.mCurrent + 1, NumberPicker.this.mSlideUpInAnimation, NumberPicker.this.mSlideUpOutAnimation);
                NumberPicker.this.mHandler.postDelayed(this, NumberPicker.this.mSpeed);
            } else if (NumberPicker.this.mDecrement) {
                NumberPicker.this.changeCurrent(NumberPicker.this.mCurrent - 1, NumberPicker.this.mSlideDownInAnimation, NumberPicker.this.mSlideDownOutAnimation);
                NumberPicker.this.mHandler.postDelayed(this, NumberPicker.this.mSpeed);
            }
        }
    }

    private class NumberPickerInputFilter implements InputFilter {
        private NumberPickerInputFilter() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (NumberPicker.this.mDisplayedValues == null) {
                return NumberPicker.this.mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
            }
            CharSequence filtered = String.valueOf(source.subSequence(start, end));
            String str = String.valueOf(new StringBuilder(String.valueOf(String.valueOf(dest.subSequence(0, dstart)))).append(filtered).append(dest.subSequence(dend, dest.length())).toString()).toLowerCase();
            for (String val : NumberPicker.this.mDisplayedValues) {
                if (val.toLowerCase().startsWith(str)) {
                    return filtered;
                }
            }
            return "";
        }
    }

    private class NumberRangeKeyListener extends NumberKeyListener {
        private NumberRangeKeyListener() {
        }

        protected char[] getAcceptedChars() {
            return NumberPicker.DIGIT_CHARACTERS;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
            if (filtered == null) {
                filtered = source.subSequence(start, end);
            }
            String result = new StringBuilder(String.valueOf(String.valueOf(dest.subSequence(0, dstart)))).append(filtered).append(dest.subSequence(dend, dest.length())).toString();
            if ("".equals(result)) {
                return result;
            }
            return NumberPicker.this.getSelectedPos(result) > NumberPicker.this.mEnd ? "" : filtered;
        }

        public int getInputType() {
            return 2;
        }
    }
}
