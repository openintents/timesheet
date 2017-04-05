package org.openintents.util;

import android.content.Context;
import android.content.Intent;

public class IntentUtils {
    public static boolean isIntentAvailable(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 65536).size() > 0;
    }
}
