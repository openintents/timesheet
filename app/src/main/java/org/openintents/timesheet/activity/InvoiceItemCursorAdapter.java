package org.openintents.timesheet.activity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.openintents.timesheet.Timesheet;
import org.openintents.timesheet.Timesheet.Reminders;
import org.openintents.util.DurationFormater;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class InvoiceItemCursorAdapter extends ResourceCursorAdapter {
    NumberFormat mDecimalFormat;

    public InvoiceItemCursorAdapter(Context context, Cursor c) {
        super(context, android.R.layout.simple_list_item_2, c);
        this.mDecimalFormat = new DecimalFormat("0.00");
    }

    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
        TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
        switch (cursor.getInt(1)) {
            case Reminders.METHOD_DEFAULT /*0*/:
                tv1.setText(cursor.getString(2));
                tv2.setText(this.mDecimalFormat.format(((double) cursor.getInt(3)) * Timesheet.RATE_FACTOR));
            case DurationFormater.TYPE_FORMAT_SECONDS /*1*/:
                tv1.setText(cursor.getString(2));
                String milage = cursor.getString(4);
                if (milage != null) {
                    String[] milageParts = milage.split("\\|");
                    if (milageParts.length >= 2) {
                        milage = milageParts[2];
                    } else {
                        milage = "?";
                    }
                } else {
                    milage = "?";
                }
                tv2.setText(new StringBuilder(String.valueOf(this.mDecimalFormat.format(((double) cursor.getInt(3)) * Timesheet.RATE_FACTOR)))
                        .append(" ").append(milage).toString());
            default:
                tv1.setText("unknown type");
        }
    }
}
