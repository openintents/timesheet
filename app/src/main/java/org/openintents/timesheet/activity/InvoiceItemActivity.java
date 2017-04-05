package org.openintents.timesheet.activity;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.openintents.timesheet.R;
import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.Timesheet.InvoiceItem;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.Timesheet.Reminders;

public class InvoiceItemActivity extends ListActivity {
    public static final int COLUMN_INDEX_DESCRIPTION = 2;
    public static final int COLUMN_INDEX_EXTRAS = 4;
    public static final int COLUMN_INDEX_TYPE = 1;
    public static final int COLUMN_INDEX_VALUE = 3;
    private static final int MENU_DELETE = 1;
    private static final String TAG = "InvoiceItemActivity";
    private long mJobId;
    private String[] mProjection;
    private FrameLayout mTypePanel;
    private Spinner mTypeSpinner;
    private ArrayAdapter<CharSequence> typeAdapter;
    private int[] type_panels;

    /* renamed from: org.openintents.timesheet.activity.InvoiceItemActivity.1 */
    class C00111 implements OnItemSelectedListener {
        C00111() {
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            InvoiceItemActivity.this.setType(position);
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    /* renamed from: org.openintents.timesheet.activity.InvoiceItemActivity.2 */
    class C00122 implements OnClickListener {
        C00122() {
        }

        public void onClick(View arg0) {
            InvoiceItemActivity.this.addInvoiceItem();
        }
    }

    public InvoiceItemActivity() {
        this.type_panels = new int[]{R.layout.type_panel_general, R.layout.type_panel_mileage};
        this.mProjection = new String[]{Reminders._ID, Job.TYPE, InvoiceItem.DESCRIPTION, InvoiceItem.VALUE, InvoiceItem.EXTRAS};
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_item);
        this.mTypeSpinner = (Spinner) findViewById(R.id.spinner);
        this.mTypeSpinner.setOnItemSelectedListener(new C00111());
        this.typeAdapter = ArrayAdapter.createFromResource(this, R.array.invoice_types, 17367048);
        this.typeAdapter.setDropDownViewResource(17367049);
        this.mTypeSpinner.setAdapter(this.typeAdapter);
        this.mTypePanel = (FrameLayout) findViewById(R.id.panel_frame);
        this.mJobId = getIntent().getLongExtra("jobid", -1);
        setType(0);
        this.mTypeSpinner.setVisibility(8);
        ((Button) findViewById(R.id.add_button)).setOnClickListener(new C00122());
        getListView().setEmptyView(findViewById(R.id.empty));
        updateListAdapter();
        registerForContextMenu(getListView());
    }

    private void updateListAdapter() {
        String[] strArr = new String[MENU_DELETE];
        strArr[0] = String.valueOf(this.mJobId);
        setListAdapter(new InvoiceItemCursorAdapter(this, getContentResolver().query(InvoiceItem.CONTENT_URI, this.mProjection, "job_id = ?", strArr, null)));
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, MENU_DELETE, 0, R.string.delete);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menu = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_DELETE /*1*/:
                getContentResolver().delete(Uri.withAppendedPath(InvoiceItem.CONTENT_URI, String.valueOf(menu.id)), null, null);
                break;
        }
        return true;
    }

    protected void addInvoiceItem() {
        int type = this.mTypeSpinner.getSelectedItemPosition();
        ContentValues values = new ContentValues();
        values.put(Job.TYPE, Integer.valueOf(type));
        values.put(InvoiceItem.JOB_ID, Long.valueOf(this.mJobId));
        switch (type) {
            case Reminders.METHOD_DEFAULT /*0*/:
                values.put(InvoiceItem.DESCRIPTION, ((TextView) findViewById(R.id.description)).getText().toString());
                String valueString = ((TextView) findViewById(R.id.value)).getText().toString();
                try {
                    values.put(InvoiceItem.VALUE, Long.valueOf((long) (100.0f * Float.parseFloat(valueString))));
                    break;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing expense value: " + valueString);
                    Toast.makeText(this, R.string.enter_numbers_only, 0);
                    return;
                }
            case MENU_DELETE /*1*/:
                String description = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceActivity.PREFS_MILAGE_DESCRIPTION, getString(R.string.mileage));
                values.put(InvoiceItem.DESCRIPTION, description);
                String startValueString = ((TextView) findViewById(R.id.start_value)).getText().toString();
                String endValueString = ((TextView) findViewById(R.id.end_value)).getText().toString();
                String rateString = ((TextView) findViewById(R.id.rate)).getText().toString();
                try {
                    int startValue = Integer.parseInt(startValueString);
                    int endValue = Integer.parseInt(endValueString);
                    values.put(InvoiceItem.VALUE, Integer.valueOf((endValue - startValue) * Integer.parseInt(rateString)));
                    values.put(InvoiceItem.EXTRAS, new StringBuilder(String.valueOf(startValue)).append("|").append(endValue).append("|").append("10").toString());
                    break;
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Error parsing mileage: " + startValueString + " | " + endValueString + " | " + rateString);
                    Toast.makeText(this, R.string.enter_digits_only, 0);
                    return;
                }
        }
        getContentResolver().insert(InvoiceItem.CONTENT_URI, values);
    }

    protected void setType(int type) {
        this.mTypePanel.removeAllViews();
        View typePanel = LayoutInflater.from(this).inflate(this.type_panels[type], this.mTypePanel);
        if (type == MENU_DELETE) {
            TextView setRate = (TextView) typePanel.findViewById(R.id.rate);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int defaultRate = -1;
            try {
                defaultRate = Integer.parseInt(prefs.getString(PreferenceActivity.PREFS_MILEAGE_RATE, "-1"));
            } catch (NumberFormatException e) {
            }
            if (defaultRate > 0) {
                setRate.setText(String.valueOf(defaultRate));
            }
            if (defaultRate <= 0 || prefs.getBoolean(PreferenceActivity.PREFS_SHOW_MILEAGE_RATE, true)) {
                setRate.setVisibility(0);
            } else {
                setRate.setVisibility(8);
            }
        }
    }
}
