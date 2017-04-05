package org.openintents.distribution;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openintents.timesheet.R;

public class EulaActivity extends Activity {
    private static final String EXTRA_LAUNCH_ACTIVITY_CLASS = "org.openintents.extra.launch_activity_class";
    private static final String EXTRA_LAUNCH_ACTIVITY_PACKAGE = "org.openintents.extra.launch_activity_package";
    static final String PREFERENCES_EULA_ACCEPTED = "eula_accepted";
    private static final String TAG = "EulaActivity";
    private Button mAgree;
    private Button mDisagree;
    private String mLaunchClass;
    private String mLaunchPackage;

    /* renamed from: org.openintents.distribution.EulaActivity.1 */
    class C00001 implements OnClickListener {
        C00001() {
        }

        public void onClick(View view) {
            EulaActivity.this.acceptEula();
        }
    }

    /* renamed from: org.openintents.distribution.EulaActivity.2 */
    class C00012 implements OnClickListener {
        C00012() {
        }

        public void onClick(View view) {
            EulaActivity.this.refuseEula();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.eula);
        Bundle b = getIntent().getExtras();
        this.mLaunchPackage = b.getString(EXTRA_LAUNCH_ACTIVITY_PACKAGE);
        this.mLaunchClass = b.getString(EXTRA_LAUNCH_ACTIVITY_CLASS);
        this.mAgree = (Button) findViewById(R.id.button1);
        this.mAgree.setOnClickListener(new C00001());
        this.mDisagree = (Button) findViewById(R.id.button2);
        this.mDisagree.setOnClickListener(new C00012());
        ((TextView) findViewById(R.id.text)).setText(readLicenseFromRawResource(R.raw.license));
    }

    public void acceptEula() {
        Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
        e.putBoolean(PREFERENCES_EULA_ACCEPTED, true);
        e.commit();
        Intent i = new Intent();
        i.setClassName(this.mLaunchPackage, this.mLaunchClass);
        startActivity(i);
        finish();
    }

    public void refuseEula() {
        Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
        e.putBoolean(PREFERENCES_EULA_ACCEPTED, false);
        e.commit();
        finish();
    }

    public static boolean checkEula(Activity activity) {
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(PREFERENCES_EULA_ACCEPTED, false)) {
            Log.i(TAG, "Eula has been accepted.");
            return true;
        }
        Log.i(TAG, "Eula has not been accepted yet.");
        Intent i = new Intent(activity, EulaActivity.class);
        ComponentName ci = activity.getComponentName();
        Log.d(TAG, "Local package name: " + ci.getPackageName());
        Log.d(TAG, "Local class name: " + ci.getClassName());
        i.putExtra(EXTRA_LAUNCH_ACTIVITY_PACKAGE, ci.getPackageName());
        i.putExtra(EXTRA_LAUNCH_ACTIVITY_CLASS, ci.getClassName());
        activity.startActivity(i);
        activity.finish();
        return false;
    }

    private String readLicenseFromRawResource(int resourceid) {
        String license = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(resourceid)));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return sb.toString();
            }
            if (TextUtils.isEmpty(line)) {
                sb.append("\n\n");
            } else {
                try {
                    sb.append(line);
                    sb.append(" ");
                } catch (IOException e) {
                    e.printStackTrace();
                    return license;
                }
            }
        }
    }
}
