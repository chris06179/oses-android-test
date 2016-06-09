package de.stm.oses;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.customtabs.CustomTabsIntent;
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

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.stm.oses.helper.OSESBase;

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

        if (button_about != null)
            button_about.setOnClickListener(this);

        if (button_lost_pass != null)
            button_lost_pass.setOnClickListener(this);

        if (button_registrierung != null)
            button_registrierung.setOnClickListener(this);

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
    
    public void Login(View v) throws Exception {
    	
    	boolean stop = false;    	
    	
    	EditText username = (EditText)findViewById(R.id.username);
		EditText password = (EditText)findViewById(R.id.password);

        if (username != null && password != null) {

            username.clearFocus();
            password.clearFocus();

            if (username.getText().toString().length() == 0) {
                username.setError("Benutzername darf nicht leer sein");
                stop = true;
            }

            if (password.getText().toString().length() == 0) {
                password.setError("Passwort darf nicht leer sein");
                stop = true;
            }

            if (stop)
                return;

            new DoLogin().execute();
        }
   	    
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
        }

    }

    private class DoLogin extends AsyncTask<Void, Void, String> {
    	
    	private ProgressDialog dialog;
		private String username = "";
		private String password = "";


    	protected void onPreExecute() {

            mFirebaseAnalytics.logEvent("OSES_login_start", null);
    		
    		dialog = ProgressDialog.show(OSESActivity.this, "Bitte warten...", 
                    "", true);
    		dialog.setMessage("Anmeldung wird ausgeführt...");

            EditText username = (EditText)findViewById(R.id.username);
            EditText password = (EditText)findViewById(R.id.password);

            if (username != null && password != null) {

                this.username = username.getText().toString();
                this.password = password.getText().toString();

            }


        }
    	
    	protected String doInBackground(Void... params) {

            String android_id = Secure.getString(getBaseContext().getContentResolver(),
                    Secure.ANDROID_ID);

            Map<String, String> postdata = new HashMap<>();

                postdata.put("username", username);
                postdata.put("password", password);
                postdata.put("device", android_id);
                postdata.put("model", android.os.Build.MODEL+"|"+android.os.Build.PRODUCT);
                postdata.put("gcm_regid", OSES.getSession().getSessionFcmInstanceId());
                postdata.put("androidversion", String.valueOf(OSES.getVersionCode()));

            return OSES.getJSON("https://oses.mobi/api.php?request=login", postdata, 60000);

            
        }
    	
    	protected void onPostExecute(String response) {
    		
    		dialog.dismiss();   
    		
    		String SessionIdentifier = "";		
			String StatusCode = "";

    		
    		try {
    			
				JSONObject json = new JSONObject(response);
							
				StatusCode = json.getString("StatusCode");

				if (StatusCode.equals("200") ) {
					
					SessionIdentifier = json.getString("SessionIdentifier");
					
				}
				
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		if (!StatusCode.equals("200")) {

                mFirebaseAnalytics.logEvent("OSES_login_failed", null);
    			
    			Toast.makeText(OSESActivity.this, "Anmeldung fehlgeschlagen! Benutzername oder Passwort ist nicht korrekt!", Toast.LENGTH_LONG).show();


    		}  else {

                mFirebaseAnalytics.logEvent("OSES_login_ok", null);
    			
    			// We need an Editor object to make preference changes.
    		    // All objects are from android.context.Context
    		    SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
    		    SharedPreferences.Editor editor = settings.edit();
    			
    			editor.putString("SessionIdentifier", SessionIdentifier);

    			// Commit the edits!
    		    editor.apply();
    		    
    		    Intent intent = new Intent(OSESActivity.this,StartActivity.class);
    			startActivity(intent);
    			    			
    			finish();
    			
    		}
            
        }


    }
    
    
    
}
