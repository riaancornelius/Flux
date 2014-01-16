package com.riaancornelius.flux;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.octo.android.robospice.SpiceManager;
import com.riaancornelius.flux.api.JiraSpiceService;
import com.riaancornelius.flux.domain.Settings;
import com.riaancornelius.flux.ui.MainActivity;
import com.riaancornelius.flux.ui.issue.IssueActivity;
import com.riaancornelius.flux.ui.settings.SettingsActivity;
import roboguice.activity.RoboActivity;

/**
 * Created by riaan.cornelius on 2013/12/22.
 */
public class BaseActivity extends RoboActivity {

    protected static final String KEY_LAST_REQUEST_CACHE_KEY = "lastRequestCacheKey";

    public static final String INTENT_KEY_BOARD_ID = "INTENT_BOARD_ID";

    protected SpiceManager spiceManager = new SpiceManager(
            JiraSpiceService.class);

    protected String lastRequestCacheKey;

    protected ProgressDialog progressDialog;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        ActionBar actionBar = getActionBar();
//        actionBar.setSubtitle("Pre-Alpha test version");
//        actionBar.setTitle("Project Flux");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                loadMainActivity();
                break;
            case R.id.action_scan_ticket:
                scanTicketCode();
                break;
            case R.id.action_settings:
                loadSettingsActivity();
                break;
            default:
                break;
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        if (scanResult != null) {
            Intent myIntent = new Intent(this, IssueActivity.class);
            myIntent.putExtra("issueKey", scanResult.getContents());
            this.startActivity(myIntent);
        }   else {
            toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setText("Invalid code");
            toast.show();
        }
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    protected void beforeRequest() {
        this.setProgressBarIndeterminateVisibility(true);
        progressDialog = ProgressDialog.show(this, "Loading data", "Please wait");
    }

    protected void afterRequest() {
        this.setProgressBarIndeterminateVisibility(false);
        progressDialog.dismiss();
    }

    protected void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void loadSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    protected SharedPreferences getJiraSharedPreferences() {
        return getSharedPreferences(Settings.JIRA_SHARED_PREFERENCES_KEY, 0);
    }

    protected boolean isUserDetailsSet() {
        String username = getJiraSharedPreferences().getString(Settings.JIRA_USERNAME_KEY, "");
        String encodedPassword = getJiraSharedPreferences().getString(Settings.JIRA_PASSWORD_KEY, "");
        return !(username == null || encodedPassword == null ||
                username.isEmpty() || encodedPassword.isEmpty());
    }

    protected void scanTicketCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setButtonNo("No");
        integrator.setButtonYes("Yes");
        integrator.setTitle("Warning");
        integrator.setMessage("Barcode scanner not installed. Download?");
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

}