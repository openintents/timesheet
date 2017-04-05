package org.openintents.intents;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import org.openintents.timesheet.Timesheet.Reminders;

public class ProviderUtils {
    public static long[] getAffectedRows(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
        if (TextUtils.isEmpty(whereClause)) {
            return null;
        }
        Cursor c = db.query(table, new String[]{Reminders._ID}, whereClause, whereArgs, null, null, null);
        long[] affectedRows = null;
        if (c != null) {
            affectedRows = new long[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                affectedRows[i] = c.getLong(0);
                i++;
            }
        }
        return affectedRows;
    }
}
