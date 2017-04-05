package org.openintents.timesheet;

import android.content.res.Resources;
import android.preference.PreferenceManager;
import org.openintents.distribution.LicenseChecker;
import org.openintents.distribution.LicensePackage;
import org.openintents.distribution.LicensedApplication;

public class Application extends android.app.Application implements LicensedApplication {
    public static final int APPLICATION_VARIANT_ANDROID_MARKET = 1;
    public static final int APPLICATION_VARIANT_DEFAULT = 0;
    public static final int APPLICATION_VARIANT_PDASSI = 2;
    public static int mApplicationVariant;
    public static String mApplicationVariantVersionSuffix;
    private boolean mChecked;
    private boolean mValid;

    public Application() {
        this.mChecked = false;
        this.mValid = false;
    }

    static {
        mApplicationVariant = APPLICATION_VARIANT_DEFAULT;
        mApplicationVariantVersionSuffix = "";
    }

    public void onCreate() {
        super.onCreate();
        Resources res = getResources();
        int id = res.getIdentifier("application_variant", "integer", getPackageName());
        if (id != 0) {
            mApplicationVariant = res.getInteger(id);
        }
        id = res.getIdentifier("application_variant_version_suffix", "string", getPackageName());
        if (id != 0) {
            mApplicationVariantVersionSuffix = res.getString(id);
        }
    }

    public static String getVersionSuffix() {
        return mApplicationVariantVersionSuffix;
    }

    public boolean isLicenseValid() {
        if (LicensePackage.checkLicense(getApplicationContext())) {
            return true;
        }
        if (!this.mChecked) {
            this.mValid = LicenseChecker.checkLicense(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(PreferenceActivity.PREFS_LICENSE_DEVELOPER, null));
            this.mChecked = true;
        }
        return this.mValid;
    }

    public void newLicense() {
        this.mChecked = false;
    }
}
