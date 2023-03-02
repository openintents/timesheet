package org.openintents.timesheet;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String PREFS_ASK_IF_FILE_EXISTS = "ask_if_file_exists";
    public static final String PREFS_BILLING_UNIT = "billing_unit";
    public static final String PREFS_BILLING_UNIT_DEFAULT = "60000";
    public static final String PREFS_CALENDAR_ID = "calendar_id";
    public static final String PREFS_EXPORT_CALENDAR = "export_calendar";
    public static final String PREFS_EXPORT_COMPLETED_ONLY = "export_complete_only";
    public static final String PREFS_EXPORT_DATE_FORMAT = "export_date_format";
    public static final String PREFS_EXPORT_DIRECTORY = "export_directory";
    public static final String PREFS_EXPORT_FILENAME = "export_filename";
    public static final String PREFS_EXPORT_REPLACE_BR = "export_replace_br";
    public static final String PREFS_EXPORT_SINGLE_FILE = "export_single_file";
    public static final String PREFS_EXPORT_TIME_FORMAT = "export_time_format";
    public static final String PREFS_EXPORT_TOTALS = "export_totals";
    //public static final String PREFS_EXTENSIONS_MARKET = "preference_extensions_market";
    //public static final String PREFS_LICENSE_DEVELOPER = "org.openintents.lickey";
    //public static final String PREFS_LICENSE_MARKET = "preference_license_market";
    //public static final String PREFS_LICENSE_PDASSI = "preference_license_pdassi";
    public static final String PREFS_MILAGE_DESCRIPTION = "mileage_description";
    public static final String PREFS_MILEAGE_RATE = "mileage_rate";
    public static final String PREFS_OMIT_TEMPLATES = "omit_templates";
    public static final String PREFS_REMINDER = "reminder";
    public static final String PREFS_SHOW_MILEAGE_RATE = "show_mileage_rate";
    public static final String PREFS_SHOW_NOTIFICATION = "show_notification";
    public static final String PREFS_START_JOBS_IMMEDIATELY = "start_jobs_immediately";
    //private static final String PREFS_LICENSE_CATEGORY = "preference_screen_license";

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREFS_SHOW_NOTIFICATION)) {
            Timesheet.updateNotification(this, true);
        }
    }
}
