package org.openintents.timesheet.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet.Job;
import org.openintents.timesheet.TimesheetIntent;
import org.openintents.util.DateTimeFormater;

public class InsertJobActivity extends Activity {
    private String mCustomer;
    private int mHourlyRate;
    private String mNote;
    private long mPlannedDate;
    private long mPlannedDuration;

    /* renamed from: org.openintents.timesheet.activity.InsertJobActivity.1 */
    class C00091 implements OnClickListener {
        C00091() {
        }

        public void onClick(View view) {
            InsertJobActivity.this.insertJob();
            InsertJobActivity.this.finish();
        }
    }

    /* renamed from: org.openintents.timesheet.activity.InsertJobActivity.2 */
    class C00102 implements OnClickListener {
        C00102() {
        }

        public void onClick(View view) {
            InsertJobActivity.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insert_job);
        this.mCustomer = getIntent().getStringExtra(TimesheetIntent.EXTRA_CUSTOMER);
        this.mPlannedDate = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DATE, 0);
        this.mPlannedDuration = getIntent().getLongExtra(TimesheetIntent.EXTRA_PLANNED_DURATION, 0);
        this.mHourlyRate = getIntent().getIntExtra(TimesheetIntent.EXTRA_HOURLY_RATE, -1);
        this.mNote = getIntent().getStringExtra(TimesheetIntent.EXTRA_NOTE);
        if (TextUtils.isEmpty(this.mCustomer)) {
            Toast.makeText(this, R.string.job_denied, 0).show();
            finish();
        }
        TextView text = (TextView) findViewById(R.id.text);
        if (this.mPlannedDate > 0) {
            Date date = new Date(this.mPlannedDate);
            if (this.mPlannedDuration > 0) {
                text.setText(getString(R.string.insert_job_full, new Object[]{DateTimeFormater.mDateFormater.format(date), DateTimeFormater.mTimeFormater.format(date), DateTimeFormater.mTimeFormater.format(new Date(this.mPlannedDuration))}));
            } else {
                text.setText(getString(R.string.insert_job_no_duration, new Object[]{DateTimeFormater.mDateFormater.format(date), DateTimeFormater.mTimeFormater.format(date)}));
            }
        } else if (this.mPlannedDuration > 0) {
            text.setText(getString(R.string.insert_job_no_date, new Object[]{DateTimeFormater.mTimeFormater.format(new Date(this.mPlannedDuration))}));
        } else {
            text.setText(getString(R.string.insert_job));
        }
        ((TextView) findViewById(R.id.customer)).setText(this.mCustomer);
        text = (TextView) findViewById(R.id.hourly_rate);
        if (this.mHourlyRate >= 0) {
            text.setText(getString(R.string.hourly_rate_info, new Object[]{Integer.valueOf(this.mHourlyRate)}));
            text.setVisibility(0);
        } else {
            text.setVisibility(8);
        }
        ((Button) findViewById(R.id.accept)).setOnClickListener(new C00091());
        ((Button) findViewById(R.id.deny)).setOnClickListener(new C00102());
    }

    protected void insertJob() {
        getIntent().setClass(this, JobActivity.class);
        getIntent().setAction("android.intent.action.INSERT");
        getIntent().setData(Job.CONTENT_URI);
        startActivity(getIntent());
    }
}
