package org.openintents.distribution;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import org.openintents.intents.AboutMiniIntents;
import org.openintents.timesheet.R;
import org.openintents.timesheet.Timesheet;
import org.openintents.util.IntentUtils;
import org.openintents.util.VersionUtils;

public class AboutDialog extends GetFromMarketDialog {
    private static final String TAG = "About";

    public AboutDialog(Context context) {
        super(context, R.string.aboutapp_not_available, R.string.aboutapp_get, R.string.aboutapp_market_uri, R.string.aboutapp_developer_uri);
        String version = VersionUtils.getVersionNumber(context);
        setTitle(VersionUtils.getApplicationName(context));
        setMessage(context.getString(R.string.aboutapp_not_available, new Object[]{version}));
    }

    public static void showDialogOrStartActivity(Activity activity, int dialogId) {
        Intent intent = new Intent(Timesheet.ACTION_SHOW_ABOUT_DIALOG);
        intent.putExtra(AboutMiniIntents.EXTRA_PACKAGE_NAME, activity.getPackageName());
        if (IntentUtils.isIntentAvailable(activity, intent)) {
            activity.startActivity(intent);
        } else {
            activity.showDialog(dialogId);
        }
    }
}
