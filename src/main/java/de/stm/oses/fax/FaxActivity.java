package de.stm.oses.fax;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.stm.oses.R;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;


public class FaxActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int PERMISSIONS_REQUEST_LOCATION = 4100;
    private String docid;
    private String doctype;
    private String doctypetext;
    private String docdate;
    private String docdest;
    private String docdouble = "";
    private String docmonat;
    private String docjahr;
    private String excludestring = "";

    private Button cancel;
    private Button save;

    private GoogleMap map;
    private OSESBase OSES;
    private Location mCurrentLocation;
    private OSESRequest faxRequest;
    private Menu menu;

    private GoogleApiClient mGoogleApiClient;
    private FaxListFragment mFaxListFragment;

    private FirebaseAnalytics mFirebaseAnalytics;

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) != 0)
                outRect.top = space;
        }
    }

    //Your member variable declaration here

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {

        new AlertDialog.Builder(FaxActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fax);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mFirebaseAnalytics.logEvent("OSES_fax_start", null);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(FaxActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(FaxActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                showMessageOKCancel("Um die in der Nähe befindlichen Faxgeräte und Drucker abzurufen, muss dein Standort abgefragt werden. Bitte gib OSES für Android die Berechtigung zur Standortabfrage.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(FaxActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_LOCATION);
                    }
                });
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(FaxActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        FragmentManager fm = getFragmentManager();
        mFaxListFragment = (FaxListFragment) fm.findFragmentByTag("fax_list_fragment");

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mFaxListFragment == null) {
            mFaxListFragment = new FaxListFragment();
            fm.beginTransaction().add(mFaxListFragment, "fax_list_fragment").commit();
        }

        OSES = new OSESBase(FaxActivity.this);
        fm.findFragmentById(R.id.map).setRetainInstance(true);


        MapFragment mapFragment = ((MapFragment) fm.findFragmentById(R.id.map));
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.1681391, 10.2928216), 5));
                if (ActivityCompat.checkSelfPermission(FaxActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                }
                map.getUiSettings().setMapToolbarEnabled(false);

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        FaxClass item = getFaxAdapter().setSelectionByMarker(marker);
                        getFaxList().smoothScrollToPosition(getFaxAdapter().getItems().indexOf(item));
                        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                        marker.showInfoWindow();
                        save.setEnabled(true);
                        return true;
                    }
                });
            }
        });

        cancel = (Button) findViewById(R.id.fax_back);
        save = (Button) findViewById(R.id.fax_send);

        getFaxList().setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(FaxActivity.this);
        getFaxList().setLayoutManager(llm);
        ((SimpleItemAnimator) getFaxList().getItemAnimator()).setSupportsChangeAnimations(false);
        getFaxList().getItemAnimator().setAddDuration(500);
        getFaxList().getItemAnimator().setRemoveDuration(500);
        getFaxList().getItemAnimator().setMoveDuration(500);

        getFaxList().addItemDecoration(new SpacesItemDecoration(2));


        if (savedInstanceState == null) {

        } else {

            if (mFaxListFragment.getAdapter() != null) {
                getFaxList().setAdapter(mFaxListFragment.getAdapter());
                View wait = findViewById(R.id.fax_wait);
                wait.setVisibility(View.GONE);

                if (getFaxAdapter().getSelectedItem() != null)
                    save.setEnabled(true);
            }
        }

        if (getFaxList().getAdapter() == null)
            getFaxList().setAdapter(new FaxAdapter(FaxActivity.this));


        getFaxAdapter().setOnItemClickListener(new FaxAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FaxClass item = getFaxAdapter().getItems().get(position);
                getFaxAdapter().getItems().updateItemAt(position, item);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(), 16));
                item.getMarker().showInfoWindow();
                save.setEnabled(true);
                getFaxAdapter().setSelectionId(item.getId());

            }
        });

        buildGoogleApiClient();

        Toolbar toolbar = (Toolbar) findViewById(R.id.fax_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Faxziel auswählen");


        doctype = getIntent().getExtras().getString("type");
        docdate = getIntent().getExtras().getString("date");

        cancel.setOnClickListener(this);
        save.setOnClickListener(this);


        if (doctype.equals("SDL")) {

            docid = getIntent().getExtras().getString("id");
            doctypetext = "SDL (" + docdate + ")";

        }

        if (doctype.equals("ausbleibe")) {

            docmonat = getIntent().getExtras().getString("monat");
            docjahr = getIntent().getExtras().getString("jahr");
            excludestring = getIntent().getExtras().getString("excludestring");
            doctypetext = "Ausbleibezeiten (" + docdate + ")";

        }

        if (doctype.equals("auslagen")) {

            docmonat = getIntent().getExtras().getString("monat");
            docjahr = getIntent().getExtras().getString("jahr");
            excludestring = getIntent().getExtras().getString("excludestring");
            doctypetext = "Fahrauslagen (" + docdate + ")";

        }

        getSupportActionBar().setSubtitle(doctypetext);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    map.setMyLocationEnabled(true);

                } else {

                    finish();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mFaxListFragment.setAdapter(getFaxAdapter());
        super.onSaveInstanceState(outState);
    }

    private RecyclerView getFaxList() {
        return ((RecyclerView) findViewById(R.id.fax_destinations));
    }

    private FaxAdapter getFaxAdapter() {
        return ((FaxAdapter) ((RecyclerView) findViewById(R.id.fax_destinations)).getAdapter());
    }

    private void setWaitText(String text) {

        ((TextView) findViewById(R.id.fax_wait_text)).setText(text);

    }

    private void showFaxList(boolean showList) {
        View wait = findViewById(R.id.fax_wait);
        View error = findViewById(R.id.list_error);

        if (showList) {
            if (wait.getVisibility() == View.VISIBLE) {
                wait.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_out));
                wait.setVisibility(View.GONE);
            }

            if (error.getVisibility() == View.VISIBLE) {
                error.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_out));
                error.setVisibility(View.GONE);
            }
        }

        if (!showList) {
            if (wait.getVisibility() == View.GONE) {
                wait.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_in));
                wait.setVisibility(View.VISIBLE);
            }

            if (error.getVisibility() == View.VISIBLE) {
                error.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_out));
                error.setVisibility(View.GONE);
            }
        }

    }

    private void showError(String message, int image) {
        View wait = findViewById(R.id.fax_wait);
        View error = findViewById(R.id.list_error);

        TextView errortext = (TextView) findViewById(R.id.list_error_text);
        ImageView errorimage = (ImageView) findViewById(R.id.list_error_image);

        errortext.setText(message);
        errorimage.setImageResource(image);


        if (wait.getVisibility() == View.VISIBLE) {
            wait.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_out));
            wait.setVisibility(View.GONE);
        }

        if (error.getVisibility() == View.GONE) {
            error.startAnimation(AnimationUtils.loadAnimation(FaxActivity.this, android.R.anim.fade_in));
            error.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fax_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_copy_fax:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                return true;
            case R.id.action_refresh_fax:
                getFaxes();
                return true;
            case R.id.action_number_fax:
                DialogFragment newFragment = FaxNumberDialog.newInstance();
                newFragment.show(getSupportFragmentManager(), "faxNumberDialog");
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {
        // Handle presses on the action bar items
        switch (view.getId()) {
            case R.id.fax_back:
                setResult(400);
                finish();
                break;
            case R.id.fax_send:
                DoSend(null);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getFaxAdapter() != null && getFaxAdapter().getSelectionId() != -1) {
            getFaxAdapter().setSelectionId(-1);
            save.setEnabled(false);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, FaxActivity.this);
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }


        ProcessFax();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(final Location location) {

        mCurrentLocation = location;
        ProcessFax();
    }

    private void ProcessFax() {

        if (mCurrentLocation == null)
            return;

        if (mCurrentLocation.getAccuracy() < 10000 && faxRequest == null && getFaxAdapter().getItemCount() == 0)
            getFaxes();


        if (getFaxAdapter() != null && !getFaxList().getItemAnimator().isRunning()) {

            for (int i = 0; i < getFaxAdapter().getItems().size(); i++) {
                FaxClass item = getFaxAdapter().getItems().get(i);
                item.setDistance(item.getDistanceTo(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())) / 1000);
                getFaxAdapter().getItems().updateItemAt(i, item);
            }



        }

    }

    public void DoSend(String number) {

        if (number != null) {
            docdest = "number=" + number;
        } else {
            FaxClass item = getFaxAdapter().getSelectedItem();
            docdest = "destid=" + item.getId();
        }

        if (menu.findItem(R.id.action_copy_fax).isChecked())
            docdouble = "&double=1";


        new SendFax().execute();

    }

    private void getFaxes() {

        if (faxRequest != null)
            faxRequest.cancel(true);

        if (mCurrentLocation == null)
            return;

        setWaitText("Faxgeräte werden abgerufen...");
        showFaxList(false);
        getFaxAdapter().clear();
        save.setEnabled(false);

        faxRequest = new OSESRequest(FaxActivity.this);
        faxRequest.setUrl("https://oses.mobi/api.php?request=list_fax&type=new&lat=" + mCurrentLocation.getLatitude() + "&lon=" + mCurrentLocation.getLongitude() + "&session=" + OSES.getSession().getIdentifier());

        faxRequest.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(String response) {

                map.clear();
                showFaxList(true);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                try {
                    JSONArray faxes = new JSONArray(response);


                    for (int i = 0; i < faxes.length(); i++) {
                        JSONObject fax = faxes.getJSONObject(i);

                        FaxClass currentFax = new FaxClass();

                        currentFax.setId(fax.optInt("id", -1));
                        currentFax.setGb(fax.optString("gb"));
                        currentFax.setBeschreibung(fax.optString("beschreibung"));
                        currentFax.setPosition(new LatLng(fax.optDouble("lat"), fax.optDouble("lon")));
                        currentFax.setFax(fax.optString("fax"));
                        currentFax.setDistance(fax.optDouble("distance"));
                        currentFax.setRil100(fax.optString("ril100"));
                        currentFax.setName(fax.optString("name"));

                        if (i < 3)
                            builder.include(currentFax.getPosition());

                        Marker marker = map.addMarker(new MarkerOptions().position(currentFax.getPosition()).title(currentFax.getName()).snippet(currentFax.getBeschreibung() + " (" + currentFax.getGb() + ")"));
                        currentFax.setMarker(marker);

                        getFaxAdapter().getItems().add(currentFax);

                    }
                } catch (JSONException e) {
                    showError("Die Antwort des Servers konnte leider nicht verarbeitet werden...", R.drawable.ic_action_fail);
                    e.printStackTrace();
                    return;
                }

                LatLngBounds bounds = builder.build();

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            }

            @Override
            public void onRequestException(Exception e) {

                showError(e.getLocalizedMessage(), R.drawable.ic_action_fail);

            }

            @Override
            public void onRequestUnknown(int status) {

            }

            @Override
            public void onIsNotConnected() {

                showError("Keine Internetverbindung", R.drawable.ic_action_cloud_off);

            }
        });

        faxRequest.execute();
    }

    private class SendFax extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;

        protected void onPreExecute() {

            dialog = ProgressDialog.show(FaxActivity.this, "Bitte warten...",
                    "Fax wird in Auftrag gegeben...", true);

        }

        protected String doInBackground(Void... params) {

            String docextra = "";

            if (doctype.equals("SDL")) {
                docextra = "&id=" + docid;
            }

            if (doctype.equals("ausbleibe") || doctype.equals("auslagen")) {
                docextra = "&jahr=" + docjahr + "&monat=" + docmonat;
            }

            String query = "https://oses.mobi/api.php?request=fax&session=" + OSES.getSession().getIdentifier() + docextra + "&" + docdest + "&command=" + doctype + docdouble + excludestring;

            return OSES.getJSON(query, 60000);

        }

        protected void onPostExecute(String response) {

            int StatusCode;
            String StatusR;
            String StatusExtra1 = "";

            JSONObject json;

            dialog.dismiss();

            try {

                json = new JSONObject(response);

                StatusCode = json.getInt("StatusCode");
                StatusR = json.getString("StatusR");
                if (json.has("StatusExtra1"))
                    StatusExtra1 = json.getString("StatusExtra1");

                Bundle extra = new Bundle();
                extra.putString("doctype", doctype);
                extra.putString("status", String.valueOf(StatusCode));
                mFirebaseAnalytics.logEvent("OSES_fax_sent", extra);


                switch (StatusCode) {
                    case 200:
                        Toast.makeText(FaxActivity.this, StatusR, Toast.LENGTH_LONG).show();
                        setResult(200);
                        FaxActivity.this.finish();
                        return;
                    case 410:

                        JSONArray jaEst = new JSONArray(StatusExtra1);
                        String sMessage = StatusR + "\n\n";

                        for (int i = 0; i < jaEst.length(); i++) {
                            JSONObject joEst = jaEst.getJSONObject(i);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
                            Date date = format.parse(joEst.getString("datum"));
                            format = new SimpleDateFormat("dd.MM.", Locale.GERMAN);
                            sMessage += joEst.getString("name") + " (" + joEst.getString("ril100") + ") am " + format.format(date) + "\n";
                        }

                        new AlertDialog.Builder(FaxActivity.this)
                                .setTitle("Fehler")
                                .setMessage(sMessage)
                                .setPositiveButton("Berichtigen (Web)", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String url = "https://oses.mobi/system.php?action=pers&tab=e";
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(url));
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("Schließen", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        break;
                    default:
                        Toast.makeText(FaxActivity.this, StatusR, Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(FaxActivity.this, "Anwendungsfehler: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }


        }

    }


}

