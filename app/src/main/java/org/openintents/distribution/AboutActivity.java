package org.openintents.distribution;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.openintents.timesheet.Application;
import org.openintents.timesheet.R;

public class AboutActivity extends Activity {
    private static final String TAG = "About";

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(16973835);
        setContentView(R.layout.about);
        String version = getVersionNumber() + Application.getVersionSuffix();
        setTitle(getString(R.string.about_title, new Object[]{getApplicationName()}));
        ((TextView) findViewById(R.id.text)).setText(getString(R.string.about_text, new Object[]{version}));
    }

    private String getVersionNumber() {
        String version = "?";
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
            return version;
        }
    }

    private String getApplicationName() {
        String name = "?";
        try {
            name = getString(getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.labelRes);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return name;
    }
}
