package org.openintents.timesheet.blockstack


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.content_account.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.blockstack.android.sdk.BlockstackSession
import org.openintents.timesheet.R


class AccountActivity : AppCompatActivity() {
    private val TAG = AccountActivity::class.java.simpleName

    private lateinit var _blockstackSession: BlockstackSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        signInButton.isEnabled = false
        signOutButton.isEnabled = false

        _blockstackSession = BlockstackSession(this, config)
        if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }
        onLoaded()
        signInButton.setOnClickListener { _ ->
            _blockstackSession.redirectUserToSignIn { _ ->
                Log.d(TAG, "signed in error!")
            }
        }

        signOutButton.setOnClickListener { _ ->
            _blockstackSession.signUserOut()
            Log.d(TAG, "signed out!")
            finish()
        }
    }

    private fun onLoaded() {
        signInButton.isEnabled = true
        signOutButton.isEnabled = true
        val signedIn = _blockstackSession.isUserSignedIn()
        if (signedIn) {
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
        } else {
            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }

    }

    private fun onSignIn() {
        _blockstackSession.loadUserData()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")

        if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }

    }

    private fun handleAuthResponse(intent: Intent?) {
        val authResponse = intent?.data?.getQueryParameter("authResponse")
        if (authResponse != null) {
            launch(UI) {
                _blockstackSession.handlePendingSignIn(authResponse) {
                    if (it.hasErrors) {
                        runOnUiThread {
                            Toast.makeText(this@AccountActivity, it.error, Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    } else {
                        Log.d(TAG, "signed in!")
                        runOnUiThread {
                            onSignIn()
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}


