package org.openintents.timesheet.activity;

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

import androidx.appcompat.app.AppCompatActivity;

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

public class JobActivityExpense extends AppCompatActivity {
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
        mDecimalFormat = new DecimalFormat("0.00");
        mShowRecentNotesButton = false;
        mRecentNoteList = null;
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
            String customer = c.getString(0);
            if (!TextUtils.isEmpty(customer)) {
                set.add(customer);
            }
        }
        c.close();
        return set.toArray(new String[0]);
    }

    public static String[] getTitlesList(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Job.CONTENT_URI;
        String[] strArr = new String[1];
        strArr[0] = Job.TITLE;
        Cursor c = contentResolver.query(uri, strArr, null, null, "modified DESC");
        Vector<String> vec = new Vector();
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
        mRecentNoteList = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String action = intent.getAction();
        if ("android.intent.action.EDIT".equals(action) || "android.intent.action.VIEW".equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if ("android.intent.action.INSERT".equals(action) || "android.intent.action.MAIN".equals(action) || action == null) {
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
                    values.put(TimesheetIntent.EXTRA_CUSTOMER, mPreselectedCustomer);
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
            if (TextUtils.isEmpty(mPreselectedCustomer)) {
                mPreselectedCustomer = getLastCustomer(this);
            }
            mUri = getContentResolver().insert(intent.getData(), values);
            intent.setAction("android.intent.action.EDIT");
            intent.setData(mUri);
            setIntent(intent);
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new job into " + getIntent().getData());
                setResult(STATE_EDIT);
                finish();
                return;
            }
            setResult(-1, new Intent().setAction(mUri.toString()));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
        setContentView(R.layout.job_expense);
        mText = (EditText) findViewById(R.id.note);
        mText.addTextChangedListener(new C00221());
        mCustomer = (AutoCompleteTextView) findViewById(R.id.customer);
        mRecentNotes = (Button) findViewById(R.id.recent_notes);
        mRecentNotes.setOnClickListener(new C00232());
        mSetValue = (EditText) findViewById(R.id.set_hourly_rate);
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
        mCustomerList = getCustomerList(this);
        mCustomer.setAdapter(new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mCustomerList));
        mCustomer.setThreshold(STATE_EDIT);
        mCustomer.setOnClickListener(new C00243());
        if (mCustomerList.length < STATE_INSERT) {
            mCustomer.setHint(R.string.customer_hint_first_time);
        }
        mCustomer.setOnItemClickListener(new C00254());
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            mState = savedInstanceState.getInt(ORIGINAL_STATE);
            mShowRecentNotesButton = savedInstanceState.getBoolean(SHOW_RECENT_NOTES_BUTTON);
        }
        mCursor.moveToFirst();
    }

    private void showRecentNotesDialog() {
        Log.d(TAG, "showRecentNOtes");
        if (mRecentNoteList == null) {
            Log.d(TAG, "getTitlesList");
            mRecentNoteList = getTitlesList(this);
        }
        if (mRecentNoteList.length > 0) {
            Log.d(TAG, "show Dialog + " + mRecentNoteList.length);
            showDialog(STATE_INSERT);
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

    protected void onResume() {
        super.onResume();
        DateTimeFormater.getFormatFromPreferences(this);
        if (mCursor != null) {
            updateFromCursor();
        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
        updateRecentNotesButton(false);
        if (!TextUtils.isEmpty(mPreselectedCustomer)) {
            Log.i(TAG, ">>> Autofillin preselected customer informaton for " + mPreselectedCustomer);
            mCustomer.setText(mPreselectedCustomer);
            mPreselectedCustomer = null;
        }
    }

    private void updateFromCursor() {
        mCursor.requery();
        mCursor.moveToFirst();
        if (mState == 0) {
            setTitle(getText(R.string.title_edit));
        } else if (mState == STATE_INSERT) {
            setTitle(getText(R.string.title_create));
        }
        String note = mCursor.getString(STATE_INSERT);
        mText.setTextKeepState(note);
        if (mOriginalContent == null) {
            mOriginalContent = note;
        }
        mHourlyRate = mCursor.getLong(DELETE_ID);
        mCustomer.setText(mCursor.getString(LIST_ID));
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
        outState.putInt(ORIGINAL_STATE, mState);
        outState.putBoolean(SHOW_RECENT_NOTES_BUTTON, mShowRecentNotesButton);
    }

    protected void onPause() {
        super.onPause();
        updateDatabase();
    }

    private void updateDatabase() {
        updateDatabase(getContentValues());
    }

    private ContentValues getContentValues() {
        String text = mText.getText().toString();
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
        values.put(TimesheetIntent.EXTRA_CUSTOMER, mCustomer.getText().toString());
        long hourlyrate = 0;
        try {
            hourlyrate = (long) (100.0f * Float.parseFloat(mSetValue.getText().toString()));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing hourly rate: " + mSetValue.getText().toString());
        }
        values.put(Job.HOURLY_RATE, Long.valueOf(hourlyrate));
        values.put(Job.TYPE, Job.TYPE_EXPENSE);
        return values;
    }

    protected void updateDatabase(ContentValues values) {
        if (mCursor != null) {
            getContentResolver().update(mUri, values, null, null);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, REVERT_ID, 0, R.string.menu_revert).setShortcut('0', 'r').setIcon(android.R.drawable.ic_menu_revert);
        menu.add(1, DELETE_ID, 0, R.string.menu_delete).setShortcut('1', 'd').setIcon(android.R.drawable.ic_menu_delete);
        menu.add(1, ADD_EXTRA_ITEM_ID, 0, R.string.menu_extra_items).setShortcut('7', 'x').setIcon(android.R.drawable.ic_menu_add);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

        // Workaround to add icons:
        MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
                menu);
        menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, JobActivityExpense.class), null,
                intent, 0, null);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Show "revert" menu item only if content has changed.
        boolean contentChanged = !mOriginalContent.equals(mText.getText()
                .toString());
        menu.setGroupVisible(0, contentChanged);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case DELETE_ID:
                deleteJob();
                finish();
                break;
            case DISCARD_ID:
                cancelJob();
                break;
            case REVERT_ID:
                cancelJob();
                break;
            case LIST_ID:
                Intent intent = new Intent(this, JobListActivity.class);
                startActivity(intent);
                break;
            case ADD_EXTRA_ITEM_ID:
                intent = new Intent(this, InvoiceItemActivity.class);
                long jobId = Long.parseLong(mUri.getLastPathSegment());
                intent.putExtra("jobid", jobId);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_RECENT_NOTES /*1*/:
                if (mRecentNoteList == null) {
                    Log.d(TAG, "getTitlesList");
                    mRecentNoteList = getTitlesList(this);
                }
                Log.i(TAG, "Show recent notes create");
                return new Builder(this).setTitle(R.string.recent_notes)
                        .setItems(mRecentNoteList,
                                new C00265()).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {

            case DIALOG_ID_RECENT_NOTES:
                Log.i(TAG, "Show recent notes prepare");
                break;
            default:
        }
    }

    private void cancelJob() {
        if (mCursor != null) {
            String tmp = mText.getText().toString();
            mText.setText(mOriginalContent);
            mOriginalContent = tmp;
        }
    }

    private void deleteJob() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
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
