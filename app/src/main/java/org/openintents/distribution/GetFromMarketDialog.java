package org.openintents.distribution;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.openintents.timesheet.R;

public class GetFromMarketDialog extends AlertDialog implements OnClickListener {
    private static final String TAG = "StartSaveActivity";
    Context mContext;
    int mDeveloperUri;
    int mMarketUri;

    public GetFromMarketDialog(Context context, int message, int buttontext, int market_uri, int developer_uri) {
        super(context);
        this.mContext = context;
        this.mMarketUri = market_uri;
        this.mDeveloperUri = developer_uri;
        setMessage(this.mContext.getText(message));
        setButton(this.mContext.getText(buttontext), this);
    }

    public static void startSaveActivity(Context context, Intent intent, Intent intent2) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Error starting activity.", e);
            try {
                context.startActivity(intent2);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(context, R.string.update_error, 0).show();
                Log.e(TAG, "Error starting second activity.", e2);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            Uri uri = Uri.parse(this.mContext.getString(this.mMarketUri));
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(uri);
            uri = Uri.parse(this.mContext.getString(this.mDeveloperUri));
            Intent intent2 = new Intent("android.intent.action.VIEW");
            intent2.setData(uri);
            startSaveActivity(this.mContext, intent, intent2);
        }
    }
}
