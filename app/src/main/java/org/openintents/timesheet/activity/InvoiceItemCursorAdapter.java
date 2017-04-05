package org.openintents.timesheet.activity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.util.DurationFormater;

public class InvoiceItemCursorAdapter extends ResourceCursorAdapter {
    NumberFormat mDecimalFormat;

    public InvoiceItemCursorAdapter(Context context, Cursor c) {
        super(context, 17367044, c);
        this.mDecimalFormat = new DecimalFormat("0.00");
    }

    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv1 = (TextView) view.findViewById(16908308);
        TextView tv2 = (TextView) view.findViewById(16908309);
        switch (cursor.getInt(1)) {
            case Reminders.METHOD_DEFAULT /*0*/:
                tv1.setText(cursor.getString(2));
                tv2.setText(this.mDecimalFormat.format(((double) cursor.getInt(3)) * Timesheet.RATE_FACTOR));
            case DurationFormater.TYPE_FORMAT_SECONDS /*1*/:
                tv1.setText(cursor.getString(2));
                tv2.setText(new StringBuilder(String.valueOf(this.mDecimalFormat.format(((double) cursor.getInt(3)) * Timesheet.RATE_FACTOR))).append(" ").append(cursor.getString(4).split("\\|")[2]).toString());
            default:
                tv1.setText("unknown type");
        }
    }
}
