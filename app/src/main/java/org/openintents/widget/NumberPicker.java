package org.openintents.widget;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
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
        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.number_picker, this, true);
        mHandler = new Handler();
        mInputFilter = new NumberPickerInputFilter();
        mNumberInputFilter = new NumberRangeKeyListener();
        mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
        mIncrementButton.setOnClickListener(this);
        mIncrementButton.setOnLongClickListener(this);
        mIncrementButton.setNumberPicker(this);
        mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
        mDecrementButton.setOnClickListener(this);
        mDecrementButton.setOnLongClickListener(this);
        mDecrementButton.setNumberPicker(this);

        mText = (TextView) findViewById(R.id.timepicker_input);
        mText.setOnFocusChangeListener(this);
        mText.setFilters(new InputFilter[]{mInputFilter});

        mSlideUpOutAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -100);
        mSlideUpOutAnimation.setDuration(200);
        mSlideUpInAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 100, Animation.RELATIVE_TO_SELF, 0);
        mSlideUpInAnimation.setDuration(200);
        mSlideDownOutAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 100);
        mSlideDownOutAnimation.setDuration(200);
        mSlideDownInAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -100, Animation.RELATIVE_TO_SELF, 0);
        mSlideDownInAnimation.setDuration(200);

        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIncrementButton.setEnabled(enabled);
        mDecrementButton.setEnabled(enabled);
        mText.setEnabled(enabled);
    }

    public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }

    public void setFormatter(Formatter formatter) {
        mFormatter = formatter;
    }

    /**
     * Set the range of numbers allowed for the number picker. The current value
     * will be automatically set to the start.
     *
     * @param start the start of the range (inclusive)
     * @param end   the end of the range (inclusive)
     */
    public void setRange(int start, int end) {
        mStart = start;
        mEnd = end;
        mCurrent = start;
        updateView();
    }

    /**
     * Set the range of numbers allowed for the number picker. The current value
     * will be automatically set to the start. Also provide a mapping for values
     * used to display to the user.
     *
     * @param start           the start of the range (inclusive)
     * @param end             the end of the range (inclusive)
     * @param displayedValues the values displayed to the user.
     */
    public void setRange(int start, int end, String[] displayedValues) {
        mDisplayedValues = displayedValues;
        mStart = start;
        mEnd = end;
        mCurrent = start;
        updateView();
    }

    public void setCurrent(int current) {
        mCurrent = current;
        updateView();
    }

    /**
     * The speed (in milliseconds) at which the numbers will scroll when the the
     * +/- buttons are longpressed. Default is 300ms.
     */
    public void setSpeed(long speed) {
        mSpeed = speed;
    }

    public void onClick(View v) {

		/*
         * The text view may still have focus so clear it's focus which will
		 * trigger the on focus changed and any typed values to be pulled.
		 */
        mText.clearFocus();

        // now perform the increment/decrement
        if (R.id.increment == v.getId()) {
            int step = mStep - (mCurrent % mStep);
            changeCurrent(mCurrent + step, mSlideUpInAnimation,
                    mSlideUpOutAnimation);
        } else if (R.id.decrement == v.getId()) {
            int step = mCurrent % mStep;
            if (step == 0) {
                step = mStep;
            }
            changeCurrent(mCurrent - step, mSlideDownInAnimation,
                    mSlideDownOutAnimation);
        }
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.toString(value) : String
                .valueOf(value);
    }

    private void changeCurrent(int current, Animation in, Animation out) {

        // Wrap around the values if we go past the start or end
        if (current > mEnd) {
            current = ((current - mStart) % (mEnd + 1 - mStart)) + mStart;
        } else if (current < mStart) {
            current = ((current - mStart) % (mEnd + 1 - mStart)) + mEnd + 1;
        }
        mPrevious = mCurrent;
        mCurrent = current;
        notifyChange();
        updateView();
    }

    private void notifyChange() {
        if (mListener != null) {
            mListener.onChanged(this, mPrevious, mCurrent);
        }
    }

    private void updateView() {

		/*
         * If we don't have displayed values then use the current number else
		 * find the correct value in the displayed values for the current
		 * number.
		 */
        if (mDisplayedValues == null) {
            mText.setText(formatNumber(mCurrent));
        } else {
            mText.setText(mDisplayedValues[mCurrent - mStart]);
        }
    }

    private void validateCurrentView(CharSequence str) {
        int val = getSelectedPos(str.toString());
        if ((val >= mStart) && (val <= mEnd)) {
            mPrevious = mCurrent;
            mCurrent = val;
            notifyChange();
        }
        updateView();
    }

    public void onFocusChange(View v, boolean hasFocus) {

		/*
         * When focus is lost check that the text field has valid values.
		 */
        if (!hasFocus) {
            String str = String.valueOf(((TextView) v).getText());
            if ("".equals(str)) {

                // Restore to the old value as we don't allow empty values
                updateView();
            } else {

                // Check the new value and ensure it's in range
                validateCurrentView(str);
            }
        }
    }

    /**
     * We start the long click here but rely on the {@link NumberPickerButton}
     * to inform us when the long click has ended.
     */
    public boolean onLongClick(View v) {

		/*
		 * The text view may still have focus so clear it's focus which will
		 * trigger the on focus changed and any typed values to be pulled.
		 */
        mText.clearFocus();

        if (R.id.increment == v.getId()) {
            mIncrement = true;
            mHandler.post(mRunnable);
        } else if (R.id.decrement == v.getId()) {
            mDecrement = true;
            mHandler.post(mRunnable);
        }
        return true;
    }

    public void cancelIncrement() {
        mIncrement = false;
    }

    public void cancelDecrement() {
        mDecrement = false;
    }

    private int getSelectedPos(String str) {
        if (mDisplayedValues == null) {
            return Integer.parseInt(str);
        }
        for (int i = 0; i < mDisplayedValues.length; i++) {

				/* Don't force the user to type in jan when ja will do */
            str = str.toLowerCase();
            if (mDisplayedValues[i].toLowerCase().startsWith(str)) {
                return mStart + i;
            }
        }

			/*
			 * The user might have typed in a number into the month field i.e.
			 * 10 instead of OCT so support that too.
			 */
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            /* Ignore as if it's not a number we don't care */
            return mStart;
        }
    }

    public void setStep(int step) {
        mStep = step;
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
            String result = String.valueOf(String.valueOf(dest.subSequence(0, dstart))) + filtered + dest.subSequence(dend, dest.length());
            if ("".equals(result)) {
                return result;
            }
            return NumberPicker.this.getSelectedPos(result) > NumberPicker.this.mEnd ? "" : filtered;
        }

        @Override
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER;
        }
    }

}
