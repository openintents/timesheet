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
import org.openintents.distribution.LicenseUtils;
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
    private static final int COLUMN_INDEX_ID = 0;
    private static final double HOUR_FACTOR = 2.7777777777E-7d;
    private static final int II_COLUMN_INDEX_DESCRIPTION = 2;
    private static final int II_COLUMN_INDEX_VALUE = 1;
    private static final String[] INVOICE_ITEMS_PROJECTION;
    private static final int MENU_SETTINGS = 1;
    private static final String[] PROJECTION;
    private static final double RATE_FACTOR = 0.01d;
    private static final String TAG = "ConvertCsvActivity";

    static {
        PROJECTION = new String[]{Reminders._ID, Job.TITLE, TimesheetIntent.EXTRA_NOTE, Job.START_DATE, Job.END_DATE, Job.LAST_START_BREAK, Job.BREAK_DURATION, TimesheetIntent.EXTRA_CUSTOMER, Job.HOURLY_RATE, Job.LAST_START_BREAK2, Job.BREAK2_DURATION, Job.CALENDAR_REF};
        INVOICE_ITEMS_PROJECTION = new String[]{Reminders._ID, InvoiceItem.VALUE, InvoiceItem.DESCRIPTION};
    }

    String mCustomer;
    EditText mEditText;
    TextView mExportFor;
    private TextView mCalendar;
    private TextView mFilePathLabel;
    private TextView mInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convertcsv);
        LicenseUtils.modifyTitle(this);
        this.mCustomer = getIntent().getStringExtra(TimesheetIntent.EXTRA_CUSTOMER);
        if (TextUtils.isEmpty(this.mCustomer)) {
            this.mCustomer = getString(R.string.all_customers);
        }
        this.mEditText = (EditText) findViewById(R.id.file_path);
        this.mExportFor = (TextView) findViewById(R.id.export_for);
        TextView textView = this.mExportFor;
        Object[] objArr = new Object[MENU_SETTINGS];
        objArr[COLUMN_INDEX_ID] = this.mCustomer;
        textView.setText(getString(R.string.export_for, objArr));
        this.mInfo = (TextView) findViewById(R.id.info);
        this.mFilePathLabel = (TextView) findViewById(R.id.file_path_label);
        this.mCalendar = (TextView) findViewById(R.id.calendar_info);
        ((Button) findViewById(R.id.file_export)).setOnClickListener(new C00431());
        ((Button) findViewById(R.id.file_import)).setOnClickListener(new C00442());
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_SINGLE_FILE, true)) {
            this.mInfo.setText(R.string.export_to_single_file);
            this.mFilePathLabel.setText(R.string.file_path);
            this.mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_FILENAME, getString(R.string.default_path)));
        } else {
            this.mInfo.setText(R.string.export_to_directory);
            this.mFilePathLabel.setText(R.string.dir_path);
            this.mEditText.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, getString(R.string.default_directory)));
        }
        if (prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_CALENDAR, false)) {
            this.mCalendar.setVisibility(COLUMN_INDEX_ID);
        } else {
            this.mCalendar.setVisibility(8);
        }
    }

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
        String fileName = this.mEditText.getText().toString();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void doExport(boolean r64, boolean r65, boolean r66, boolean r67, NumberFormat r68, boolean r69, File r70, ExportContext r71, int r72) {
        /*
        r63 = this;
        r39 = 0;
        r3 = r63.getApplication();
        r3 = (org.openintents.timesheet.Application) r3;
        r54 = r3.isLicenseValid();
        r53 = 1;
        r0 = r71;
        r3 = r0.mSingleFile;
        if (r3 != 0) goto L_0x0017;
    L_0x0014:
        r70.mkdirs();
    L_0x0017:
        r8 = "start_date ASC, customer ASC";
        r55 = new java.lang.StringBuffer;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r55.<init>();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r7 = 0;
        r7 = (java.lang.String[]) r7;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r65 == 0) goto L_0x003c;
    L_0x0023:
        r3 = "end_date";
        r0 = r55;
        r3 = r0.append(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = " is not null AND ";
        r3 = r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = "start_date";
        r3 = r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = " is not null";
        r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x003c:
        if (r66 == 0) goto L_0x0058;
    L_0x003e:
        r3 = r55.length();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r3 <= 0) goto L_0x004b;
    L_0x0044:
        r3 = " AND ";
        r0 = r55;
        r0.append(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x004b:
        r3 = "start_date";
        r0 = r55;
        r3 = r0.append(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = " is not null";
        r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x0058:
        r6 = r55.toString();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r63;
        r3 = r0.mCustomer;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = 2131230780; // 0x7f08003c float:1.8077622E38 double:1.052967912E-314;
        r0 = r63;
        r4 = r0.getString(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = r3.equals(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r3 != 0) goto L_0x00a5;
    L_0x006f:
        r3 = android.text.TextUtils.isEmpty(r6);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r3 != 0) goto L_0x0088;
    L_0x0075:
        r3 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = java.lang.String.valueOf(r6);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3.<init>(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = " AND ";
        r3 = r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r6 = r3.toString();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x0088:
        r3 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = java.lang.String.valueOf(r6);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3.<init>(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = "customer = ?";
        r3 = r3.append(r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r6 = r3.toString();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = 1;
        r7 = new java.lang.String[r3];	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = 0;
        r0 = r63;
        r4 = r0.mCustomer;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r7[r3] = r4;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x00a5:
        r3 = r63.getContentResolver();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = org.openintents.timesheet.Timesheet.Job.CONTENT_URI;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r5 = PROJECTION;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r37 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r37 == 0) goto L_0x010e;
    L_0x00b3:
        r3 = "start_date";
        r0 = r37;
        r30 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "end_date";
        r0 = r37;
        r27 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "break_duration";
        r0 = r37;
        r24 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "break2_duration";
        r0 = r37;
        r23 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "note";
        r0 = r37;
        r29 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "hourly_rate";
        r0 = r37;
        r28 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "customer";
        r0 = r37;
        r26 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "calendar_ref";
        r0 = r37;
        r25 = r0.getColumnIndexOrThrow(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r42 = new java.util.HashSet;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r42.<init>();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r40 = r39;
    L_0x00fa:
        r3 = r37.moveToNext();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r3 != 0) goto L_0x0126;
    L_0x0100:
        if (r69 == 0) goto L_0x04e9;
    L_0x0102:
        r4 = r42.iterator();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x0106:
        r3 = r4.hasNext();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r3 != 0) goto L_0x03d3;
    L_0x010c:
        r39 = r40;
    L_0x010e:
        r37.close();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = 2131230829; // 0x7f08006d float:1.8077722E38 double:1.052967936E-314;
        r4 = 0;
        r0 = r63;
        r3 = android.widget.Toast.makeText(r0, r3, r4);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3.show();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x011e:
        r3 = "ConvertCsvActivity";
        r4 = "end of export";
        android.util.Log.v(r3, r4);
        return;
    L_0x0126:
        r0 = r37;
        r1 = r30;
        r11 = r0.getLong(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r37;
        r1 = r27;
        r46 = r0.getLong(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r37;
        r1 = r24;
        r33 = r0.getLong(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r37;
        r1 = r23;
        r31 = r0.getLong(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r37;
        r1 = r29;
        r16 = r0.getString(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r64 == 0) goto L_0x015a;
    L_0x0150:
        r3 = "\n";
        r4 = "\\n";
        r0 = r16;
        r16 = r0.replace(r3, r4);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x015a:
        r0 = r37;
        r1 = r28;
        r48 = r0.getLong(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r37;
        r1 = r26;
        r15 = r0.getString(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r13 = r46 - r11;
        r3 = r46 - r11;
        r3 = r3 - r33;
        r61 = r3 - r31;
        r35 = r33;
        r0 = r61;
        r3 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r3 = r3 * r17;
        r0 = r48;
        r0 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r19 = 4576918229304087675; // 0x3f847ae147ae147b float:89128.96 double:0.01;
        r17 = r17 * r19;
        r44 = r3 * r17;
        r3 = org.openintents.util.DateTimeFormater.mDateFormater;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = java.lang.Long.valueOf(r11);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r56 = r3.format(r4);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = org.openintents.util.DateTimeFormater.mTimeFormater;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = java.lang.Long.valueOf(r11);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r57 = r3.format(r4);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r42;
        r0.add(r15);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r71;
        r41 = r0.getCsvWriter(r15);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 0;
        r3 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1));
        if (r3 <= 0) goto L_0x01e0;
    L_0x01b1:
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r3.sumTotalhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r4 + r13;
        r3.sumTotalhours = r4;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r3.sumBreakhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r4 + r35;
        r3.sumBreakhours = r4;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r3.sumWorkhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r4 + r61;
        r3.sumWorkhours = r4;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r3.sumEarning;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = r4 + r44;
        r3.sumEarning = r4;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x01e0:
        r3 = 0;
        r3 = (r11 > r3 ? 1 : (r11 == r3 ? 0 : -1));
        if (r3 <= 0) goto L_0x02d6;
    L_0x01e6:
        r0 = r41;
        r1 = r56;
        r0.writeValue(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r1 = r57;
        r0.writeValue(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x01f4:
        r3 = 0;
        r3 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1));
        if (r3 <= 0) goto L_0x0344;
    L_0x01fa:
        if (r53 == 0) goto L_0x0308;
    L_0x01fc:
        r3 = (double) r13;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r3 = r3 * r17;
        r0 = r68;
        r3 = r0.format(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r61;
        r3 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r3 = r3 * r17;
        r0 = r68;
        r3 = r0.format(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r35;
        r3 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r3 = r3 * r17;
        r0 = r68;
        r3 = r0.format(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r68;
        r1 = r44;
        r3 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r67 == 0) goto L_0x0505;
    L_0x0248:
        r0 = r37;
        r1 = r25;
        r10 = r0.getString(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r9 = r63;
        r17 = r72;
        r10 = org.openintents.timesheet.Timesheet.CalendarApp.setOrUpdateCalendarEvent(r9, r10, r11, r13, r15, r16, r17);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = org.openintents.timesheet.Timesheet.Job.CONTENT_URI;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r4 = 0;
        r0 = r37;
        r4 = r0.getString(r4);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r52 = android.net.Uri.withAppendedPath(r3, r4);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r40 != 0) goto L_0x0301;
    L_0x0267:
        r39 = new android.content.ContentValues;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r39.<init>();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x026c:
        r3 = "calendar_ref";
        r0 = r39;
        r0.put(r3, r10);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = r63.getContentResolver();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = 0;
        r5 = 0;
        r0 = r52;
        r1 = r39;
        r3.update(r0, r1, r4, r5);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x0280:
        r53 = r54;
    L_0x0282:
        r0 = r48;
        r3 = (double) r0;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r17 = 4576918229304087675; // 0x3f847ae147ae147b float:89128.96 double:0.01;
        r3 = r3 * r17;
        r0 = r68;
        r3 = r0.format(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r1 = r16;
        r0.write(r1);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r0.write(r15);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r41.writeNewline();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = 0;
        r0 = r37;
        r3 = r0.getLong(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r51 = java.lang.String.valueOf(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r17 = r63.getContentResolver();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r18 = org.openintents.timesheet.Timesheet.InvoiceItem.CONTENT_URI;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r19 = INVOICE_ITEMS_PROJECTION;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r20 = "job_id = ?";
        r3 = 1;
        r0 = new java.lang.String[r3];	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r21 = r0;
        r3 = 0;
        r21[r3] = r51;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r22 = 0;
        r38 = r17.query(r18, r19, r20, r21, r22);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
    L_0x02c9:
        r3 = r38.moveToNext();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        if (r3 != 0) goto L_0x0364;
    L_0x02cf:
        r38.close();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r40 = r39;
        goto L_0x00fa;
    L_0x02d6:
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        goto L_0x01f4;
    L_0x02e6:
        r43 = move-exception;
        r39 = r40;
    L_0x02e9:
        r3 = 2131230828; // 0x7f08006c float:1.807772E38 double:1.0529679355E-314;
        r4 = 0;
        r0 = r63;
        r3 = android.widget.Toast.makeText(r0, r3, r4);
        r3.show();
        r3 = "ConvertCsvActivity";
        r4 = "File not found";
        r0 = r43;
        android.util.Log.i(r3, r4, r0);
        goto L_0x011e;
    L_0x0301:
        r40.clear();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r39 = r40;
        goto L_0x026c;
    L_0x0308:
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r39 = r40;
        goto L_0x0282;
    L_0x0344:
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r39 = r40;
        goto L_0x0282;
    L_0x0364:
        r3 = 1;
        r0 = r38;
        r3 = r0.getLong(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = (double) r3;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r17 = 4576918229304087675; // 0x3f847ae147ae147b float:89128.96 double:0.01;
        r58 = r3 * r17;
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = r3.sumEarning;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r4 = r4 + r58;
        r3.sumEarning = r4;	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r68;
        r1 = r58;
        r60 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = 2;
        r0 = r38;
        r50 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r1 = r60;
        r0.writeValue(r1);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r1 = r50;
        r0.write(r1);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r0 = r41;
        r0.write(r15);	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        r41.writeNewline();	 Catch:{ FileNotFoundException -> 0x03d0, IOException -> 0x0503 }
        goto L_0x02c9;
    L_0x03d0:
        r43 = move-exception;
        goto L_0x02e9;
    L_0x03d3:
        r15 = r4.next();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r15 = (java.lang.String) r15;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r71;
        r41 = r0.getCsvWriter(r15);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r54 == 0) goto L_0x04b0;
    L_0x03ef:
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r3.sumTotalhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r0 = r17;
        r0 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r19 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r17 = r17 * r19;
        r0 = r68;
        r1 = r17;
        r3 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r3.sumWorkhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r0 = r17;
        r0 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r19 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r17 = r17 * r19;
        r0 = r68;
        r1 = r17;
        r3 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r3.sumBreakhours;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r0 = r17;
        r0 = (double) r0;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r19 = 4508846701240204243; // 0x3e92a42f961d3bd3 float:-1.2701236E-25 double:2.7777777777E-7;
        r17 = r17 * r19;
        r0 = r68;
        r1 = r17;
        r3 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r3 = r0.extras;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = (org.openintents.timesheet.convert.ConvertCsvActivity.Totals) r3;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r3.sumEarning;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r17 = r0;
        r0 = r68;
        r1 = r17;
        r3 = r0.format(r1);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x046f:
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230850; // 0x7f080082 float:1.8077764E38 double:1.0529679463E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = "";
        r0 = r41;
        r0.writeValue(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r41.writeNewline();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r71;
        r0.closeCsvWriter(r15);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        goto L_0x0106;
    L_0x0495:
        r43 = move-exception;
        r39 = r40;
    L_0x0498:
        r3 = 2131230828; // 0x7f08006c float:1.807772E38 double:1.0529679355E-314;
        r4 = 0;
        r0 = r63;
        r3 = android.widget.Toast.makeText(r0, r3, r4);
        r3.show();
        r3 = "ConvertCsvActivity";
        r4 = "IO exception";
        r0 = r43;
        android.util.Log.i(r3, r4, r0);
        goto L_0x011e;
    L_0x04b0:
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r3 = 2131230721; // 0x7f080001 float:1.8077503E38 double:1.0529678826E-314;
        r0 = r63;
        r3 = r0.getString(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r41;
        r0.write(r3);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        goto L_0x046f;
    L_0x04e9:
        r3 = r42.iterator();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
    L_0x04ed:
        r4 = r3.hasNext();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        if (r4 != 0) goto L_0x04f7;
    L_0x04f3:
        r39 = r40;
        goto L_0x010e;
    L_0x04f7:
        r15 = r3.next();	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r15 = (java.lang.String) r15;	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        r0 = r71;
        r0.closeCsvWriter(r15);	 Catch:{ FileNotFoundException -> 0x02e6, IOException -> 0x0495 }
        goto L_0x04ed;
    L_0x0503:
        r43 = move-exception;
        goto L_0x0498;
    L_0x0505:
        r39 = r40;
        goto L_0x0280;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.openintents.timesheet.convert.ConvertCsvActivity.doExport(boolean, boolean, boolean, boolean, java.text.NumberFormat, boolean, java.io.File, org.openintents.timesheet.convert.ConvertCsvActivity$ExportContext, int):void");
    }

    public void startImportAndFinish() {
    }

    public void doImport() throws IOException {
        Reader reader;
        File file = new File(this.mEditText.getText().toString());
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
        menu.add(COLUMN_INDEX_ID, MENU_SETTINGS, COLUMN_INDEX_ID, R.string.menu_preferences).setShortcut('1', 's').setIcon(17301577);
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
            this.mSingleFile = true;
            this.mWriter = new HashMap();
            this.mCSVWriter = new HashMap();
            this.mFile = file;
            this.mSingleFile = singleFile;
        }

        private void closeCsvWriter(String customer) throws IOException {
            if (this.mSingleFile) {
                customer = "";
            }
            ((Writer) this.mWriter.get(customer)).close();
        }

        private CSVWriter<Totals> getCsvWriter(String customer) throws IOException {
            if (this.mSingleFile) {
                customer = "";
            }
            if (this.mWriter.containsKey(customer)) {
                return (CSVWriter) this.mCSVWriter.get(customer);
            }
            File file;
            if (this.mSingleFile) {
                file = this.mFile;
            } else {
                file = new File(this.mFile + "/" + customer + ".csv");
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
            this.mWriter.put(customer, writer);
            this.mCSVWriter.put(customer, csvwriter);
            return csvwriter;
        }
    }
}
