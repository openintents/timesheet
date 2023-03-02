/*
 * Copyright (C) 2007-2017 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.distribution;

// Version Nov 12, 2008
// Version Nov 26, 2008: getVersionSuffix.

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.openintents.timesheet.R;

/**
 * About dialog
 *
 * @author Peli
 */
public class AboutActivity extends Activity {
    private static final String TAG = "About";

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.about);
        String version = getVersionNumber();
        setTitle(getString(R.string.about_title, new Object[]{getApplicationName()}));
        ((TextView) findViewById(R.id.text)).setText(getString(R.string.about_text, new Object[]{version}));
    }

    /**
     * Get current version number.
     *
     * @return
     */
    private String getVersionNumber() {
        String version = "?";
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
            return version;
        }
    }

    /**
     * Get application name.
     *
     * @return
     */
    private String getApplicationName() {
        String name = "?";
        try {
            name = getString(getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.labelRes);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        ;
        return name;
    }

}
