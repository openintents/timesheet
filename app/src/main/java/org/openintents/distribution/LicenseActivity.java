package org.openintents.distribution;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.openintents.timesheet.Application;
import org.openintents.timesheet.R;
import org.openintents.timesheet.PreferenceActivity;

public class LicenseActivity extends Activity {
    static final int DIALOG_ID_MARKET_WARNING = 1;
    public static final String EXTRA_TITLE = "title";
    private static final String LICENSE_URL = "http://www.openintents.org/licenses/oitimesheet.php";
    private String mAppCode;
    private String mCtmCode;
    private TextView mLic1;
    private TextView mLic2;
    private TextView mLic3;
    private TextView mLic4;
    private String mLicCode;

    /* renamed from: org.openintents.distribution.LicenseActivity.1 */
    class C00021 implements OnClickListener {
        C00021() {
        }

        public void onClick(View view) {
            LicenseActivity.this.storeLicenseAndFinish();
        }
    }

    /* renamed from: org.openintents.distribution.LicenseActivity.2 */
    class C00032 implements OnClickListener {
        C00032() {
        }

        public void onClick(View view) {
            if (Application.mApplicationVariant == LicenseActivity.DIALOG_ID_MARKET_WARNING) {
                LicenseActivity.this.showDialog(LicenseActivity.DIALOG_ID_MARKET_WARNING);
                return;
            }
            LicenseActivity.this.requestLicense();
            LicenseActivity.this.finish();
        }
    }

    /* renamed from: org.openintents.distribution.LicenseActivity.3 */
    class C00043 implements DialogInterface.OnClickListener {
        C00043() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            LicenseActivity.this.requestLicense();
            LicenseActivity.this.finish();
        }
    }

    /* renamed from: org.openintents.distribution.LicenseActivity.4 */
    class C00054 implements DialogInterface.OnClickListener {
        C00054() {
        }

        public void onClick(DialogInterface dialog, int whichButton) {
        }
    }

    private class TextChangedWatcher implements TextWatcher {
        TextView mNextTextView;
        TextView mPrevTextView;

        public TextChangedWatcher(TextView prevTextView, TextView nextTextView) {
            this.mPrevTextView = prevTextView;
            this.mNextTextView = nextTextView;
        }

        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 4 && this.mNextTextView != null) {
                this.mNextTextView.requestFocus();
            }
            if (s.toString().length() == 0 && this.mPrevTextView != null) {
                this.mPrevTextView.requestFocus();
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(16973835);
        setContentView(R.layout.license);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }
        this.mLic1 = (TextView) findViewById(R.id.lic1);
        this.mLic2 = (TextView) findViewById(R.id.lic2);
        this.mLic3 = (TextView) findViewById(R.id.lic3);
        this.mLic4 = (TextView) findViewById(R.id.lic4);
        this.mLic1.addTextChangedListener(new TextChangedWatcher(null, this.mLic2));
        this.mLic2.addTextChangedListener(new TextChangedWatcher(this.mLic1, this.mLic3));
        this.mLic3.addTextChangedListener(new TextChangedWatcher(this.mLic2, this.mLic4));
        this.mLic4.addTextChangedListener(new TextChangedWatcher(this.mLic3, null));
        this.mCtmCode = LicenseChecker.getCtmCode(this);
        this.mAppCode = LicenseChecker.getAppCode(this);
        this.mLicCode = LicenseChecker.getLicCode(this);
        TextView t = (TextView) findViewById(R.id.lic_text);
        Object[] objArr;
        if (Application.mApplicationVariant == DIALOG_ID_MARKET_WARNING) {
            objArr = new Object[DIALOG_ID_MARKET_WARNING];
            objArr[0] = this.mCtmCode;
            t.setText(getString(R.string.license_text_market, objArr));
        } else {
            objArr = new Object[DIALOG_ID_MARKET_WARNING];
            objArr[0] = this.mCtmCode;
            t.setText(getString(R.string.license_text, objArr));
        }
        setLicenseText(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceActivity.PREFS_LICENSE_DEVELOPER, null));
        ((Button) findViewById(R.id.ok)).setOnClickListener(new C00021());
        ((Button) findViewById(R.id.request)).setOnClickListener(new C00032());
    }

    protected void onResume() {
        super.onResume();
        checkClipboard();
    }

    private void checkClipboard() {
        ClipboardManager clippy = (ClipboardManager) getSystemService("clipboard");
        if (clippy.hasText()) {
            String clip = clippy.getText().toString().trim();
            if (clip.length() == 19 && clip.substring(4, 5).equals("-") && clip.substring(9, 10).equals("-") && clip.substring(14, 15).equals("-")) {
                setLicenseText(clip);
                Toast.makeText(this, getString(R.string.license_from_clipboard), DIALOG_ID_MARKET_WARNING).show();
            }
        }
    }

    private void setLicenseText(String licensekey) {
        if (licensekey != null && licensekey.length() == 19) {
            this.mLic1.setText(licensekey.substring(0, 4));
            this.mLic2.setText(licensekey.substring(5, 9));
            this.mLic3.setText(licensekey.substring(10, 14));
            this.mLic4.setText(licensekey.substring(15, 19));
        }
    }

    protected void requestLicense() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://www.openintents.org/licenses/oitimesheet.php?ctmcode=" + this.mCtmCode + "&appcode=" + this.mAppCode + "&liccode=" + this.mLicCode)));
    }

    protected void storeLicenseAndFinish() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        StringBuilder sb = new StringBuilder();
        sb.append(this.mLic1.getText());
        sb.append('-');
        sb.append(this.mLic2.getText());
        sb.append('-');
        sb.append(this.mLic3.getText());
        sb.append('-');
        sb.append(this.mLic4.getText());
        if (LicenseChecker.checkLicense(this, sb.toString())) {
            editor.putString(PreferenceActivity.PREFS_LICENSE_DEVELOPER, sb.toString());
            editor.commit();
            ((LicensedApplication) getApplication()).newLicense();
            finish();
            return;
        }
        Toast.makeText(this, getString(R.string.invalid_license), DIALOG_ID_MARKET_WARNING).show();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_MARKET_WARNING /*1*/:
                return new Builder(this).setIcon(17301543).setTitle(R.string.alert).setMessage(R.string.dialog_market_warning).setPositiveButton(17039370, new C00043()).setNegativeButton(17039360, new C00054()).create();
            default:
                return null;
        }
    }
}
