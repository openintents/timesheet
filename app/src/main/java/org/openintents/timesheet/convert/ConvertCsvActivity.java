package org.openintents.timesheet.convert;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.openintents.convertcsv.opencsv.CSVReader;
import org.openintents.convertcsv.opencsv.CSVWriter;
import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.InvoiceItem;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.timesheet.activity.JobActivity;
import org.openintents.util.DateTimeFormater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class ConvertCsvActivity extends Activity {
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
        mEditText = (EditText) findViewById(R.id.file_path);
        
        mExportFor = (TextView) findViewById(R.id.export_for);
        mExportFor.setText(getString(R.string.export_for, mCustomer));
        
        mInfo = (TextView) findViewById(R.id.info);
        mFilePathLabel = (TextView) findViewById(R.id.file_path_label);
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
            mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_FILENAME, getString(R.string.default_path)));
        } else {
            mInfo.setText(R.string.export_to_directory);
            mFilePathLabel.setText(R.string.dir_path);
            mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, getString(R.string.default_directory)));
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
     *
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
        NumberFormat decimalFormat = new DecimalFormat("0.00");
        String fileName = mEditText.getText().toString();
        Editor editor = prefs.edit();
        if (singleFile) {
            editor.putString(PreferenceActivity.PREFS_EXPORT_FILENAME, fileName);
        } else {
            editor.putString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, fileName);
        }
        editor.commit();
        File file = new File(fileName);
        ExportContext exportContext = new ExportContext(file, singleFile);
        if (file.exists() && askIfExists) {
            View view = LayoutInflater.from(this).inflate(R.layout.file_exists, null);
            int i = R.string.yes;
            new Builder(this).setView(view).setPositiveButton(i, new C00453(editor, (CheckBox) view.findViewById(R.id.dont_ask_again), exportReplaceBr, exportCompletedOnly, omitTemplates, updateCalendar, decimalFormat, exportTotals, file, exportContext, calendarAuthority)).setNegativeButton(R.string.no, new C00464()).show();
            return;
        }
        doExport(exportReplaceBr, exportCompletedOnly, omitTemplates, updateCalendar, decimalFormat, exportTotals, file, exportContext, calendarAuthority);
        finish();
    }

    private void doExport(boolean r64, boolean r65, boolean r66, boolean r67, NumberFormat r68, boolean r69, File r70, ExportContext r71, int r72) {
        // TODO
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
        long sumBreakhours;
        double sumEarning;
        long sumTotalhours;
        long sumWorkhours;

        Totals() {
            this.sumTotalhours = 0;
            this.sumBreakhours = 0;
            this.sumWorkhours = 0;
            this.sumEarning = 0.0d;
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

    class ExportContext {
        HashMap<String, CSVWriter<Totals>> mCSVWriter;
        File mFile;
        boolean mSingleFile;
        HashMap<String, Writer> mWriter;

        ExportContext(File file, boolean singleFile) {
            mSingleFile = true;
            mWriter = new HashMap();
            mCSVWriter = new HashMap();
            mFile = file;
            mSingleFile = singleFile;
        }

        private void closeCsvWriter(String customer) throws IOException {
            if (mSingleFile) {
                customer = "";
            }
            ((Writer) mWriter.get(customer)).close();
        }

        private CSVWriter<Totals> getCsvWriter(String customer) throws IOException {
            if (mSingleFile) {
                customer = "";
            }
            if (mWriter.containsKey(customer)) {
                return (CSVWriter) mCSVWriter.get(customer);
            }
            File file;
            if (mSingleFile) {
                file = mFile;
            } else {
                file = new File(mFile + "/" + customer + ".csv");
            }
            FileWriter writer = new FileWriter(file);
            CSVWriter<Totals> csvwriter = new CSVWriter(writer);
            csvwriter.extras = new Totals();
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_date), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_time_started), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_total), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_work), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_break_time), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_earnings), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_rate), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_description), false);
            csvwriter.write(ConvertCsvActivity.this.getString(R.string.header_customer), false);
            csvwriter.writeNewline();
            mWriter.put(customer, writer);
            mCSVWriter.put(customer, csvwriter);
            return csvwriter;
        }
    }
}
