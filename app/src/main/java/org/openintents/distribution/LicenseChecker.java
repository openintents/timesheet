package org.openintents.distribution;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;

public class LicenseChecker {
    public static final String PREFS_LICENSE_KEY = "org.openintents.lickey";
    private static final String APP_CODE = "org.openintents.appcode";
    private static final String LIC_CODE = "org.openintents.liccode";

    public static boolean checkLicense(Context context, String license) {
        if (license == null || license.length() != 19) {
            return false;
        }
        String barelicense = Decrypt.decrypt(license.substring(0, 4) + license.substring(5, 9) + license.substring(10, 14) + license.substring(15, 19));
        String appcode = barelicense.substring(4, 6);
        String liccode = barelicense.substring(6, 8);
        String ctmcode = barelicense.substring(8, 16);
        String hashcode = barelicense.substring(0, 4);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (!appcode.equals(String.format("%02d", new Object[]{appInfo.metaData.get(APP_CODE)}))) {
                return false;
            }
            if (!liccode.equals(String.format("%02d", new Object[]{appInfo.metaData.get(LIC_CODE)}))) {
                return false;
            }
            if (!getCtmCode(context).equals(ctmcode)) {
                return false;
            }
            String licenseHash = String.format("%04d", new Object[]{Integer.valueOf(new StringBuilder(String.valueOf(appcode)).append(liccode).append(ctmcode).toString().hashCode())});
            if (licenseHash.substring(licenseHash.length() - 4).equals(hashcode)) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final void getUserAccounts(Activity inActivity, int inRequestCode, boolean inRequireGoogle) {
        try {
            Class.forName("com.google.android.googleapps.GoogleLoginServiceHelper").getDeclaredMethod("getAccount", new Class[]{Activity.class, Integer.TYPE, Boolean.TYPE}).invoke(null, new Object[]{inActivity, Integer.valueOf(inRequestCode), Boolean.valueOf(inRequireGoogle)});
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (ClassNotFoundException e3) {
            e3.printStackTrace();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
        } catch (IllegalAccessException e5) {
            e5.printStackTrace();
        } catch (InvocationTargetException e6) {
            e6.printStackTrace();
        }
    }

    public static String getCtmCode(Context context) {
        String id = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
        id = String.format("%08d", new Object[]{Integer.valueOf(id.hashCode())});
        return id.substring(id.length() - 8);
    }

    public static String getAppCode(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            return String.format("%02d", new Object[]{appInfo.metaData.get(APP_CODE)});
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static String getLicCode(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            return String.format("%02d", new Object[]{appInfo.metaData.get(LIC_CODE)});
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
