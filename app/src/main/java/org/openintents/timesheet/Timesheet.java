package org.openintents.timesheet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import org.openintents.timesheet.activity.JobListActivity;

import java.util.ArrayList;

import static org.openintents.timesheet.R.id.time;

public class Timesheet {
    public static final String ACTION_SHOW_ABOUT_DIALOG = "org.openintents.action.SHOW_ABOUT_DIALOG";
    public static final String AUTHORITY = "org.openintents.timesheet";
    public static final String CALENDAR_AUTHORITY_1 = "calendar";
    public static final String CALENDAR_AUTHORITY_2 = "com.android.calendar";
    public static final double HOUR_FACTOR = 2.7777777777E-7d;
    public static final double RATE_FACTOR = 0.01d;
    public static final String _TAG = "Timesheet";

    public static void updateNotification(Context context, boolean force) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceActivity.PREFS_SHOW_NOTIFICATION, false)) {
            Cursor cursor = context.getContentResolver().query(Job.CONTENT_URI, new String[]{Reminders._ID}, "start_date is not null and end_date is null and last_start_break2 is null", null, null);
            int jobsInProgress = cursor.getCount();
            cursor.close();
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (jobsInProgress > 0) {
                String text;
                if (jobsInProgress == 1) {
                    text = context.getString(R.string.job_in_progress);
                } else {
                    text = context.getString(R.string.jobs_in_progress, new Object[]{String.valueOf(jobsInProgress)});
                }


                Intent intent = new Intent(context, JobListActivity.class);
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.setAction("android.intent.action.MAIN");
                PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context);
                Notification notification = builder.setContentIntent(pi)
                        .setSmallIcon(R.drawable.icon_timesheet)
                        .setTicker(null)
                        .setWhen(time)
                        .setAutoCancel(true)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(text).build();
                notification.flags = 2;
                nm.notify(R.string.jobs_in_progress, notification);
                return;
            }
            nm.cancel(R.string.jobs_in_progress);
        } else if (force) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(R.string.jobs_in_progress);
        }
    }

    public interface InvoiceItem extends BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.timesheet/invoiceitems");
        public static final String CREATED_DATE = "created";
        public static final String DEFAULT_SORT_ORDER = "description";
        public static final String DESCRIPTION = "description";
        public static final String EXTRAS = "extras";
        public static final String JOB_ID = "job_id";
        public static final String MODIFIED_DATE = "modified";
        public static final String TYPE = "type";
        public static final String VALUE = "value";
    }

    public static final class CalendarApp {
        public static final String EVENT_BEGIN_TIME = "beginTime";
        public static final String EVENT_END_TIME = "endTime";

        public static ContentValues createContentValues(String title, long startMillis, long endMillis, boolean isAllDay, String location, String description, long calendarId, int transparency, int visibility, boolean hasAlarm) {
            ContentValues values = new ContentValues();
            values.put(Events.EVENT_TIMEZONE, "UTC");
            values.put(Events.CALENDAR_ID, calendarId);
            values.put(Events.TITLE, title);
            values.put(Events.ALL_DAY, isAllDay ? 1 : 0);
            values.put(Events.DTSTART, startMillis);
            values.put(Events.DTEND, endMillis);
            values.put(Events.DESCRIPTION, description);
            values.put(Events.EVENT_LOCATION, location);
            values.put(Events.TRANSPARENCY, transparency);
            values.put(Events.HAS_ALARM, hasAlarm);
            if (visibility > 0) {
                visibility++;
            }
            values.put(Events.VISIBILITY, visibility);
            return values;
        }

        public static ContentValues createContentValues2(String title, long startMillis, long endMillis, boolean isAllDay, String location, String description, long calendarId, boolean hasAlarm) {
            ContentValues values = new ContentValues();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");
                values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.ALL_DAY, isAllDay ? 1 : 0);
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.DESCRIPTION, description);
                values.put(CalendarContract.Events.EVENT_LOCATION, location);
                values.put(CalendarContract.Events.HAS_ALARM, hasAlarm);
            }
            return values;
        }

        static void saveReminders(ContentResolver cr, int calendarAuthority, long eventId, ArrayList<Integer> reminderMinutes, ArrayList<Integer> originalMinutes) {
            if (!reminderMinutes.equals(originalMinutes)) {
                Uri uri;
                String where = "event_id=?";
                String[] args = new String[]{Long.toString(eventId)};
                if (calendarAuthority == 1) {
                    cr.delete(Reminders.CONTENT_URI_1, where, args);
                } else if (calendarAuthority == 2) {
                    cr.delete(Reminders.CONTENT_URI_2, where, args);
                } else {
                    return;
                }
                ContentValues values = new ContentValues();
                int len = reminderMinutes.size();
                values.put(Events.HAS_ALARM, Integer.valueOf(len > 0 ? 1 : 0));
                if (calendarAuthority == 1) {
                    uri = ContentUris.withAppendedId(Events.CONTENT_URI_1, eventId);
                } else {
                    uri = ContentUris.withAppendedId(Events.CONTENT_URI_2, eventId);
                }
                cr.update(uri, values, null, null);
                for (int i = 0; i < len; i++) {
                    int minutes = ((Integer) reminderMinutes.get(i)).intValue();
                    values.clear();
                    values.put(Reminders.MINUTES, Integer.valueOf(minutes));
                    values.put(Reminders.METHOD, Integer.valueOf(1));
                    values.put(Reminders.EVENT_ID, Long.valueOf(eventId));
                    if (calendarAuthority == 1) {
                        cr.insert(Reminders.CONTENT_URI_1, values);
                    } else {
                        cr.insert(Reminders.CONTENT_URI_2, values);
                    }
                }
            }
        }

        public static void createReminder(ContentResolver cr, long eventId, int minutes, int calendarAuthority) {
            ContentValues values = new ContentValues();
            values.put(Reminders.MINUTES, Integer.valueOf(minutes));
            values.put(Reminders.METHOD, Integer.valueOf(1));
            values.put(Reminders.EVENT_ID, Long.valueOf(eventId));
            if (calendarAuthority == 1) {
                cr.insert(Reminders.CONTENT_URI_1, values);
            } else if (calendarAuthority == 2) {
                cr.insert(Reminders.CONTENT_URI_2, values);
            }
        }

        public static String insertCalendarEvent(Context context, String uri, long startDate, long duration, String customer, String text, int calendarAuthority) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String calString = prefs.getString(Events.CALENDAR_ID, Job.TYPE_MILEAGE);
            if (TextUtils.isEmpty(calString)) {
                calString = Job.TYPE_MILEAGE;
            }
            long calendarId = Long.parseLong(calString);
            int minutes = Integer.parseInt(prefs.getString(PreferenceActivity.PREFS_REMINDER, "0"));
            Uri eventUri = null;
            if (calendarAuthority == 1) {
                try {
                    ContentValues values = createContentValues(customer, startDate, startDate + duration, false, null, text, calendarId, 0, 0, minutes > 0);
                    eventUri = context.getContentResolver().insert(Events.CONTENT_URI_1, values);
                } catch (Exception e) {
                    Toast.makeText(context, R.string.calendar_missing, Toast.LENGTH_SHORT).show();
                }
            } else if (calendarAuthority == 2) {
                ContentValues values = createContentValues2(customer, startDate, startDate + duration, false, null, text, calendarId, minutes > 0);
                eventUri = context.getContentResolver().insert(Events.CONTENT_URI_2, values);
            } else {
                throw new RuntimeException();
            }
            if (eventUri == null) {
                return null;
            }
            uri = eventUri.toString();
            createReminder(context.getContentResolver(), Long.parseLong(eventUri.getLastPathSegment()), minutes, calendarAuthority);
            return uri;
        }

        public static String setOrUpdateCalendarEvent(Context context, String uri, long millis, long duration, String customer, String text, int calendarAuthority) {
            if (TextUtils.isEmpty(uri)) {
                return insertCalendarEvent(context, uri, millis, duration, customer, text, calendarAuthority);
            }
            ContentValues values = new ContentValues();
            values.put(Job.TITLE, customer);
            values.put(InvoiceItem.DESCRIPTION, text);
            values.put(Events.DTSTART, Long.valueOf(millis));
            values.put(Events.DTEND, Long.valueOf(millis + duration));
            if (context.getContentResolver().update(Uri.parse(uri), values, null, null) == 0) {
                return insertCalendarEvent(context, uri, millis, duration, customer, text, calendarAuthority);
            }
            return uri;
        }
    }

    public static class Calendars {
        public static final String ACCESS_LEVEL = "access_level";
        public static final Uri CONTENT_URI_1 = Uri.parse("content://calendar/calendars");
        public static final Uri CONTENT_URI_2 = Uri.parse("content://com.android.calendar/calendars");
        public static final int CONTRIBUTOR_ACCESS = 500;
        public static final String DISPLAY_NAME = "displayName";
        public static final String TIMEZONE = "timezone";
        public static final String _ID = "_id";
        public static final String[] NAME_PROJECTION = new String[]{_ID, DISPLAY_NAME};
        public static final String[] NAME_PROJECTION2 = new String[]{_ID, "calendar_displayName"};
    }

    public static class Customer implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.timesheet/customers");
        public static final String CREATED_DATE = "created";
        public static final String CUSTOMER = "customer";
        public static final String DEFAULT_SORT_ORDER = "customer";
        public static final String HOURLY_RATE = "hourly_rate";
        public static final String JOB_COUNT = "job_count";
        public static final String MODIFIED_DATE = "modified";
        public static final String TAX_RATE = "tax_rate";
    }

    public static class Events {
        public static final String ALL_DAY = "allDay";
        public static final String CALENDAR_ID = "calendar_id";
        public static final Uri CONTENT_URI_1 = Uri.parse("content://calendar/events");
        public static final Uri CONTENT_URI_2 = Uri.parse("content://com.android.calendar/events");
        public static final String DESCRIPTION = "description";
        public static final String DTEND = "dtend";
        public static final String DTSTART = "dtstart";
        public static final String DURATION = "duration";
        public static final String EVENT_LOCATION = "eventLocation";
        public static final String EVENT_TIMEZONE = "eventTimezone";
        public static final String HAS_ALARM = "hasAlarm";
        public static final String RRULE = "rrule";
        public static final String TITLE = "title";
        public static final String TRANSPARENCY = "transparency";
        public static final String VISIBILITY = "visibility";
        public static final String _ID = "_id";
        public static String _SYNC_ID;
    }

    public static class Job implements BaseColumns {
        public static final String BREAK2_COUNT = "break2_count";
        public static final String BREAK2_DURATION = "break2_duration";
        public static final String BREAK_DURATION = "break_duration";
        public static final String CALENDAR_REF = "calendar_ref";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openintents.timesheet.job";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openintents.timesheet.job";
        public static final Uri CONTENT_URI;
        public static final String CREATED_DATE = "created";
        public static final String CUSTOMER = "customer";
        public static final String CUSTOMER_REF = "customer_ref";
        public static final String DEFAULT_SORT_ORDER = "last_start_break DESC, last_start_break2 DESC, start_date DESC";
        public static final String DELEGATEE_REF = "delegatee_ref";
        public static final String END_DATE = "end_date";
        public static final String END_LONG = "end_long";
        public static final String EXTERNAL_REF = "external_ref";
        public static final String EXTERNAL_SYSTEM = "external_system";
        public static final String EXTRAS_TOTAL = "extras_total";
        public static final String HOURLY_RATE = "hourly_rate";
        public static final String HOURLY_RATE2 = "hourly_rate2";
        public static final String HOURLY_RATE2_START = "hourly_rate2_Start";
        public static final String HOURLY_RATE3 = "hourly_rate3";
        public static final String HOURLY_RATE3_START = "hourly_rate3_Start";
        public static final String LAST_START_BREAK = "last_start_break";
        public static final String LAST_START_BREAK2 = "last_start_break2";
        public static final String MODIFIED_DATE = "modified";
        public static final String NOTE = "note";
        public static final String NOTES_REF = "notes_ref";
        public static final String PARENT_ID = "parent_id";
        public static final String PLANNED_DATE = "planned_date";
        public static final String PLANNED_DURATION = "planned_duration";
        public static final String RATE_LONG = "rate_long";
        public static final String START_DATE = "start_date";
        public static final String START_LONG = "start_long";
        public static final String STATUS = "status";
        public static final String TAX_RATE = "tax_rate";
        public static final String TITLE = "title";
        public static final String TOTAL_LONG = "total_long";
        public static final String TYPE = "type";
        public static final String TYPE_EXPENSE = "2";
        public static final String TYPE_MILEAGE = "1";

        static {
            CONTENT_URI = Uri.parse("content://org.openintents.timesheet/jobs");
        }
    }

    public static class Reminders {
        public static final Uri CONTENT_URI_1;
        public static final Uri CONTENT_URI_2;
        public static final String EVENT_ID = "event_id";
        public static final String METHOD = "method";
        public static final int METHOD_ALERT = 1;
        public static final int METHOD_DEFAULT = 0;
        public static final String MINUTES = "minutes";
        public static final String _ID = "_id";

        static {
            CONTENT_URI_1 = Uri.parse("content://calendar/reminders");
            CONTENT_URI_2 = Uri.parse("content://com.android.calendar/reminders");
        }
    }
}
