package org.openintents.timesheet.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openintents.timesheet.R;

public class JobListItemView extends LinearLayout {
    public static final int STATUS_NEW = 1;
    public static final int STATUS_STARTED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_BREAK = 4;
    public static final int STATUS_BREAK2 = 5;
    private static final String TAG = "UpdateListListItemView";
    Context mContext;
    private boolean isSyncItem;
    private TextView mInfo;
    private ImageView mStatus;
    private TextView mTitle;

    public JobListItemView(Context context) {
        super(context);
        mContext = context;

        // inflate rating
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(
                R.layout.jobslist_item, this, true);

        mTitle = (TextView) findViewById(R.id.title);
        mInfo = (TextView) findViewById(R.id.info);
        mStatus = (ImageView) findViewById(R.id.status);
    }

    /**
     * Convenience method to set the title of a NewsView
     */
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setInfo(String info) {
        mInfo.setText(info);
    }

    public void setIsSyncItem(boolean b) {
        isSyncItem = b;
    }

    public void setStatus(int status) {
        if (isSyncItem) {
            switch (status) {
                case STATUS_NEW:
                    mStatus.setImageResource(R.drawable.ic_status_new_sync);
                    break;
                case STATUS_STARTED:
                    mStatus.setImageResource(R.drawable.ic_status_work_sync);
                    break;
                case STATUS_FINISHED:
                    mStatus.setImageResource(R.drawable.ic_status_ok_sync);
                    break;
                case STATUS_BREAK:
                    mStatus.setImageResource(R.drawable.ic_status_sleep_sync);
                    break;
                case STATUS_BREAK2:
                    mStatus.setImageResource(R.drawable.ic_status_bed_sync);
                    break;
                default:
                    Log.e(TAG, "Unknown status " + status);
                    mStatus.setImageDrawable(null);
            }


        } else {

            switch (status) {
                case STATUS_NEW:
                    mStatus.setImageResource(R.drawable.ic_new_item);
                    break;
                case STATUS_STARTED:
                    mStatus.setImageResource(R.drawable.ic_status_work);
                    break;
                case STATUS_FINISHED:
                    mStatus.setImageResource(R.drawable.ic_ok);
                    break;
                case STATUS_BREAK:
                    mStatus.setImageResource(R.drawable.ic_status_sleep);
                    break;
                case STATUS_BREAK2:
                    mStatus.setImageResource(R.drawable.ic_status_bed);
                    break;
                default:
                    Log.e(TAG, "Unknown status " + status);
                    mStatus.setImageDrawable(null);
            }
        }

    }
}
