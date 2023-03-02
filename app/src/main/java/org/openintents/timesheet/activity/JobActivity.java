package org.openintents.timesheet.activity;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.Contacts.People;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.CalendarApp;
import org.openintents.timesheet.Timesheet.Calendars;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.activity.TimePickerDialog.OnTimeSetListener;
import org.openintents.timesheet.animation.FadeAnimation;
import org.openintents.util.DateTimeFormater;
import org.openintents.util.DurationFormater;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.openintents.widget.NumberPicker;
import org.openintents.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import static java.lang.Long.*;

public class JobActivity extends AppCompatActivity {
    static final int DIALOG_ID_START_DATE = 1;
    static final int DIALOG_ID_START_TIME = 2;
    static final int DIALOG_ID_END_DATE = 3;
    static final int DIALOG_ID_END_TIME = 4;
    static final int DIALOG_ID_BREAK = 5;
    static final int DIALOG_ID_RESTART_JOB = 6;
    static final int DIALOG_ID_STOP_JOB = 7;
    static final int DIALOG_ID_RECENT_NOTES = 8;
    static final int DIALOG_ID_PLANNED_DATE = 9;
    static final int DIALOG_ID_PLANNED_TIME = 10;
    static final int DIALOG_ID_PLANNED_DURATION = 11;
    static final int DIALOG_ID_RATES = 12;

    static final String TAG = "JobActivity";

    /**
     * The index of the columns
     */
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final int COLUMN_INDEX_START = 2;
    private static final int COLUMN_INDEX_END = 3;
    private static final int COLUMN_INDEX_LAST_START_BREAK = 4;
    private static final int COLUMN_INDEX_BREAK_DURATION = 5;
    private static final int COLUMN_INDEX_HOURLY_RATE = 6;
    private static final int COLUMN_INDEX_CUSTOMER = 7;
    private static final int COLUMN_INDEX_PLANNED_DATE = 8;
    private static final int COLUMN_INDEX_PLANNED_DURATION = 9;
    private static final int COLUMN_INDEX_CALENDAR_REF = 10;
    private static final int COLUMN_INDEX_EXTRA_TOTAL = 11;
    private static final int COLUMN_INDEX_LAST_START_BREAK2 = 12;
    private static final int COLUMN_INDEX_BREAK2_DURATION = 13;
    private static final int COLUMN_INDEX_BREAK2_COUNT = 14;
    private static final int COLUMN_INDEX_HOURLY_RATE2 = 15;
    private static final int COLUMN_INDEX_HOURLY_RATE2_START = 16;
    private static final int COLUMN_INDEX_HOURLY_RATE3 = 17;
    private static final int COLUMN_INDEX_HOURLY_RATE3_START = 18;
    private static final int COLUMN_INDEX_CUSTOMER_REF = 19;

    private static final String ORIGINAL_CONTENT = "origNote";
    private static final String ORIGINAL_STATE = "origState";
    private static final String ORIGINAL_URI = "orgUri";

    private static final int REQUEST_PICK_CONTACT = 1;

    private static final String SHOW_MORE = "show_more";
    private static final String SHOW_RECENT_NOTES_BUTTON = "show_recent_notes";

    // Identifiers for our menu items.
    private static final int REVERT_ID = Menu.FIRST;
    private static final int DISCARD_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int LIST_ID = Menu.FIRST + 3;
    private static final int RESTART_ID = Menu.FIRST + 4;
    private static final int PAUSE_ID = Menu.FIRST + 5;
    private static final int CONTINUE_ID = Menu.FIRST + 6;
    private static final int END_ID = Menu.FIRST + 7;
    private static final int EVENT_ID = Menu.FIRST + 8;
    private static final int SEND_ID = Menu.FIRST + 9;
    private static final int ADD_EXTRA_ITEM_ID = Menu.FIRST + 10;
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;


    private static final String[] PROJECTION = new String[]{
                Job._ID, // 0
                Job.NOTE, // 1
                Job.START_DATE, Job.END_DATE, Job.LAST_START_BREAK,
                Job.BREAK_DURATION, Job.HOURLY_RATE, Job.CUSTOMER,
                Job.PLANNED_DATE, Job.PLANNED_DURATION, Job.CALENDAR_REF,
                Job.EXTRAS_TOTAL, Job.LAST_START_BREAK2, Job.BREAK2_DURATION,
                Job.BREAK2_COUNT, Job.HOURLY_RATE2, Job.HOURLY_RATE2_START,
                Job.HOURLY_RATE3, Job.HOURLY_RATE3_START, Job.CUSTOMER_REF};
    private static final boolean SHOWING_MORE_DEFAULT = true;

    NumberFormat mDecimalFormat;
    String[] mRecentNoteList;
    private long mBillingUnit;
    private long mBillingUnitHalf;
    private long mBillingUnitMinutes;
    private boolean mBreak;
    private boolean mBreak2;
    private Button mBreak2Button;
    private long mBreak2PreviousDuration;
    private Button mBreakButton;
    private TextView mBreakInfo;
    private long mBreakPreviousDuration;
    private OnTimeSetListener mBreakSetListener;
    private Calendar mCalendar;
    private int mCalendarAuthority;
    private String mCalendarUri;
    private Cursor mCursor;
    private AutoCompleteTextView mCustomer;
    private String[] mCustomerList;
    private String mCustomerRef;
    private Runnable mDisplayUpdater;
    private TextView mDurationInfo;
    private LinearLayout mEditHourlyRate;
    private LinearLayout mEditPanel1;
    private LinearLayout mEditPanel2;
    private LinearLayout mEditPanel3;
    private long mEndDate;
    private OnDateSetListener mEndDateSetListener;
    private OnTimeSetListener mEndTimeSetListener;
    private long mExtraTotal;
    private TextView mExtrasInfo;
    private boolean mFinished;
    private Handler mHandler;
    private long mHourlyRate;
    private TextView mHourlyRateInfo;
    private RelativeLayout mInfoPanel1;
    private RelativeLayout mInfoPanel3;
    private Button mJobButton;
    private long mLastStartBreak;
    private long mLastStartBreak2;
    private String mOriginalContent;
    private long mPlannedDate;
    private OnDateSetListener mPlannedDateSetListener;
    private long mPlannedDuration;
    private OnTimeSetListener mPlannedMinutesSetListener;
    private OnTimeSetListener mPlannedTimeSetListener;
    private String mPreselectedCustomer;
    private RateListAdapter mRateAdatper;
    private ImageButton mRateButton;
    private Button mRecentNotes;
    private int mRecentNotesButtonState;
    private Button mSetBreak;
    private Button mSetEndDate;
    private Button mSetEndTime;
    private EditText mSetHourlyRate;
    private Button mSetPlannedDate;
    private Button mSetPlannedDuration;
    private Button mSetPlannedTime;
    private Button mSetStartDate;
    private Button mSetStartTime;
    private boolean mShowRecentNotesButton;
    private boolean mShowingMore;
    private boolean mStartBillingImmediately;
    private long mStartDate;
    private OnDateSetListener mStartDateSetListener;
    private OnTimeSetListener mStartTimeSetListener;
    private boolean mStarted;
    private int mState;
    private EditText mText;
    private TextView mTimestamp;
    private TextView mTotalInfo;
    private boolean mUpdateCalendarEvent;
    private Uri mUri;
    private boolean mShowCustomerPopUp = false;

