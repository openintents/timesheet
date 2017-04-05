package org.openintents.timesheet.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.distribution.LicenseUtils;
import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.CalendarApp;
import org.openintents.timesheet.Timesheet.Calendars;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.TimesheetProvider;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class JobActivity extends Activity {
    private static final int ADD_EXTRA_ITEM_ID = 11;
    private static final int COLUMN_INDEX_BREAK2_COUNT = 14;
    private static final int COLUMN_INDEX_BREAK2_DURATION = 13;
    private static final int COLUMN_INDEX_BREAK_DURATION = 5;
    private static final int COLUMN_INDEX_CALENDAR_REF = 10;
    private static final int COLUMN_INDEX_CUSTOMER = 7;
    private static final int COLUMN_INDEX_CUSTOMER_REF = 19;
    private static final int COLUMN_INDEX_END = 3;
    private static final int COLUMN_INDEX_EXTRA_TOTAL = 11;
    private static final int COLUMN_INDEX_HOURLY_RATE = 6;
    private static final int COLUMN_INDEX_HOURLY_RATE2 = 15;
    private static final int COLUMN_INDEX_HOURLY_RATE2_START = 16;
    private static final int COLUMN_INDEX_HOURLY_RATE3 = 17;
    private static final int COLUMN_INDEX_HOURLY_RATE3_START = 18;
    private static final int COLUMN_INDEX_LAST_START_BREAK = 4;
    private static final int COLUMN_INDEX_LAST_START_BREAK2 = 12;
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final int COLUMN_INDEX_PLANNED_DATE = 8;
    private static final int COLUMN_INDEX_PLANNED_DURATION = 9;
    private static final int COLUMN_INDEX_START = 2;
    private static final int CONTINUE_ID = 7;
    private static final int DELETE_ID = 3;
    static final int DIALOG_ID_BREAK = 5;
    static final int DIALOG_ID_END_DATE = 3;
    static final int DIALOG_ID_END_TIME = 4;
    static final int DIALOG_ID_PLANNED_DATE = 9;
    static final int DIALOG_ID_PLANNED_DURATION = 11;
    static final int DIALOG_ID_PLANNED_TIME = 10;
    static final int DIALOG_ID_RATES = 12;
    static final int DIALOG_ID_RECENT_NOTES = 8;
    static final int DIALOG_ID_RESTART_JOB = 6;
    static final int DIALOG_ID_START_DATE = 1;
    static final int DIALOG_ID_START_TIME = 2;
    static final int DIALOG_ID_STOP_JOB = 7;
    private static final int DISCARD_ID = 2;
    private static final int END_ID = 8;
    private static final int EVENT_ID = 9;
    private static final int LIST_ID = 4;
    private static final String ORIGINAL_CONTENT = "origNote";
    private static final String ORIGINAL_STATE = "origState";
    private static final String ORIGINAL_URI = "orgUri";
    private static final int PAUSE_ID = 6;
    private static final String[] PROJECTION;
    private static final int REQUEST_PICK_CONTACT = 1;
    private static final int RESTART_ID = 5;
    private static final int REVERT_ID = 1;
    private static final int SEND_ID = 10;
    private static final String SHOW_MORE = "show_more";
    private static final String SHOW_RECENT_NOTES_BUTTON = "show_recent_notes";
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    static final String TAG = "JobActivity";
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
    NumberFormat mDecimalFormat;
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
    String[] mRecentNoteList;
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

    /* renamed from: org.openintents.timesheet.activity.JobActivity.1 */
    class C00131 implements OnDateSetListener {
        C00131() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.DISCARD_ID));
            int hour = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_PLANNED_DURATION);
            int minute = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_RATES);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            JobActivity.this.updateDatabase();
            ContentValues values = new ContentValues();
            values.put(Job.START_DATE, Long.valueOf(millis));
            JobActivity.this.getContentResolver().update(JobActivity.this.mUri, values, null, null);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.2 */
    class C00142 implements OnTimeSetListener {
        C00142() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.DISCARD_ID));
            int year = JobActivity.this.mCalendar.get(JobActivity.STATE_INSERT);
            int month = JobActivity.this.mCalendar.get(JobActivity.DISCARD_ID);
            int day = JobActivity.this.mCalendar.get(JobActivity.RESTART_ID);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.START_DATE, Long.valueOf(millis));
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.3 */
    class C00153 implements OnDateSetListener {
        C00153() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.DIALOG_ID_END_DATE));
            int hour = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_PLANNED_DURATION);
            int minute = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_RATES);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.END_DATE, Long.valueOf(millis));
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.4 */
    class C00164 implements OnTimeSetListener {
        C00164() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.DIALOG_ID_END_DATE));
            int year = JobActivity.this.mCalendar.get(JobActivity.STATE_INSERT);
            int month = JobActivity.this.mCalendar.get(JobActivity.DISCARD_ID);
            int day = JobActivity.this.mCalendar.get(JobActivity.RESTART_ID);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.END_DATE, Long.valueOf(millis));
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.5 */
    class C00175 implements OnTimeSetListener {
        C00175() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            long millis = JobActivity.this.round((long) (((hourOfDay * 60) + minute) * 60000));
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.LAST_START_BREAK, (String) null);
            values.put(Job.BREAK_DURATION, Long.valueOf(millis));
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.6 */
    class C00186 implements OnDateSetListener {
        C00186() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.END_ID));
            int hour = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_PLANNED_DURATION);
            int minute = JobActivity.this.mCalendar.get(JobActivity.DIALOG_ID_RATES);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, monthOfYear, dayOfMonth, hour, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            String calendarRef = JobActivity.this.setOrUpdateCalendarEvent(JobActivity.this.mCursor.getString(JobActivity.SEND_ID), millis, Long.valueOf(JobActivity.this.mCursor.getLong(JobActivity.EVENT_ID)).longValue(), JobActivity.this.mCalendarAuthority);
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.PLANNED_DATE, Long.valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.7 */
    class C00197 implements OnTimeSetListener {
        C00197() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            JobActivity.this.mCalendar.setTimeInMillis(JobActivity.this.mCursor.getLong(JobActivity.END_ID));
            int year = JobActivity.this.mCalendar.get(JobActivity.STATE_INSERT);
            int month = JobActivity.this.mCalendar.get(JobActivity.DISCARD_ID);
            int day = JobActivity.this.mCalendar.get(JobActivity.RESTART_ID);
            JobActivity.this.mCalendar.setTimeInMillis(0);
            JobActivity.this.mCalendar.set(year, month, day, hourOfDay, minute);
            long millis = JobActivity.this.round(JobActivity.this.mCalendar.getTimeInMillis());
            String calendarRef = JobActivity.this.setOrUpdateCalendarEvent(JobActivity.this.mCursor.getString(JobActivity.SEND_ID), millis, Long.valueOf(JobActivity.this.mCursor.getLong(JobActivity.EVENT_ID)).longValue(), JobActivity.this.mCalendarAuthority);
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.PLANNED_DATE, Long.valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.8 */
    class C00208 implements OnTimeSetListener {
        C00208() {
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            long millis = JobActivity.this.round((long) (((hourOfDay * 60) + minute) * 60000));
            String calendarRef = JobActivity.this.setOrUpdateCalendarEvent(JobActivity.this.mCursor.getString(JobActivity.SEND_ID), Long.valueOf(JobActivity.this.mCursor.getLong(JobActivity.END_ID)).longValue(), millis, JobActivity.this.mCalendarAuthority);
            ContentValues values = JobActivity.this.getContentValues();
            values.put(Job.PLANNED_DURATION, Long.valueOf(millis));
            values.put(Job.CALENDAR_REF, calendarRef);
            JobActivity.this.updateDatabase(values);
            JobActivity.this.updateFromCursor();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivity.9 */
    class C00219 implements TextWatcher {
        C00219() {
        }

        public void afterTextChanged(Editable s) {
            JobActivity.this.mShowRecentNotesButton = false;
            JobActivity.this.updateRecentNotesButton(true);
            JobActivity.this.mUpdateCalendarEvent = true;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    public class DisplayUpdater implements Runnable {
        public void run() {
            updateEndTime();
            JobActivity.this.updateDisplay(1000);
        }

        private void updateEndTime() {
            JobActivity.this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            JobActivity.this.mSetEndTime.setText(DateTimeFormater.mTimeWithSecondsFormater.format(JobActivity.this.mCalendar.getTime()));
            JobActivity.this.mSetEndDate.setText(DateTimeFormater.mDateFormater.format(JobActivity.this.mCalendar.getTime()));
            JobActivity.this.updateInfo(JobActivity.STATE_INSERT);
        }
    }

    public JobActivity() {
        this.mStartBillingImmediately = false;
        this.mHandler = new Handler();
        this.mDisplayUpdater = new DisplayUpdater();
        this.mCalendar = Calendar.getInstance();
        this.mDecimalFormat = new DecimalFormat("0.00");
        this.mShowingMore = false;
        this.mShowRecentNotesButton = false;
        this.mRecentNoteList = null;
        this.mExtraTotal = 0;
        this.mStartDateSetListener = new C00131();
        this.mStartTimeSetListener = new C00142();
        this.mEndDateSetListener = new C00153();
        this.mEndTimeSetListener = new C00164();
        this.mBreakSetListener = new C00175();
        this.mPlannedDateSetListener = new C00186();
        this.mPlannedTimeSetListener = new C00197();
        this.mPlannedMinutesSetListener = new C00208();
    }

    static {
        PROJECTION = new String[]{Reminders._ID, TimesheetIntent.EXTRA_NOTE, Job.START_DATE, Job.END_DATE, Job.LAST_START_BREAK, Job.BREAK_DURATION, Job.HOURLY_RATE, TimesheetIntent.EXTRA_CUSTOMER, Job.PLANNED_DATE, Job.PLANNED_DURATION, Job.CALENDAR_REF, TimesheetProvider.QUERY_EXTRAS_TOTAL, Job.LAST_START_BREAK2, Job.BREAK2_DURATION, Job.BREAK2_COUNT, Job.HOURLY_RATE2, Job.HOURLY_RATE2_START, Job.HOURLY_RATE3, Job.HOURLY_RATE3_START, Job.CUSTOMER_REF};
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mRateAdatper = new RateListAdapter(this);
        this.mRecentNoteList = null;
        this.mStartBillingImmediately = false;
        this.mBillingUnit = 0;
        this.mBillingUnitHalf = 0;
        this.mBillingUnitMinutes = 1;
        this.mCalendarAuthority = getCalendarAuthority(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.mBillingUnit = Long.parseLong(prefs.getString(PreferenceActivity.PREFS_BILLING_UNIT, PreferenceActivity.PREFS_BILLING_UNIT_DEFAULT));
        this.mBillingUnitHalf = this.mBillingUnit / 2;
        this.mBillingUnitMinutes = this.mBillingUnit / 60000;
        if (this.mBillingUnitMinutes == 0) {
            this.mBillingUnitMinutes = 1;
        }
        boolean createEvent = false;
        String action = intent.getAction();
        if (savedInstanceState != null) {
            this.mState = STATE_EDIT;
            if (savedInstanceState.getString(ORIGINAL_URI) != null) {
                this.mUri = Uri.parse(savedInstanceState.getString(ORIGINAL_URI));
            } else {
                startActivity(new Intent(this, JobList.class));
                finish();
            }
        } else if ("android.intent.action.EDIT".equals(action) || "android.intent.action.VIEW".equals(action)) {
            this.mState = STATE_EDIT;
            this.mUri = intent.getData();
        } else if ("android.intent.action.INSERT".equals(action) || "android.intent.action.MAIN".equals(action) || action == null) {
            this.mState = STATE_INSERT;
            if (intent.getData() == null) {
                intent.setData(Job.CONTENT_URI);
            }
            ContentValues values = new ContentValues();
            this.mPreselectedCustomer = intent.getStringExtra(TimesheetIntent.EXTRA_CUSTOMER);
            Log.i(TAG, "Intent extra: Customer = " + this.mPreselectedCustomer);
            if (!TextUtils.isEmpty(this.mPreselectedCustomer)) {
                if (this.mPreselectedCustomer.equals(getString(R.string.all_customers))) {
                    this.mPreselectedCustomer = null;
                } else {
                    values.put(TimesheetIntent.EXTRA_CUSTOMER, this.mPreselectedCustomer);
                }
            }
            String note = getIntent().getStringExtra(TimesheetIntent.EXTRA_NOTE);
            if (!TextUtils.isEmpty(note)) {
                values.put(TimesheetIntent.EXTRA_NOTE, note);
            }
            int rate = getIntent().getIntExtra(TimesheetIntent.EXTRA_HOURLY_RATE, -1);
            if (rate >= 0) {
                values.put(Job.HOURLY_RATE, Integer.valueOf(rate));
            }
            long plannedDate = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DATE, 0);
            if (plannedDate > 0) {
                values.put(Job.PLANNED_DATE, Long.valueOf(plannedDate));
                createEvent = true;
            }
            long plannedDuration = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DURATION, 0);
            if (plannedDuration > 0) {
                values.put(Job.PLANNED_DURATION, Long.valueOf(plannedDuration));
                createEvent = true;
            }
            if (TextUtils.isEmpty(this.mPreselectedCustomer)) {
                this.mPreselectedCustomer = getLastCustomer(this);
            }
            this.mUri = getContentResolver().insert(intent.getData(), values);
            this.mStartBillingImmediately = prefs.getBoolean(PreferenceActivity.PREFS_START_JOBS_IMMEDIATELY, false);
            intent.setAction("android.intent.action.EDIT");
            intent.setData(this.mUri);
            setIntent(intent);
            if (this.mUri == null) {
                Log.e(TAG, "Failed to insert new job into " + getIntent().getData());
                setResult(STATE_EDIT);
                finish();
                return;
            }
            setResult(-1, new Intent().setAction(this.mUri.toString()));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
        setContentView(R.layout.job);
        this.mText = (EditText) findViewById(R.id.note);
        this.mText.addTextChangedListener(new C00219());
        this.mCustomer = (AutoCompleteTextView) findViewById(R.id.customer);
        this.mCustomer.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
                JobActivity.this.mUpdateCalendarEvent = true;
            }

            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
            }

            public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
            }
        });
        registerForContextMenu(this.mCustomer);
        this.mRateButton = (ImageButton) findViewById(R.id.rate_button);
        this.mRateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                JobActivity.this.rateButtonClicked();
            }
        });
        this.mTimestamp = (TextView) findViewById(R.id.timestamp);
        this.mBreakInfo = (TextView) findViewById(R.id.break_info);
        this.mDurationInfo = (TextView) findViewById(R.id.duration_info);
        this.mTotalInfo = (TextView) findViewById(R.id.total_info);
        this.mExtrasInfo = (TextView) findViewById(R.id.extras_info);
        this.mHourlyRateInfo = (TextView) findViewById(R.id.hourly_rate_info);
        this.mJobButton = (Button) findViewById(R.id.start_end);
        this.mJobButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                JobActivity.this.jobTimeClicked();
            }
        });
        this.mBreakButton = (Button) findViewById(R.id.break_start_end);
        this.mBreakButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                JobActivity.this.breakTimeClicked();
            }
        });
        this.mBreak2Button = (Button) findViewById(R.id.break2_start_end);
        this.mBreak2Button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                JobActivity.this.break2TimeClicked();
            }
        });
        this.mEditPanel1 = (LinearLayout) findViewById(R.id.edit_panel1);
        this.mEditPanel2 = (LinearLayout) findViewById(R.id.edit_panel2);
        this.mEditPanel3 = (LinearLayout) findViewById(R.id.edit_panel3);
        this.mEditHourlyRate = (LinearLayout) findViewById(R.id.edit_hourlyrate);
        this.mInfoPanel1 = (RelativeLayout) findViewById(R.id.info_panel1);
        this.mInfoPanel3 = (RelativeLayout) findViewById(R.id.info_panel3);
        this.mSetHourlyRate = (EditText) findViewById(R.id.set_hourly_rate);
        this.mSetHourlyRate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                JobActivity.this.hourlyRateChanged();
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
                        JobActivity.this.showDialog(JobActivity.DIALOG_ID_RATES);
                    } catch (Exception ex) {
                        Log.e(JobActivity.TAG, ex.getMessage());
                    }
                }
            });
        }
        this.mSetStartDate = (Button) findViewById(R.id.set_start_date);
        this.mSetStartDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.STATE_INSERT);
            }
        });
        this.mSetStartTime = (Button) findViewById(R.id.set_start_time);
        this.mSetStartTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.DISCARD_ID);
            }
        });
        this.mSetPlannedDate = (Button) findViewById(R.id.set_planned_date);
        this.mSetPlannedDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.EVENT_ID);
            }
        });
        this.mSetPlannedTime = (Button) findViewById(R.id.set_planned_time);
        this.mSetPlannedTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.SEND_ID);
            }
        });
        this.mSetPlannedDuration = (Button) findViewById(R.id.set_planned_minutes);
        this.mSetPlannedDuration.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.DIALOG_ID_PLANNED_DURATION);
            }
        });
        this.mSetEndDate = (Button) findViewById(R.id.set_end_date);
        this.mSetEndDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.DIALOG_ID_END_DATE);
            }
        });
        this.mSetEndTime = (Button) findViewById(R.id.set_end_time);
        this.mSetEndTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.LIST_ID);
            }
        });
        this.mSetBreak = (Button) findViewById(R.id.set_break_minutes);
        this.mSetBreak.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showDialog(JobActivity.RESTART_ID);
            }
        });
        this.mRecentNotes = (Button) findViewById(R.id.recent_notes);
        this.mRecentNotes.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                JobActivity.this.showRecentNotesDialog();
            }
        });
        this.mCursor = managedQuery(this.mUri, PROJECTION, null, null, null);
        this.mCustomerList = getCustomerList(this);
        this.mCustomer.setAdapter(new ArrayAdapter(this, 17367050, this.mCustomerList));
        this.mCustomer.setThreshold(STATE_EDIT);
        this.mCustomer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (JobActivity.this.mCustomer.isPopupShowing()) {
                    JobActivity.this.mCustomer.dismissDropDown();
                } else {
                    JobActivity.this.mCustomer.showDropDown();
                }
            }
        });
        if (this.mCustomerList.length < STATE_INSERT) {
            this.mCustomer.setHint(R.string.customer_hint_first_time);
        }
        this.mCustomer.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                JobActivity.this.autoFillInHourlyRate(JobActivity.this.mCustomerList[position]);
            }
        });
        if (savedInstanceState != null) {
            this.mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            this.mState = savedInstanceState.getInt(ORIGINAL_STATE);
            this.mShowingMore = savedInstanceState.getBoolean(SHOW_MORE);
            this.mShowRecentNotesButton = savedInstanceState.getBoolean(SHOW_RECENT_NOTES_BUTTON);
        } else {
            this.mShowingMore = false;
        }
        this.mCursor.moveToFirst();
        if (createEvent) {
            CalendarApp.insertCalendarEvent(this, this.mUri.toString(), this.mCursor.getLong(END_ID), this.mCursor.getLong(EVENT_ID), this.mCustomer.getText().toString(), this.mText.getText().toString(), this.mCalendarAuthority);
        }
        if (this.mStartBillingImmediately) {
            startJob();
        }
    }

    public static int getCalendarAuthority(Context ctx) {
        ContentResolver contentResolver = ctx.getContentResolver();
        Uri uri = Calendars.CONTENT_URI_1;
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = Reminders._ID;
        Cursor result = contentResolver.query(uri, strArr, null, null, null);
        if (result != null && result.getCount() > 0) {
            return STATE_INSERT;
        }
        contentResolver = ctx.getContentResolver();
        uri = Calendars.CONTENT_URI_2;
        strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = Reminders._ID;
        result = contentResolver.query(uri, strArr, null, null, null);
        return (result == null || result.getCount() <= 0) ? STATE_EDIT : DISCARD_ID;
    }

    private void showRecentNotesDialog() {
        Log.d(TAG, "showRecentNOtes");
        if (this.mRecentNoteList == null) {
            Log.d(TAG, "getTitlesList");
            this.mRecentNoteList = getTitlesList(this);
        }
        if (this.mRecentNoteList.length > 0) {
            Log.d(TAG, "show Dialog + " + this.mRecentNoteList.length);
            showDialog(END_ID);
            return;
        }
        Toast.makeText(this, getString(R.string.no_recent_notes_available), STATE_EDIT).show();
    }

    private void updateRecentNotesButton(boolean animate) {
        if (this.mShowRecentNotesButton || TextUtils.isEmpty(this.mText.getText())) {
            this.mShowRecentNotesButton = true;
            if (!animate) {
                this.mRecentNotes.setVisibility(STATE_EDIT);
                this.mRecentNotesButtonState = STATE_EDIT;
            } else if (this.mRecentNotesButtonState == END_ID) {
                Log.i(TAG, "Fade in");
                FadeAnimation.fadeIn(this, this.mRecentNotes);
                this.mRecentNotesButtonState = STATE_EDIT;
            }
        } else if (!animate) {
            this.mRecentNotes.setVisibility(END_ID);
            this.mRecentNotesButtonState = END_ID;
        } else if (this.mRecentNotesButtonState == 0) {
            Log.i(TAG, "Fade out");
            FadeAnimation.fadeOut(this, this.mRecentNotes);
            this.mRecentNotesButtonState = END_ID;
        }
    }

    protected void setHourlyRate(int whichButton) {
        setHourlyRateViews(0, 0, 0, 0, 0);
    }

    private void autoFillInHourlyRate(String customer) {
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = customer;
        Cursor c = getContentResolver().query(Job.CONTENT_URI, PROJECTION, "customer = ? AND hourly_rate <> 0", strArr, "modified DESC");
        if (c != null && c.moveToFirst()) {
            Log.i(TAG, "Customer information found. Auto-fill in for: " + customer);
            setHourlyRateViews(c.getLong(PAUSE_ID), c.getLong(COLUMN_INDEX_HOURLY_RATE2), c.getLong(COLUMN_INDEX_HOURLY_RATE2_START), c.getLong(COLUMN_INDEX_HOURLY_RATE3), c.getLong(COLUMN_INDEX_HOURLY_RATE3_START));
        }
        if (c != null) {
            c.close();
        }
    }

    private void setHourlyRateViews(long hourlyrate, long hourlyrate2, long hourlyrate2Start, long hourlyrate3, long hourlyrate3Start) {
        String rateString = this.mDecimalFormat.format(((double) hourlyrate) * Timesheet.RATE_FACTOR);
        if (hourlyrate2Start > 0) {
            rateString = new StringBuilder(String.valueOf(rateString)).append(" / ").append(this.mDecimalFormat.format(((double) hourlyrate2) * Timesheet.RATE_FACTOR)).toString();
            if (hourlyrate3Start > 0) {
                rateString = new StringBuilder(String.valueOf(rateString)).append(" / ").append(this.mDecimalFormat.format(((double) hourlyrate3) * Timesheet.RATE_FACTOR)).toString();
            }
            this.mSetHourlyRate.setEnabled(false);
        } else {
            this.mSetHourlyRate.setEnabled(true);
        }
        this.mSetHourlyRate.setText(rateString);
        TextView textView = this.mHourlyRateInfo;
        Object[] objArr = new Object[STATE_INSERT];
        objArr[STATE_EDIT] = rateString;
        textView.setText(getString(R.string.hourly_rate_info, objArr));
    }

    public static String[] getCustomerList(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Job.CONTENT_URI;
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = TimesheetIntent.EXTRA_CUSTOMER;
        Cursor c = contentResolver.query(uri, strArr, null, null, "modified DESC");
        Set<String> set = new TreeSet();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            String customer = c.getString(STATE_EDIT);
            if (!TextUtils.isEmpty(customer)) {
                set.add(customer);
            }
        }
        c.close();
        return (String[]) set.toArray(new String[STATE_EDIT]);
    }

    public static String[] getTitlesList(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Job.CONTENT_URI;
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = Job.TITLE;
        Cursor c = contentResolver.query(uri, strArr, null, null, "modified DESC");
        Vector<String> vec = new Vector();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            String title = c.getString(STATE_EDIT);
            if (!(TextUtils.isEmpty(title) || title.equals(context.getString(17039375)) || vec.contains(title))) {
                vec.add(title);
            }
        }
        c.close();
        return (String[]) vec.toArray(new String[STATE_EDIT]);
    }

    public static String getNote(Context context, String title) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Job.CONTENT_URI;
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = Job.TITLE;
        String[] strArr2 = new String[STATE_INSERT];
        strArr2[STATE_EDIT] = title;
        Cursor c = contentResolver.query(uri, strArr, "title = ?", strArr2, "modified DESC");
        if (c != null && c.moveToFirst()) {
            return c.getString(STATE_EDIT);
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    public static String getLastCustomer(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Job.CONTENT_URI;
        String[] strArr = new String[STATE_INSERT];
        strArr[STATE_EDIT] = TimesheetIntent.EXTRA_CUSTOMER;
        Cursor c = contentResolver.query(uri, strArr, null, null, "modified DESC");
        String customer = "";
        while (c != null && c.moveToNext()) {
            customer = c.getString(STATE_EDIT);
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

    private void rateButtonClicked() {
        this.mShowingMore = !this.mShowingMore;
        showMoreIfRequired();
    }

    private void showMoreIfRequired() {
        if (this.mShowingMore) {
            this.mEditHourlyRate.setVisibility(STATE_EDIT);
            if (this.mStarted) {
                this.mEditPanel1.setVisibility(STATE_EDIT);
                this.mEditPanel2.setVisibility(STATE_EDIT);
                this.mEditPanel3.setVisibility(END_ID);
            } else {
                this.mEditPanel1.setVisibility(END_ID);
                this.mEditPanel2.setVisibility(END_ID);
                this.mEditPanel3.setVisibility(STATE_EDIT);
            }
            this.mInfoPanel1.setVisibility(END_ID);
            this.mInfoPanel3.setVisibility(END_ID);
            return;
        }
        this.mEditPanel1.setVisibility(END_ID);
        this.mEditHourlyRate.setVisibility(END_ID);
        this.mEditPanel2.setVisibility(END_ID);
        this.mEditPanel3.setVisibility(END_ID);
        this.mInfoPanel1.setVisibility(STATE_EDIT);
        this.mInfoPanel3.setVisibility(STATE_EDIT);
    }

    protected void breakTimeClicked() {
        if (this.mBreak) {
            stopBreak();
        } else {
            startBreak();
        }
    }

    protected void break2TimeClicked() {
        if (this.mBreak2) {
            stopBreak2();
        } else {
            startBreak2();
        }
    }

    private void stopBreak() {
        long breakDuration = getBreakDuration();
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK, null);
        values.put(Job.BREAK_DURATION, Long.valueOf(breakDuration));
        updateDatabase(values);
        updateFromCursor();
    }

    private void startBreak() {
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK, Long.valueOf(System.currentTimeMillis()));
        updateDatabase(values);
        updateFromCursor();
    }

    private void stopBreak2() {
        long breakDuration = getBreak2Duration();
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK2, null);
        values.put(Job.BREAK2_DURATION, Long.valueOf(breakDuration));
        updateDatabase(values);
        updateFromCursor();
    }

    private void startBreak2() {
        if (this.mBreak) {
            stopBreak();
        }
        ContentValues values = getContentValues();
        values.put(Job.LAST_START_BREAK2, Long.valueOf(System.currentTimeMillis()));
        updateDatabase(values);
        updateFromCursor();
    }

    private void hourlyRateChanged() {
        long j = this.mHourlyRate;
        j = getHourlyRateFromText();
        ContentValues values = getContentValues();
        values.put(Job.HOURLY_RATE, Long.valueOf(j));
        updateDatabase(values);
        this.mHourlyRate = j;
        updateInfo(DIALOG_ID_END_DATE);
    }

    private long getHourlyRateFromText() {
        try {
            return (long) (100.0d * this.mDecimalFormat.parse(this.mSetHourlyRate.getText().toString()).doubleValue());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing break duration: " + this.mSetHourlyRate.getText());
            return 0;
        }
    }

    private long getBreakDuration() {
        long breakDuration = this.mBreakPreviousDuration;
        if (this.mBreak) {
            return breakDuration + (System.currentTimeMillis() - this.mLastStartBreak);
        }
        return breakDuration;
    }

    private long getBreak2Duration() {
        long break2Duration = this.mBreak2PreviousDuration;
        if (this.mBreak2) {
            return break2Duration + (System.currentTimeMillis() - this.mLastStartBreak2);
        }
        return break2Duration;
    }

    private long getWorkDuration() {
        if (!this.mStarted || this.mFinished) {
            return (this.mEndDate - this.mStartDate) - this.mBreak2PreviousDuration;
        }
        if (this.mBreak2) {
            return (this.mLastStartBreak2 - this.mStartDate) - this.mBreak2PreviousDuration;
        }
        return (System.currentTimeMillis() - this.mStartDate) - this.mBreak2PreviousDuration;
    }

    protected void jobTimeClicked() {
        if (this.mStarted) {
            stopJob();
        } else {
            startJob();
        }
    }

    private void stopJob() {
        if (this.mBreak) {
            stopBreak();
        }
        if (this.mBreak2) {
            stopBreak2();
        }
        ContentValues values = getContentValues();
        this.mStartDate = round(this.mStartDate);
        this.mBreakPreviousDuration = round(this.mBreakPreviousDuration);
        this.mBreak2PreviousDuration = round(this.mBreak2PreviousDuration);
        values.put(Job.END_DATE, Long.valueOf(round(System.currentTimeMillis())));
        values.put(Job.START_DATE, Long.valueOf(this.mStartDate));
        values.put(Job.BREAK_DURATION, Long.valueOf(this.mBreakPreviousDuration));
        values.put(Job.BREAK2_DURATION, Long.valueOf(this.mBreak2PreviousDuration));
        updateDatabase(values);
        updateFromCursor();
    }

    private long round(long millis) {
        if (this.mBillingUnit > 0) {
            return ((this.mBillingUnitHalf + millis) / this.mBillingUnit) * this.mBillingUnit;
        }
        return millis;
    }

    private void continueJob() {
        if (this.mBreak) {
            stopBreak();
        }
        ContentValues values = getContentValues();
        values.put(Job.END_DATE, null);
        updateDatabase(values);
        updateFromCursor();
    }

    private void startJob() {
        ContentValues values = getContentValues();
        values.put(Job.START_DATE, Long.valueOf(System.currentTimeMillis()));
        values.put(Job.END_DATE, (Long) null);
        values.put(Job.LAST_START_BREAK, (Long) null);
        values.put(Job.BREAK_DURATION, Integer.valueOf(STATE_EDIT));
        values.put(Job.LAST_START_BREAK2, (Long) null);
        values.put(Job.BREAK2_DURATION, Integer.valueOf(STATE_EDIT));
        updateDatabase(values);
        updateFromCursor();
        showMoreIfRequired();
    }

    protected void onResume() {
        super.onResume();
        this.mUpdateCalendarEvent = false;
        DateTimeFormater.getFormatFromPreferences(this);
        if (this.mCursor != null) {
            updateFromCursor();
        } else {
            setTitle(getText(R.string.error_title));
            this.mText.setText(getText(R.string.error_message));
        }
        showMoreIfRequired();
        updateRecentNotesButton(false);
        if (!TextUtils.isEmpty(this.mPreselectedCustomer)) {
            Log.i(TAG, ">>> Autofillin preselected customer informaton for " + this.mPreselectedCustomer);
            autoFillInHourlyRate(this.mPreselectedCustomer);
            this.mCustomer.setText(this.mPreselectedCustomer);
            this.mPreselectedCustomer = null;
        }
    }

    private void updateDisplay(long delayMillis) {
        cancelUpdateDisplay();
        if (this.mStarted && !this.mFinished) {
            this.mHandler.postDelayed(this.mDisplayUpdater, delayMillis);
        }
    }

    private void cancelUpdateDisplay() {
        this.mHandler.removeCallbacks(this.mDisplayUpdater);
    }

    private void updateFromCursor() {
        this.mCursor.requery();
        this.mCursor.moveToFirst();
        updateFromCursorWithoutRequery();
    }

    private void updateFromCursorWithoutRequery() {
        boolean z;
        if (this.mState == 0) {
            setTitle(getText(R.string.title_edit));
        } else if (this.mState == STATE_INSERT) {
            setTitle(getText(R.string.title_create));
        }
        LicenseUtils.modifyTitle(this);
        String note = this.mCursor.getString(STATE_INSERT);
        this.mText.setTextKeepState(note);
        if (this.mOriginalContent == null) {
            this.mOriginalContent = note;
        }
        this.mStartDate = this.mCursor.getLong(DISCARD_ID);
        this.mEndDate = this.mCursor.getLong(DIALOG_ID_END_DATE);
        this.mLastStartBreak = this.mCursor.getLong(LIST_ID);
        this.mLastStartBreak2 = this.mCursor.getLong(DIALOG_ID_RATES);
        this.mBreakPreviousDuration = this.mCursor.getLong(RESTART_ID);
        this.mBreak2PreviousDuration = this.mCursor.getLong(COLUMN_INDEX_BREAK2_DURATION);
        this.mHourlyRate = this.mCursor.getLong(PAUSE_ID);
        String customer = this.mCursor.getString(DIALOG_ID_STOP_JOB);
        this.mPlannedDate = this.mCursor.getLong(END_ID);
        this.mPlannedDuration = this.mCursor.getLong(EVENT_ID);
        this.mExtraTotal = this.mCursor.getLong(DIALOG_ID_PLANNED_DURATION);
        this.mCustomer.setText(customer);
        this.mCalendarUri = this.mCursor.getString(SEND_ID);
        this.mCustomerRef = this.mCursor.getString(COLUMN_INDEX_CUSTOMER_REF);
        if (this.mStartDate > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mStarted = z;
        if (!this.mStarted || this.mEndDate <= 0) {
            z = false;
        } else {
            z = true;
        }
        this.mFinished = z;
        if (this.mLastStartBreak > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mBreak = z;
        if (this.mLastStartBreak2 > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mBreak2 = z;
        if (this.mStarted) {
            if (this.mFinished) {
                this.mJobButton.setVisibility(END_ID);
                this.mBreakButton.setVisibility(END_ID);
                this.mBreak2Button.setVisibility(END_ID);
            } else {
                this.mJobButton.setVisibility(STATE_EDIT);
                if (this.mBreak2) {
                    this.mBreakButton.setVisibility(END_ID);
                    this.mBreak2Button.setVisibility(STATE_EDIT);
                } else {
                    this.mBreakButton.setVisibility(STATE_EDIT);
                    this.mBreak2Button.setVisibility(STATE_EDIT);
                }
            }
            this.mCalendar.setTimeInMillis(this.mStartDate);
            String datetime = new StringBuilder(String.valueOf(DateTimeFormater.mDateFormater.format(this.mCalendar.getTime()))).append(" ").append(DateTimeFormater.mTimeFormater.format(this.mCalendar.getTime())).toString();
            TextView textView = this.mTimestamp;
            Object[] objArr = new Object[STATE_INSERT];
            objArr[STATE_EDIT] = datetime;
            textView.setText(getString(R.string.started_at, objArr));
            this.mJobButton.setText(R.string.job_done);
        } else {
            this.mJobButton.setVisibility(STATE_EDIT);
            this.mBreakButton.setVisibility(END_ID);
            this.mBreak2Button.setVisibility(END_ID);
            this.mTimestamp.setText(R.string.job_not_yet_started);
            this.mJobButton.setText(R.string.starting_job);
        }
        if (this.mBreak) {
            this.mBreakButton.setText(R.string.end_of_break);
        } else {
            this.mBreakButton.setText(R.string.having_a_break);
        }
        if (this.mBreak2) {
            this.mBreak2Button.setText(R.string.continue_now);
        } else {
            this.mBreak2Button.setText(R.string.continue_tomorrow);
        }
        this.mSetPlannedDuration.setText(DurationFormater.formatDuration(this, this.mPlannedDuration, DISCARD_ID));
        updateInfo(DISCARD_ID);
        updateDateTimeButtons();
        updateDisplay(0);
    }

    private void updateInfo(int type) {
        TextView textView;
        Object[] objArr;
        if (type == DISCARD_ID || type == DIALOG_ID_END_DATE) {
            String rateString = this.mDecimalFormat.format(((double) this.mHourlyRate) * Timesheet.RATE_FACTOR);
            textView = this.mHourlyRateInfo;
            objArr = new Object[STATE_INSERT];
            objArr[STATE_EDIT] = rateString;
            textView.setText(getString(R.string.hourly_rate_info, objArr));
            if (type != DIALOG_ID_END_DATE) {
                this.mSetHourlyRate.setText(rateString);
            }
        }
        int breaktype = DISCARD_ID;
        if (this.mStarted && this.mBreak && !this.mBreak2) {
            breaktype = STATE_INSERT;
        }
        long breakDuration = getBreakDuration();
        String breakString = DurationFormater.formatDuration(this, breakDuration, breaktype);
        textView = this.mBreakInfo;
        objArr = new Object[STATE_INSERT];
        objArr[STATE_EDIT] = breakString;
        textView.setText(getString(R.string.break_info, objArr));
        this.mSetBreak.setText(breakString);
        int worktype = DISCARD_ID;
        if (!(!this.mStarted || this.mFinished || this.mBreak2)) {
            worktype = STATE_INSERT;
        }
        long workDuration = getWorkDuration();
        textView = this.mDurationInfo;
        objArr = new Object[STATE_INSERT];
        objArr[STATE_EDIT] = DurationFormater.formatDuration(this, workDuration, worktype);
        textView.setText(getString(R.string.duration_info, objArr));
        double extrasTotal = ((double) this.mExtraTotal) * Timesheet.RATE_FACTOR;
        double total = ((((double) (workDuration - breakDuration)) * Timesheet.HOUR_FACTOR) * (((double) this.mHourlyRate) * Timesheet.RATE_FACTOR)) + extrasTotal;
        textView = this.mTotalInfo;
        objArr = new Object[STATE_INSERT];
        objArr[STATE_EDIT] = this.mDecimalFormat.format(total);
        textView.setText(getString(R.string.total_info, objArr));
        textView = this.mExtrasInfo;
        objArr = new Object[STATE_INSERT];
        objArr[STATE_EDIT] = this.mDecimalFormat.format(extrasTotal);
        textView.setText(getString(R.string.extras_info, objArr));
        if (this.mExtraTotal == 0) {
            this.mExtrasInfo.setVisibility(END_ID);
        } else {
            this.mExtrasInfo.setVisibility(STATE_EDIT);
        }
        if (this.mPlannedDuration <= 0 || workDuration - breakDuration <= this.mPlannedDuration) {
            this.mDurationInfo.setTextColor(getResources().getColor(17170437));
        } else {
            this.mDurationInfo.setTextColor(-65536);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, this.mOriginalContent);
        outState.putInt(ORIGINAL_STATE, this.mState);
        outState.putString(ORIGINAL_URI, this.mUri.toString());
        outState.putBoolean(SHOW_MORE, this.mShowingMore);
        outState.putBoolean(SHOW_RECENT_NOTES_BUTTON, this.mShowRecentNotesButton);
    }

    protected void onPause() {
        super.onPause();
        updateDatabase();
        if (this.mUpdateCalendarEvent && !TextUtils.isEmpty(this.mCalendarUri)) {
            setOrUpdateCalendarEvent(this.mCalendarUri, this.mPlannedDate, this.mPlannedDuration, this.mCalendarAuthority);
        }
        cancelUpdateDisplay();
    }

    private void updateDatabase() {
        updateDatabase(getContentValues());
    }

    private ContentValues getContentValues() {
        String text = this.mText.getText().toString();
        int length = text.length();
        ContentValues values = new ContentValues();
        values.put(Job.MODIFIED_DATE, Long.valueOf(System.currentTimeMillis()));
        String title = text.substring(STATE_EDIT, Math.min(30, length));
        int firstNewline = title.indexOf(SEND_ID);
        if (firstNewline > 0) {
            title = title.substring(STATE_EDIT, firstNewline);
        } else if (length > 30) {
            int lastSpace = title.lastIndexOf(32);
            if (lastSpace > 0) {
                title = title.substring(STATE_EDIT, lastSpace);
            }
        }
        if (title.length() > 0) {
            values.put(Job.TITLE, title);
        }
        values.put(TimesheetIntent.EXTRA_NOTE, text);
        values.put(TimesheetIntent.EXTRA_CUSTOMER, this.mCustomer.getText().toString());
        values.put(Job.HOURLY_RATE, Long.valueOf(getHourlyRateFromText()));
        return values;
    }

    protected void updateDatabase(ContentValues values) {
        if (this.mCursor != null) {
            getContentResolver().update(this.mUri, values, null, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(STATE_INSERT, RESTART_ID, STATE_EDIT, R.string.menu_restart).setShortcut('1', 's').setIcon(R.drawable.ic_menu_play);
        menu.add(STATE_INSERT, PAUSE_ID, STATE_EDIT, R.string.menu_pause).setShortcut('2', 'p').setIcon(R.drawable.ic_menu_pause);
        menu.add(STATE_INSERT, DIALOG_ID_STOP_JOB, STATE_EDIT, R.string.menu_continue).setShortcut('3', 'c').setIcon(R.drawable.ic_menu_continue);
        menu.add(STATE_INSERT, END_ID, STATE_EDIT, R.string.menu_end).setShortcut('4', 'e').setIcon(R.drawable.ic_menu_stop);
        menu.add(STATE_EDIT, STATE_INSERT, STATE_EDIT, R.string.menu_revert).setShortcut('0', 'r').setIcon(android.R.drawable.ic_menu_revert);
        menu.add(STATE_INSERT, DIALOG_ID_END_DATE, STATE_EDIT, R.string.menu_delete).setShortcut('1', 'd').setIcon(android.R.drawable.ic_menu_delete);
        menu.add(STATE_INSERT, EVENT_ID, STATE_EDIT, R.string.menu_calendar).setShortcut('5', 'c').setIcon(android.R.drawable.ic_menu_my_calendar);
        menu.add(STATE_INSERT, DIALOG_ID_PLANNED_DURATION, STATE_EDIT, R.string.menu_extra_items).setShortcut('7', 'x').setIcon(android.R.drawable.ic_menu_add);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory("android.intent.category.ALTERNATIVE");
        new MenuIntentOptionsWithIcons(this, menu).addIntentOptions(262144, STATE_EDIT, STATE_EDIT, new ComponentName(this, JobActivity.class), null, intent, STATE_EDIT, null);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean contentChanged;
        boolean z;
        boolean z2 = false;
        if (this.mOriginalContent.equals(this.mText.getText().toString())) {
            contentChanged = false;
        } else {
            contentChanged = true;
        }
        menu.setGroupVisible(STATE_EDIT, contentChanged);
        if (this.mStarted) {
            menu.findItem(RESTART_ID).setTitle(R.string.menu_restart);
        } else {
            menu.findItem(RESTART_ID).setTitle(R.string.menu_start);
        }
        MenuItem findItem = menu.findItem(PAUSE_ID);
        if (!this.mStarted || this.mBreak || this.mFinished) {
            z = false;
        } else {
            z = true;
        }
        findItem.setVisible(z);
        findItem = menu.findItem(DIALOG_ID_STOP_JOB);
        if (this.mFinished || this.mBreak) {
            z = true;
        } else {
            z = false;
        }
        findItem.setVisible(z);
        menu.findItem(END_ID).setVisible(this.mStarted);
        findItem = menu.findItem(EVENT_ID);
        if (this.mCursor.getString(SEND_ID) != null) {
            z = true;
        } else {
            z = false;
        }
        findItem.setVisible(z);
        MenuItem sendMenu = menu.findItem(SEND_ID);
        if (sendMenu != null) {
            if (!this.mStarted) {
                z2 = true;
            }
            sendMenu.setVisible(z2);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case STATE_INSERT /*1*/:
                cancelJob();
                break;
            case DISCARD_ID /*2*/:
                cancelJob();
                break;
            case DIALOG_ID_END_DATE /*3*/:
                deleteJob();
                finish();
                break;
            case LIST_ID /*4*/:
                startActivity(new Intent(this, JobList.class));
                break;
            case RESTART_ID /*5*/:
                startJob();
                break;
            case PAUSE_ID /*6*/:
                startBreak();
                break;
            case DIALOG_ID_STOP_JOB /*7*/:
                continueJob();
                break;
            case END_ID /*8*/:
                stopJob();
                break;
            case EVENT_ID /*9*/:
                Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse(this.mCursor.getString(SEND_ID)));
                intent2.putExtra(CalendarApp.EVENT_BEGIN_TIME, this.mPlannedDate);
                intent2.putExtra(CalendarApp.EVENT_END_TIME, this.mPlannedDate + this.mPlannedDuration);
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
            case DIALOG_ID_PLANNED_DURATION /*11*/:
                Intent intent = new Intent(this, InvoiceItemActivity.class);
                intent.putExtra("jobid", Long.parseLong(this.mUri.getLastPathSegment()));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v == this.mCustomer) {
            getMenuInflater().inflate(R.menu.context_menu_customer, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.pick_contact) {
            intent = new Intent("android.intent.action.PICK");
            intent.setData(People.CONTENT_URI);
            startActivityForResult(intent, STATE_INSERT);
            return true;
        } else if (item.getItemId() != R.id.call_contact) {
            return super.onContextItemSelected(item);
        } else {
            Log.v(TAG, "customer ref:" + this.mCustomerRef);
            if (this.mCustomerRef == null || !this.mCustomerRef.startsWith("content://contacts")) {
                intent = new Intent("android.intent.action.PICK");
                intent.setData(People.CONTENT_URI);
                intent.putExtra("call", true);
                startActivityForResult(intent, STATE_INSERT);
                return true;
            }
            intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(this.mCustomerRef));
            startActivity(intent);
            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == STATE_INSERT && resultCode == -1) {
            ContentValues values = new ContentValues(STATE_INSERT);
            Cursor c = managedQuery(data.getData(), null, null, null, null);
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                values.put(TimesheetIntent.EXTRA_CUSTOMER, name);
                this.mCustomer.setText(name);
                values.put(Job.CUSTOMER_REF, data.getDataString());
                updateDatabase(values);
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        int year = this.mCalendar.get(Calendar.YEAR);
        int month = this.mCalendar.get(Calendar.MONTH);
        int day = this.mCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = this.mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = this.mCalendar.get(Calendar.MINUTE);
        switch (id) {
            case STATE_INSERT /*1*/:
                return new DatePickerDialog(this, this.mStartDateSetListener, year, month, day);
            case DISCARD_ID /*2*/:
                return new TimePickerDialog(this, this.mStartTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case DIALOG_ID_END_DATE /*3*/:
                return new DatePickerDialog(this, this.mEndDateSetListener, year, month, day);
            case LIST_ID /*4*/:
                return new TimePickerDialog(this, this.mEndTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case RESTART_ID /*5*/:
                return new DurationPickerDialog(this, this.mBreakSetListener, STATE_EDIT, STATE_EDIT);
            case PAUSE_ID /*6*/:
                return new Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setMessage(R.string.dialog_restart_job).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        JobActivity.this.startJob();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
            case DIALOG_ID_STOP_JOB /*7*/:
                return new Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setMessage(R.string.dialog_stop_job).setPositiveButton(R.string.end_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        JobActivity.this.stopJob();
                    }
                }).setNeutralButton(R.string.continue_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        JobActivity.this.continueJob();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
            case END_ID /*8*/:
                if (this.mRecentNoteList == null) {
                    Log.d(TAG, "getTitlesList");
                    this.mRecentNoteList = getTitlesList(this);
                }
                Log.i(TAG, "Show recent notes create");
                return new Builder(this).setTitle(R.string.recent_notes).setItems(this.mRecentNoteList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        JobActivity.this.mText.setText(JobActivity.getNote(JobActivity.this, JobActivity.this.mRecentNoteList[which]));
                    }
                }).create();
            case EVENT_ID /*9*/:
                return new DatePickerDialog(this, this.mPlannedDateSetListener, year, month, day);
            case SEND_ID /*10*/:
                return new TimePickerDialog(this, this.mPlannedTimeSetListener, hour, minute, DateTimeFormater.mUse24hour);
            case DIALOG_ID_PLANNED_DURATION /*11*/:
                return new DurationPickerDialog(this, this.mPlannedMinutesSetListener, STATE_EDIT, STATE_EDIT);
            case DIALOG_ID_RATES /*12*/:
                Log.i(TAG, "DIALOG_ID_RATES create");
                return new DurationPickerDialog(this, null, STATE_EDIT, STATE_EDIT);
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        long startDate = this.mCursor.getLong(DISCARD_ID);
        long endDate = this.mCursor.getLong(DIALOG_ID_END_DATE);
        long plannedDate = this.mCursor.getLong(END_ID);
        int minutes;
        switch (id) {
            case STATE_INSERT /*1*/:
                this.mCalendar.setTimeInMillis(startDate);
                ((DatePickerDialog) dialog).updateDate(this.mCalendar.get(STATE_INSERT), this.mCalendar.get(DISCARD_ID), this.mCalendar.get(RESTART_ID));
            case DISCARD_ID /*2*/:
                this.mCalendar.setTimeInMillis(startDate);
                ((TimePickerDialog) dialog).updateTime(this.mCalendar.get(DIALOG_ID_PLANNED_DURATION), this.mCalendar.get(DIALOG_ID_RATES));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) this.mBillingUnitMinutes);
            case DIALOG_ID_END_DATE /*3*/:
                this.mCalendar.setTimeInMillis(endDate);
                ((DatePickerDialog) dialog).updateDate(this.mCalendar.get(STATE_INSERT), this.mCalendar.get(DISCARD_ID), this.mCalendar.get(RESTART_ID));
            case LIST_ID /*4*/:
                this.mCalendar.setTimeInMillis(endDate);
                ((TimePickerDialog) dialog).updateTime(this.mCalendar.get(DIALOG_ID_PLANNED_DURATION), this.mCalendar.get(DIALOG_ID_RATES));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) this.mBillingUnitMinutes);
            case RESTART_ID /*5*/:
                minutes = (int) (this.mCursor.getLong(RESTART_ID) / 60000);
                ((DurationPickerDialog) dialog).updateTime(minutes / 60, minutes % 60);
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) this.mBillingUnitMinutes);
            case END_ID /*8*/:
                Log.i(TAG, "Show recent notes prepare");
            case EVENT_ID /*9*/:
                if (plannedDate == 0) {
                    plannedDate = System.currentTimeMillis();
                }
                this.mCalendar.setTimeInMillis(plannedDate);
                ((DatePickerDialog) dialog).updateDate(this.mCalendar.get(STATE_INSERT), this.mCalendar.get(DISCARD_ID), this.mCalendar.get(RESTART_ID));
            case SEND_ID /*10*/:
                if (plannedDate == 0) {
                    plannedDate = System.currentTimeMillis();
                }
                this.mCalendar.setTimeInMillis(plannedDate);
                ((TimePickerDialog) dialog).updateTime(this.mCalendar.get(DIALOG_ID_PLANNED_DURATION), this.mCalendar.get(DIALOG_ID_RATES));
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) this.mBillingUnitMinutes);
            case DIALOG_ID_PLANNED_DURATION /*11*/:
                minutes = (int) (this.mCursor.getLong(EVENT_ID) / 60000);
                ((DurationPickerDialog) dialog).updateTime(minutes / 60, minutes % 60);
                ((NumberPicker) dialog.findViewById(R.id.minute)).setStep((int) this.mBillingUnitMinutes);
            case DIALOG_ID_RATES /*12*/:
                float currentRate = this.mCursor.getFloat(PAUSE_ID) / 100.0f;
                Log.d(TAG, "hrate>" + currentRate);
                String[] parts = Float.toString(currentRate).split("\\.");
                Log.d(TAG, "pre>" + parts[STATE_EDIT] + "< post>" + parts[STATE_INSERT]);
                ((DurationPickerDialog) dialog).updateTime(Integer.parseInt(parts[STATE_EDIT]), Integer.parseInt(parts[STATE_INSERT]));
            default:
        }
    }

    private void updateDateTimeButtons() {
        long startDate = this.mCursor.getLong(DISCARD_ID);
        long endDate = this.mCursor.getLong(DIALOG_ID_END_DATE);
        long plannedDate = this.mCursor.getLong(END_ID);
        if (startDate != 0) {
            this.mSetStartDate.setEnabled(true);
            this.mSetStartTime.setEnabled(true);
            this.mSetStartDate.setText(DateTimeFormater.mDateFormater.format(Long.valueOf(startDate)));
            this.mSetStartTime.setText(DateTimeFormater.mTimeFormater.format(Long.valueOf(startDate)));
        } else {
            this.mSetStartDate.setEnabled(false);
            this.mSetStartTime.setEnabled(false);
            this.mSetStartDate.setText(R.string.unknown);
            this.mSetStartTime.setText(R.string.unknown);
        }
        if (endDate != 0) {
            this.mSetEndDate.setEnabled(true);
            this.mSetEndTime.setEnabled(true);
            this.mSetEndDate.setText(DateTimeFormater.mDateFormater.format(Long.valueOf(endDate)));
            this.mSetEndTime.setText(DateTimeFormater.mTimeFormater.format(Long.valueOf(endDate)));
        } else {
            this.mSetEndDate.setEnabled(false);
            this.mSetEndTime.setEnabled(false);
            this.mSetEndDate.setText(R.string.unknown);
            this.mSetEndTime.setText(R.string.unknown);
        }
        if (plannedDate != 0) {
            this.mSetPlannedDate.setText(DateTimeFormater.mDateFormater.format(Long.valueOf(plannedDate)));
            this.mSetPlannedTime.setText(DateTimeFormater.mTimeFormater.format(Long.valueOf(plannedDate)));
        } else {
            this.mSetPlannedDate.setText(R.string.unknown);
            this.mSetPlannedTime.setText(R.string.unknown);
        }
        if (this.mStarted && !this.mFinished && this.mBreak) {
            this.mSetBreak.setEnabled(false);
        } else {
            this.mSetBreak.setEnabled(true);
        }
    }

    protected String setOrUpdateCalendarEvent(String uri, long millis, long duration, int calendarAuthority) {
        this.mCalendarUri = CalendarApp.setOrUpdateCalendarEvent(this, uri, millis, duration, this.mCustomer.getText().toString(), this.mText.getText().toString(), calendarAuthority);
        this.mUpdateCalendarEvent = false;
        return uri;
    }

    protected Intent createSendIntent() {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse("content://org.openintents.timesheet/persons"));
        intent.setClass(this, InsertJobActivity.class);
        intent.putExtra(TimesheetIntent.EXTRA_CUSTOMER, this.mCustomer.getText().toString());
        intent.putExtra(TimesheetIntent.EXTRA_HOURLY_RATE, this.mHourlyRate);
        intent.putExtra(TimesheetIntent.EXTRA_NOTE, this.mText.getText().toString());
        intent.putExtra(TimesheetIntent.EXTRA_PLANNED_DATE, this.mCursor.getLong(END_ID));
        intent.putExtra(TimesheetIntent.EXTRA_PLANNED_DURATION, this.mCursor.getLong(EVENT_ID));
        return intent;
    }

    private final void cancelJob() {
        if (this.mCursor != null) {
            String tmp = this.mText.getText().toString();
            this.mText.setText(this.mOriginalContent);
            this.mOriginalContent = tmp;
        }
    }

    private final void deleteJob() {
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
            getContentResolver().delete(this.mUri, null, null);
            this.mText.setText("");
        }
    }
}
