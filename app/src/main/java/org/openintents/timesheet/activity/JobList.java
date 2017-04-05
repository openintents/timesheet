package org.openintents.timesheet.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.LicenseActivity;
import org.openintents.distribution.LicenseUtils;
import org.openintents.distribution.UpdateMenu;
import org.openintents.timesheet.Application;
import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.TimesheetProvider;
import org.openintents.timesheet.convert.ConvertCsvActivity;
import org.openintents.util.DateTimeFormater;
import org.openintents.util.DurationFormater;
import org.openintents.util.MenuIntentOptionsWithIcons;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class JobList extends ListActivity {
    static final int COLUMN_INDEX_BREAK2_DURATION = 13;
    static final int COLUMN_INDEX_BREAK_DURATION = 5;
    static final int COLUMN_INDEX_CUSTOMER = 6;
    static final int COLUMN_INDEX_CUSTOMER_REF = 16;
    static final int COLUMN_INDEX_END = 3;
    static final int COLUMN_INDEX_EXTERNAL_REF = 14;
    static final int COLUMN_INDEX_EXTRAS_TOTAL = 8;
    static final int COLUMN_INDEX_HOURLY_RATE = 7;
    static final int COLUMN_INDEX_LAST_START_BREAK = 4;
    static final int COLUMN_INDEX_LAST_START_BREAK2 = 12;
    static final int COLUMN_INDEX_START = 2;
    static final int COLUMN_INDEX_STATUS = 15;
    static final int COLUMN_INDEX_TITLE = 1;
    static final int COLUMN_INDEX_TYPE = 9;
    static final int DIALOG_ID_CHOOSE_JOB_TYPE = 4;
    static final int DIALOG_ID_DELETE_ALL = 1;
    static final int DIALOG_ID_DELETE_LAST = 3;
    static final int DIALOG_ID_TRIAL_LICENSE = 2;
    private static final int MENU_ABOUT = 6;
    private static final int MENU_DELETE_ALL = 5;
    private static final int MENU_EXPORT = 4;
    private static final int MENU_ITEM_DELETE = 1;
    private static final int MENU_ITEM_INSERT = 2;
    private static final int MENU_SETTINGS = 8;
    private static final int MENU_TIMEXCHANGE = 9;
    private static final int MENU_UPDATE = 7;
    static final String PREFERENCES = "preferences";
    static final String PREFERENCE_SPINNER = "spinner";
    private static final String[] PROJECTION;
    private static final int RESULT_CODE_NEW_JOB = 1;
    private static final int RESULT_CODE_SETTINGS = 2;
    private static final String TAG = "JobList";
    private TextView mBreakInfo;
    private boolean mContinueUpdate;
    private Cursor mCursor;
    private String[] mCustomerList;
    NumberFormat mDecimalFormat;
    private Runnable mDisplayUpdater;
    private TextView mDurationInfo;
    private Handler mHandler;
    protected Class mJobActivityClass;
    private TextView mJobCountInfo;
    private String mSelectedCustomer;
    private Spinner mSpinner;
    private TextView mTotalInfo;
    private String[] mWhereArguments;
    private String mWhereClause;

    /* renamed from: org.openintents.timesheet.activity.JobList.1 */
    class C00321 implements OnItemSelectedListener {
        C00321() {
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            JobList.this.updateChoice(position);
            JobList.this.refreshList();
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.2 */
    class C00332 implements OnClickListener {
        C00332() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.deleteAll();
            JobList.this.refreshSpinner();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.3 */
    class C00343 implements OnClickListener {
        C00343() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.leaveTemplate();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.4 */
    class C00354 implements OnClickListener {
        C00354() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.5 */
    class C00365 implements OnClickListener {
        C00365() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.startActivity(new Intent(JobList.this, LicenseActivity.class));
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.6 */
    class C00376 implements OnClickListener {
        C00376() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.deleteAll();
            JobList.this.refreshSpinner();
            JobList.this.updateTotal();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.7 */
    class C00387 implements OnClickListener {
        C00387() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.leaveTemplate();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.8 */
    class C00398 implements OnClickListener {
        C00398() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.JobList.9 */
    class C00409 implements OnClickListener {
        C00409() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            JobList.this.insertNewJobInternal(JobList.this.mJobActivityClass);
        }
    }

    public class DisplayUpdater implements Runnable {
        public void run() {
            JobList.this.updateTotal();
            JobList.this.updateDisplay(1000);
        }
    }

    public JobList() {
        this.mDecimalFormat = new DecimalFormat("0.00");
        this.mHandler = new Handler();
        this.mDisplayUpdater = new DisplayUpdater();
        this.mJobActivityClass = JobActivity.class;
    }

    static {
        PROJECTION = new String[]{Reminders._ID, Job.TITLE, Job.START_DATE, Job.END_DATE, Job.LAST_START_BREAK, Job.BREAK_DURATION, TimesheetIntent.EXTRA_CUSTOMER, Job.HOURLY_RATE, TimesheetProvider.QUERY_EXTRAS_TOTAL, Job.TYPE, Job.TOTAL_LONG, Job.RATE_LONG, Job.LAST_START_BREAK2, Job.BREAK2_DURATION, Job.EXTERNAL_REF, Job.STATUS, Job.CUSTOMER_REF};
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (EulaActivity.checkEula(this)) {
            LicenseUtils.modifyTitle(this);
            setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
            Intent intent = getIntent();
            if (intent.getData() == null) {
                intent.setData(Job.CONTENT_URI);
            }
            setContentView(R.layout.jobslist);
            getListView().setOnCreateContextMenuListener(this);
            getListView().setEmptyView(findViewById(R.id.empty));
            this.mSpinner = (Spinner) findViewById(R.id.spinner);
            refreshSpinner();
            this.mSpinner.setOnItemSelectedListener(new C00321());
            this.mDurationInfo = (TextView) findViewById(R.id.duration_info);
            this.mTotalInfo = (TextView) findViewById(R.id.total_info);
            this.mBreakInfo = (TextView) findViewById(R.id.break_info);
            this.mJobCountInfo = (TextView) findViewById(R.id.job_count_info);
            refreshList();
        }
    }

    protected void onResume() {
        super.onResume();
        DateTimeFormater.getFormatFromPreferences(this);
        String customer = getSharedPreferences(PREFERENCES, 0).getString(PREFERENCE_SPINNER, "");
        Log.i(TAG, "Prefs customer : " + customer);
        if (TextUtils.isEmpty(customer)) {
            updateChoice(0);
        } else {
            this.mSpinner.setSelection(findChoice(customer));
        }
        this.mContinueUpdate = true;
        updateDisplay(100);
    }

    protected void onPause() {
        super.onPause();
        cancelUpdateDisplay();
        SharedPreferences prefs = getSharedPreferences(PREFERENCES, 0);
        String currentSelection = this.mSelectedCustomer;
        if (currentSelection != null && currentSelection.equals(this.mCustomerList[0])) {
            currentSelection = "";
        }
        Editor editor = prefs.edit();
        editor.putString(PREFERENCE_SPINNER, currentSelection);
        editor.commit();
    }

    private void refreshSpinner() {
        this.mCustomerList = getCustomerListAndAll();
        ArrayAdapter<String> customerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.mCustomerList);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mSpinner.setAdapter(customerAdapter);
    }

    private void refreshList() {
        this.mCursor = managedQuery(Job.CONTENT_URI, PROJECTION, this.mWhereClause, this.mWhereArguments, Job.DEFAULT_SORT_ORDER);
        setListAdapter(new JobListCursorAdapter(this, this.mCursor, TextUtils.isEmpty(this.mWhereClause)));
        this.mContinueUpdate = true;
        updateDisplay(100);
    }

    void updateTotal() {
        if (this.mCursor == null) {
            this.mContinueUpdate = true;
            updateDisplay(1000);
        }
        this.mContinueUpdate = false;
        int worktype = RESULT_CODE_SETTINGS;
        int breaktype = RESULT_CODE_SETTINGS;
        this.mCursor.moveToPosition(-1);
        long now = System.currentTimeMillis();
        long sumWorkDuration = 0;
        long sumBreakDuration = 0;
        double sumTotal = 0.0d;
        int count = 0;
        while (true) {
            if (this.mCursor.moveToNext()) {
                long start = this.mCursor.getLong(RESULT_CODE_SETTINGS);
                long stop = this.mCursor.getLong(DIALOG_ID_DELETE_LAST);
                long breakstart = this.mCursor.getLong(MENU_EXPORT);
                long breakDuration = this.mCursor.getLong(MENU_DELETE_ALL);
                long break2start = this.mCursor.getLong(COLUMN_INDEX_LAST_START_BREAK2);
                long break2Duration = this.mCursor.getLong(COLUMN_INDEX_BREAK2_DURATION);
                long hourlyRate = this.mCursor.getLong(MENU_UPDATE);
                long extrasTotal = this.mCursor.getLong(MENU_SETTINGS);
                if (start > 0 && stop == 0 && break2start == 0) {
                    worktype = RESULT_CODE_NEW_JOB;
                    this.mContinueUpdate = true;
                }
                if (start > 0 && breakstart > 0) {
                    breaktype = RESULT_CODE_NEW_JOB;
                    this.mContinueUpdate = true;
                }
                if (breakstart > 0) {
                    breakDuration += now - breakstart;
                }
                if (break2start > 0) {
                    break2Duration += now - break2start;
                }
                long workDuration = 0;
                if (start > 0) {
                    if (stop > 0) {
                        workDuration = (stop - start) - break2Duration;
                    } else {
                        workDuration = (now - start) - break2Duration;
                    }
                }
                sumWorkDuration += workDuration;
                sumBreakDuration += breakDuration;
                sumTotal += ((((double) (workDuration - breakDuration)) * Timesheet.HOUR_FACTOR) * (((double) hourlyRate) * Timesheet.RATE_FACTOR)) + (((double) extrasTotal) * Timesheet.RATE_FACTOR);
                if (start > 0) {
                    count += RESULT_CODE_NEW_JOB;
                }
            } else {
                TextView textView = this.mDurationInfo;
                String[] strArr = new String[RESULT_CODE_NEW_JOB];
                strArr[0] = DurationFormater.formatDuration(this, sumWorkDuration, worktype);
                textView.setText(getString(R.string.duration_info, strArr));
                textView = this.mTotalInfo;
                strArr = new String[RESULT_CODE_NEW_JOB];
                strArr[0] = this.mDecimalFormat.format(sumTotal);
                textView.setText(getString(R.string.total_info, strArr));
                String breakString = DurationFormater.formatDuration(this, sumBreakDuration, breaktype);
                textView = this.mBreakInfo;
                strArr = new String[RESULT_CODE_NEW_JOB];
                strArr[0] = breakString;
                textView.setText(getString(R.string.break_info, strArr));
                textView = this.mJobCountInfo;
                Integer[] numArr = new Integer[RESULT_CODE_NEW_JOB];
                numArr[0] = Integer.valueOf(count);
                textView.setText(getString(R.string.number_of_jobs_info, numArr));
                return;
            }
        }
    }

    private int findChoice(String customer) {
        for (int i = RESULT_CODE_NEW_JOB; i < this.mCustomerList.length; i += RESULT_CODE_NEW_JOB) {
            Log.i(TAG, "Compare " + this.mCustomerList[i] + " : " + customer);
            if (this.mCustomerList[i].equals(customer)) {
                return i;
            }
        }
        return 0;
    }

    private void updateChoice(int choice) {
        String selectCustomer;
        this.mSelectedCustomer = this.mCustomerList[choice];
        if (choice == 0) {
            selectCustomer = "";
        } else {
            selectCustomer = this.mCustomerList[choice];
        }
        this.mWhereClause = "";
        this.mWhereArguments = new String[0];
        if (!TextUtils.isEmpty(selectCustomer)) {
            this.mWhereClause = "customer = ?";
            String[] strArr = new String[RESULT_CODE_NEW_JOB];
            strArr[0] = selectCustomer;
            this.mWhereArguments = strArr;
        }
        Log.i(TAG, "Select: " + this.mWhereClause + ", " + selectCustomer);
    }

    private String[] getCustomerListAndAll() {
        String[] customerList = JobActivity.getCustomerList(this);
        String[] tmp = new String[(customerList.length + RESULT_CODE_NEW_JOB)];
        tmp[0] = getString(R.string.all_customers);
        for (int i = 0; i < customerList.length; i += RESULT_CODE_NEW_JOB) {
            tmp[i + RESULT_CODE_NEW_JOB] = customerList[i];
        }
        return tmp;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, RESULT_CODE_SETTINGS, 0, R.string.menu_insert).setShortcut('1', 'i').setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_EXPORT, 0, R.string.menu_export).setShortcut('2', 'e').setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_SETTINGS, 0, R.string.menu_preferences).setShortcut('4', 's').setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, MENU_DELETE_ALL, 0, R.string.menu_delete_all).setShortcut('3', 'd').setIcon(android.R.drawable.ic_menu_delete);
        UpdateMenu.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);
        menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Intent i = new Intent(null, Job.CONTENT_URI);
        i.addCategory("android.intent.category.ALTERNATIVE");
        MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
        menu2.addIntentOptions(262144, 0, 0, new ComponentName(this, JobList.class), null, i, 0, null);
        if (getListAdapter().getCount() > 0) {
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());
            Intent[] specifics = new Intent[RESULT_CODE_NEW_JOB];
            specifics[0] = new Intent("android.intent.action.EDIT", uri);
            MenuItem[] items = new MenuItem[RESULT_CODE_NEW_JOB];
            Intent intent = new Intent(null, uri);
            intent.addCategory("android.intent.category.ALTERNATIVE");
            menu2.addIntentOptions(262144, 0, 0, null, specifics, intent, RESULT_CODE_NEW_JOB, items);
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case RESULT_CODE_SETTINGS /*2*/:
                insertNewJob();
                return true;
            case MENU_EXPORT /*4*/:
                startExport();
                return true;
            case MENU_DELETE_ALL /*5*/:
                showDialog(RESULT_CODE_NEW_JOB);
                return true;
            case MENU_ABOUT /*6*/:
                showAboutBox();
                return true;
            case MENU_UPDATE /*7*/:
                UpdateMenu.showUpdateBox(this);
                return true;
            case MENU_SETTINGS /*8*/:
                startActivityForResult(new Intent(this, PreferenceActivity.class), RESULT_CODE_SETTINGS);
                return true;
            case MENU_TIMEXCHANGE /*9*/:
                startTimeXchange();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void insertNewJob() {
        boolean insertAllowed = false;
        if (((Application) getApplication()).isLicenseValid()) {
            insertAllowed = true;
        } else {
            Cursor c = getContentResolver().query(Job.CONTENT_URI, new String[0], null, null, null);
            int count = c.getCount();
            c.close();
            if (count < 11) {
                insertAllowed = true;
            }
        }
        if (insertAllowed) {
            insertNewJobInternal(JobActivity.class);
        } else {
            showDialog(RESULT_CODE_SETTINGS);
        }
    }

    private void insertNewJobInternal(Class clazz) {
        Intent intent = new Intent("android.intent.action.INSERT", getIntent().getData());
        intent.setClass(this, clazz);
        intent.putExtra(TimesheetIntent.EXTRA_CUSTOMER, this.mSelectedCustomer);
        startActivityForResult(intent, RESULT_CODE_NEW_JOB);
    }

    private void startExport() {
        Intent intent = new Intent("android.intent.action.INSERT", getIntent().getData());
        intent.setClass(this, ConvertCsvActivity.class);
        intent.putExtra(TimesheetIntent.EXTRA_CUSTOMER, this.mSelectedCustomer);
        startActivity(intent);
    }

    private void startTimeXchange() {
        Intent intent = new Intent("org.openintents.actions.CONVERT_TIMEXCHANGE");
        intent.setData(getIntent().getData());
        intent.putExtra(TimesheetIntent.EXTRA_CUSTOMER, this.mSelectedCustomer);
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            startActivity(intent);
        }
    }

    private void deleteAll() {
        getContentResolver().delete(getIntent().getData(), this.mWhereClause, this.mWhereArguments);
    }

    private void leaveTemplate() {
        this.mCursor.moveToFirst();
        String customer = this.mCursor.getString(this.mCursor.getColumnIndexOrThrow(TimesheetIntent.EXTRA_CUSTOMER));
        long hourlyrate = this.mCursor.getLong(this.mCursor.getColumnIndexOrThrow(Job.HOURLY_RATE));
        deleteAll();
        ContentValues values = new ContentValues();
        values.put(TimesheetIntent.EXTRA_CUSTOMER, customer);
        values.put(Job.HOURLY_RATE, Long.valueOf(hourlyrate));
        getContentResolver().insert(Job.CONTENT_URI, values);
        this.mCursor.requery();
        updateTotal();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        try {
            Cursor cursor = (Cursor) getListAdapter().getItem(((AdapterContextMenuInfo) menuInfo).position);
            if (cursor != null) {
                menu.setHeaderTitle(cursor.getString(RESULT_CODE_NEW_JOB));
                menu.add(0, RESULT_CODE_NEW_JOB, 0, R.string.menu_delete);
                menu.add(0, R.id.call_contact, 0, R.string.call_contact);
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        try {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case RESULT_CODE_NEW_JOB /*1*/:
                    if (this.mCursor.getCount() == RESULT_CODE_NEW_JOB) {
                        showDialog(DIALOG_ID_DELETE_LAST);
                        return true;
                    }
                    getContentResolver().delete(ContentUris.withAppendedId(getIntent().getData(), info.id), null, null);
                    updateTotal();
                    return true;
                case R.id.call_contact:
                    String customerRef = ((Cursor) getListAdapter().getItem(info.position)).getString(COLUMN_INDEX_CUSTOMER_REF);
                    if (customerRef != null && customerRef.startsWith("content://contacts")) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setData(Uri.parse(customerRef));
                        startActivity(intent);
                        break;
                    }
            }
            return false;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
    }

    private void showAboutBox() {
        try {
            startActivityForResult(new Intent(Timesheet.ACTION_SHOW_ABOUT_DIALOG), 0);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(this, AboutActivity.class));
        }
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        String action = getIntent().getAction();
        if ("android.intent.action.PICK".equals(action) || "android.intent.action.GET_CONTENT".equals(action)) {
            setResult(-1, new Intent().setData(uri));
            return;
        }
        Cursor c = (Cursor) getListAdapter().getItem(position);
        Class activityClass = typeToActivityClass(c.getInt(MENU_TIMEXCHANGE));
        Intent intent = new Intent("android.intent.action.EDIT", cloneIfNecessary(c, uri));
        intent.setClass(this, activityClass);
        startActivity(intent);
    }

    private Uri cloneIfNecessary(Cursor c, Uri uri) {
        if (!(c.getInt(RESULT_CODE_SETTINGS) == 0 || c.getInt(DIALOG_ID_DELETE_LAST) == 0)) {
            c.getInt(COLUMN_INDEX_STATUS);
        }
        return uri;
    }

    protected Dialog onCreateDialog(int id) {
        Builder icon;
        Object[] objArr;
        switch (id) {
            case RESULT_CODE_NEW_JOB /*1*/:
                icon = new Builder(this).setIcon(android.R.drawable.ic_dialog_alert);
                objArr = new Object[RESULT_CODE_NEW_JOB];
                objArr[0] = this.mSelectedCustomer;
                return icon.setMessage(getString(R.string.dialog_delete_all, objArr)).setPositiveButton(R.string.delete_all, new C00332()).setNeutralButton(R.string.leave_template, new C00343()).setNegativeButton(android.R.string.cancel, new C00354()).create();
            case RESULT_CODE_SETTINGS /*2*/:
                return new Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.trial_version_description).setPositiveButton(R.string.enter_license_code, new C00365()).create();
            case DIALOG_ID_DELETE_LAST /*3*/:
                icon = new Builder(this).setIcon(android.R.drawable.ic_dialog_alert);
                objArr = new Object[RESULT_CODE_NEW_JOB];
                objArr[0] = this.mSelectedCustomer;
                return icon.setMessage(getString(R.string.dialog_delete_last, objArr)).setPositiveButton(R.string.menu_delete, new C00376()).setNeutralButton(R.string.leave_template, new C00387()).setNegativeButton(android.R.string.cancel, new C00398()).create();
            case MENU_EXPORT /*4*/:
                return new Builder(this).setPositiveButton(R.string.ok, new C00409()).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).setSingleChoiceItems(R.array.job_types, 0, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        JobList.this.mJobActivityClass = JobList.this.typeToActivityClass(whichButton);
                    }
                }).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog alertDialog;
        Object[] objArr;
        switch (id) {
            case RESULT_CODE_NEW_JOB /*1*/:
                alertDialog = (AlertDialog) dialog;
                objArr = new Object[RESULT_CODE_NEW_JOB];
                objArr[0] = this.mSelectedCustomer;
                alertDialog.setMessage(getString(R.string.dialog_delete_all, objArr));
            case DIALOG_ID_DELETE_LAST /*3*/:
                alertDialog = (AlertDialog) dialog;
                objArr = new Object[RESULT_CODE_NEW_JOB];
                objArr[0] = this.mSelectedCustomer;
                alertDialog.setMessage(getString(R.string.dialog_delete_last, objArr));
            default:
        }
    }

    private void updateDisplay(long delayMillis) {
        cancelUpdateDisplay();
        if (this.mContinueUpdate) {
            this.mHandler.postDelayed(this.mDisplayUpdater, delayMillis);
        }
    }

    private void cancelUpdateDisplay() {
        this.mHandler.removeCallbacks(this.mDisplayUpdater);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");
        switch (requestCode) {
            case RESULT_CODE_NEW_JOB /*1*/:
                if (this.mCustomerList.length != getCustomerListAndAll().length) {
                    Log.i(TAG, "Refresh spinner");
                    refreshSpinner();
                }
            case RESULT_CODE_SETTINGS /*2*/:
                refreshList();
            default:
        }
    }

    private Class typeToActivityClass(int type) {
        switch (type) {
            case Reminders.METHOD_DEFAULT /*0*/:
                return JobActivity.class;
            case RESULT_CODE_NEW_JOB /*1*/:
                return JobActivityMileage.class;
            case RESULT_CODE_SETTINGS /*2*/:
                return JobActivityExpense.class;
            default:
                return JobActivity.class;
        }
    }
}
