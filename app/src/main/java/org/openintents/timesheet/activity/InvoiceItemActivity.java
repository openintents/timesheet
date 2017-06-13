package org.openintents.timesheet.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.timesheet.PreferenceActivity;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.InvoiceItem;

public class InvoiceItemActivity extends AppCompatListActivity {
    public static final int COLUMN_INDEX_TYPE = 1;
    public static final int COLUMN_INDEX_DESCRIPTION = 2;
    public static final int COLUMN_INDEX_VALUE = 3;
    public static final int COLUMN_INDEX_EXTRAS = 4;
    private static final int MENU_DELETE = Menu.FIRST;
    private static final String TAG = "InvoiceItemActivity";
    private static final int[] type_panels = new int[]{R.layout.type_panel_general,
            R.layout.type_panel_mileage};
    private static final String[] mProjection = new String[]{InvoiceItem._ID,
            InvoiceItem.TYPE, // 1
            InvoiceItem.DESCRIPTION, // 2
            InvoiceItem.VALUE, // 3
            InvoiceItem.EXTRAS // 4
    };
    private ArrayAdapter<CharSequence> typeAdapter;
    private Spinner mTypeSpinner;
    private FrameLayout mTypePanel;
    private long mJobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_item);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner);
        mTypeSpinner.setOnItemSelectedListener(new C00111());

        typeAdapter = ArrayAdapter.createFromResource(this, R.array.invoice_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(typeAdapter);

        mTypePanel = (FrameLayout) findViewById(R.id.panel_frame);
        mJobId = getIntent().getLongExtra("jobid", -1L);
        setType(0);
        mTypeSpinner.setVisibility(View.GONE);
        findViewById(R.id.add_button).setOnClickListener(new C00122());
        getListView().setEmptyView(findViewById(R.id.empty));
        updateListAdapter();

        registerForContextMenu(getListView());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateListAdapter() {
        Cursor c = getContentResolver().query(InvoiceItem.CONTENT_URI,
                mProjection, InvoiceItem.JOB_ID + " = ?",
                new String[]{String.valueOf(mJobId)}, null);
        CursorAdapter adapter = new InvoiceItemCursorAdapter(this, c);
        setListAdapter(adapter);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.add(0, MENU_DELETE, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menu = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_DELETE /*1*/:
                getContentResolver().delete(Uri.withAppendedPath(InvoiceItem.CONTENT_URI, String.valueOf(menu.id)), null, null);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.setAction(Intent.ACTION_EDIT);
                upIntent.setData(getIntent().getData());
                NavUtils.navigateUpTo(this, upIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void addInvoiceItem() {
        int type = mTypeSpinner.getSelectedItemPosition();
        ContentValues values = new ContentValues();
        values.put(InvoiceItem.TYPE, type);
        values.put(InvoiceItem.JOB_ID, mJobId);
        switch (type) {
            case 0:
                values.put(InvoiceItem.DESCRIPTION, ((TextView) findViewById(R.id.description)).getText().toString());
                String valueString = ((TextView) findViewById(R.id.value)).getText().toString();
                try {
                    values.put(InvoiceItem.VALUE, (long) (100.0f * Float.parseFloat(valueString)));
                    break;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing expense value: " + valueString);
                    Toast.makeText(this, R.string.enter_numbers_only, Toast.LENGTH_SHORT).show();
                    return;
                }
            case 1:
                String description = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceActivity.PREFS_MILAGE_DESCRIPTION, getString(R.string.mileage));
                values.put(InvoiceItem.DESCRIPTION, description);
                String startValueString = ((TextView) findViewById(R.id.start_value)).getText().toString();
                String endValueString = ((TextView) findViewById(R.id.end_value)).getText().toString();
                String rateString = ((TextView) findViewById(R.id.rate)).getText().toString();
                try {
                    int startValue = Integer.parseInt(startValueString);
                    int endValue = Integer.parseInt(endValueString);
                    values.put(InvoiceItem.VALUE, (endValue - startValue) * Integer.parseInt(rateString));
                    values.put(InvoiceItem.EXTRAS, String.valueOf(startValue) + "|" + endValue + "|" + "10");
                    break;
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Error parsing numbers: " + startValueString + " | " + endValueString + " | " + rateString);
                    Toast.makeText(this, R.string.enter_digits_only, Toast.LENGTH_SHORT).show();
                    return;
                }
        }
        getContentResolver().insert(InvoiceItem.CONTENT_URI, values);
    }

    protected void setType(int type) {
        mTypePanel.removeAllViews();
        View typePanel = LayoutInflater.from(this).inflate(type_panels[type], mTypePanel);
        if (type == 1) {
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
                setRate.setVisibility(View.VISIBLE);
            } else {
                setRate.setVisibility(View.GONE);
            }
        }
    }

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
}
