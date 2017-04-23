package de.stm.oses;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.dialogs.ProgressDialogFragment;

public class OSESActivity extends AppCompatActivity implements View.OnClickListener {
	
    private OSESBase OSES;
    private FirebaseAnalytics mFirebaseAnalytics;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        OSES = new OSESBase(OSESActivity.this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Arbeitsverzeichnis aufräumen und anpassen -> ab v345
        OSES.rebuildWorkingDirectory();


        if (!OSES.getSession().getIdentifier().equals("")) {
            Intent intent = new Intent(OSESActivity.this,StartActivity.class);
            startActivity(intent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.oses_start_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setTitle("Anmeldung");

        }

                
        TextView copyright = (TextView)findViewById(R.id.textView4);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        if (copyright != null)
            copyright.setText("© "+String.valueOf(year)+" Steiner Media - v "+OSES.getVersion());

        Button button_about = (Button) findViewById(R.id.login_about);
        Button button_lost_pass = (Button) findViewById(R.id.login_lost_pass);
        Button button_registrierung = (Button) findViewById(R.id.login_registrierung);
        Button button_login = (Button) findViewById(R.id.login_login);

        if (button_about != null)
            button_about.setOnClickListener(this);

        if (button_lost_pass != null)
            button_lost_pass.setOnClickListener(this);

        if (button_registrierung != null)
            button_registrierung.setOnClickListener(this);

        if (button_login != null)
            button_login.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_login_imprint:
                ImprintOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void LoginOnClick() {
    	
    	boolean stop = false;

        String username;
        String password;
    	
    	EditText usernameEdit = (EditText)findViewById(R.id.username);
		EditText passwordEdit = (EditText)findViewById(R.id.password);

        if (usernameEdit != null && passwordEdit != null) {

            usernameEdit.clearFocus();
            passwordEdit.clearFocus();

            username = usernameEdit.getText().toString();
            password = passwordEdit.getText().toString();

            if (username.isEmpty()) {
                usernameEdit.setError("Benutzername darf nicht leer sein");
                stop = true;
            }

            if (password.isEmpty()) {
                passwordEdit.setError("Passwort darf nicht leer sein");
                stop = true;
            }

            if (stop)
                return;

            mFirebaseAnalytics.logEvent("OSES_login_start", null);

            //dialog = new ProgressDialog(this);
            //dialog.setTitle("Bitte warten");
            //dialog.setMessage("Anmeldung wird ausgeführt...");
            //dialog.setCancelable(false);
            //dialog.show();

            ShowWaitDialog();


            @SuppressLint("HardwareIds")
            String android_id = Secure.getString(getBaseContext().getContentResolver(),
                    Secure.ANDROID_ID);

            Map<String, String> postdata = new HashMap<>();
            postdata.put("username", username);
            postdata.put("password", password);
            postdata.put("device", android_id);
            postdata.put("model", android.os.Build.MODEL+"|"+android.os.Build.PRODUCT);
            postdata.put("gcm_regid", OSES.getSession().getSessionFcmInstanceId());
            postdata.put("androidversion", String.valueOf(OSES.getVersionCode()));


            OSESRequest LoginRequest = new OSESRequest(this);
            LoginRequest.setUrl("https://oses.mobi/api.php?request=login");
            LoginRequest.setTimeout(30000);
            LoginRequest.setParams(postdata);
            LoginRequest.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
                @Override
                public void onRequestFinished(String response) {

                    hideWaitDialog();


                    String SessionIdentifier = "";
                    String StatusCode = "";

                    try {

                        JSONObject json = new JSONObject(response);

                        if (json.has("StatusCode"))
                            StatusCode = json.getString("StatusCode");

                        if (json.has("SessionIdentifier")) {
                            SessionIdentifier = json.getString("SessionIdentifier");
                        }

                    } catch (Exception e) {
                        FirebaseCrash.report(e);
                    }

                    if (!StatusCode.equals("200")) {

                        mFirebaseAnalytics.logEvent("OSES_login_failed", null);
                        Toast.makeText(OSESActivity.this, "Anmeldung fehlgeschlagen! Benutzername oder Passwort ist nicht korrekt!", Toast.LENGTH_LONG).show();


                    }  else {

                        mFirebaseAnalytics.logEvent("OSES_login_ok", null);

                        SharedPreferences settings = getSharedPreferences("OSESPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putString("SessionIdentifier", SessionIdentifier);
                        editor.apply();

                        Intent intent = new Intent(OSESActivity.this,StartActivity.class);
                        startActivity(intent);

                        finish();

                    }


                }

                @Override
                public void onRequestException(Exception e) {
                    hideWaitDialog();
                    Toast.makeText(OSESActivity.this, "Technischer Fehler beim Anmeldevorgang! Bitte versuch es erneut!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onRequestUnknown(int status) {
                    hideWaitDialog();
                    Toast.makeText(OSESActivity.this, "Der Server hat eine unerwartete Antwort gesendet! Bitte versuch es erneut!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onIsNotConnected() {
                    hideWaitDialog();
                    Toast.makeText(OSESActivity.this, "Bitte stelle eine Internetverbindung her um den Anmeldevorgang fortzusetzen!", Toast.LENGTH_LONG).show();
                }
            });

            LoginRequest.execute();


        }
   	    
    }

    private void ShowWaitDialog() {

        ProgressDialogFragment loginWaitDialog = ProgressDialogFragment.newInstance("Anmeldung", "Bitte warten, deine Zugangsdaten werden überprüft...", ProgressDialog.STYLE_SPINNER);
        loginWaitDialog.show(getSupportFragmentManager(), "loginWaitDialog");

    }

    private void hideWaitDialog() {

        Fragment f = getSupportFragmentManager().findFragmentByTag("loginWaitDialog");

        if (f != null)
            ((ProgressDialogFragment) f).dismiss();


    }

    private void VergessenOnClick() {

        String url = "https://oses.mobi/index.php?action=acclost";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(OSESActivity.this, R.color.oses_green));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));

    }

    private void RegisterOnClick() {

        String url = "https://oses.mobi/index.php?action=register";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(OSESActivity.this, R.color.oses_green));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));

    }

    private void ImprintOnClick() {

        String url = "https://oses.mobi/index.php?action=imprint";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(OSESActivity.this, R.color.oses_green));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));

    }

    private void AboutOnClick() {

        String url = "https://oses.mobi/index.php?action=about";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(OSESActivity.this, R.color.oses_green));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_registrierung:
                RegisterOnClick();
                break;
            case R.id.login_lost_pass:
                VergessenOnClick();
                break;
            case R.id.login_about:
                AboutOnClick();
                break;
            case R.id.login_login:
                LoginOnClick();
                break;
        }

    }
    
}
