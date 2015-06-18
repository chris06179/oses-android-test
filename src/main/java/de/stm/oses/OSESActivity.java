package de.stm.oses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.stm.oses.helper.OSESBase;

public class OSESActivity extends AppCompatActivity {
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
	
	String TAG = "OSES Push";
    String regid;
    String SENDER_ID = "246201402657";
    GoogleCloudMessaging gcm;
    private OSESBase OSES;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OSES = new OSESBase(OSESActivity.this);



        if (!OSES.getSession().getIdentifier().equals("")) {
            Intent intent = new Intent(OSESActivity.this,StartActivity.class);
            startActivity(intent);
            OSESActivity.this.finish();
        }

        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.oses_start_toolbar);
        setSupportActionBar(toolbar);
        
        getSupportActionBar().setTitle("Anmeldung");
        
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid.equals("")) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
                
        TextView copyright = (TextView)findViewById(R.id.textView4);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        
        copyright.setText("© "+String.valueOf(year)+" Steiner Media - v "+OSES.getVersion());
        

    }
    
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(OSESActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private void registerInBackground() {
    	new AsyncTask<Void,Void,String>() {   		
    	    @Override
    	    protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    
                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }


    	    @Override
            protected void onPostExecute(String msg) {
    	    	Log.i(TAG, "GCM: "+msg);
            }
            
        }.execute(null, null, null);
        
    }
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
            case R.id.action_login_lostpass:
                VergessenOnClick();
                return true;
            case R.id.action_login_imprint:
                ImprintOnClick();
                return true;
            case R.id.action_login_was_ist_das:
                AboutOnClick();
                return true;
            case R.id.action_login_register:
                RegisterOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void Login(View v) throws Exception {
    	
    	boolean stop = false;    	
    	
    	EditText username = (EditText)findViewById(R.id.username);
		EditText password = (EditText)findViewById(R.id.password);		
		
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


    public void VergessenOnClick() {

        String url = "https://oses.mobi/index.php?action=acclost";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    public void RegisterOnClick() {

        String url = "https://oses.mobi/index.php?action=register";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    public void ImprintOnClick() {

        String url = "https://oses.mobi/index.php?action=imprint";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    public void AboutOnClick() {

        String url = "https://oses.mobi/index.php?action=about";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }
    
    private class DoLogin extends AsyncTask<Void, Void, String> {
    	
    	private ProgressDialog dialog;
		private EditText username = (EditText)findViewById(R.id.username);
		private EditText password = (EditText)findViewById(R.id.password);


    	protected void onPreExecute() {
    		
    		dialog = ProgressDialog.show(OSESActivity.this, "Bitte warten...", 
                    "", true);
    		dialog.setMessage("Anmeldung wird ausgeführt...");
    		
        }
    	
    	protected String doInBackground(Void... params) {

            String android_id = Secure.getString(getBaseContext().getContentResolver(),
                    Secure.ANDROID_ID);


            Map<String, String> postdata = new HashMap<>();

                postdata.put("username", username.getText().toString());
                postdata.put("password", password.getText().toString());
                postdata.put("device", android_id);
                postdata.put("model", android.os.Build.MODEL+"|"+android.os.Build.PRODUCT);
                postdata.put("gcm_regid", regid);
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
    			
    			Toast.makeText(OSESActivity.this, "Anmeldung fehlgeschlagen! Benutzername oder Passwort ist nicht korrekt!", Toast.LENGTH_LONG).show();


    		}  else {   				
    			
    			// We need an Editor object to make preference changes.
    		    // All objects are from android.context.Context
    		    SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
    		    SharedPreferences.Editor editor = settings.edit();
    			
    			editor.putString("SessionIdentifier", SessionIdentifier);

    			// Commit the edits!
    		    editor.commit();
    		    
    		    Intent intent = new Intent(OSESActivity.this,StartActivity.class);
    			startActivity(intent);    			
    			    			
    			OSESActivity.this.finish();

    			
    		}
            
        }


    }
    
    
    
}
