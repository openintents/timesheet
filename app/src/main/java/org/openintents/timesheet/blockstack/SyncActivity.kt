package org.openintents.timesheet.blockstack

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.sync_blockstack.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.GetFileOptions
import org.blockstack.android.sdk.PutFileOptions
import org.json.JSONArray
import org.json.JSONObject
import org.openintents.convertcsv.opencsv.CSVReader
import org.openintents.timesheet.PreferenceActivity
import org.openintents.timesheet.R
import org.openintents.timesheet.Timesheet
import org.openintents.timesheet.Timesheet.InvoiceItem
import org.openintents.timesheet.Timesheet.Job
import org.openintents.timesheet.TimesheetIntent
import org.openintents.timesheet.activity.JobActivity
import org.openintents.util.DateTimeFormater
import java.io.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

class SyncActivity : AppCompatActivity() {
    internal lateinit var mCustomer: String

    private lateinit var mSession: BlockstackSession

    private val REQUEST_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sync_blockstack)
        val customer = intent.getStringExtra(TimesheetIntent.EXTRA_CUSTOMER)
        if (TextUtils.isEmpty(customer)) {
            mCustomer = getString(R.string.all_customers)
        } else {
            mCustomer = customer
        }

        export_for.text = getString(R.string.export_for, mCustomer)

        file_export.setOnClickListener { startExportAndFinish() }
        file_import.setOnClickListener { startImportAndFinish() }

        mSession = BlockstackSession(this, config)
        if (!mSession.isUserSignedIn()) {
            showSyncUI(false)
            startActivityForResult(Intent(this, AccountActivity::class.java), REQUEST_SIGN_IN)
        } else {
            showSyncUI(true)
        }
    }

    private fun showSyncUI(showUI: Boolean) {
        file_export.isEnabled = showUI
        file_import.isEnabled = showUI
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                showSyncUI(true)
            } else {
                finish()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val singleFile = prefs.getBoolean(
                PreferenceActivity.PREFS_EXPORT_SINGLE_FILE, true)
        if (singleFile) {
            info!!.setText(R.string.export_to_single_file)
            user_label!!.setText(R.string.file_path)
            val defaultFile = Environment.getExternalStorageDirectory().absolutePath + "/timesheet.csv"
            user.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_FILENAME, defaultFile))
        } else {
            info!!.setText(R.string.export_to_directory)
            user_label!!.setText(R.string.dir_path)
            val defaultPath = Environment.getExternalStorageDirectory().absolutePath + "/timesheet"
            user.setText(prefs.getString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, defaultPath))
        }
        val updateCalendar = prefs.getBoolean(
                PreferenceActivity.PREFS_EXPORT_CALENDAR, false)
        if (updateCalendar) {
            calendar_info!!.visibility = View.VISIBLE
        } else {
            calendar_info!!.visibility = View.GONE
        }
    }

    /**
     * start export, overwrite fileName if exists
     */
    private fun startExportAndFinish() {
        val calendarAuthority = JobActivity.getCalendarAuthority(this)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val askIfExists = prefs.getBoolean(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, true)
        val exportReplaceBr = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_REPLACE_BR, false)
        val exportCompletedOnly = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_COMPLETED_ONLY, false)
        val omitTemplates = prefs.getBoolean(PreferenceActivity.PREFS_OMIT_TEMPLATES, true)
        val exportTotals = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_TOTALS, true)
        val singleFile = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_SINGLE_FILE, true)
        val updateCalendar = prefs.getBoolean(PreferenceActivity.PREFS_EXPORT_CALENDAR, false)
        DateTimeFormater.getFormatFromPreferences(this)
        val decimalFormat = DecimalFormat("0.00")
        val fileName = user.text.toString()
        val editor = prefs.edit()
        if (singleFile) {
            editor.putString(PreferenceActivity.PREFS_EXPORT_FILENAME, fileName)
        } else {
            editor.putString(PreferenceActivity.PREFS_EXPORT_DIRECTORY, fileName)
        }
        editor.apply()
        val exportContext = ExportContext(singleFile, fileName)

        // TODO handle existing file

        doExport(exportReplaceBr, exportCompletedOnly, omitTemplates, updateCalendar, decimalFormat, exportTotals, exportContext, calendarAuthority)
        finish()

    }

    private fun doExport(exportReplaceBr: Boolean, exportCompletedOnly: Boolean,
                         omitTemplates: Boolean, updateCalendar: Boolean,
                         decimalFormat: NumberFormat, exportTotals: Boolean, exportContext: ExportContext,
                         calendarAuthority: Int) {

        var contentValues: ContentValues? = null


        try {

            val SORT_ORDER = "start_date ASC, customer ASC"

            val selectionSb = StringBuffer()
            var selectionArgs: Array<String>? = null
            if (exportCompletedOnly) {
                selectionSb.append(Job.END_DATE).append(" is not null AND ")
                        .append(Job.START_DATE).append(" is not null")
            }

            if (omitTemplates) {
                if (selectionSb.length > 0) {
                    selectionSb.append(" AND ")
                }
                selectionSb.append(Job.START_DATE).append(" is not null")
            }
            var selection = selectionSb.toString()

            if (mCustomer != getString(R.string.all_customers)) {
                if (!TextUtils.isEmpty(selection)) {
                    selection += " AND "
                }
                selection += Job.CUSTOMER + " = ?"
                selectionArgs = arrayOf(mCustomer)
            }

            val c = contentResolver.query(Job.CONTENT_URI,
                    PROJECTION, selection, selectionArgs, SORT_ORDER)

            if (c != null) {

                val COLUMN_START_DATE = c
                        .getColumnIndexOrThrow(Job.START_DATE)
                val COLUMN_END_DATE = c
                        .getColumnIndexOrThrow(Job.END_DATE)
                val COLUMN_BREAK_DURATION = c
                        .getColumnIndexOrThrow(Job.BREAK_DURATION)
                val COLUMN_BREAK2_DURATION = c
                        .getColumnIndexOrThrow(Job.BREAK2_DURATION)
                val COLUMN_NOTE = c.getColumnIndexOrThrow(Job.NOTE)
                val COLUMN_HOURLY_RATE = c
                        .getColumnIndexOrThrow(Job.HOURLY_RATE)
                val COLUMN_CUSTOMER = c
                        .getColumnIndexOrThrow(Job.CUSTOMER)
                val COLUMN_CALENDAR_REF = c
                        .getColumnIndexOrThrow(Job.CALENDAR_REF)

                val customerSet = HashSet<String>()

                while (c.moveToNext()) {
                    // get values
                    val startdate = c.getLong(COLUMN_START_DATE)
                    val enddate = c.getLong(COLUMN_END_DATE)

                    val breakduration = c.getLong(COLUMN_BREAK_DURATION)
                    val break2duration = c.getLong(COLUMN_BREAK2_DURATION)
                    var description = c.getString(COLUMN_NOTE)
                    if (exportReplaceBr) {
                        description = description.replace("\n", "\\n")
                    }
                    val hourlyRate = c.getLong(COLUMN_HOURLY_RATE)
                    val customer = c.getString(COLUMN_CUSTOMER)

                    val totalhours = enddate - startdate
                    val workhours = (enddate - startdate - breakduration
                            - break2duration)

                    val earning = (workhours.toDouble() * Timesheet.HOUR_FACTOR
                            * (hourlyRate * Timesheet.RATE_FACTOR))

                    val stringStartDate = DateTimeFormater.mDateFormater
                            .format(startdate)
                    val stringStartTime = DateTimeFormater.mTimeFormater
                            .format(startdate)

                    //
                    // write values
                    //
                    customerSet.add(customer)
                    val extras = exportContext
                            .getTotals(customer)
                    if (totalhours > 0) {
                        extras.sumTotalhours += totalhours
                        extras.sumBreakhours += breakduration
                        extras.sumWorkhours += workhours
                        extras.sumEarning += earning
                    }
                    val job = JSONObject()

                    if (startdate > 0) {
                        job.put("startDate", stringStartDate)
                        job.put("startTime", stringStartTime)
                    }
                    if (totalhours > 0) {
                        job.put("totalHours", decimalFormat.format(totalhours * Timesheet.HOUR_FACTOR))
                        job.put("workHours", decimalFormat.format(workhours * Timesheet.HOUR_FACTOR))
                        job.put("breakDuration", decimalFormat.format(breakduration * Timesheet.HOUR_FACTOR))
                        job.put("earning", decimalFormat.format(earning))

                        if (updateCalendar) {
                            // export to calendar
                            var calendarUri = c
                                    .getString(COLUMN_CALENDAR_REF)
                            calendarUri = Timesheet.CalendarApp.setOrUpdateCalendarEvent(
                                    this, calendarUri,
                                    startdate, totalhours, customer,
                                    description, calendarAuthority)
                            val jobUri = Uri.withAppendedPath(Job.CONTENT_URI, c.getString(0))
                            if (contentValues == null) {
                                contentValues = ContentValues()
                            } else {
                                contentValues.clear()
                            }
                            contentValues.put(Job.CALENDAR_REF, calendarUri)
                            contentResolver.update(jobUri, contentValues, null, null)
                        }

                    }
                    job.put("hourlyRate", decimalFormat.format(hourlyRate * RATE_FACTOR))
                    job.put("description", description)
                    job.put("customer", customer)


                    // export extra invoice items
                    job.put("invoiceItems", JSONArray())
                    val jobId = c.getLong(COLUMN_INDEX_ID).toString()
                    val c2 = contentResolver.query(
                            InvoiceItem.CONTENT_URI, INVOICE_ITEMS_PROJECTION,
                            InvoiceItem.JOB_ID + " = ?",
                            arrayOf(jobId), null)
                    var invoiceItem: JSONObject
                    while (c2.moveToNext()) {

                        val value = c2.getLong(II_COLUMN_INDEX_VALUE) * RATE_FACTOR
                        extras.sumEarning += value
                        val valueString = decimalFormat.format(value)
                        invoiceItem = JSONObject()
                        val invoiceDescription = c2
                                .getString(II_COLUMN_INDEX_DESCRIPTION)
                        invoiceItem.put("earning", valueString)
                        invoiceItem.put("description", invoiceDescription)
                        invoiceItem.put("customer", customer)
                        job.getJSONArray("invoiceItems").put(invoiceItem)
                    }
                    c2.close()

                    // end export invoice items
                    exportContext.getJson(customer).put(job)
                }

                if (exportTotals) {
                    var totals: JSONObject
                    for (customer in customerSet) {
                        totals = JSONObject()
                        val extras = exportContext
                                .getTotals(customer)

                        totals.put("totalHours", decimalFormat
                                .format(extras.sumTotalhours * Timesheet.HOUR_FACTOR))
                        totals.put("workHours", decimalFormat
                                .format(extras.sumWorkhours * Timesheet.HOUR_FACTOR))
                        totals.put("breakHours", decimalFormat
                                .format(extras.sumBreakhours * Timesheet.HOUR_FACTOR))
                        totals.put("earning", decimalFormat
                                .format(extras.sumEarning))

                        totals.put("description", getString(R.string.export_total))
                        totals.put("customer", customer)
                        exportContext.getTotalsMap(customer).put(totals)
                    }
                }

                if (exportContext.singleFile) {
                    val jobs = JSONObject()
                    jobs.put("jobs", exportContext.getJson(""))
                    jobs.put("totals", exportContext.getTotalsMap(""))
                    mSession.putFile(exportContext.fileName, jobs.toString(), PutFileOptions()) { _ ->
                        Toast.makeText(this, R.string.export_finished, Toast.LENGTH_SHORT)
                                .show()
                    }
                } else {
                    var jobs: JSONObject
                    var fileName:String
                    var count = 0
                    for (c in customerSet) {
                        jobs = JSONObject()
                        jobs.put("jobs", exportContext.getJson(c))
                        jobs.put("totals", exportContext.getTotalsMap(c))
                        fileName = "${exportContext.fileName}/$c.json"
                        mSession.putFile(fileName, jobs.toString(), PutFileOptions()) { _ ->
                            count++
                            if (count == customerSet.size) {
                                Toast.makeText(this, R.string.export_finished, Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }
                    }
                }
            }
            c!!.close()



        } catch (e: FileNotFoundException) {
            Toast.makeText(this, R.string.error_writing_file,
                    Toast.LENGTH_SHORT).show()
            Log.i(TAG, "File not found", e)
        } catch (e: IOException) {
            Toast.makeText(this, R.string.error_writing_file,
                    Toast.LENGTH_SHORT).show()
            Log.i(TAG, "IO exception", e)
        }

        Log.v(TAG, "end of export")

    }

    fun startImportAndFinish() {

    }

    @Throws(IOException::class)
    fun doImport() {
        mSession.getFile(user.text.toString(), GetFileOptions()) {result ->
            if (result.hasValue) {
                val projects = JSONObject(result.value as String)
                val jobs = projects.getJSONArray("jobs")

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, MENU_SETTINGS, 0, R.string.menu_preferences).setShortcut('1', 's')
                .setIcon(android.R.drawable.ic_menu_preferences)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_SETTINGS /*1*/ -> {
                val intent = Intent(this, PreferenceActivity::class.java)
                intent.putExtra("exportOnly", true)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    internal class Totals {
        var sumTotalhours: Long = 0
        var sumBreakhours: Long = 0
        var sumWorkhours: Long = 0
        var sumEarning = 0.0
    }

    internal inner class ExportContext(val singleFile: Boolean, val fileName: String) {

        val totals = HashMap<String, Totals>()
        val jobs = HashMap<String, JSONArray>()
        val totalsMap = HashMap<String, JSONArray>()
        fun getTotals(customer: String): Totals {
            val c = if (singleFile) {
                ""
            } else {
                customer
            }

            if (!totals.containsKey(c)) {
                totals[c] = Totals()
            }
            return totals[c]!!
        }

        fun getJson(customer: String): JSONArray {
            val c = if (singleFile) {
                ""
            } else {
                customer
            }
            if (!jobs.containsKey(c)) {
                jobs[c] = JSONArray()
            }
            return jobs[c]!!
        }

        fun getTotalsMap(customer: String): JSONArray {
            val c = if (singleFile) {
                ""
            } else {
                customer
            }
            if (!totalsMap.containsKey(c)) {
                totalsMap[c] = JSONArray()
            }
            return totalsMap[c]!!
        }
    }


    companion object {

        private val HOUR_FACTOR = 2.7777777777E-7
        private val RATE_FACTOR = 0.01
        private val MENU_SETTINGS = Menu.FIRST
        private val PROJECTION = arrayOf(Job._ID, Job.TITLE, Job.NOTE, Job.START_DATE, Job.END_DATE, Job.LAST_START_BREAK, Job.BREAK_DURATION, Job.CUSTOMER, Job.HOURLY_RATE, Job.LAST_START_BREAK2, Job.BREAK2_DURATION, Job.CALENDAR_REF)
        private val COLUMN_INDEX_ID = 0
        private val INVOICE_ITEMS_PROJECTION = arrayOf(//
                InvoiceItem._ID, // 0
                InvoiceItem.VALUE, // 1
                InvoiceItem.DESCRIPTION // 2
        )
        private val II_COLUMN_INDEX_VALUE = 1
        private val II_COLUMN_INDEX_DESCRIPTION = 2
        private val TAG = "ConvertCsvActivity"
    }
}
