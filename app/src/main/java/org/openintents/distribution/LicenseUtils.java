package org.openintents.distribution;

import android.app.Activity;
import org.openintents.timesheet.Application;
import org.openintents.timesheet.R;

public class LicenseUtils {
    public static void modifyTitle(Activity activity) {
        if (!((Application) activity.getApplication()).isLicenseValid()) {
            activity.setTitle(new StringBuilder(String.valueOf(activity.getTitle().toString())).append(activity.getString(R.string.trial_suffix)).toString());
        }
    }
}
