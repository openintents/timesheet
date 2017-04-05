package org.openintents.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class VersionUtils {
    private static final String TAG = "VersionUtils";

    public static String getVersionNumber(Context context) {
        String version = "?";
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
            return version;
        }
    }

    public static String getApplicationName(Context context) {
        String name = "?";
        try {
            name = context.getString(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.labelRes);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return name;
    }
}