    public JobActivity() {
        mStartBillingImmediately = false;
        mHandler = new Handler();
        mDisplayUpdater = new DisplayUpdater();
        mCalendar = Calendar.getInstance();
        mDecimalFormat = new DecimalFormat("0.00");
        mShowRecentNotesButton = false;
        mRecentNoteList = null;
        mExtraTotal = 0;
        mStartDateSetListener = new C00131();
        mStartTimeSetListener = new C00142();
        mEndDateSetListener = new C00153();
        mEndTimeSetListener = new C00164();
        mBreakSetListener = new C00175();
        mPlannedDateSetListener = new C00186();
        mPlannedTimeSetListener = new C00197();
        mPlannedMinutesSetListener = new C00208();
    }

    public static int getCalendarAuthority(Context ctx) {
        Cursor result = ctx.getContentResolver().query(Calendars.CONTENT_URI_1,
                new String[]{BaseColumns._ID}, null, null, null);
        if (result != null && result.getCount() > 0) {
            result.close();
            return 1;
        } else {
            result = ctx.getContentResolver().query(Calendars.CONTENT_URI_2,
                    new String[]{BaseColumns._ID}, null, null, null);
            if (result != null && result.getCount() > 0) {
                result.close();
                return 2;
            } else {
                return 0;
            }
        }
    }

    /**
     * Create a list of all customers.
     *
     * @return
     */
    public static String[] getCustomerList(Context context) {

        Cursor c = context.getContentResolver().query(
                Timesheet.Job.CONTENT_URI, new String[]{Job.CUSTOMER}, null,
                null, Job.MODIFIED_DATE + " DESC");

        Set<String> set = new TreeSet<String>();

        c.moveToPosition(-1);

        while (c.moveToNext()) {
            String customer = c.getString(0);
            if (!TextUtils.isEmpty(customer)) {
                set.add(customer);
            }
        }

        c.close();
        return set.toArray(new String[0]);
    }

    /**
     * Create a list of recent titles.
     *
     * @return
     */
    public static String[] getTitlesList(Context context) {

        Cursor c = context.getContentResolver().query(
                Timesheet.Job.CONTENT_URI, new String[]{Job.TITLE}, null,
                null, Job.MODIFIED_DATE + " DESC");

        Vector<String> vec = new Vector<String>();

        c.moveToPosition(-1);
        while (c.moveToNext()) {
            String title = c.getString(0);
            if (!(TextUtils.isEmpty(title) || title.equals(context.getString(android.R.string.untitled)) || vec.contains(title))) {
                vec.add(title);
            }
        }

        c.close();

        return vec.toArray(new String[0]);
    }

    /**
     * Create a list of recent titles.
     *
     * @return
     */
    public static String getNote(Context context, String title) {

        Cursor c = context.getContentResolver().query(
                Timesheet.Job.CONTENT_URI, new String[]{Job.TITLE},
                Job.TITLE + " = ?", new String[]{title},
                Job.MODIFIED_DATE + " DESC");

        if (c != null && c.moveToFirst()) {
            String note = c.getString(0);
            return note;
        }

        if (c != null) {
            c.close();
        }
        return null;
    }

