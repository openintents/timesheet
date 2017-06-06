package org.openintents.timesheet;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.openintents.intents.ProviderIntents;
import org.openintents.intents.ProviderUtils;
import org.openintents.timesheet.Timesheet.InvoiceItem;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;

import java.util.HashMap;
import java.util.Map;

public class TimesheetProvider extends ContentProvider {
    public static final String QUERY_EXTRAS_TOTAL = "extras_total";
    private static final String DATABASE_NAME = "timesheet.db";
    private static final int DATABASE_VERSION = 3;
    private static final int DELEGATED_JOBS = 3;
    private static final int DELEGATED_JOB_ID = 4;
    private static final int INVOICEITEMS = 5;
    private static final int INVOICEITEM_ID = 6;
    private static final String INVOICE_ITEMS_TABLE_NAME = "invoice_items";
    private static final int JOBS = 1;
    private static final int JOB_ID = 2;
    private static final String JOB_TABLE_NAME = "jobs";
    private static final String TAG = "TimesheetProvider";
    private static final UriMatcher sUriMatcher;
    static Map<String, String> sInvoiceItemsProjectionMap;
    private static HashMap<String, String> sJobsProjectionMap;

    static {
        sUriMatcher = new UriMatcher(-1);
        sUriMatcher.addURI(Timesheet.AUTHORITY, JOB_TABLE_NAME, JOBS);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "jobs/#", JOB_ID);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "invoiceitems", INVOICEITEMS);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "invoiceitems/#", INVOICEITEM_ID);
        sInvoiceItemsProjectionMap = new HashMap();
        sInvoiceItemsProjectionMap.put(Reminders._ID, Reminders._ID);
        sInvoiceItemsProjectionMap.put(InvoiceItem.DESCRIPTION, InvoiceItem.DESCRIPTION);
        sInvoiceItemsProjectionMap.put(InvoiceItem.EXTRAS, InvoiceItem.EXTRAS);
        sInvoiceItemsProjectionMap.put(InvoiceItem.JOB_ID, InvoiceItem.JOB_ID);
        sInvoiceItemsProjectionMap.put(Job.TYPE, Job.TYPE);
        sInvoiceItemsProjectionMap.put(InvoiceItem.VALUE, InvoiceItem.VALUE);
        sJobsProjectionMap = new HashMap();
        sJobsProjectionMap.put(Reminders._ID, "jobs._id");
        sJobsProjectionMap.put(Job.TITLE, Job.TITLE);
        sJobsProjectionMap.put(TimesheetIntent.EXTRA_NOTE, TimesheetIntent.EXTRA_NOTE);
        sJobsProjectionMap.put(TimesheetIntent.EXTRA_CUSTOMER, TimesheetIntent.EXTRA_CUSTOMER);
        sJobsProjectionMap.put(Job.START_DATE, Job.START_DATE);
        sJobsProjectionMap.put(Job.END_DATE, Job.END_DATE);
        sJobsProjectionMap.put(Job.BREAK_DURATION, Job.BREAK_DURATION);
        sJobsProjectionMap.put(Job.LAST_START_BREAK, Job.LAST_START_BREAK);
        sJobsProjectionMap.put(Job.BREAK2_DURATION, Job.BREAK2_DURATION);
        sJobsProjectionMap.put(Job.LAST_START_BREAK2, Job.LAST_START_BREAK2);
        sJobsProjectionMap.put(Job.BREAK2_COUNT, Job.BREAK2_COUNT);
        sJobsProjectionMap.put(Job.HOURLY_RATE, Job.HOURLY_RATE);
        sJobsProjectionMap.put(Job.HOURLY_RATE2, Job.HOURLY_RATE2);
        sJobsProjectionMap.put(Job.HOURLY_RATE2_START, Job.HOURLY_RATE2_START);
        sJobsProjectionMap.put(Job.HOURLY_RATE3, Job.HOURLY_RATE3);
        sJobsProjectionMap.put(Job.HOURLY_RATE3_START, Job.HOURLY_RATE3_START);
        sJobsProjectionMap.put(Job.TAX_RATE, Job.TAX_RATE);
        sJobsProjectionMap.put(Job.PLANNED_DATE, Job.PLANNED_DATE);
        sJobsProjectionMap.put(Job.PLANNED_DURATION, Job.PLANNED_DURATION);
        sJobsProjectionMap.put(Job.CUSTOMER_REF, Job.CUSTOMER_REF);
        sJobsProjectionMap.put(Job.NOTES_REF, Job.NOTES_REF);
        sJobsProjectionMap.put(Job.CALENDAR_REF, Job.CALENDAR_REF);
        sJobsProjectionMap.put(QUERY_EXTRAS_TOTAL, QUERY_EXTRAS_TOTAL);
        sJobsProjectionMap.put(Job.DELEGATEE_REF, Job.DELEGATEE_REF);
        sJobsProjectionMap.put(Job.STATUS, Job.STATUS);
        sJobsProjectionMap.put(Job.START_LONG, Job.START_LONG);
        sJobsProjectionMap.put(Job.END_LONG, Job.END_LONG);
        sJobsProjectionMap.put(Job.RATE_LONG, Job.RATE_LONG);
        sJobsProjectionMap.put(Job.TOTAL_LONG, Job.TOTAL_LONG);
        sJobsProjectionMap.put(Job.TYPE, Job.TYPE);
        sJobsProjectionMap.put(Job.PARENT_ID, Job.PARENT_ID);
        sJobsProjectionMap.put(Job.EXTERNAL_REF, Job.EXTERNAL_REF);
        sJobsProjectionMap.put(Job.EXTERNAL_SYSTEM, Job.EXTERNAL_SYSTEM);
        sJobsProjectionMap.put(Job.CREATED_DATE, Job.CREATED_DATE);
        sJobsProjectionMap.put(Job.MODIFIED_DATE, Job.MODIFIED_DATE);
    }

    private DatabaseHelper mOpenHelper;

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch (sUriMatcher.match(uri)) {
            case JOBS /*1*/:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere("delegatee_ref is null");
                if (!TextUtils.isEmpty(sortOrder)) {
                    orderBy = sortOrder;
                    break;
                }
                orderBy = Job.DEFAULT_SORT_ORDER;
                break;
            case JOB_ID /*2*/:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere("_id=" + uri.getLastPathSegment() + " AND " + Job.DELEGATEE_REF + " is null");
                if (!TextUtils.isEmpty(sortOrder)) {
                    orderBy = sortOrder;
                    break;
                }
                orderBy = Job.DEFAULT_SORT_ORDER;
                break;
            case DELEGATED_JOBS /*3*/:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere("delegatee_ref is not null");
                if (!TextUtils.isEmpty(sortOrder)) {
                    orderBy = sortOrder;
                    break;
                }
                orderBy = Job.DEFAULT_SORT_ORDER;
                break;
            case DELEGATED_JOB_ID /*4*/:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere("_id=" + uri.getLastPathSegment() + " AND " + Job.DELEGATEE_REF + " is not null");
                break;
            case INVOICEITEMS /*5*/:
                qb.setTables(INVOICE_ITEMS_TABLE_NAME);
                qb.setProjectionMap(sInvoiceItemsProjectionMap);
                if (!TextUtils.isEmpty(sortOrder)) {
                    orderBy = sortOrder;
                    break;
                }
                orderBy = InvoiceItem.DESCRIPTION;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor c = qb.query(this.mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case JOBS /*1*/:
                return Job.CONTENT_TYPE;
            case JOB_ID /*2*/:
                return Job.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        Long now;
        long rowId;
        Uri noteUri;
        Intent intent;
        switch (sUriMatcher.match(uri)) {
            case JOBS /*1*/:
                if (initialValues != null) {
                    values = new ContentValues(initialValues);
                } else {
                    values = new ContentValues();
                }
                now = Long.valueOf(System.currentTimeMillis());
                if (!values.containsKey(Job.CREATED_DATE)) {
                    values.put(Job.CREATED_DATE, now);
                }
                if (!values.containsKey(Job.MODIFIED_DATE)) {
                    values.put(Job.MODIFIED_DATE, now);
                }
                if (!values.containsKey(Job.TITLE)) {
                    values.put(Job.TITLE, Resources.getSystem().getString(17039375));
                }
                if (!values.containsKey(TimesheetIntent.EXTRA_NOTE)) {
                    values.put(TimesheetIntent.EXTRA_NOTE, "");
                }
                if (!values.containsKey(TimesheetIntent.EXTRA_CUSTOMER)) {
                    Resources system = Resources.getSystem();
                    values.put(TimesheetIntent.EXTRA_CUSTOMER, "");
                }
                rowId = this.mOpenHelper.getWritableDatabase().insert(JOB_TABLE_NAME, TimesheetIntent.EXTRA_NOTE, values);
                if (rowId > 0) {
                    noteUri = ContentUris.withAppendedId(Job.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    intent = new Intent(ProviderIntents.ACTION_INSERTED);
                    intent.setData(noteUri);
                    getContext().sendBroadcast(intent);
                    return noteUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case INVOICEITEMS /*5*/:
                if (initialValues != null) {
                    values = new ContentValues(initialValues);
                } else {
                    values = new ContentValues();
                }
                now = Long.valueOf(System.currentTimeMillis());
                if (!values.containsKey(Job.CREATED_DATE)) {
                    values.put(Job.CREATED_DATE, now);
                }
                if (!values.containsKey(Job.MODIFIED_DATE)) {
                    values.put(Job.MODIFIED_DATE, now);
                }
                SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
                rowId = db.insert(INVOICE_ITEMS_TABLE_NAME, InvoiceItem.DESCRIPTION, values);
                if (rowId > 0) {
                    noteUri = ContentUris.withAppendedId(InvoiceItem.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    intent = new Intent(ProviderIntents.ACTION_INSERTED);
                    intent.setData(noteUri);
                    getContext().sendBroadcast(intent);
                    updateTotal(db, (Long) values.get(InvoiceItem.JOB_ID));
                    return noteUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private void updateTotal(SQLiteDatabase writableDb, Long jobId) {
        String str = INVOICE_ITEMS_TABLE_NAME;
        String[] strArr = new String[JOBS];
        strArr[0] = "SUM(value)";
        String[] strArr2 = new String[JOBS];
        strArr2[0] = String.valueOf(jobId);
        Cursor c = writableDb.query(str, strArr, "job_id = ?", strArr2, InvoiceItem.JOB_ID, null, null);
        c.moveToFirst();
        Long total = Long.valueOf(c.getLong(0));
        c.close();
        ContentValues values = new ContentValues();
        values.put(QUERY_EXTRAS_TOTAL, total);
        strArr = new String[JOBS];
        strArr[0] = String.valueOf(jobId);
        writableDb.update(JOB_TABLE_NAME, values, "_id = ?", strArr);
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        long[] affectedRows = null;
        String whereString;
        switch (sUriMatcher.match(uri)) {
            case JOBS /*1*/:
                affectedRows = ProviderUtils.getAffectedRows(db, JOB_TABLE_NAME, where, whereArgs);
                count = db.delete(JOB_TABLE_NAME, where, whereArgs);
                break;
            case JOB_ID /*2*/:
                whereString = "_id=" + ((String) uri.getPathSegments().get(JOBS)) + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
                affectedRows = ProviderUtils.getAffectedRows(db, JOB_TABLE_NAME, whereString, whereArgs);
                count = db.delete(JOB_TABLE_NAME, whereString, whereArgs);
                break;
            case INVOICEITEM_ID /*6*/:
                String noteId = (String) uri.getPathSegments().get(JOBS);
                String str = INVOICE_ITEMS_TABLE_NAME;
                String[] strArr = new String[JOBS];
                strArr[0] = InvoiceItem.JOB_ID;
                String[] strArr2 = new String[JOBS];
                strArr2[0] = noteId;
                Cursor c = db.query(str, strArr, "_id = ?", strArr2, null, null, null);
                c.moveToFirst();
                Long jobId = Long.valueOf(c.getLong(0));
                c.close();
                whereString = "_id=" + noteId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
                affectedRows = ProviderUtils.getAffectedRows(db, INVOICE_ITEMS_TABLE_NAME, whereString, whereArgs);
                count = db.delete(INVOICE_ITEMS_TABLE_NAME, whereString, whereArgs);
                updateTotal(db, jobId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Intent intent = new Intent(ProviderIntents.ACTION_DELETED);
        intent.setData(uri);
        intent.putExtra(ProviderIntents.EXTRA_AFFECTED_ROWS, affectedRows);
        getContext().sendBroadcast(intent);
        Timesheet.updateNotification(getContext(), false);
        return count;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case JOBS /*1*/:
                count = db.update(JOB_TABLE_NAME, values, where, whereArgs);
                break;
            case JOB_ID /*2*/:
                count = db.update(JOB_TABLE_NAME, values, "_id=" + uri.getLastPathSegment() + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Intent intent = new Intent(ProviderIntents.ACTION_MODIFIED);
        intent.setData(uri);
        getContext().sendBroadcast(intent);
        Timesheet.updateNotification(getContext(), false);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, TimesheetProvider.DATABASE_NAME, null, TimesheetProvider.DELEGATED_JOBS);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE jobs (_id INTEGER PRIMARY KEY,title TEXT,note TEXT,customer TEXT,start_date INTEGER,end_date INTEGER,last_start_break INTEGER,break_duration INTEGER,last_start_break2 INTEGER,break2_duration INTEGER,break2_count INTEGER,hourly_rate INTEGER,hourly_rate2 INTEGER,hourly_rate2_Start INTEGER,hourly_rate3 INTEGER,hourly_rate3_Start INTEGER,planned_date INTEGER,planned_duration INTEGER,customer_ref TEXT,notes_ref TEXT,calendar_ref TEXT,tax_rate DECIMAL,extras_total INTEGER,delegatee_ref TEXT,status INTEGER,start_long INTEGER,end_long INTEGER,total_long INTEGER,rate_long INTEGER,parent_id INTEGER,external_system TEXT,external_ref TEXT,type INTEGER,created INTEGER,modified INTEGER);");
            createInvoiceItemsTable(db);
        }

        private void createInvoiceItemsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE invoice_items (_id INTEGER PRIMARY KEY,job_id INTEGER,description TEXT,extras TEXT,type INTEGER,value INTEGER,created INTEGER,modified INTEGER);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TimesheetProvider.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            if (newVersion > oldVersion) {
                switch (oldVersion) {
                    case TimesheetProvider.JOBS /*1*/:
                        try {
                            db.execSQL("ALTER TABLE jobs ADD COLUMN last_start_break2 INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN break2_duration INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN break2_count INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN planned_date INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN planned_duration INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN customer_ref TEXT;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN notes_ref TEXT;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN calendar_ref TEXT;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN tax_rate DECIMAL;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN extras_total INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN delegatee_ref TEXT;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN status INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN start_long INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN end_long INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN rate_long INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN total_long INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN parent_id INTEGER;");
                            db.execSQL("ALTER TABLE jobs ADD COLUMN type INTEGER;");
                            createInvoiceItemsTable(db);
                            break;
                        } catch (SQLException e) {
                            Log.e(TimesheetProvider.TAG, "Error executing SQL: ", e);
                            break;
                        }
                    case TimesheetProvider.JOB_ID /*2*/:
                        break;
                    case TimesheetProvider.DELEGATED_JOBS /*3*/:
                        break;
                    default:
                        Log.w(TimesheetProvider.TAG, "Unknown version " + oldVersion + ". Creating new database.");
                        db.execSQL("DROP TABLE IF EXISTS notes");
                        onCreate(db);
                        return;
                }
                try {
                    db.execSQL("ALTER TABLE jobs ADD COLUMN hourly_rate2 INTEGER;");
                    db.execSQL("ALTER TABLE jobs ADD COLUMN hourly_rate2_Start INTEGER;");
                    db.execSQL("ALTER TABLE jobs ADD COLUMN hourly_rate3 INTEGER;");
                    db.execSQL("ALTER TABLE jobs ADD COLUMN hourly_rate3_Start INTEGER;");
                } catch (SQLException e2) {
                    Log.e(TimesheetProvider.TAG, "Error executing SQL: ", e2);
                }
                try {
                    db.execSQL("ALTER TABLE jobs ADD COLUMN external_system TEXT;");
                    db.execSQL("ALTER TABLE jobs ADD COLUMN external_ref TEXT;");
                    return;
                } catch (SQLException e22) {
                    Log.e(TimesheetProvider.TAG, "Error executing SQL: ", e22);
                    return;
                }
            }
            Log.w(TimesheetProvider.TAG, "Don't know how to downgrade. Will not touch database and hope they are compatible.");
        }
    }
}
