/*
 * Copyright (C) 2008-2017 OpenIntents.biz
 *
 */

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
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.openintents.intents.ProviderIntents;
import org.openintents.intents.ProviderUtils;
import org.openintents.timesheet.Timesheet.InvoiceItem;
import org.openintents.timesheet.Timesheet.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class TimesheetProvider extends ContentProvider {

    public static final String QUERY_EXTRAS_TOTAL = "extras_total";
    private static final String TAG = "TimesheetProvider";
    private static final String DATABASE_NAME = "timesheet.db";
    private static final int DATABASE_VERSION = 3;
    private static final String JOB_TABLE_NAME = "jobs";
    private static final String INVOICE_ITEMS_TABLE_NAME = "invoice_items";
    private static final int JOBS = 1;
    private static final int JOB_ID = 2;
    private static final int DELEGATED_JOBS = 3;
    private static final int DELEGATED_JOB_ID = 4;
    private static final int INVOICEITEMS = 5;
    private static final int INVOICEITEM_ID = 6;

    private static final UriMatcher sUriMatcher;
    static Map<String, String> sInvoiceItemsProjectionMap;
    private static HashMap<String, String> sJobsProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "jobs", JOBS);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "jobs/#", JOB_ID);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "invoiceitems", INVOICEITEMS);
        sUriMatcher.addURI(Timesheet.AUTHORITY, "invoiceitems/#", INVOICEITEM_ID);

        sInvoiceItemsProjectionMap = new HashMap<String, String>();
        sInvoiceItemsProjectionMap.put(InvoiceItem._ID, InvoiceItem._ID);
        sInvoiceItemsProjectionMap.put(InvoiceItem.DESCRIPTION,
                InvoiceItem.DESCRIPTION);
        sInvoiceItemsProjectionMap.put(InvoiceItem.EXTRAS, InvoiceItem.EXTRAS);
        sInvoiceItemsProjectionMap.put(InvoiceItem.JOB_ID, InvoiceItem.JOB_ID);
        sInvoiceItemsProjectionMap.put(InvoiceItem.TYPE, InvoiceItem.TYPE);
        sInvoiceItemsProjectionMap.put(InvoiceItem.VALUE, InvoiceItem.VALUE);

        sJobsProjectionMap = new HashMap<String, String>();
        sJobsProjectionMap.put(Job._ID, JOB_TABLE_NAME + "." + Job._ID);
        sJobsProjectionMap.put(Job.TITLE, Job.TITLE);
        sJobsProjectionMap.put(Job.NOTE, Job.NOTE);
        sJobsProjectionMap.put(Job.CUSTOMER, Job.CUSTOMER);
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
        sJobsProjectionMap.put(Job.EXTRAS_TOTAL, Job.EXTRAS_TOTAL);
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

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;

        switch (sUriMatcher.match(uri)) {
            case JOBS:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere(Job.DELEGATEE_REF + " is null");

                // If no sort order is specified use the default
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = Job.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }

                break;

            case JOB_ID:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere(Job._ID + "=" + uri.getLastPathSegment() + " AND "
                        + Job.DELEGATEE_REF + " is null");
                // If no sort order is specified use the default
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = Job.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }

                break;

            case DELEGATED_JOBS:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere(Job.DELEGATEE_REF + " is not null");
                // If no sort order is specified use the default

                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = Job.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;

            case DELEGATED_JOB_ID:
                qb.setTables(JOB_TABLE_NAME);
                qb.setProjectionMap(sJobsProjectionMap);
                qb.appendWhere(Job._ID + "=" + uri.getLastPathSegment() + " AND "
                        + Job.DELEGATEE_REF + " is not null");
                break;
            case INVOICEITEMS:
                qb.setTables(INVOICE_ITEMS_TABLE_NAME);
                qb.setProjectionMap(sInvoiceItemsProjectionMap);
                // If no sort order is specified use the default

                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = InvoiceItem.DEFAULT_SORT_ORDER;
                } else {
                    orderBy = sortOrder;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case JOBS:
                return Job.CONTENT_TYPE;

            case JOB_ID:
                return Job.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        switch (sUriMatcher.match(uri)) {
            case JOBS:

                ContentValues values;
                if (initialValues != null) {
                    values = new ContentValues(initialValues);
                } else {
                    values = new ContentValues();
                }

                Long now = System.currentTimeMillis();

                // Make sure that the fields are all set
                if (!values.containsKey(Job.CREATED_DATE)) {
                    values.put(Job.CREATED_DATE, now);
                }

                if (!values.containsKey(Job.MODIFIED_DATE)) {
                    values.put(Job.MODIFIED_DATE, now);
                }

                if (!values.containsKey(Job.TITLE)) {
                    Resources r = Resources.getSystem();
                    values.put(Job.TITLE, r.getString(android.R.string.untitled));
                }

                if (!values.containsKey(Job.NOTE)) {
                    values.put(Job.NOTE, "");
                }

                if (!values.containsKey(Job.CUSTOMER)) {
                    values.put(Job.CUSTOMER, "");
                }

                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                long rowId = db.insert(JOB_TABLE_NAME, Job.NOTE, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris
                            .withAppendedId(Job.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);

                    Intent intent = new Intent(ProviderIntents.ACTION_INSERTED);
                    intent.setData(noteUri);
                    getContext().sendBroadcast(intent);

                    return noteUri;
                }

                throw new SQLException("Failed to insert row into " + uri);

            case INVOICEITEMS:

                if (initialValues != null) {
                    values = new ContentValues(initialValues);
                } else {
                    values = new ContentValues();
                }

                now = System.currentTimeMillis();

                // Make sure that the fields are all set
                if (!values.containsKey(Job.CREATED_DATE)) {
                    values.put(InvoiceItem.CREATED_DATE, now);
                }

                if (!values.containsKey(Job.MODIFIED_DATE)) {
                    values.put(InvoiceItem.MODIFIED_DATE, now);
                }

                db = mOpenHelper.getWritableDatabase();
                rowId = db.insert(INVOICE_ITEMS_TABLE_NAME,
                        InvoiceItem.DESCRIPTION, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(
                            InvoiceItem.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);

                    Intent intent = new Intent(ProviderIntents.ACTION_INSERTED);
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

        Cursor c = writableDb.query(INVOICE_ITEMS_TABLE_NAME, new String[]{"SUM(value)"}, InvoiceItem.JOB_ID + " = ?", new String[]{String.valueOf(jobId)}, InvoiceItem.JOB_ID, null, null);
        long total;
        if (c.moveToFirst()) {
            total = c.getLong(0);
        } else {
            total = 0;
        }
        c.close();


        ContentValues values = new ContentValues();
        values.put(Job.EXTRAS_TOTAL, total);
        writableDb.update(JOB_TABLE_NAME, values, Job._ID + " = ?", new String[]{String.valueOf(jobId)});

    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long[] affectedRows;
        switch (sUriMatcher.match(uri)) {
            case JOBS:
                affectedRows = ProviderUtils.getAffectedRows(db, JOB_TABLE_NAME,
                        where, whereArgs);
                count = db.delete(JOB_TABLE_NAME, where, whereArgs);
                break;

            case JOB_ID:
                String noteId = uri.getPathSegments().get(1);
                String whereString = Job._ID + "=" + noteId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");

                affectedRows = ProviderUtils.getAffectedRows(db, JOB_TABLE_NAME,
                        whereString, whereArgs);
                count = db.delete(JOB_TABLE_NAME, whereString, whereArgs);
                break;

            case INVOICEITEM_ID:
                noteId = uri.getPathSegments().get(1);

                Cursor c = db.query(INVOICE_ITEMS_TABLE_NAME, new String[]{InvoiceItem.JOB_ID}, InvoiceItem._ID + " = ?", new String[]{noteId}, null, null, null);
                c.moveToFirst();
                Long jobId = c.getLong(0);
                c.close();

                whereString = InvoiceItem._ID + "=" + noteId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");

                affectedRows = ProviderUtils.getAffectedRows(db,
                        INVOICE_ITEMS_TABLE_NAME, whereString, whereArgs);
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

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case JOBS:
                count = db.update(JOB_TABLE_NAME, values, where, whereArgs);
                break;

            case JOB_ID:
                String id = uri.getLastPathSegment();
                count = db.update(JOB_TABLE_NAME, values,
                        Job._ID
                                + "="
                                + id
                                + (!TextUtils.isEmpty(where) ? " AND (" + where
                                + ')' : ""), whereArgs);
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

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + JOB_TABLE_NAME + " (" + Job._ID
                    + " INTEGER PRIMARY KEY," // Version 1
                    + Job.TITLE + " TEXT," // Version 1
                    + Job.NOTE + " TEXT," // Version 1
                    + Job.CUSTOMER + " TEXT," // Version 1
                    + Job.START_DATE + " INTEGER," // Version 1
                    + Job.END_DATE + " INTEGER," // Version 1
                    + Job.LAST_START_BREAK + " INTEGER," // Version 1
                    + Job.BREAK_DURATION + " INTEGER," // Version 1
                    + Job.LAST_START_BREAK2 + " INTEGER," // Version 2
                    + Job.BREAK2_DURATION + " INTEGER," // Version 2
                    + Job.BREAK2_COUNT + " INTEGER," // Version 2
                    + Job.HOURLY_RATE + " INTEGER," // Version 1
                    + Job.HOURLY_RATE2 + " INTEGER," // Version 3
                    + Job.HOURLY_RATE2_START + " INTEGER," // Version 3
                    + Job.HOURLY_RATE3 + " INTEGER," // Version 3
                    + Job.HOURLY_RATE3_START + " INTEGER," // Version 3
                    + Job.PLANNED_DATE + " INTEGER," // Version 2
                    + Job.PLANNED_DURATION + " INTEGER," // Version 2
                    + Job.CUSTOMER_REF + " TEXT," // Version 2
                    + Job.NOTES_REF + " TEXT," // Version 2
                    + Job.CALENDAR_REF + " TEXT," // Version 2
                    + Job.TAX_RATE + " DECIMAL," // Version 2
                    + Job.EXTRAS_TOTAL + " INTEGER," // Version 2
                    + Job.DELEGATEE_REF + " TEXT," // Version 2
                    + Job.STATUS + " INTEGER," // Version 2
                    + Job.START_LONG + " INTEGER," // Version 2
                    + Job.END_LONG + " INTEGER," // Version 2
                    + Job.TOTAL_LONG + " INTEGER," // Version 2
                    + Job.RATE_LONG + " INTEGER," // Version 2
                    + Job.PARENT_ID + " INTEGER," // Version 2
                    + Job.EXTERNAL_SYSTEM + " TEXT," // Version 4
                    + Job.EXTERNAL_REF + " TEXT," // Version 4
                    + Job.TYPE + " INTEGER," // Version 2
                    + Job.CREATED_DATE + " INTEGER," // Version 1
                    + Job.MODIFIED_DATE + " INTEGER" // Version 1
                    + ");");

            createInvoiceItemsTable(db);
        }

        private void createInvoiceItemsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + INVOICE_ITEMS_TABLE_NAME + " ("
                    + InvoiceItem._ID + " INTEGER PRIMARY KEY," // Version 2
                    + InvoiceItem.JOB_ID + " INTEGER," // Version 2
                    + InvoiceItem.DESCRIPTION + " TEXT," // Version 2
                    + InvoiceItem.EXTRAS + " TEXT," // Version 2
                    + InvoiceItem.TYPE + " INTEGER," // Version 2
                    + InvoiceItem.VALUE + " INTEGER," // Version 2
                    + InvoiceItem.CREATED_DATE + " INTEGER," // Version 2
                    + InvoiceItem.MODIFIED_DATE + " INTEGER" // Version 2
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            if (newVersion > oldVersion) {
                // Upgrade
                switch (oldVersion) {
                    case 1:
                        // Upgrade from version 1 to 2.
                        // It seems SQLite3 only allows to add one column at a time,
                        // so we need three SQL statements:
                        try {
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.LAST_START_BREAK2
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.BREAK2_DURATION
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.BREAK2_COUNT
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.PLANNED_DATE
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.PLANNED_DURATION
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.CUSTOMER_REF + " TEXT;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.NOTES_REF + " TEXT;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.CALENDAR_REF + " TEXT;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.TAX_RATE + " DECIMAL;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.EXTRAS_TOTAL
                                    + " INTEGER;");
                            db
                                    .execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                            + " ADD COLUMN " + Job.DELEGATEE_REF
                                            + " TEXT;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.STATUS + " INTEGER;");

                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.START_LONG + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.END_LONG + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.RATE_LONG + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.TOTAL_LONG + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.PARENT_ID + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.TYPE + " INTEGER;");

                            createInvoiceItemsTable(db);
                        } catch (SQLException e) {
                            Log.e(TAG, "Error executing SQL: ", e);
                            // If the error is "duplicate column name" then
                            // everything is fine,
                            // as this happens after upgrading 1->2, then
                            // downgrading 2->1,
                            // and then upgrading again 1->2.
                        }
                        // fall through for further upgrades.

                    case 2: // add more columns
                        try {
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.HOURLY_RATE2
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.HOURLY_RATE2_START
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.HOURLY_RATE3
                                    + " INTEGER;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.HOURLY_RATE3_START
                                    + " INTEGER;");
                        } catch (SQLException e) {
                            Log.e(TAG, "Error executing SQL: ", e);
                            // If the error is "duplicate column name" then
                            // everything is fine,
                            // as this happens after upgrading 2->3, then
                            // downgrading 3->2,
                            // and then upgrading again 2->3.
                        }
                        // fall through for further upgrades.

                    case 3: // add more columns
                        try {
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.EXTERNAL_SYSTEM
                                    + " TEXT;");
                            db.execSQL("ALTER TABLE " + JOB_TABLE_NAME
                                    + " ADD COLUMN " + Job.EXTERNAL_REF
                                    + " TEXT;");
                        } catch (SQLException e) {
                            Log.e(TAG, "Error executing SQL: ", e);
                            // If the error is "duplicate column name" then
                            // everything is fine,
                            // as this happens after upgrading 3->4, then
                            // downgrading 4->3,
                            // and then upgrading again 3->4.
                        }
                        // fall through for further upgrades.
                        /*
                         * case 4: // add more columns
                         */
                        break;
                    default:
                        Log.w(TimesheetProvider.TAG, "Unknown version " + oldVersion + ". Creating new database.");
                        db.execSQL("DROP TABLE IF EXISTS notes");
                        onCreate(db);
                }
            } else { // newVersion <= oldVersion
                // Downgrade
                Log.w(TAG, "Don't know how to downgrade. Will not touch database and hope they are compatible.");
                // Do nothing.
            }

        }
    }
}
