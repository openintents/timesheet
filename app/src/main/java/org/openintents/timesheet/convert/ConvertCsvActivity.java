package org.openintents.timesheet.convert;

import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.convertcsv.opencsv.CSVReader;
import org.openintents.convertcsv.opencsv.CSVWriter;
import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.InvoiceItem;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.activity.JobActivity;
import org.openintents.util.DateTimeFormater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;

public class ConvertCsvActivity extends AppCompatActivity {

    private static final double HOUR_FACTOR = 2.7777777777E-7d;
    private static final double RATE_FACTOR = 0.01d;
    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final String[] PROJECTION = new String[]{Job._ID,
            Job.TITLE, Job.NOTE, Job.START_DATE, Job.END_DATE,
            Job.LAST_START_BREAK, Job.BREAK_DURATION, Job.CUSTOMER,
            Job.HOURLY_RATE, Job.LAST_START_BREAK2, Job.BREAK2_DURATION,
            Job.CALENDAR_REF};
    private static final int COLUMN_INDEX_ID = 0;
    private static final String[] INVOICE_ITEMS_PROJECTION = new String[]{//
            InvoiceItem._ID,// 0
            InvoiceItem.VALUE, // 1
            InvoiceItem.DESCRIPTION // 2
    };
    private static final int II_COLUMN_INDEX_VALUE = 1;
    private static final int II_COLUMN_INDEX_DESCRIPTION = 2;
    private static final String TAG = "ConvertCsvActivity";
    String mCustomer;
    EditText mEditText;
    TextView mExportFor;
    private TextView mCalendar;
    private TextView mFilePathLabel;
    private TextView mInfo;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convertcsv);
        mCustomer = getIntent().getStringExtra(TimesheetIntent.EXTRA_CUSTOMER);
        if (TextUtils.isEmpty(mCustomer)) {
            mCustomer = getString(R.string.all_customers);
        }
        mEditText = (EditText) findViewById(R.id.user);

        mExportFor = (TextView) findViewById(R.id.export_for);
        mExportFor.setText(getString(R.string.export_for, mCustomer));

        mInfo = (TextView) findViewById(R.id.info);
        mFilePathLabel = (TextView) findViewById(R.id.user_label);
        mCalendar = (TextView) findViewById(R.id.calendar_info);
        findViewById(R.id.file_export).setOnClickListener(new C00431());
        findViewById(R.id.file_import).setOnClickListener(new C00442());
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean singleFile = prefs.getBoolean(
                PreferenceActivity.PREFS_EXPORT_SINGLE_FILE, true);
        if (singleFile) {
            mInfo.setText(R.string.export_to_single_file);
            mFilePathLabel.setText(R.string.file_path);
            String defaultFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/timesheet.csv";
            mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_FILENAME, defaultFile));
        } else {
            mInfo.setText(R.string.export_to_directory);
            mFilePathLabel.setText(R.string.dir_path);
            String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/timesheet";
            mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, defaultPath));
        }
        final boolean updateCalendar = prefs.getBoolean(
                PreferenceActivity.PREFS_EXPORT_CALENDAR, false);
        if (updateCalendar) {
            mCalendar.setVisibility(View.VISIBLE);
        } else {
            mCalendar.setVisibility(View.GONE);
        }
    }

    /**
     * start export, overwrite fileName if exists
     */
    private void startExportAndFinish() {
        int calendarAuthority = JobActivity.getCalendarAuthority(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean askIfExists = prefs.getBoolean(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, true);
        boolean exportReplaceBr = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_REPLACE_BR, false);
        boolean exportCompletedOnly = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_COMPLETED_ONLY, false);
        boolean omitTemplates = prefs.getBoolean(PreferenceActivity.PREFS_OMIT_TEMPLATES, true);
        boolean exportTotals = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_TOTALS, true);
        boolean singleFile = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_SINGLE_FILE, true);
        boolean updateCalendar = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_CALENDAR, false);
        DateTimeFormater.getFormatFromPreferences(this);
        final NumberFormat decimalFormat = new DecimalFormat("0.00");
        String fileName = mEditText.getText().toString();
        Editor editor = prefs.edit();
        if (singleFile) {
            editor.putString(PreferenceActivity.PREFS_EXPORT_FILENAME, fileName);
        } else {
            editor.putString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, fileName);
        }
        editor.apply();
        File file = new File(fileName);
        ExportContext exportContext = new ExportContext(file, singleFile);
        if (file.exists() && askIfExists) {
            View view = LayoutInflater.from(this).inflate(R.layout.file_exists, null);
            int i = R.string.yes;
            new Builder(this).setView(view)
                    .setPositiveButton(R.string.yes, new C00453(editor, (CheckBox) view.findViewById(R.id.dont_ask_again), exportReplaceBr, exportCompletedOnly, omitTemplates, updateCalendar, decimalFormat, exportTotals, file, exportContext, calendarAuthority))
                    .setNegativeButton(R.string.no, new C00464())
                    .show();
            return;
        }
        doExport(exportReplaceBr, exportCompletedOnly, omitTemplates, updateCalendar, decimalFormat, exportTotals, file, exportContext, calendarAuthority);
        finish();
    }

    private void doExport(boolean exportReplaceBr, boolean exportCompletedOnly,
                          boolean omitTemplates, boolean updateCalendar,
                          NumberFormat decimalFormat, boolean exportTotals, File file,
                          ExportContext exportContext, int calendarAuthority) {

        ContentValues contentValues = null;

        if (!exportContext.mSingleFile) {
            file.mkdirs();
        }

        try {

            String SORT_ORDER = "start_date ASC, customer ASC";

            StringBuffer selectionSb = new StringBuffer();
            String[] selectionArgs = null;
            if (exportCompletedOnly) {
                selectionSb.append(Job.END_DATE).append(" is not null AND ")
                        .append(Job.START_DATE).append(" is not null");
            }

            if (omitTemplates) {
                if (selectionSb.length() > 0) {
                    selectionSb.append(" AND ");
                }
                selectionSb.append(Job.START_DATE).append(" is not null");
            }
            String selection = selectionSb.toString();

            if (!mCustomer.equals(getString(R.string.all_customers))) {
                if (!TextUtils.isEmpty(selection)) {
                    selection += " AND ";
                }
                selection += Job.CUSTOMER + " = ?";
                selectionArgs = new String[]{mCustomer};
            }
            Cursor c = getContentResolver().query(Job.CONTENT_URI,
                    PROJECTION, selection, selectionArgs, SORT_ORDER);

            if (c != null) {

                int COLUMN_START_DATE = c
                        .getColumnIndexOrThrow(Job.START_DATE);
                int COLUMN_END_DATE = c
                        .getColumnIndexOrThrow(Job.END_DATE);
                int COLUMN_BREAK_DURATION = c
                        .getColumnIndexOrThrow(Job.BREAK_DURATION);
                int COLUMN_BREAK2_DURATION = c
                        .getColumnIndexOrThrow(Job.BREAK2_DURATION);
                int COLUMN_NOTE = c.getColumnIndexOrThrow(Job.NOTE);
                int COLUMN_HOURLY_RATE = c
                        .getColumnIndexOrThrow(Job.HOURLY_RATE);
                int COLUMN_CUSTOMER = c
                        .getColumnIndexOrThrow(Job.CUSTOMER);
                int COLUMN_CALENDAR_REF = c
                        .getColumnIndexOrThrow(Job.CALENDAR_REF);

                HashSet<String> customerSet = new HashSet<>();

                while (c.moveToNext()) {
                    // get values
                    long startdate = c.getLong(COLUMN_START_DATE);
                    long enddate = c.getLong(COLUMN_END_DATE);

                    long breakduration = c.getLong(COLUMN_BREAK_DURATION);
                    long break2duration = c.getLong(COLUMN_BREAK2_DURATION);
                    String description = c.getString(COLUMN_NOTE);
                    if (exportReplaceBr) {
                        description = description.replace("\n", "\\n");
                    }
                    long hourlyRate = c.getLong(COLUMN_HOURLY_RATE);
                    String customer = c.getString(COLUMN_CUSTOMER);

                    long totalhours = enddate - startdate;
                    long workhours = enddate - startdate - breakduration
                            - break2duration;
                    long breakhours = breakduration;

                    double earning = workhours * Timesheet.HOUR_FACTOR
                            * (hourlyRate * Timesheet.RATE_FACTOR);

                    String stringStartDate = DateTimeFormater.mDateFormater
                            .format(startdate);
                    String stringStartTime = DateTimeFormater.mTimeFormater
                            .format(startdate);

                    //
                    // write values
                    //
                    customerSet.add(customer);
                    CSVWriter<Totals> csvwriter = exportContext
                            .getCsvWriter(customer);
                    if (totalhours > 0) {
                        csvwriter.extras.sumTotalhours += totalhours;
                        csvwriter.extras.sumBreakhours += breakhours;
                        csvwriter.extras.sumWorkhours += workhours;
                        csvwriter.extras.sumEarning += earning;
                    }

                    if (startdate > 0) {
                        csvwriter.writeValue(stringStartDate);
                        csvwriter.writeValue(stringStartTime);
                    } else {
                        // not started yet
                        csvwriter.writeValue("");
                        csvwriter.writeValue("");
                    }
                    if (totalhours > 0) {
                        csvwriter
                                .writeValue(decimalFormat.format(totalhours
                                        * Timesheet.HOUR_FACTOR));
                        csvwriter.writeValue(decimalFormat.format(workhours
                                * Timesheet.HOUR_FACTOR));
                        csvwriter
                                .writeValue(decimalFormat.format(breakhours
                                        * Timesheet.HOUR_FACTOR));
                        csvwriter.writeValue(decimalFormat.format(earning));

                        if (updateCalendar) {
                            // export to calendar
                            String calendarUri = c
                                    .getString(COLUMN_CALENDAR_REF);
                            calendarUri = Timesheet.CalendarApp.setOrUpdateCalendarEvent(
                                    this, calendarUri,
                                    startdate, totalhours, customer,
                                    description, calendarAuthority);
                            Uri jobUri = Uri.withAppendedPath(Job.CONTENT_URI, c.getString(0));
                            if (contentValues == null) {
                                contentValues = new ContentValues();
                            } else {
                                contentValues.clear();
                            }
                            contentValues.put(Job.CALENDAR_REF, calendarUri);
                            getContentResolver().update(jobUri, contentValues, null, null);
                        }

                    } else {
                        // Show empty fields for ongoing tasks
                        csvwriter.writeValue("");
                        csvwriter.writeValue("");
                        csvwriter.writeValue("");
                        csvwriter.writeValue("");
                    }
                    csvwriter.writeValue(decimalFormat.format(hourlyRate
                            * RATE_FACTOR));
                    csvwriter.write(description);
                    csvwriter.write(customer);
                    csvwriter.writeNewline();

                    // export extra invoice items
                    String jobId = String.valueOf(c.getLong(COLUMN_INDEX_ID));
                    Cursor c2 = getContentResolver().query(
                            InvoiceItem.CONTENT_URI, INVOICE_ITEMS_PROJECTION,
                            InvoiceItem.JOB_ID + " = ?",
                            new String[]{jobId}, null);
                    while (c2.moveToNext()) {

                        double value = c2.getLong(II_COLUMN_INDEX_VALUE)
                                * RATE_FACTOR;
                        csvwriter.extras.sumEarning += value;
                        String valueString = decimalFormat.format(value);

                        String invoiceDescription = c2
                                .getString(II_COLUMN_INDEX_DESCRIPTION);
                        csvwriter.writeValue(""); // date
                        csvwriter.writeValue(""); // time started
                        csvwriter.writeValue(""); // total
                        csvwriter.writeValue(""); // work
                        csvwriter.writeValue(""); // break time
                        csvwriter.writeValue(valueString); // earnings
                        csvwriter.writeValue(""); // rate
                        csvwriter.write(invoiceDescription);
                        csvwriter.write(customer);
                        csvwriter.writeNewline();
                    }
                    c2.close();

                    // end export invoice items

                }

                if (exportTotals) {
                    for (String customer : customerSet) {
                        CSVWriter<Totals> csvwriter = exportContext
                                .getCsvWriter(customer);
                        // write row of totals
                        csvwriter.writeValue("");
                        csvwriter.writeValue("");

                        csvwriter.writeValue(decimalFormat
                                .format(csvwriter.extras.sumTotalhours
                                        * Timesheet.HOUR_FACTOR));
                        csvwriter.writeValue(decimalFormat
                                .format(csvwriter.extras.sumWorkhours
                                        * Timesheet.HOUR_FACTOR));
                        csvwriter.writeValue(decimalFormat
                                .format(csvwriter.extras.sumBreakhours
                                        * Timesheet.HOUR_FACTOR));
                        csvwriter.writeValue(decimalFormat
                                .format(csvwriter.extras.sumEarning));

                        csvwriter.writeValue("");
                        csvwriter.write(getString(R.string.export_total));
                        csvwriter.writeValue("");
                        csvwriter.writeNewline();

                        exportContext.closeCsvWriter(customer);
                    }
                } else {
                    // no totals, just close writer
                    for (String customer : customerSet) {
                        exportContext.closeCsvWriter(customer);
                    }
                }
            }
            c.close();

            Toast.makeText(this, R.string.export_finished, Toast.LENGTH_SHORT)
                    .show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, R.string.error_writing_file,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "File not found", e);
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_writing_file,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "IO exception", e);
        }
        Log.v(TAG, "end of export");

    }

    public void startImportAndFinish() {
    }

    public void doImport() throws IOException {
        Reader reader;
        File file = new File(mEditText.getText().toString());
        String enc = "utf-8";
        if (enc == null) {
            reader = new InputStreamReader(new FileInputStream(file));
        } else {
            reader = new InputStreamReader(new FileInputStream(file), enc);
        }
        CSVReader csvreader = new CSVReader(reader);
        while (true) {
            String[] nextLine = csvreader.readNext();
            if (nextLine != null) {
                String stringStartDate = nextLine[COLUMN_INDEX_ID];
                String stringStartTime = nextLine[MENU_SETTINGS];
                String totalhours = nextLine[II_COLUMN_INDEX_DESCRIPTION];
                String workhours = nextLine[3];
                String breakhours = nextLine[4];
                String earning = nextLine[5];
                String hourlyRate = nextLine[6];
                String description = nextLine[7];
                String str = nextLine[8];
            } else {
                return;
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SETTINGS, 0, R.string.menu_preferences).setShortcut('1', 's')
                .setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SETTINGS /*1*/:
                Intent intent = new Intent(this, PreferenceActivity.class);
                intent.putExtra("exportOnly", true);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static class Totals {
        long sumTotalhours = 0;
        long sumBreakhours = 0;
        long sumWorkhours = 0;
        double sumEarning = 0;
    }

    class ExportContext {

        HashMap<String, Writer> mWriter;
        HashMap<String, CSVWriter<Totals>> mCSVWriter;
        File mFile;
        boolean mSingleFile = true;

        ExportContext(File file, boolean singleFile) {
            mWriter = new HashMap<>();
            mCSVWriter = new HashMap<>();
            mFile = file;
            mSingleFile = singleFile;
        }

        private void closeCsvWriter(String customer) throws IOException {
            if (mSingleFile) {
                customer = "";
            }
            mWriter.get(customer).close();

        }

        private CSVWriter<Totals> getCsvWriter(String customer)
                throws IOException {
            CSVWriter<Totals> csvwriter;

            if (mSingleFile) {
                customer = "";
            }
            if (!mWriter.containsKey(customer)) {

                File file;
                if (!mSingleFile) {
                    file = new File(mFile + "/" + customer + ".csv");
                } else {
                    file = mFile;
                }
                FileWriter writer = new FileWriter(file);
                csvwriter = new CSVWriter<>(writer);
                csvwriter.extras = new Totals();

                csvwriter.write(getString(R.string.header_date), false);
                csvwriter.write(getString(R.string.header_time_started), false);
                csvwriter.write(getString(R.string.header_total), false);
                csvwriter.write(getString(R.string.header_work), false);
                csvwriter.write(getString(R.string.header_break_time), false);
                csvwriter.write(getString(R.string.header_earnings), false);
                csvwriter.write(getString(R.string.header_rate), false);
                csvwriter.write(getString(R.string.header_description), false);
                csvwriter.write(getString(R.string.header_customer), false);
                csvwriter.writeNewline();

                mWriter.put(customer, writer);
                mCSVWriter.put(customer, csvwriter);
            } else {
                csvwriter = mCSVWriter.get(customer);
            }

            return csvwriter;
        }
    }

    /* renamed from: org.openintents.timesheet.convert.ConvertCsvActivity.1 */
    class C00431 implements OnClickListener {
        C00431() {
        }

        public void onClick(View arg0) {
            ConvertCsvActivity.this.startExportAndFinish();
        }
    }

    /* renamed from: org.openintents.timesheet.convert.ConvertCsvActivity.2 */
    class C00442 implements OnClickListener {
        C00442() {
        }

        public void onClick(View arg0) {
            ConvertCsvActivity.this.startImportAndFinish();
        }
    }

    /* renamed from: org.openintents.timesheet.convert.ConvertCsvActivity.3 */
    class C00453 implements DialogInterface.OnClickListener {
        private final /* synthetic */ int val$calendarAuthority;
        private final /* synthetic */ CheckBox val$cb;
        private final /* synthetic */ NumberFormat val$decimalFormat;
        private final /* synthetic */ Editor val$editor;
        private final /* synthetic */ boolean val$exportCompletedOnly;
        private final /* synthetic */ ExportContext val$exportContext;
        private final /* synthetic */ boolean val$exportReplaceBr;
        private final /* synthetic */ boolean val$exportTotals;
        private final /* synthetic */ File val$file;
        private final /* synthetic */ boolean val$omitTemplates;
        private final /* synthetic */ boolean val$updateCalendar;

        C00453(Editor editor, CheckBox checkBox, boolean z, boolean z2, boolean z3, boolean z4, NumberFormat numberFormat, boolean z5, File file, ExportContext exportContext, int i) {
            this.val$editor = editor;
            this.val$cb = checkBox;
            this.val$exportReplaceBr = z;
            this.val$exportCompletedOnly = z2;
            this.val$omitTemplates = z3;
            this.val$updateCalendar = z4;
            this.val$decimalFormat = numberFormat;
            this.val$exportTotals = z5;
            this.val$file = file;
            this.val$exportContext = exportContext;
            this.val$calendarAuthority = i;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.val$editor.putBoolean(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, !this.val$cb.isChecked());
            this.val$editor.commit();
            ConvertCsvActivity.this.doExport(this.val$exportReplaceBr, this.val$exportCompletedOnly, this.val$omitTemplates, this.val$updateCalendar, this.val$decimalFormat, this.val$exportTotals, this.val$file, this.val$exportContext, this.val$calendarAuthority);
            ConvertCsvActivity.this.finish();
        }
    }

    /* renamed from: org.openintents.timesheet.convert.ConvertCsvActivity.4 */
    class C00464 implements DialogInterface.OnClickListener {
        C00464() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }
}
