package org.openintents.timesheet.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.openintents.distribution.LicenseUtils;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.animation.FadeAnimation;
import org.openintents.util.DateTimeFormater;
import org.openintents.util.MenuIntentOptionsWithIcons;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class JobActivityExpense extends Activity {
    static final int DIALOG_ID_RECENT_NOTES = 1;
    static final String TAG = "JobActivityExpense";
    private static final int ADD_EXTRA_ITEM_ID = 11;
    private static final int COLUMN_INDEX_CUSTOMER = 4;
    private static final int COLUMN_INDEX_HOURLY_RATE = 3;
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final int COLUMN_INDEX_START = 2;
    private static final int DELETE_ID = 3;
    private static final int DISCARD_ID = 2;
    private static final int LIST_ID = 4;
    private static final String ORIGINAL_CONTENT = "origNote";
    private static final String ORIGINAL_STATE = "origState";
    private static final String[] PROJECTION;
    private static final int REVERT_ID = 1;
    private static final String SHOW_RECENT_NOTES_BUTTON = "show_recent_notes";
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    static {
        PROJECTION = new String[]{Reminders._ID, TimesheetIntent.EXTRA_NOTE, Job.START_DATE, Job.HOURLY_RATE, TimesheetIntent.EXTRA_CUSTOMER};
    }

    NumberFormat mDecimalFormat;
    String[] mRecentNoteList;
    private Cursor mCursor;
    private AutoCompleteTextView mCustomer;
    private String[] mCustomerList;
    private long mHourlyRate;
    private String mOriginalContent;
    private String mPreselectedCustomer;
    private Button mRecentNotes;
    private int mRecentNotesButtonState;
    private EditText mSetValue;
    private boolean mShowRecentNotesButton;
    private int mState;
    private EditText mText;
    private Uri mUri;

    public JobActivityExpense() {
        this.mDecimalFormat = new DecimalFormat("0.00");
        this.mShowRecentNotesButton = false;
        this.mRecentNoteList = null;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mRecentNoteList = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String action = intent.getAction();
        if ("android.intent.action.EDIT".equals(action) || "android.intent.action.VIEW".equals(action)) {
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
            if (TextUtils.isEmpty(this.mPreselectedCustomer)) {
                this.mPreselectedCustomer = getLastCustomer(this);
            }
            this.mUri = getContentResolver().insert(intent.getData(), values);
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
        setContentView(R.layout.job_expense);
        this.mText = (EditText) findViewById(R.id.note);
        this.mText.addTextChangedListener(new C00221());
        this.mCustomer = (AutoCompleteTextView) findViewById(R.id.customer);
        this.mRecentNotes = (Button) findViewById(R.id.recent_notes);
        this.mRecentNotes.setOnClickListener(new C00232());
        this.mSetValue = (EditText) findViewById(R.id.set_hourly_rate);
        this.mCursor = managedQuery(this.mUri, PROJECTION, null, null, null);
        this.mCustomerList = getCustomerList(this);
        this.mCustomer.setAdapter(new ArrayAdapter(this, 17367050, this.mCustomerList));
        this.mCustomer.setThreshold(STATE_EDIT);
        this.mCustomer.setOnClickListener(new C00243());
        if (this.mCustomerList.length < STATE_INSERT) {
            this.mCustomer.setHint(R.string.customer_hint_first_time);
        }
        this.mCustomer.setOnItemClickListener(new C00254());
        if (savedInstanceState != null) {
            this.mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            this.mState = savedInstanceState.getInt(ORIGINAL_STATE);
            this.mShowRecentNotesButton = savedInstanceState.getBoolean(SHOW_RECENT_NOTES_BUTTON);
        }
        this.mCursor.moveToFirst();
    }

    private void showRecentNotesDialog() {
        Log.d(TAG, "showRecentNOtes");
        if (this.mRecentNoteList == null) {
            Log.d(TAG, "getTitlesList");
            this.mRecentNoteList = getTitlesList(this);
        }
        if (this.mRecentNoteList.length > 0) {
            Log.d(TAG, "show Dialog + " + this.mRecentNoteList.length);
            showDialog(STATE_INSERT);
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
            } else if (this.mRecentNotesButtonState == 8) {
                Log.i(TAG, "Fade in");
                FadeAnimation.fadeIn(this, this.mRecentNotes);
                this.mRecentNotesButtonState = STATE_EDIT;
            }
        } else if (!animate) {
            this.mRecentNotes.setVisibility(8);
            this.mRecentNotesButtonState = 8;
        } else if (this.mRecentNotesButtonState == 0) {
            Log.i(TAG, "Fade out");
            FadeAnimation.fadeOut(this, this.mRecentNotes);
            this.mRecentNotesButtonState = 8;
        }
    }

    protected void onResume() {
        super.onResume();
        DateTimeFormater.getFormatFromPreferences(this);
        if (this.mCursor != null) {
            updateFromCursor();
        } else {
            setTitle(getText(R.string.error_title));
            this.mText.setText(getText(R.string.error_message));
        }
        updateRecentNotesButton(false);
        if (!TextUtils.isEmpty(this.mPreselectedCustomer)) {
            Log.i(TAG, ">>> Autofillin preselected customer informaton for " + this.mPreselectedCustomer);
            this.mCustomer.setText(this.mPreselectedCustomer);
            this.mPreselectedCustomer = null;
        }
    }

    private void updateFromCursor() {
        this.mCursor.requery();
        this.mCursor.moveToFirst();
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
        this.mHourlyRate = this.mCursor.getLong(DELETE_ID);
        this.mCustomer.setText(this.mCursor.getString(LIST_ID));
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, this.mOriginalContent);
        outState.putInt(ORIGINAL_STATE, this.mState);
        outState.putBoolean(SHOW_RECENT_NOTES_BUTTON, this.mShowRecentNotesButton);
    }

    protected void onPause() {
        super.onPause();
        updateDatabase();
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
        int firstNewline = title.indexOf(10);
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
        long hourlyrate = 0;
        try {
            hourlyrate = (long) (100.0f * Float.parseFloat(this.mSetValue.getText().toString()));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing hourly rate: " + this.mSetValue.getText().toString());
        }
        values.put(Job.HOURLY_RATE, Long.valueOf(hourlyrate));
        values.put(Job.TYPE, Job.TYPE_EXPENSE);
        return values;
    }

    protected void updateDatabase(ContentValues values) {
        if (this.mCursor != null) {
            getContentResolver().update(this.mUri, values, null, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(STATE_EDIT, STATE_INSERT, STATE_EDIT, R.string.menu_revert).setShortcut('0', 'r').setIcon(17301580);
        menu.add(STATE_INSERT, DELETE_ID, STATE_EDIT, R.string.menu_delete).setShortcut('1', 'd').setIcon(17301564);
        menu.add(STATE_INSERT, ADD_EXTRA_ITEM_ID, STATE_EDIT, R.string.menu_extra_items).setShortcut('7', 'x').setIcon(17301555);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory("android.intent.category.ALTERNATIVE");
        new MenuIntentOptionsWithIcons(this, menu).addIntentOptions(262144, STATE_EDIT, STATE_EDIT, new ComponentName(this, JobActivityExpense.class), null, intent, STATE_EDIT, null);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(STATE_EDIT, !this.mOriginalContent.equals(this.mText.getText().toString()));
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
            case DELETE_ID /*3*/:
                deleteJob();
                finish();
                break;
            case LIST_ID /*4*/:
                startActivity(new Intent(this, JobList.class));
                break;
            case ADD_EXTRA_ITEM_ID /*11*/:
                Intent intent = new Intent(this, InvoiceItemActivity.class);
                intent.putExtra("jobid", Long.parseLong(this.mUri.getLastPathSegment()));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case STATE_INSERT /*1*/:
                if (this.mRecentNoteList == null) {
                    Log.d(TAG, "getTitlesList");
                    this.mRecentNoteList = getTitlesList(this);
                }
                Log.i(TAG, "Show recent notes create");
                return new Builder(this).setTitle(R.string.recent_notes).setItems(this.mRecentNoteList, new C00265()).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case STATE_INSERT /*1*/:
                Log.i(TAG, "Show recent notes prepare");
            default:
        }
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

    /* renamed from: org.openintents.timesheet.activity.JobActivityExpense.1 */
    class C00221 implements TextWatcher {
        C00221() {
        }

        public void afterTextChanged(Editable s) {
            JobActivityExpense.this.mShowRecentNotesButton = false;
            JobActivityExpense.this.updateRecentNotesButton(true);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivityExpense.2 */
    class C00232 implements OnClickListener {
        C00232() {
        }

        public void onClick(View v) {
            JobActivityExpense.this.showRecentNotesDialog();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivityExpense.3 */
    class C00243 implements OnClickListener {
        C00243() {
        }

        public void onClick(View v) {
            if (JobActivityExpense.this.mCustomer.isPopupShowing()) {
                JobActivityExpense.this.mCustomer.dismissDropDown();
            } else {
                JobActivityExpense.this.mCustomer.showDropDown();
            }
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivityExpense.4 */
    class C00254 implements OnItemClickListener {
        C00254() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobActivityExpense.5 */
    class C00265 implements DialogInterface.OnClickListener {
        C00265() {
        }

        public void onClick(DialogInterface dialog, int which) {
            JobActivityExpense.this.mText.setText(JobActivityExpense.getNote(JobActivityExpense.this, JobActivityExpense.this.mRecentNoteList[which]));
        }
    }
}
