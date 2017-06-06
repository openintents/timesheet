package org.openintents.timesheet.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openintents.timesheet.R;

public class JobListItemView extends LinearLayout {
    public static final int STATUS_BREAK = 4;
    public static final int STATUS_BREAK2 = 5;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_STARTED = 2;
    private static final String TAG = "UpdateListListItemView";
    Context mContext;
    private boolean isSyncItem;
    private TextView mInfo;
    private ImageView mStatus;
    private TextView mTitle;

    public JobListItemView(Context context) {
        super(context);
        this.isSyncItem = false;
        this.mContext = context;
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.jobslist_item, this, true);
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mInfo = (TextView) findViewById(R.id.info);
        this.mStatus = (ImageView) findViewById(R.id.status);
    }

    public void setTitle(String title) {
        this.mTitle.setText(title);
    }

    public void setInfo(String info) {
        this.mInfo.setText(info);
    }

    public void setIsSyncItem(boolean b) {
        this.isSyncItem = b;
    }

    public void setStatus(int status) {
        if (this.isSyncItem) {
            switch (status) {
                case STATUS_NEW /*1*/:
                    this.mStatus.setImageResource(R.drawable.ic_status_new_sync);
                    return;
                case STATUS_STARTED /*2*/:
                    this.mStatus.setImageResource(R.drawable.ic_status_work_sync);
                    return;
                case STATUS_FINISHED /*3*/:
                    this.mStatus.setImageResource(R.drawable.ic_status_ok_sync);
                    return;
                case STATUS_BREAK /*4*/:
                    this.mStatus.setImageResource(R.drawable.ic_status_sleep_sync);
                    return;
                case STATUS_BREAK2 /*5*/:
                    this.mStatus.setImageResource(R.drawable.ic_status_bed_sync);
                    return;
                default:
                    Log.e(TAG, "Unknown status " + status);
                    this.mStatus.setImageDrawable(null);
                    return;
            }
        }
        switch (status) {
            case STATUS_NEW /*1*/:
                this.mStatus.setImageResource(R.drawable.ic_new_item);
            case STATUS_STARTED /*2*/:
                this.mStatus.setImageResource(R.drawable.ic_status_work);
            case STATUS_FINISHED /*3*/:
                this.mStatus.setImageResource(R.drawable.ic_ok);
            case STATUS_BREAK /*4*/:
                this.mStatus.setImageResource(R.drawable.ic_status_sleep);
            case STATUS_BREAK2 /*5*/:
                this.mStatus.setImageResource(R.drawable.ic_status_bed);
            default:
                Log.e(TAG, "Unknown status " + status);
                this.mStatus.setImageDrawable(null);
        }
    }
}
