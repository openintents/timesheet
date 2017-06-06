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

    static {
        mApplicationVariant = APPLICATION_VARIANT_DEFAULT;
        mApplicationVariantVersionSuffix = "";
    }

    private boolean mChecked;
    private boolean mValid;

    public Application() {
        this.mChecked = false;
        this.mValid = false;
    }

    public static String getVersionSuffix() {
        return mApplicationVariantVersionSuffix;
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

    public boolean isLicenseValid() {
        return true;
    }

    public void newLicense() {
        this.mChecked = true;
    }
}