    /**
     * Get the last customer.
     *
     * @return
     */
    public static String getLastCustomer(Context context) {

        Cursor c = context.getContentResolver().query(
                Timesheet.Job.CONTENT_URI, new String[]{Job.CUSTOMER}, null,
                null, Job.MODIFIED_DATE + " DESC");

        String customer = "";
        while (c != null && c.moveToNext()) {
            customer = c.getString(0);
            if (!TextUtils.isEmpty(customer)) {
                break;
            }
        }
        Log.i(TAG, "LastCustomer:" + customer);

        if (c != null) {
            c.close();
        }
        return customer;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mRateAdatper = new RateListAdapter(this);

        mRecentNoteList = null;
        mStartBillingImmediately = false;
        mBillingUnit = 0;
        mBillingUnitHalf = 0;
        mBillingUnitMinutes = 1;
        mCalendarAuthority = getCalendarAuthority(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mBillingUnit = parseLong(prefs.getString(PreferenceActivity.PREFS_BILLING_UNIT, PreferenceActivity.PREFS_BILLING_UNIT_DEFAULT));
        mBillingUnitHalf = mBillingUnit / 2L;
        mBillingUnitMinutes = mBillingUnit / 60000L;
        if (mBillingUnitMinutes == 0) {
            mBillingUnitMinutes = 1;
        }

        boolean createEvent = false;

        final String action = intent.getAction();
        if (savedInstanceState != null) {
            mState = STATE_EDIT;
            if (savedInstanceState.getString(ORIGINAL_URI) != null) {
                mUri = Uri.parse(savedInstanceState.getString(ORIGINAL_URI));
            } else {
                startActivity(new Intent(this, JobListActivity.class));
                finish();
            }
        } else if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action) || Intent.ACTION_MAIN.equals(action) || action == null) {
            mState = STATE_INSERT;
            if (intent.getData() == null) {
                intent.setData(Job.CONTENT_URI);
            }
            ContentValues values = new ContentValues();
            mPreselectedCustomer = intent.getStringExtra(TimesheetIntent.EXTRA_CUSTOMER);
            Log.i(TAG, "Intent extra: Customer = " + mPreselectedCustomer);
            if (!TextUtils.isEmpty(mPreselectedCustomer)) {
                if (mPreselectedCustomer.equals(getString(R.string.all_customers))) {
                    mPreselectedCustomer = null;
                } else {
                    values.put(Job.CUSTOMER, mPreselectedCustomer);
                }
            }
            String note = getIntent().getStringExtra(TimesheetIntent.EXTRA_NOTE);
            if (!TextUtils.isEmpty(note)) {
                values.put(Job.NOTE, note);
            }
            int rate = getIntent().getIntExtra(TimesheetIntent.EXTRA_HOURLY_RATE, -1);
            if (rate >= 0) {
                values.put(Job.HOURLY_RATE, rate);
            }
            long plannedDate = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DATE, 0);
            if (plannedDate > 0) {
                values.put(Job.PLANNED_DATE, valueOf(plannedDate));
                createEvent = true;
            }
            long plannedDuration = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DURATION, 0);
            if (plannedDuration > 0) {
                values.put(Job.PLANNED_DURATION, valueOf(plannedDuration));
                createEvent = true;
            }
            if (TextUtils.isEmpty(mPreselectedCustomer)) {
                mPreselectedCustomer = getLastCustomer(this);
            }
            mUri = getContentResolver().insert(intent.getData(), values);
            mStartBillingImmediately = prefs.getBoolean(PreferenceActivity.PREFS_START_JOBS_IMMEDIATELY, false);
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mUri);
            setIntent(intent);
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new job into " + getIntent().getData());
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            setResult(RESULT_OK, new Intent().setAction(mUri.toString()));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
        setContentView(R.layout.job);
        mText = (EditText) findViewById(R.id.note);
        mText.addTextChangedListener(new C00219());
        mCustomer = (AutoCompleteTextView) findViewById(R.id.customer);
        mCustomer.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                mUpdateCalendarEvent = true;
            }

            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
            }

            public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
            }
        });
        registerForContextMenu(mCustomer);
        mRateButton = (ImageButton) findViewById(R.id.rate_button);
        mRateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                rateButtonClicked();
            }
        });
        mTimestamp = (TextView) findViewById(R.id.timestamp);
        mBreakInfo = (TextView) findViewById(R.id.break_info);
        mDurationInfo = (TextView) findViewById(R.id.duration_info);
        mTotalInfo = (TextView) findViewById(R.id.total_info);
        mExtrasInfo = (TextView) findViewById(R.id.extras_info);
        mHourlyRateInfo = (TextView) findViewById(R.id.hourly_rate_info);
        mJobButton = (Button) findViewById(R.id.start_end);
        mJobButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                jobTimeClicked();
            }
        });
        mBreakButton = (Button) findViewById(R.id.break_start_end);
        mBreakButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                breakTimeClicked();
            }
        });
        mBreak2Button = (Button) findViewById(R.id.break2_start_end);
        mBreak2Button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                break2TimeClicked();
            }
        });
        mEditPanel1 = (LinearLayout) findViewById(R.id.edit_panel1);
        mEditPanel2 = (LinearLayout) findViewById(R.id.edit_panel2);
        mEditPanel3 = (LinearLayout) findViewById(R.id.edit_panel3);
        mEditHourlyRate = (LinearLayout) findViewById(R.id.edit_hourlyrate);
        mInfoPanel1 = (RelativeLayout) findViewById(R.id.info_panel1);
        mInfoPanel3 = (RelativeLayout) findViewById(R.id.info_panel3);
        mSetHourlyRate = (EditText) findViewById(R.id.set_hourly_rate);
        mSetHourlyRate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                hourlyRateChanged();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        ImageButton btn = (ImageButton) findViewById(R.id.rate_edit_button);
        if (btn != null) {
            btn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    try {
                        showDialog(DIALOG_ID_RATES);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            });
        }
        mSetStartDate = (Button) findViewById(R.id.set_start_date);
        mSetStartDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_START_DATE);
            }
        });
        mSetStartTime = (Button) findViewById(R.id.set_start_time);
        mSetStartTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_START_TIME);
            }
        });
        mSetPlannedDate = (Button) findViewById(R.id.set_planned_date);
        mSetPlannedDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_PLANNED_DATE);
            }
        });
        mSetPlannedTime = (Button) findViewById(R.id.set_planned_time);
        mSetPlannedTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_PLANNED_TIME);
            }
        });
        mSetPlannedDuration = (Button) findViewById(R.id.set_planned_minutes);
        mSetPlannedDuration.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_PLANNED_DURATION);
            }
        });
        mSetEndDate = (Button) findViewById(R.id.set_end_date);
        mSetEndDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_END_DATE);
            }
        });
        mSetEndTime = (Button) findViewById(R.id.set_end_time);
        mSetEndTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_END_TIME);
            }
        });
        mSetBreak = (Button) findViewById(R.id.set_break_minutes);
        mSetBreak.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_BREAK);
            }
        });
        mRecentNotes = (Button) findViewById(R.id.recent_notes);
        mRecentNotes.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showRecentNotesDialog();
            }
        });
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
        mCustomerList = getCustomerList(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mCustomerList);
        mCustomer.setAdapter(arrayAdapter);
        mCustomer.setThreshold(0);
        mCustomer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mShowCustomerPopUp) {
                    mCustomer.dismissDropDown();
                } else {
                    mCustomer.showDropDown();
                }
                mShowCustomerPopUp = !mShowCustomerPopUp;
            }
        });
        if (mCustomerList.length < 1) {
            mCustomer.setHint(R.string.customer_hint_first_time);
        }
        mCustomer.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoFillInHourlyRate(mCustomerList[position]);
            }
        });
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            mState = savedInstanceState.getInt(ORIGINAL_STATE);
            mShowingMore = savedInstanceState.getBoolean(SHOW_MORE);
            mShowRecentNotesButton = savedInstanceState.getBoolean(SHOW_RECENT_NOTES_BUTTON);
        } else {
            mShowingMore = SHOWING_MORE_DEFAULT;
        }
        mCursor.moveToFirst();
        if (createEvent) {
            CalendarApp.insertCalendarEvent(this, mUri.toString(),
                    mCursor.getLong(COLUMN_INDEX_PLANNED_DATE),
                    mCursor.getLong(COLUMN_INDEX_PLANNED_DURATION),
                    mCustomer.getText().toString(),
                    mText.getText().toString(),
                    mCalendarAuthority);
        }
        if (mStartBillingImmediately) {
            startJob();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showRecentNotesDialog() {
        Log.d(TAG, "showRecentNOtes");
        if (mRecentNoteList == null) {
            Log.d(TAG, "getTitlesList");
            mRecentNoteList = getTitlesList(this);
        }
        if (mRecentNoteList.length > 0) {
            Log.d(TAG, "show Dialog + " + mRecentNoteList.length);
            showDialog(DIALOG_ID_RECENT_NOTES);
            return;
        }
        Toast.makeText(this, getString(R.string.no_recent_notes_available), Toast.LENGTH_SHORT).show();
    }

    private void updateRecentNotesButton(boolean animate) {
        if (mShowRecentNotesButton || TextUtils.isEmpty(mText.getText())) {
            mShowRecentNotesButton = true;
            if (!animate) {
                mRecentNotes.setVisibility(View.VISIBLE);
                mRecentNotesButtonState = View.VISIBLE;
            } else if (mRecentNotesButtonState == View.GONE) {
                Log.i(TAG, "Fade in");
                FadeAnimation.fadeIn(this, mRecentNotes);
                mRecentNotesButtonState = View.VISIBLE;
            }
        } else if (!animate) {
            mRecentNotes.setVisibility(View.GONE);
            mRecentNotesButtonState = View.GONE;
        } else if (mRecentNotesButtonState == View.VISIBLE) {
            Log.i(TAG, "Fade out");
            FadeAnimation.fadeOut(this, mRecentNotes);
            mRecentNotesButtonState = View.GONE;
        }
    }

    /**
     * Auto-fill in values for a customer, like hourly rate.
     *
     * @param customer
     */
    private void autoFillInHourlyRate(String customer) {
        Cursor c = getContentResolver().query(Timesheet.Job.CONTENT_URI,
                PROJECTION,
                Job.CUSTOMER + " = ? AND " + Job.HOURLY_RATE + " <> 0",
                new String[]{customer}, Job.MODIFIED_DATE + " DESC");

        if (c != null && c.moveToFirst()) {
            Log.i(TAG, "Customer information found. Auto-fill in for: "
                    + customer);

            long hourlyrate = c.getLong(COLUMN_INDEX_HOURLY_RATE);
            long hourlyrate2 = c.getLong(COLUMN_INDEX_HOURLY_RATE2);
            long hourlyrate2Start = c.getLong(COLUMN_INDEX_HOURLY_RATE2_START);
            long hourlyrate3 = c.getLong(COLUMN_INDEX_HOURLY_RATE3);
            long hourlyrate3Start = c.getLong(COLUMN_INDEX_HOURLY_RATE3_START);
            setHourlyRateViews(hourlyrate, hourlyrate2, hourlyrate2Start,
                    hourlyrate3, hourlyrate3Start);
        }

        if (c != null) {
            c.close();
        }
    }

    private void setHourlyRateViews(long hourlyrate, long hourlyrate2, long hourlyrate2Start, long hourlyrate3, long hourlyrate3Start) {
        String rateString = mDecimalFormat.format(((double) hourlyrate) * Timesheet.RATE_FACTOR);
        if (hourlyrate2Start > 0) {
            rateString = String.valueOf(rateString) + " / " + mDecimalFormat.format(((double) hourlyrate2) * Timesheet.RATE_FACTOR);
            if (hourlyrate3Start > 0) {
                rateString = String.valueOf(rateString) + " / " + mDecimalFormat.format(((double) hourlyrate3) * Timesheet.RATE_FACTOR);
            }
            mSetHourlyRate.setEnabled(false);
        } else {
            mSetHourlyRate.setEnabled(true);
        }
        mSetHourlyRate.setText(rateString);
        mHourlyRateInfo
                .setText(getString(R.string.hourly_rate_info, rateString));
    }

    private void rateButtonClicked() {
        mShowingMore = !mShowingMore;
        showMoreIfRequired();
    }

    private void showMoreIfRequired() {
        if (mShowingMore) {
            mEditHourlyRate.setVisibility(View.VISIBLE);
            if (mStarted) {
                mEditPanel1.setVisibility(View.VISIBLE);
                mEditPanel2.setVisibility(View.VISIBLE);
                mEditPanel3.setVisibility(View.GONE);
            } else {
                mEditPanel1.setVisibility(View.GONE);
                mEditPanel2.setVisibility(View.GONE);
                mEditPanel3.setVisibility(View.VISIBLE);
            }
            mInfoPanel1.setVisibility(View.GONE);
            mInfoPanel3.setVisibility(View.GONE);

        } else {
            mEditPanel1.setVisibility(View.GONE);
            mEditHourlyRate.setVisibility(View.GONE);
            mEditPanel2.setVisibility(View.GONE);
            mEditPanel3.setVisibility(View.GONE);
            mInfoPanel1.setVisibility(View.VISIBLE);
            mInfoPanel3.setVisibility(View.VISIBLE);
        }
    }

    protected void breakTimeClicked() {
        if (mBreak) {
            stopBreak();
        } else {
            startBreak();
        }
    }

    protected void break2TimeClicked() {
        if (mBreak2) {
            stopBreak2();
        } else {
            startBreak2();
        }
    }

    private void stopBreak() {
        long breakDuration = getBreakDuration();
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK, (Long) null);
        values.put(Job.BREAK_DURATION, valueOf(breakDuration));
        updateDatabase(values);
        updateFromCursor();
    }

    private void startBreak() {
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK, valueOf(System.currentTimeMillis()));
        updateDatabase(values);
        updateFromCursor();
    }

    private void stopBreak2() {
        long breakDuration = getBreak2Duration();
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK2, (Long) null);
        values.put(Job.BREAK2_DURATION, valueOf(breakDuration));
        updateDatabase(values);
        updateFromCursor();
    }

    private void startBreak2() {
        if (mBreak) {
            stopBreak();
        }
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK2, System.currentTimeMillis());
        updateDatabase(values);
        updateFromCursor();
    }

    private void hourlyRateChanged() {
        long hourlyRate = getHourlyRateFromText();
        ContentValues values = getContentValues();
        values.put(Job.HOURLY_RATE, hourlyRate);
        updateDatabase(values);
        mHourlyRate = hourlyRate;
        updateInfo(DIALOG_ID_END_DATE);
    }

    private long getHourlyRateFromText() {
        try {
            return (long) (100.0d * mDecimalFormat.parse(mSetHourlyRate.getText().toString()).doubleValue());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing break duration: " + mSetHourlyRate.getText());
            return 0;
        }
    }

    private long getBreakDuration() {
        long breakDuration = mBreakPreviousDuration;
        if (mBreak) {
            return breakDuration + (System.currentTimeMillis() - mLastStartBreak);
        }
        return breakDuration;
    }

    private long getBreak2Duration() {
        long break2Duration = mBreak2PreviousDuration;
        if (mBreak2) {
            return break2Duration + (System.currentTimeMillis() - mLastStartBreak2);
        }
        return break2Duration;
    }

    private long getWorkDuration() {
        if (!mStarted || mFinished) {
            return (mEndDate - mStartDate) - mBreak2PreviousDuration;
        }
        if (mBreak2) {
            return (mLastStartBreak2 - mStartDate) - mBreak2PreviousDuration;
        }
        return (System.currentTimeMillis() - mStartDate) - mBreak2PreviousDuration;
    }

    protected void jobTimeClicked() {

        if (mStarted) {
            stopJob();

        } else {
            startJob();
        }

    }

    private void stopJob() {
        if (mBreak) {
            stopBreak();
        }
        if (mBreak2) {
            stopBreak2();
        }
        ContentValues values = getContentValues();
        mStartDate = round(mStartDate);
        mBreakPreviousDuration = round(mBreakPreviousDuration);
        mBreak2PreviousDuration = round(mBreak2PreviousDuration);
        values.put(Job.END_DATE, round(System.currentTimeMillis()));
        values.put(Job.START_DATE, mStartDate);
        values.put(Job.BREAK_DURATION, mBreakPreviousDuration);
        values.put(Job.BREAK2_DURATION, mBreak2PreviousDuration);
        updateDatabase(values);
        updateFromCursor();
    }

    private long round(long millis) {
        if (mBillingUnit > 0) {
            return ((mBillingUnitHalf + millis) / mBillingUnit) * mBillingUnit;
        }
        return millis;
    }

    private void continueJob() {
        if (mBreak) {
            stopBreak();
        }
        ContentValues values = getContentValues();
        values.put(Job.END_DATE, (Long) null);
        updateDatabase(values);
        updateFromCursor();
    }

    /**
     *
     */
    private void startJob() {

        ContentValues values = getContentValues();
        values.put(Job.START_DATE, System.currentTimeMillis());
        values.put(Job.END_DATE, (Long) null);
        values.put(Job.LAST_START_BREAK, (Long) null);
        values.put(Job.BREAK_DURATION, 0);
        values.put(Job.LAST_START_BREAK2, (Long) null);
        values.put(Job.BREAK2_DURATION, 0);
        updateDatabase(values);
        updateFromCursor();

        // if required show different edit panels
        showMoreIfRequired();
    }

    ;

    @Override
    protected void onResume() {
        super.onResume();

        mUpdateCalendarEvent = false;

        DateTimeFormater.getFormatFromPreferences(this);

        // If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null) {
            updateFromCursor();
        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }

        showMoreIfRequired();

        updateRecentNotesButton(false);

        // If customer had filtered a specific list in JobListActivity.java,
        // auto-fill in that customer information.
        if (!TextUtils.isEmpty(mPreselectedCustomer)) {
            Log.i(TAG, ">>> Autofillin preselected customer informaton for "
                    + mPreselectedCustomer);
            autoFillInHourlyRate(mPreselectedCustomer);
            setCustomerText(mPreselectedCustomer);
            mPreselectedCustomer = null;
        }
    }

    private void setCustomerText(String customer) {
        ListAdapter adapter = mCustomer.getAdapter();
        mCustomer.setAdapter(null);
        mCustomer.setText(customer);
        mCustomer.setAdapter((ArrayAdapter) adapter);
    }

    private void updateDisplay(long delayMillis) {
        cancelUpdateDisplay();
        if (mStarted && !mFinished) {
            mHandler.postDelayed(mDisplayUpdater, delayMillis);
        }
    }

    private void cancelUpdateDisplay() {
        mHandler.removeCallbacks(mDisplayUpdater);
    }

    private void updateFromCursor() {
        mCursor.requery();
        mCursor.moveToFirst();
        updateFromCursorWithoutRequery();
    }

    private void updateFromCursorWithoutRequery() {
        if (mState == STATE_EDIT) {
            setTitle(getText(R.string.title_edit));
        } else if (mState == STATE_INSERT) {
            setTitle(getText(R.string.title_create));
        }

        // This is a little tricky: we may be resumed after previously being
        // paused/stopped. We want to put the new text in the text view,
        // but leave the user where they were (retain the cursor position
        // etc). This version of setText does that for us.
        String note = mCursor.getString(COLUMN_INDEX_NOTE);
        mText.setTextKeepState(note);

        // If we hadn't previously retrieved the original text, do so
        // now. This allows the user to revert their changes.
        if (mOriginalContent == null) {
            mOriginalContent = note;
        }

        mStartDate = mCursor.getLong(COLUMN_INDEX_START);
        mEndDate = mCursor.getLong(COLUMN_INDEX_END);
        mLastStartBreak = mCursor.getLong(COLUMN_INDEX_LAST_START_BREAK);
        mLastStartBreak2 = mCursor.getLong(COLUMN_INDEX_LAST_START_BREAK2);
        mBreakPreviousDuration = mCursor.getLong(COLUMN_INDEX_BREAK_DURATION);
        mBreak2PreviousDuration = mCursor.getLong(COLUMN_INDEX_BREAK2_DURATION);
        mHourlyRate = mCursor.getLong(COLUMN_INDEX_HOURLY_RATE);
        String customer = mCursor.getString(COLUMN_INDEX_CUSTOMER);
        mPlannedDate = mCursor.getLong(COLUMN_INDEX_PLANNED_DATE);
        mPlannedDuration = mCursor.getLong(COLUMN_INDEX_PLANNED_DURATION);
        mExtraTotal = mCursor.getLong(COLUMN_INDEX_EXTRA_TOTAL);
        setCustomerText(customer);
        mCalendarUri = mCursor.getString(COLUMN_INDEX_CALENDAR_REF);
        mCustomerRef = mCursor.getString(COLUMN_INDEX_CUSTOMER_REF);

        mStarted = mStartDate > 0;
        mFinished = mStarted && mEndDate > 0;
        mBreak = mLastStartBreak > 0;
        mBreak2 = mLastStartBreak2 > 0;
        if (mStarted) {
            if (mFinished) {
                // final state
                mJobButton.setVisibility(View.GONE);
                mBreakButton.setVisibility(View.GONE);
                mBreak2Button.setVisibility(View.GONE);
            } else {
                // state: started
                mJobButton.setVisibility(View.VISIBLE);
                if (mBreak2) {
                    mBreakButton.setVisibility(View.GONE);
                    mBreak2Button.setVisibility(View.VISIBLE);
                } else {
                    mBreakButton.setVisibility(View.VISIBLE);
                    mBreak2Button.setVisibility(View.VISIBLE);
                }
            }
            // Show in any case the start date
            mCalendar.setTimeInMillis(mStartDate);
            String datetime = DateTimeFormater.mDateFormater.format(mCalendar
                    .getTime())
                    + " "
                    + DateTimeFormater.mTimeFormater
                    .format(mCalendar.getTime());
            mTimestamp.setText(getString(R.string.started_at, datetime));
            mJobButton.setText(R.string.job_done);
        } else {
            // state: not yet started
            mJobButton.setVisibility(View.VISIBLE);
            mBreakButton.setVisibility(View.GONE);
            mBreak2Button.setVisibility(View.GONE);
            mTimestamp.setText(R.string.job_not_yet_started);
            mJobButton.setText(R.string.starting_job);
        }
        if (mBreak) {
            mBreakButton.setText(R.string.end_of_break);
            // break2 implies break button not visible
        } else {
            mBreakButton.setText(R.string.having_a_break);
        }
        if (mBreak2) {
            mBreak2Button.setText(R.string.continue_now);
        } else {
            mBreak2Button.setText(R.string.continue_tomorrow);
        }

        String plannedDurationString = DurationFormater.formatDuration(this,
                mPlannedDuration, DurationFormater.TYPE_FORMAT_NICE);
        mSetPlannedDuration.setText(plannedDurationString);
        updateInfo(DurationFormater.TYPE_FORMAT_NICE);
        updateDateTimeButtons();
        updateDisplay(0);
    }

    /**
     * Type can be TYPE_FORMAT_SECONDS or TYPE_FORMAT_NICE and is used in
     * formatDuration();
     *
     * @param type
     */
    private void updateInfo(int type) {
        if (type == DurationFormater.TYPE_FORMAT_NICE
                || type == DurationFormater.TYPE_FORMAT_DONT_UPDATE_INPUT_BOX) {
            // update fields that don't change over time:

            String rateString = mDecimalFormat.format(mHourlyRate
                    * Timesheet.RATE_FACTOR);
            mHourlyRateInfo.setText(getString(R.string.hourly_rate_info,
                    rateString));

            if (type != DurationFormater.TYPE_FORMAT_DONT_UPDATE_INPUT_BOX) {
                // Don't update this, if this routine was called
                // because the user edited the hourly rate in the input box.
                mSetHourlyRate.setText(rateString);
            }
        }

        int breaktype = DurationFormater.TYPE_FORMAT_NICE;
        if (mStarted && mBreak && !mBreak2) {
            breaktype = DurationFormater.TYPE_FORMAT_SECONDS;
        }
        long breakDuration = getBreakDuration();
        String breakString = DurationFormater.formatDuration(this,
                breakDuration, breaktype);
        mBreakInfo.setText(getString(R.string.break_info, breakString));
        mSetBreak.setText(breakString);

        int worktype = DurationFormater.TYPE_FORMAT_NICE;
        if (mStarted && !mFinished && !mBreak2) {
            worktype = DurationFormater.TYPE_FORMAT_SECONDS;
        }
        long workDuration = getWorkDuration();

        mDurationInfo.setText(getString(R.string.duration_info,
                DurationFormater.formatDuration(this, workDuration, worktype)));

        double extrasTotal = ((double) mExtraTotal) * Timesheet.RATE_FACTOR;
        double total = ((((double) (workDuration - breakDuration)) * Timesheet.HOUR_FACTOR) * (((double) mHourlyRate) * Timesheet.RATE_FACTOR)) + extrasTotal;
        mTotalInfo.setText(getString(R.string.total_info,
                "" + mDecimalFormat.format(total)));

        mExtrasInfo.setText(getString(R.string.extras_info,
                mDecimalFormat.format(extrasTotal)));
        if (mExtraTotal == 0) {
            mExtrasInfo.setVisibility(View.GONE);
        } else {
            mExtrasInfo.setVisibility(View.VISIBLE);
        }
        if (mPlannedDuration <= 0 || workDuration - breakDuration <= mPlannedDuration) {
            mDurationInfo.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
        } else {
            mDurationInfo.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
        outState.putInt(ORIGINAL_STATE, mState);
        outState.putString(ORIGINAL_URI, mUri.toString());
        outState.putBoolean(SHOW_MORE, mShowingMore);
        outState.putBoolean(SHOW_RECENT_NOTES_BUTTON, mShowRecentNotesButton);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider. We don't need
        // to do this if only editing.
        updateDatabase();

        if (mUpdateCalendarEvent && !TextUtils.isEmpty(mCalendarUri)) {
            // note, mPlannedDate and mPlannedDuration might be null, while
            // mCalendarUri != null
            // due to export of the final times. This results in deletion of the
            // calendar entry with the final times.
            // Maybe two calendar references are needed.
            setOrUpdateCalendarEvent(mCalendarUri, mPlannedDate,
                    mPlannedDuration, mCalendarAuthority);
        }
        cancelUpdateDisplay();
    }

    /**
     * Write all editable fields back to database.
     */
    private void updateDatabase() {
        updateDatabase(getContentValues());
    }

    private ContentValues getContentValues() {
        String text = mText.getText().toString();
        int length = text.length();
        ContentValues values = new ContentValues();
        // Bump the modification time to now.
        values.put(Job.MODIFIED_DATE, System.currentTimeMillis());
        String title = text.substring(0, Math.min(30, length));
        int firstNewline = title.indexOf('\n');
        if (firstNewline > 0) {
            title = title.substring(0, firstNewline);
        } else if (length > 30) {
            int lastSpace = title.lastIndexOf(' ');
            if (lastSpace > 0) {
                title = title.substring(0, lastSpace);
            }
        }
        if (title.length() > 0) {
            values.put(Job.TITLE, title);
        }
        // Write our text back into the provider.
        values.put(Job.NOTE, text);
        values.put(Job.CUSTOMER, mCustomer.getText().toString());

        values.put(Job.HOURLY_RATE, getHourlyRateFromText());
        return values;
    }

    protected void updateDatabase(ContentValues values) {
        if (mCursor != null) {
            getContentResolver().update(mUri, values, null, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(1, RESTART_ID, 0, R.string.menu_restart).setShortcut('1', 's').setIcon(R.drawable.ic_menu_play);
        menu.add(1, PAUSE_ID, 0, R.string.menu_pause).setShortcut('2', 'p').setIcon(R.drawable.ic_menu_pause);
        menu.add(1, CONTINUE_ID, 0, R.string.menu_continue).setShortcut('3', 'c').setIcon(R.drawable.ic_menu_continue);
        menu.add(1, END_ID, 0, R.string.menu_end).setShortcut('4', 'e').setIcon(R.drawable.ic_menu_stop);
        menu.add(0, REVERT_ID, 0, R.string.menu_revert).setShortcut('0', 'r').setIcon(android.R.drawable.ic_menu_revert);
        menu.add(1, DELETE_ID, 0, R.string.menu_delete).setShortcut('1', 'd').setIcon(android.R.drawable.ic_menu_delete);
        menu.add(1, EVENT_ID, 0, R.string.menu_calendar).setShortcut('5', 'c').setIcon(android.R.drawable.ic_menu_my_calendar);
        menu.add(1, ADD_EXTRA_ITEM_ID, 0, R.string.menu_extra_items).setShortcut('7', 'x').setIcon(android.R.drawable.ic_menu_add);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        new MenuIntentOptionsWithIcons(this, menu)
                .addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, JobActivity.class), null, intent, 0, null);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean contentChanged = !mOriginalContent.equals(mText.getText()
                .toString());
        menu.setGroupVisible(0, contentChanged);

        if (mStarted) {
            menu.findItem(RESTART_ID).setTitle(R.string.menu_restart);
        } else {
            menu.findItem(RESTART_ID).setTitle(R.string.menu_start);
        }

        menu.findItem(PAUSE_ID).setVisible(mStarted && !mBreak && !mFinished);

        menu.findItem(CONTINUE_ID).setVisible(mFinished || mBreak);

        menu.findItem(END_ID).setVisible(mStarted);
        menu.findItem(EVENT_ID).setVisible(
                mCursor.getString(COLUMN_INDEX_CALENDAR_REF) != null);

        MenuItem sendMenu = menu.findItem(SEND_ID);
        if (sendMenu != null) {
            sendMenu.setVisible(!mStarted);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case REVERT_ID /*1*/:
                cancelJob();
                break;
            case DISCARD_ID /*2*/:
                cancelJob();
                break;
            case DELETE_ID /*3*/:
                deleteJob();
                finish();
                break;
            case LIST_ID /*4*/:
                startActivity(new Intent(this, JobListActivity.class));
                break;
            case RESTART_ID /*5*/:
                startJob();
                break;
            case PAUSE_ID /*6*/:
                startBreak();
                break;
            case CONTINUE_ID /*7*/:
                continueJob();
                break;
            case END_ID /*8*/:
                stopJob();
                break;
            case EVENT_ID /*9*/:
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(mCursor.getString(COLUMN_INDEX_CALENDAR_REF)));
                intent2.putExtra(CalendarApp.EVENT_BEGIN_TIME, mPlannedDate);
                intent2.putExtra(CalendarApp.EVENT_END_TIME, mPlannedDate + mPlannedDuration);
                try {
                    startActivity(intent2);
                    break;
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.calendar_missing, Toast.LENGTH_SHORT).show();
                    break;
                }
            case SEND_ID /*10*/:
                startActivity(createSendIntent());
                break;
            case ADD_EXTRA_ITEM_ID /*11*/:
                Intent intent = new Intent(this, InvoiceItemActivity.class);
                intent.setData(mUri);
                intent.putExtra("jobid", parseLong(mUri.getLastPathSegment()));
                startActivity(intent);
                break;
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v == mCustomer) {
            getMenuInflater().inflate(R.menu.context_menu_customer, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.pick_contact) {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setData(People.CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
            return true;
        } else if (item.getItemId() != R.id.call_contact) {
            return super.onContextItemSelected(item);
        } else {
            Log.v(TAG, "customer ref:" + mCustomerRef);
            if (mCustomerRef == null || !mCustomerRef.startsWith("content://contacts")) {
                intent = new Intent(Intent.ACTION_PICK);
                intent.setData(People.CONTENT_URI);
                intent.putExtra("call", true);
                startActivityForResult(intent, STATE_INSERT);
                return true;
            }
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mCustomerRef));
            startActivity(intent);
            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
            ContentValues values = new ContentValues(1);
            Cursor c = managedQuery(data.getData(), null, null, null, null);
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndexOrThrow(People.NAME));
                values.put(Job.CUSTOMER, name);
                setCustomerText(name);
                values.put(Job.CUSTOMER_REF, data.getDataString());
                updateDatabase(values);
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        switch (id) {
            case DIALOG_ID_START_DATE /*1*/:
                return new DatePickerDialog(this, mStartDateSetListener, year, month, day);
            case DIALOG_ID_START_TIME /*2*/:
                return new TimePickerDialog(this, mStartTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case DIALOG_ID_END_DATE /*3*/:
                return new DatePickerDialog(this, mEndDateSetListener, year, month, day);
            case DIALOG_ID_END_TIME /*4*/:
                return new TimePickerDialog(this, mEndTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case DIALOG_ID_BREAK /*5*/:
                return new DurationPickerDialog(this, mBreakSetListener, STATE_EDIT, STATE_EDIT);
            case DIALOG_ID_RESTART_JOB /*6*/:
                return new Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setMessage(R.string.dialog_restart_job).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        startJob();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
            case DIALOG_ID_STOP_JOB /*7*/:
                return new Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setMessage(R.string.dialog_stop_job).setPositiveButton(R.string.end_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        stopJob();
                    }
                }).setNeutralButton(R.string.continue_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        continueJob();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
            case DIALOG_ID_RECENT_NOTES /*8*/:
                if (mRecentNoteList == null) {
                    Log.d(TAG, "getTitlesList");
                    mRecentNoteList = getTitlesList(this);
                }
                Log.i(TAG, "Show recent notes create");
                return new Builder(this).setTitle(R.string.recent_notes).setItems(mRecentNoteList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mText.setText(JobActivity.getNote(JobActivity.this, mRecentNoteList[which]));
                    }
                }).create();
            case DIALOG_ID_PLANNED_DATE /*9*/:
                return new DatePickerDialog(this, mPlannedDateSetListener, year, month, day);
            case DIALOG_ID_PLANNED_TIME /*10*/:
                return new TimePickerDialog(this, mPlannedTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case DIALOG_ID_PLANNED_DURATION /*11*/:
                return new DurationPickerDialog(this, mPlannedMinutesSetListener, STATE_EDIT, STATE_EDIT);
            case DIALOG_ID_RATES /*12*/:
                Log.i(TAG, "DIALOG_ID_RATES create");
                return new DurationPickerDialog(this, null, STATE_EDIT, STATE_EDIT);
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        long startDate = mCursor.getLong(COLUMN_INDEX_START);
        long endDate = mCursor.getLong(COLUMN_INDEX_END);
        long plannedDate = mCursor.getLong(COLUMN_INDEX_PLANNED_DATE);

        int minutes;
        switch (id) {
            case DIALOG_ID_START_DATE /*1*/:
                mCalendar.setTimeInMillis(startDate);
                ((DatePickerDialog) dialog).updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                break;
            case DIALOG_ID_START_TIME /*2*/:
                mCalendar.setTimeInMillis(startDate);
                ((TimePickerDialog) dialog).updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) mBillingUnitMinutes);
                break;
            case DIALOG_ID_END_DATE /*3*/:
                mCalendar.setTimeInMillis(endDate);
                ((DatePickerDialog) dialog).updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                break;
            case DIALOG_ID_END_TIME /*4*/:
                mCalendar.setTimeInMillis(endDate);
                ((TimePickerDialog) dialog).updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) mBillingUnitMinutes);
                break;
            case DIALOG_ID_BREAK /*5*/:
                minutes = (int) (mCursor.getLong(RESTART_ID) / 60000);
                ((DurationPickerDialog) dialog).updateTime(minutes / 60, minutes % 60);
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) mBillingUnitMinutes);
                break;
            case DIALOG_ID_RECENT_NOTES /*8*/:
                Log.i(TAG, "Show recent notes prepare");
                break;
            case DIALOG_ID_PLANNED_DATE /*9*/:
                if (plannedDate == 0) {
                    plannedDate = System.currentTimeMillis();
                }
                mCalendar.setTimeInMillis(plannedDate);
                ((DatePickerDialog) dialog).updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                break;
            case DIALOG_ID_PLANNED_TIME /*10*/:
                if (plannedDate == 0) {
                    plannedDate = System.currentTimeMillis();
                }
                mCalendar.setTimeInMillis(plannedDate);
                ((TimePickerDialog) dialog).updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) mBillingUnitMinutes);
                break;
            case DIALOG_ID_PLANNED_DURATION /*11*/:
                minutes = (int) (mCursor.getLong(COLUMN_INDEX_PLANNED_DURATION) / 60000);
                ((DurationPickerDialog) dialog).updateTime(minutes / 60, minutes % 60);
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) mBillingUnitMinutes);
                break;
            case DIALOG_ID_RATES /*12*/:
                float currentRate = mCursor.getFloat(COLUMN_INDEX_HOURLY_RATE) / 100.0f;
                Log.d(TAG, "hrate>" + currentRate);
                String[] parts = Float.toString(currentRate).split("\\.");
                Log.d(TAG, "pre>" + parts[0] + "< post>" + parts[1]);
                ((DurationPickerDialog) dialog).updateTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                break;
            default:
        }
    }

    private void updateDateTimeButtons() {
        long startDate = mCursor.getLong(COLUMN_INDEX_START);
        long endDate = mCursor.getLong(COLUMN_INDEX_END);
        long plannedDate = mCursor.getLong(COLUMN_INDEX_PLANNED_DATE);
        if (startDate != 0) {
            mSetStartDate.setEnabled(true);
            mSetStartTime.setEnabled(true);
            mSetStartDate.setText(DateTimeFormater.mDateFormater.format(valueOf(startDate)));
            mSetStartTime.setText(DateTimeFormater.mTimeFormater.format(valueOf(startDate)));
        } else {
            mSetStartDate.setEnabled(false);
            mSetStartTime.setEnabled(false);
            mSetStartDate.setText(R.string.unknown);
            mSetStartTime.setText(R.string.unknown);
        }
        if (endDate != 0) {
            mSetEndDate.setEnabled(true);
            mSetEndTime.setEnabled(true);
            mSetEndDate.setText(DateTimeFormater.mDateFormater.format(valueOf(endDate)));
            mSetEndTime.setText(DateTimeFormater.mTimeFormater.format(valueOf(endDate)));
        } else {
            mSetEndDate.setEnabled(false);
            mSetEndTime.setEnabled(false);
            mSetEndDate.setText(R.string.unknown);
            mSetEndTime.setText(R.string.unknown);
        }
        if (plannedDate != 0) {
            mSetPlannedDate.setText(DateTimeFormater.mDateFormater.format(valueOf(plannedDate)));
            mSetPlannedTime.setText(DateTimeFormater.mTimeFormater.format(valueOf(plannedDate)));
        } else {
            mSetPlannedDate.setText(R.string.unknown);
            mSetPlannedTime.setText(R.string.unknown);
        }
        if (mStarted && !mFinished && mBreak) {
            mSetBreak.setEnabled(false);
        } else {
            mSetBreak.setEnabled(true);
        }
    }

    protected String setOrUpdateCalendarEvent(String uri, long millis, long duration, int calendarAuthority) {
        mCalendarUri = CalendarApp.setOrUpdateCalendarEvent(this, uri, millis, duration, mCustomer.getText().toString(), mText.getText().toString(), calendarAuthority);
        mUpdateCalendarEvent = false;
        return uri;
    }

    protected Intent createSendIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("content://" + Timesheet.AUTHORITY
                + "/persons"));

        intent.setClass(this, InsertJobActivity.class);
        intent.putExtra(TimesheetIntent.EXTRA_CUSTOMER, mCustomer.getText().toString());
        intent.putExtra(TimesheetIntent.EXTRA_HOURLY_RATE, mHourlyRate);
        intent.putExtra(TimesheetIntent.EXTRA_NOTE, mText.getText().toString());
        intent.putExtra(TimesheetIntent.EXTRA_PLANNED_DATE, mCursor.getLong(COLUMN_INDEX_PLANNED_DATE));
        intent.putExtra(TimesheetIntent.EXTRA_PLANNED_DURATION, mCursor.getLong(COLUMN_INDEX_PLANNED_DURATION));
        return intent;
    }

    /**
     * Take care of canceling work on a note. Deletes the note if we had created
     * it, otherwise reverts to the original text.
     */
    private void cancelJob() {
        if (mCursor != null) {
            String tmp = mText.getText().toString();
            mText.setText(mOriginalContent);
            mOriginalContent = tmp;
        }
    }

    /**
     * Take care of deleting a note. Simply deletes the entry.
     */
    private void deleteJob() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.1 */
    class C00131 implements OnDateSetListener {
        C00131() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.DISCARD_ID));
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mCalendar.get(Calendar.MINUTE);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = round(mCalendar.getTimeInMillis());
            updateDatabase();
            ContentValues values = new ContentValues();
            values.put(Job.START_DATE, valueOf(millis));
            getContentResolver().update(mUri, values, null, null);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.2 */
    class C00142 implements OnTimeSetListener {
        C00142() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.DISCARD_ID));
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = round(mCalendar.getTimeInMillis());
            ContentValues values = getContentValues();
            values.put(Job.START_DATE, valueOf(millis));
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.3 */
    class C00153 implements OnDateSetListener {
        C00153() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.DIALOG_ID_END_DATE));
            int hour = mCalendar.get(Calendar.HOUR);
            int minute = mCalendar.get(Calendar.MINUTE);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = round(mCalendar.getTimeInMillis());
            ContentValues values = getContentValues();
            values.put(Job.END_DATE, valueOf(millis));
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.4 */
    class C00164 implements OnTimeSetListener {
        C00164() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.DIALOG_ID_END_DATE));
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = round(mCalendar.getTimeInMillis());
            ContentValues values = getContentValues();
            values.put(Job.END_DATE, millis);
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.5 */
    private class C00175 implements OnTimeSetListener {
        C00175() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            long millis = round((long) (((hourOfDay * 60) + minute) * 60000));
            ContentValues values = getContentValues();
            values.put(Job.LAST_START_BREAK, (String) null);
            values.put(Job.BREAK_DURATION, millis);
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.6 */
    private class C00186 implements OnDateSetListener {
        C00186() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.END_ID));
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mCalendar.get(Calendar.MINUTE);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = round(mCalendar.getTimeInMillis());
            String calendarRef = setOrUpdateCalendarEvent(mCursor.getString(JobActivity.SEND_ID), millis, valueOf(mCursor.getLong(JobActivity.EVENT_ID)), mCalendarAuthority);
            ContentValues values = getContentValues();
            values.put(Job.PLANNED_DATE, valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.7 */
    class C00197 implements OnTimeSetListener {
        C00197() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendar.setTimeInMillis(mCursor.getLong(JobActivity.END_ID));
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.setTimeInMillis(0);
            mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = round(mCalendar.getTimeInMillis());
            String calendarRef = setOrUpdateCalendarEvent(mCursor.getString(JobActivity.SEND_ID), millis, valueOf(mCursor.getLong(JobActivity.EVENT_ID)), mCalendarAuthority);
            ContentValues values = getContentValues();
            values.put(Job.PLANNED_DATE, valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.8 */
    private class C00208 implements OnTimeSetListener {
        C00208() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            long millis = round((long) (((hourOfDay * 60) + minute) * 60000));
            String calendarRef = setOrUpdateCalendarEvent(mCursor.getString(JobActivity.SEND_ID), valueOf(mCursor.getLong(JobActivity.END_ID)), millis, mCalendarAuthority);
            ContentValues values = getContentValues();
            values.put(Job.PLANNED_DURATION, valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            updateDatabase(values);
            updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.9 */
    private class C00219 implements TextWatcher {
        C00219() {
        }

        public void afterTextChanged(Editable s) {
            mShowRecentNotesButton = false;
            updateRecentNotesButton(true);
            mUpdateCalendarEvent = true;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private class DisplayUpdater implements Runnable {
        public void run() {
            updateEndTime();
            updateDisplay(1000);
        }

        private void updateEndTime() {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            Date date = mCalendar.getTime();
            mSetEndTime.setText(DateTimeFormater.mTimeWithSecondsFormater.format(date));
            mSetEndDate.setText(DateTimeFormater.mDateFormater.format(date));
            updateInfo(DurationFormater.TYPE_FORMAT_SECONDS);
        }
    }
}
