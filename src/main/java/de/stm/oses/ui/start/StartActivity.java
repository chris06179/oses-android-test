package de.stm.oses.ui.start;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.stm.oses.R;
import de.stm.oses.dialogs.IndexInfoDialog;
import de.stm.oses.dokumente.DokumenteFragment;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.FileDownload.OnDownloadFinishedListener;
import de.stm.oses.helper.MenuAdapter;
import de.stm.oses.helper.MenuClass;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.index.IndexIntentService;
import de.stm.oses.schichten.SchichtenFragment;
import de.stm.oses.ui.browser.BrowserFragment;
import de.stm.oses.ui.login.LoginActivity;
import de.stm.oses.ui.settings.SettingsFragment;
import de.stm.oses.verwendung.VerwendungFragment;


public class StartActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE_DOWNLOAD = 5500;

    //Your member variable declaration here

    private Handler mHandler = new Handler();
    private OSESBase OSES;

    private MenuAdapter mMenuAdapter;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mLeftDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private OnDownloadFinishedListener onUpdateResponse;

    public FileDownload CurrentFileDownload;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("MenuSelection", getMenuSelection());
        super.onSaveInstanceState(outState);
    }

    private void setDrawer(boolean show) {

        if (mDrawerLayout == null)
            return;

        if (show) {
            if (!mDrawerLayout.isDrawerOpen(mLeftDrawer))
                mDrawerLayout.openDrawer(mLeftDrawer);
        }

        if (!show) {
            if (mDrawerLayout.isDrawerOpen(mLeftDrawer))
                mDrawerLayout.closeDrawer(mLeftDrawer);
        }

    }

    private void startFileIndexer() {
        if (IndexIntentService.startService(this, OSES)) {
            if (!OSES.getSession().getSessionIndexReminder()) {
                IndexInfoDialog dialog = IndexInfoDialog.newInstance();
                dialog.show(getSupportFragmentManager(), "indexInfoDialog");
                OSES.getSession().setSessionIndexReminder(true);
            }
        }
    }


    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
        boolean useDev = settings.getBoolean("debugUseDevServer", false);

        if (useDev) {
            TextView devModeText = findViewById(R.id.devModeText);
            if (devModeText != null)
                devModeText.setVisibility(View.VISIBLE);
        }

        OSES = new OSESBase(this);

        // Settings holen, blockiere Upload zum Server
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.default_remote_config).addOnCompleteListener(task -> startFileIndexer());
        mFirebaseRemoteConfig.fetchAndActivate();

        mFirebaseAnalytics.setUserProperty("est", OSES.getSession().getEstText());
        mFirebaseAnalytics.setUserProperty("funktion", String.valueOf(OSES.getSession().getFunktion()));
        mFirebaseAnalytics.setUserProperty("gb", OSES.getSession().getGBText());
        mFirebaseAnalytics.setUserId(String.valueOf(OSES.getSession().getUserId()));

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ListView mMenuList = findViewById(R.id.menu_list);

        mLeftDrawer = findViewById(R.id.left_drawer);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        if (mDrawerLayout != null) {

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    toolbar,  /* nav drawer icon to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description */
                    R.string.drawer_closed  /* "close drawer" description */
            ) {

                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };

            mDrawerLayout.addDrawerListener(mDrawerToggle);

            assert getSupportActionBar() != null;

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        assert mMenuList != null;

        mMenuList.setDividerHeight(0);

        View header = getLayoutInflater().inflate(R.layout.menu_header, mMenuList, false);

        mMenuList.addHeaderView(header);
        mMenuAdapter = new MenuAdapter(this, generateMenu());
        mMenuList.setAdapter(mMenuAdapter);


        mMenuList.setOnItemClickListener((parent, view, position, id) -> {
            if (id >= 0) {
                Integer selected = mMenuAdapter.getItem((int) id).getID();

                switch (selected) {
                    case 11:
                        setMenuSelection(id);
                        openWebView("aktuell");
                        setDrawer(false);
                        return;
                    case 22:
                        setMenuSelection(id);
                        ChangeFragment(new VerwendungFragment(), "verwendung");
                        setDrawer(false);
                        return;
                    case 21:
                        setMenuSelection(id);
                        ChangeFragment(new SchichtenFragment(), "schichten");
                        setDrawer(false);
                        return;
                    case 31:
                        setMenuSelection(id);
                        ChangeFragment(new DokumenteFragment(), "dokumente");
                        setDrawer(false);
                        return;
                    case 81:
                        DoBenutzerprofil();
                        setDrawer(false);
                        return;
                    case 82:
                        setMenuSelection(id);
                        ChangeFragment(new SettingsFragment(), "settings");
                        setDrawer(false);
                        return;
                    case 84:
                        DoLogout();
                        setDrawer(false);
                        return;
                    case 92:
                        setMenuSelection(id);
                        openWebView("hilfe");
                        setDrawer(false);
                        return;
                    case 93:
                        setMenuSelection(id);
                        openWebView("anb");
                        setDrawer(false);
                        return;
                    case 94:
                        setMenuSelection(id);
                        openWebView("impressum");
                        setDrawer(false);
                        return;
                    case 95:
                        setMenuSelection(id);
                        openWebView("spenden");
                        setDrawer(false);
                        return;
                    case 99:
                        OpenOSES();
                        setDrawer(false);
                        return;
                }
            }
        });

        if (savedInstanceState != null) {

            Integer MenuSelection = savedInstanceState.getInt("MenuSelection");

            setMenuSelection(MenuSelection);

        }


        TextView menu_name = findViewById(R.id.menu_name);
        if (menu_name != null)
            menu_name.setText(OSES.getSession().getVorname() + ' ' + OSES.getSession().getNachname());

        TextView menu_est = findViewById(R.id.menu_est);
        if (menu_est != null)
            menu_est.setText(OSES.getSession().getEstText());

        TextView menu_gb = findViewById(R.id.menu_gb);
        if (menu_gb != null)
            menu_gb.setText(OSES.getSession().getGBText());

        new CheckSession().execute();

        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        androidx.fragment.app.Fragment running = fragmentManager.findFragmentById(R.id.content_frame);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);


        if (getIntent().hasExtra("fragment")) {
            String startFragment = getIntent().getExtras().getString("fragment");

            if (startFragment != null && startFragment.equals("browser") && getIntent().hasExtra("type")) {
                String type = getIntent().getExtras().getString("type");
                ChangeFragment(new BrowserFragment(), type);
                if (type != null && type.equals("aktuell"))
                    setMenuSelection(0);
                return;
            }
        } else if (running != null) {
            fragmentTransaction.replace(R.id.content_frame, running, running.getTag());
        } else {
            VerwendungFragment fragment = new VerwendungFragment();
            fragmentTransaction.replace(R.id.content_frame, fragment, "verwendung");
        }

        fragmentTransaction.commit();

        onUpdateResponse = new OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri fileUri = FileProvider.getUriForFile(StartActivity.this, "de.stm.oses.FileProvider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION+Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    startActivity(intent);
                } else {
                    Uri apkUri = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                finish();

            }

            @Override
            public void onTextReceived(String res) {

            }

            @Override
            public void onException(Exception e) {

                if (e instanceof FileDownload.NoDownloadPermissionException) {

                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        Toast.makeText(StartActivity.this, "Automatisches Update nicht möglich! Weiterleitung zum Google Play Store...", Toast.LENGTH_SHORT).show();
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
                        ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity"); // package name and activity
                        launchIntent.setComponent(comp);
                        launchIntent.setData(Uri.parse("market://details?id="+appPackageName));
                        startActivity(launchIntent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }

                    Bundle extra = new Bundle();
                    extra.putString("reason", "DOWNLOAD_PERMISSION_REFUSED");
                    mFirebaseAnalytics.logEvent("OSES_force_update_gplay", extra);

                    finish();

                }

            }

            @Override
            public void onUnknownStatus(int status) {

            }
        };

    }

    private void DoBenutzerprofil() {
        new AlertDialog.Builder(this)
                .setTitle("Benutzerprofil")
                .setMessage("Das Benutzerprofil kann derzeit nicht in der Android-App bearbeitet werden, bitte melde dich dafür in der Webversion an!")
                .setPositiveButton("Weiter zur Webversion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = "https://oses.mobi/";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                })
                .setNegativeButton("Später", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void openWebView(String request) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment running = fragmentManager.findFragmentById(R.id.content_frame);

        if (running instanceof BrowserFragment) {
            ((BrowserFragment) running).setRequest(request);
        } else

        ChangeFragment(new BrowserFragment(), request);

    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment running = fragmentManager.findFragmentById(R.id.content_frame);

        if (mDrawerLayout != null) {

            if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {

                mDrawerLayout.closeDrawer(mLeftDrawer);
                return;

            }
        }

        if (running instanceof SchichtenFragment) {

            ActionMode schichtenAction = ((SchichtenFragment) running).mActionMode;

            if (schichtenAction != null) {
                schichtenAction.finish();
                return;
            }

        }

        if (running instanceof VerwendungFragment) {

            ActionMode verwendungAction = ((VerwendungFragment) running).mActionMode;

            if (verwendungAction != null) {
                verwendungAction.finish();
                return;
            }

            Calendar c = Calendar.getInstance();
            if (((VerwendungFragment) running).selectedyear != c.get(Calendar.YEAR) || ((VerwendungFragment) running).selectedmonth != c.get(Calendar.MONTH) + 1) {
                ((VerwendungFragment) running).showCurrentMonth();
                return;
            }
        }

        if (!(running instanceof VerwendungFragment)) {

            setMenuSelection(4);
            ChangeFragment(new VerwendungFragment(), "verwendung");
            return;

        }

        super.onBackPressed();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void ChangeFragment(Fragment fragment, String ident, Bundle args) {

        mHandler.postDelayed(new ChangeFragmentRunnable(fragment, ident, args), 300);

    }

    private void ChangeFragment(Fragment fragment, String ident) {

        mHandler.postDelayed(new ChangeFragmentRunnable(fragment, ident), 300);

    }

    private void ChangeFragment(Fragment fragment, String ident, boolean AddToBackStack) {

        mHandler.postDelayed(new ChangeFragmentRunnable(fragment, ident, AddToBackStack), 300);

    }


    private class ChangeFragmentRunnable implements Runnable {
        private Fragment fragment;
        private String ident;
        private Bundle args;
        private boolean AddToBackStack = false;

        ChangeFragmentRunnable(Fragment fragment, String ident, Bundle args) {
            this.fragment = fragment;
            this.ident = ident;
            this.args = args;
        }

        ChangeFragmentRunnable(Fragment fragment, String ident) {
            this.fragment = fragment;
            this.ident = ident;
            this.args = null;
        }

        ChangeFragmentRunnable(Fragment fragment, String ident, boolean AddToBackStack) {
            this.fragment = fragment;
            this.ident = ident;
            this.args = null;
            this.AddToBackStack = AddToBackStack;
        }

        public void run() {


                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment running = fragmentManager.findFragmentByTag(ident);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                if (running != null && !running.isRemoving()) {
                    fragmentTransaction.replace(R.id.content_frame, running, running.getTag());
                } else {
                    if (args != null)
                        fragment.setArguments(args);

                    if (AddToBackStack)
                        fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.content_frame, fragment, ident);
                }
                fragmentTransaction.commit();

        }
    }

    private ArrayList<MenuClass> generateMenu() {
        ArrayList<MenuClass> items = new ArrayList<>();
        items.add(new MenuClass("Einstieg"));
        items.add(new MenuClass(R.drawable.ic_action_cloud, "Aktuelles", 11, ""));
        items.add(new MenuClass("Erfassung"));
        items.add(new MenuClass(R.drawable.ic_action_important, "Referenzschichten", 21, ""));
        items.add(new MenuClass(R.drawable.ic_action_storage, "Verwendung", 22, "", true));
        items.add(new MenuClass("Werkzeuge"));
        items.add(new MenuClass(R.drawable.ic_action_download_grey, "Dokumente", 31, ""));
        //items.add(new MenuClass(R.drawable.ic_action_cloud,"Ereignisse",32,""));
        items.add(new MenuClass("System"));
        items.add(new MenuClass(R.drawable.ic_user_profile,"Benutzerprofil",81,""));
        items.add(new MenuClass(R.drawable.ic_action_settings, "Einstellungen", 82, ""));
        items.add(new MenuClass(R.drawable.ic_action_cancel, "Abmelden", 84, ""));
        items.add(new MenuClass("Sonstiges"));
        //items.add(new MenuClass(R.drawable.ic_action_help, "Hilfe", 92, ""));
        items.add(new MenuClass(R.drawable.ic_action_donate, "Spenden", 95, ""));
        items.add(new MenuClass(R.drawable.ic_action_about, "Nutzungsbedingungen", 93, ""));
        items.add(new MenuClass(R.drawable.ic_action_para, "Impressum", 94, ""));
        items.add(new MenuClass(R.drawable.ic_action_web_site, "Webversion", 99, ""));

        return items;
    }


    private void setMenuSelection(long id) {

        for (int i = 0; i < mMenuAdapter.getCount(); i++) {
            mMenuAdapter.getItem(i).setSelected(false);

        }

        MenuClass item = mMenuAdapter.getItem((int) id);

        if (item.isGroupHeader())
            return;

        item.setSelected(true);
        mMenuAdapter.notifyDataSetChanged();

    }

    private int getMenuSelection() {

        for (int i = 0; i < mMenuAdapter.getCount(); i++) {
            if (mMenuAdapter.getItem(i).isSelected()) return i;

        }

        return -1;

    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE_DOWNLOAD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 ) {
                    CurrentFileDownload.resumeAfterPermissionCallback(grantResults[0]);
                } else {
                    CurrentFileDownload.resumeAfterPermissionCallback(0);
                }

            }
        }
    }




    private void OpenOSES() {

        mFirebaseAnalytics.logEvent("OSES_open_web", null);
        String url = "https://oses.mobi/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    private void DoLogout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Willst du dich wirklich Abmelden?")
                .setCancelable(false)
                .setTitle("Abmelden")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                        startActivity(intent);
                        StartActivity.this.finish();

                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


    }

    private void DoWebpage(final String url, String title, String message, final String requestIdent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);

                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putString("DontAskWebpage", requestIdent);

                        editor.apply();

                    }
                })
                .setNeutralButton("Später", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
                        SharedPreferences.Editor editor = settings.edit();

                        Calendar c = Calendar.getInstance();
                        editor.putLong("DelayWebpage", c.getTimeInMillis() +  60 * 1000);

                        editor.apply();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


    }


    private boolean isInstalledViaPlayStore() {

        return true;

       // String installer = getPackageManager().getInstallerPackageName(getPackageName());

        //return installer != null && installer.equals("com.android.vending");

    }


    private class CheckSession extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {


            String zusatz = "";

            if (params.length > 0)
                zusatz = params[0];

            String url = "https://oses.mobi/api.php?request=checksession" + zusatz + "&androidversion=" + OSES.getVersionCode() + "&sdk=" + OSES.getSDKLevel() + "&sdkstring=" + OSES.getSDKString() + "&session=" + OSES.getSession().getIdentifier()+"&fcmid=" + OSES.getSession().getSessionFcmInstanceId();

            return OSES.getJSON(url, 60000);


        }

        protected void onPostExecute(String response) {

            String StatusCode = "";
            String StatusMessage = "";

            String SessionUsername = "";
            int SessionUserId = -1;
            int SessionGroup = 90;
            int SessionEst = 0;
            String SessionEstText = "";
            String SessionVorname = "";
            String SessionNachname = "";
            int SessionGB = 0;
            String SessionGBText = "";
            int SessionFunktion = 0;

            String DataEstOwn = "";
            String DataEstAll = "";
            String DataFunktionen = "";
            String DataGB = "";

            boolean Webpage = false;

            String WebpageURL = "";
            String WebpageIdent = "";
            String WebpageTitle = "";
            String WebpageMessage = "";

            String UpdateFileName = "OSES.apk";

            try {

                JSONObject json = new JSONObject(response);

                StatusCode = json.getString("StatusCode");
                if (json.has("StatusMessage"))
                    StatusMessage = json.getString("StatusMessage");

                if (json.has("UpdateFileName"))
                    UpdateFileName = json.getString("UpdateFileName");

                DataEstOwn = json.getString("DataEstOwn");
                DataEstAll = json.getString("DataEstAll");
                DataFunktionen = json.getString("DataFunktionen");
                DataGB = json.getString("DataGB");

                SessionUsername = json.getString("SessionUsername");
                SessionUserId = json.getInt("SessionId");
                SessionGroup = json.getInt("SessionGruppe");
                SessionVorname = json.getString("SessionVorname");
                SessionNachname = json.getString("SessionName");
                SessionEst = json.getInt("SessionEst");
                SessionEstText = json.getString("SessionEstText");
                SessionGB = json.getInt("SessionGB");
                SessionGBText = json.getString("SessionGBText");
                SessionFunktion = json.getInt("SessionFunktion");



                if (json.has("WebpageURL") && json.has("WebpageIdent") && json.has("WebpageTitle") && json.has("WebpageMessage")) {
                    Webpage = true;
                    WebpageIdent = json.getString("WebpageIdent");
                    WebpageURL = json.getString("WebpageURL");
                    WebpageTitle = json.getString("WebpageTitle");
                    WebpageMessage = json.getString("WebpageMessage");
                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
            SharedPreferences.Editor editor = settings.edit();


            if (StatusCode.equals("200")) {

                editor.putString("StatusCode", StatusCode);
                editor.putString("StatusMessage", StatusMessage);
                editor.putString("StatusCode", StatusCode);
                editor.putString("SessionUsername", SessionUsername);
                editor.putInt("SessionUserId", SessionUserId);
                editor.putInt("SessionGruppe", SessionGroup);
                editor.putInt("SessionEst", SessionEst);
                editor.putString("SessionEstText", SessionEstText);
                editor.putString("SessionVorname", SessionVorname);
                editor.putString("SessionNachname", SessionNachname);
                editor.putInt("SessionGB", SessionGB);
                editor.putString("SessionGBText", SessionGBText);
                editor.putInt("SessionFunktion", SessionFunktion);

                editor.putString("DataEstOwn", DataEstOwn);
                editor.putString("DataEstAll", DataEstAll);
                editor.putString("DataFunktionen", DataFunktionen);
                editor.putString("DataGB", DataGB);

                // Commit the edits!
                editor.apply();


                TextView menu_name = findViewById(R.id.menu_name);
                if (menu_name != null)
                    menu_name.setText(SessionVorname + ' ' + SessionNachname);

                TextView menu_est = findViewById(R.id.menu_est);
                if (menu_est != null)
                    menu_est.setText(SessionEstText);

                TextView menu_gb = findViewById(R.id.menu_gb);
                if (menu_gb != null)
                    menu_gb.setText(SessionGBText);


                if (Webpage && !settings.getString("DontAskWebpage", "").equals(WebpageIdent)) {

                    Calendar c = Calendar.getInstance();
                    Calendar d = Calendar.getInstance();

                    d.setTimeInMillis(settings.getLong("DelayWebpage", 0));

                    if (c.after(d))
                        DoWebpage(WebpageURL, WebpageTitle, WebpageMessage, WebpageIdent);

                }


            }

            final String UpdateFile = UpdateFileName;

            if (StatusCode.equals("100")) {

                mFirebaseAnalytics.logEvent("OSES_update_start", null);

                AlertDialog.Builder alert = new AlertDialog.Builder(StartActivity.this);

                alert.setCancelable(false);

                alert.setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if (isInstalledViaPlayStore()) {

                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                Toast.makeText(StartActivity.this, "Weiterleitung zum Google Play Store...", Toast.LENGTH_SHORT).show();
                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
                                ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity"); // package name and activity
                                launchIntent.setComponent(comp);
                                launchIntent.setData(Uri.parse("market://details?id="+appPackageName));
                                startActivity(launchIntent);
                            } catch (ActivityNotFoundException | NullPointerException e) {
                                Crashlytics.log("opening GPLAY failed");
                                Crashlytics.logException(e);
                            } finally {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }

                            mFirebaseAnalytics.logEvent("OSES_update_gplay", null);

                            finish();
                        }

                        else {

                            mFirebaseAnalytics.logEvent("OSES_update_self", null);
                            FileDownload download = new FileDownload(StartActivity.this);
                            download.setTitle("Aktualisierung");
                            download.setMessage("Installationsdatei wird heruntergeladen, dieser Vorgang kann einige Minuten dauern...");
                            download.setURL("https://oses.mobi/android/" + UpdateFile);
                            download.setLocalDirectory("APK/");
                            download.setLocalFilename("OSES.apk");
                            download.setOnDownloadFinishedListener(onUpdateResponse);
                            download.execute();

                        }
                    }
                });
                alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                final AlertDialog dialog = alert.create();

                LinearLayout linear = new LinearLayout(StartActivity.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(10, 10, 10, 10);

                WebView wv = new WebView(StartActivity.this);
                wv.setLayoutParams(params);
                wv.loadUrl("https://oses.mobi/api.php?request=changelog&androidversion=" + OSES.getVersionCode() + "&session=" + OSES.getSession().getIdentifier());

                wv.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }, 2000);

                    }
                });

                linear.addView(wv);
                dialog.setView(linear);
                dialog.show();
                dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

            }

            if (StatusCode.equals("101")) {

                AlertDialog.Builder alert = new AlertDialog.Builder(StartActivity.this);

                alert.setCancelable(false);

                alert.setPositiveButton("WOLOLOO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        FileDownload download = new FileDownload(StartActivity.this);
                        download.setTitle("Aktualisierung (BETA)");
                        download.setMessage("Installationsdatei wird heruntergeladen, dieser Vorgang kann einige Minuten dauern...");
                        download.setURL("https://oses.mobi/android/" + UpdateFile);
                        download.setLocalDirectory("APK/");
                        download.setLocalFilename("OSES_BETA.apk");
                        download.setOnDownloadFinishedListener(onUpdateResponse);
                        download.execute();
                    }
                });
                alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                final AlertDialog dialog = alert.create();

                LinearLayout linear = new LinearLayout(StartActivity.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(10, 10, 10, 10);

                WebView wv = new WebView(StartActivity.this);
                wv.setLayoutParams(params);
                wv.loadUrl("https://oses.mobi/api.php?request=changelog&beta=1&androidversion=" + OSES.getVersionCode() + "&session=" + OSES.getSession().getIdentifier());

                wv.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }, 2500);

                    }
                });

                linear.addView(wv);
                dialog.setView(linear);
                dialog.show();
                dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

            }


            if (StatusCode.equals("402")) {

                editor.clear();
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                builder.setMessage("Deine OSES Sitzung ist abgelaufen und ungültig. Bitte melde dich erneut an um fortzufahren!")
                        .setCancelable(false)
                        .setTitle("Sitzung abgelaufen")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.dismiss();
                                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                                startActivity(intent);
                                StartActivity.this.finish();

                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();

            }

            if (StatusCode.equals("500")) {


                AlertDialog.Builder alert = new AlertDialog.Builder(StartActivity.this);

                alert.setTitle(StatusMessage);
                alert.setCancelable(false);

                LinearLayout linear = new LinearLayout(StartActivity.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(10, 10, 10, 10);

                WebView wv = new WebView(StartActivity.this);
                wv.setLayoutParams(params);
                wv.loadUrl("https://oses.mobi/api.php?request=anb&check=true&session=" + OSES.getSession().getIdentifier());

                wv.setWebViewClient(new WebViewClient());

                linear.addView(wv);
                alert.setView(linear);
                alert.setPositiveButton("Akzeptieren", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new CheckSession().execute("&acceptanb=true");
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("Ablehnen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alert.show();

            }

            if (StatusCode.equals("900")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                builder.setMessage(StatusMessage)
                        .setCancelable(false)
                        .setTitle("OSES nicht verfügbar")
                        .setPositiveButton("Anwendung beenden", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();

            }

        }

    }
}