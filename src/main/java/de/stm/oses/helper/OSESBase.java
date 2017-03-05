package de.stm.oses.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.stm.oses.R;

@SuppressLint("PrivateResource")
public class OSESBase {

    private Context context;
    private OSESSession session;
    private String sVersion;
    private Integer iVersionCode;
    private Integer iSDKLevel;
    private String sSDKString;


    public OSESBase(Context context) {
        this.context = context;
        this.session = new OSESSession(context);

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.iVersionCode = packageInfo.versionCode;
            this.sVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            this.iVersionCode = 0;
            this.sVersion = "";
        }

        this.iSDKLevel = android.os.Build.VERSION.SDK_INT;
        this.sSDKString = android.os.Build.VERSION.RELEASE;

    }

    public OSESSession getSession() {
        return session;
    }

    public ListAdapter getEstAdapter() throws JSONException {
        return getEstAdapter(-1, false);
    }

    public ListAdapter getEstAdapter(int selected) throws JSONException {
        return getEstAdapter(selected, false);
    }

    public ListAdapter getEstAdapter(int selected, boolean isSpinner) throws JSONException {

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", 0);

        String DataEstOwn = settings.getString("DataEstOwn", "[]");
        String DataEstAll = settings.getString("DataEstAll", "[]");

        JSONArray JSONDataEstOwn = new JSONArray(DataEstOwn);
        ArrayList<ListClass> ests = new ArrayList<ListClass>();

        ests.add(new ListClass(true, "Eigene"));
        for (int i = 0; i < JSONDataEstOwn.length(); i++)
            if (session.getEst() == JSONDataEstOwn.getJSONObject(i).getInt("est"))
                ests.add(new ListClass(JSONDataEstOwn.getJSONObject(i).getInt("est"), JSONDataEstOwn.getJSONObject(i).getString("ort"), R.drawable.ic_home));
            else
                ests.add(new ListClass(JSONDataEstOwn.getJSONObject(i).getInt("est"), JSONDataEstOwn.getJSONObject(i).getString("ort"), 0));

        JSONArray JSONDataEstAll = new JSONArray(DataEstAll);

        ests.add(new ListClass(true, "Alle"));
        for (int i = 0; i < JSONDataEstAll.length(); i++)
            if (session.getEst() == JSONDataEstAll.getJSONObject(i).getInt("id"))
                ests.add(new ListClass(JSONDataEstAll.getJSONObject(i).getInt("id"), JSONDataEstAll.getJSONObject(i).getString("ort"), R.drawable.ic_home));
            else
                ests.add(new ListClass(JSONDataEstAll.getJSONObject(i).getInt("id"), JSONDataEstAll.getJSONObject(i).getString("ort"), 0));

        for (int i = 0; i < ests.size(); i++)
            if (ests.get(i).getId() == selected) {
                ests.get(i).setSelected(true);
                break;
            }

        if (isSpinner)
            return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), ests);
        else
            return new ListAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), ests);

    }

    public ListAdapter getFunktionAdapter() throws JSONException {
        return getFunktionAdapter(-1);
    }

    public ListAdapter getFunktionAdapter(int selected) throws JSONException {
        return getFunktionAdapter(selected, false);
    }

    public ListAdapter getFunktionAdapter(int selected, boolean isSpinner) throws JSONException {

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", 0);

        String DataFunktionen = settings.getString("DataFunktionen", "[]");

        JSONArray JSONDataFunktionen = new JSONArray(DataFunktionen);
        ArrayList<ListClass> funktionen = new ArrayList<ListClass>();


        for (int i = 0; i < JSONDataFunktionen.length(); i++)
            funktionen.add(new ListClass(JSONDataFunktionen.getJSONObject(i).getInt("id"), JSONDataFunktionen.getJSONObject(i).getString("name")));


        for (int i = 0; i < funktionen.size(); i++)
            if (funktionen.get(i).getId() == selected) {
                funktionen.get(i).setSelected(true);
                break;
            }
        if (isSpinner)
            return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), funktionen);
        else
            return new ListAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), funktionen);

    }

    public ListAdapter getGBAdapter() throws JSONException {
        return getGBAdapter(-1);
    }

    public ListAdapter getGBAdapter(int selected) throws JSONException {
        return getGBAdapter(selected, false);
    }

    public ListAdapter getGBAdapter(int selected, boolean isSpinner) throws JSONException {

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", 0);

        String DataGB = settings.getString("DataGB", "[]");

        JSONArray JSONDataGB = new JSONArray(DataGB);
        ArrayList<ListClass> GB = new ArrayList<ListClass>();


        for (int i = 0; i < JSONDataGB.length(); i++)
            GB.add(new ListClass(JSONDataGB.getJSONObject(i).getInt("id"), JSONDataGB.getJSONObject(i).getString("name")));


        for (int i = 0; i < GB.size(); i++)
            if (GB.get(i).getId() == selected) {
                GB.get(i).setSelected(true);
                break;
            }

        if (isSpinner)
            return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), GB);
        else
            return new ListAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), GB);


    }

    public ListAdapter getPauseAdapter(boolean showNull) {
        return getPauseAdapter(-1, showNull);
    }

    public ListAdapter getPauseAdapter(int selected, boolean showNull) {


        ArrayList<ListClass> pause = new ArrayList<ListClass>();

        if (showNull)
            pause.add(new ListClass(-1, "Keine Änderung"));
        pause.add(new ListClass(0, "Keine Pause"));
        pause.add(new ListClass(15, "15 Minuten"));
        pause.add(new ListClass(30, "30 Minuten"));
        pause.add(new ListClass(45, "45 Minuten"));
        pause.add(new ListClass(60, "60 Minuten"));


        for (int i = 0; i < pause.size(); i++)
            if (pause.get(i).getId() == selected) {
                pause.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), pause);


    }

    public ListAdapter getDokumenteAdapter() {

        ArrayList<ListClass> dokumente = new ArrayList<ListClass>();

        dokumente.add(new ListClass("ausbleibe", "Ausbleibezeiten"));
        dokumente.add(new ListClass("auslagen", "Fahrauslagen"));
        dokumente.add(new ListClass("steuer", "Steuernachweis"));

        dokumente.get(0).setSelected(true);

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), dokumente);

    }

    public ListAdapter getExcludeAdapter() {

        ArrayList<ListClass> exclude = new ArrayList<ListClass>();

        exclude.add(new ListClass("", "Nein"));
        exclude.add(new ListClass("SKIP", "Ja, Zeile überspringen"));
        exclude.add(new ListClass("FREE", "Ja, Zeile frei lassen"));

        exclude.get(0).setSelected(true);

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), exclude);

    }

    public ListAdapter getKategorieAdapter(String selected) {

        ArrayList<ListClass> kategorie = new ArrayList<ListClass>();

        kategorie.add(new ListClass("S", "Schicht", "#A4A4A4"));
        kategorie.add(new ListClass("D", "Dispo", "#0080FF"));
        kategorie.add(new ListClass("R", "Ruhe", "#FFFF00"));
        kategorie.add(new ListClass("U", "Urlaub", "#00C000"));
        kategorie.add(new ListClass("K", "Krank", "#FF8000"));
        kategorie.add(new ListClass("T", "Streik", "#FF0000"));
        //kategorie.add(new ListClass("F", "Fortbildung",  "#5E00AA"));
        kategorie.add(new ListClass("B", "Büro", "#804000"));
        //kategorie.add(new ListClass("O", "Sonstiges",  "#282828"));

        for (int i = 0; i < kategorie.size(); i++)
            if (kategorie.get(i).getIdent().equals(selected)) {
                kategorie.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), kategorie, true);


    }

    public ListAdapter getAnrechnungAdapter(int selected) {

        ArrayList<ListClass> anrechnung = new ArrayList<ListClass>();

        anrechnung.add(new ListClass(0, "0%"));
        anrechnung.add(new ListClass(50, "50%"));
        anrechnung.add(new ListClass(100, "100%"));

        for (int i = 0; i < anrechnung.size(); i++)
            if (anrechnung.get(i).getId() == selected) {
                anrechnung.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), anrechnung);

    }

    public ListAdapter getAufenthaltAdapter(String selected) {

        ArrayList<ListClass> anrechnung = new ArrayList<ListClass>();

        anrechnung.add(new ListClass("auto", "Automatisch"));
        anrechnung.add(new ListClass("manual", "Manuell"));

        for (int i = 0; i < anrechnung.size(); i++)
            if (anrechnung.get(i).getIdent().equals(selected)) {
                anrechnung.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), anrechnung);

    }

    public ListAdapter getDispoAdapter(String selected) {

        ArrayList<ListClass> dispo = new ArrayList<ListClass>();

        dispo.add(new ListClass("DF", "Früh"));
        dispo.add(new ListClass("DT", "Tag"));
        dispo.add(new ListClass("DS", "Spät"));
        dispo.add(new ListClass("DN", "Nacht"));
        dispo.add(new ListClass("DX", "ohne Absage"));

        for (int i = 0; i < dispo.size(); i++)
            if (dispo.get(i).getIdent().equals(selected)) {
                dispo.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), dispo);

    }

    public ListAdapter getUrlaubAdapter(String selected) {

        ArrayList<ListClass> dispo = new ArrayList<ListClass>();

        dispo.add(new ListClass("U", "Erholungsurlaub"));
        dispo.add(new ListClass("UB", "Bildungsurlaub"));
        dispo.add(new ListClass("US", "Sonderurlaub"));
        dispo.add(new ListClass("UN", "Sonstiger Urlaub (ohne Anrechnung)"));

        for (int i = 0; i < dispo.size(); i++)
            if (dispo.get(i).getIdent().equals(selected)) {
                dispo.get(i).setSelected(true);
                break;
            }

        return new ListSpinnerAdapter(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog), dispo);

    }

    public String getVersion() {
        return sVersion;
    }

    public Integer getVersionCode() {
        return iVersionCode;
    }

    public Integer getSDKLevel() {
        return iSDKLevel;
    }

    public String getSDKString() {
        return sSDKString;
    }

    public String getJSON(String url, Map<String, String> params, int timeout) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDev = settings.getBoolean("debugUseDevServer", false);

        if (useDev) {
            url = url.replace("https://oses.mobi", "https://dev.oses.mobi");
        }

        try {
            URL u = new URL(url);
            HttpsURLConnection c = (HttpsURLConnection) u.openConnection();

            if (useDev) {
                final String devUser = settings.getString("debugDevServerUser", "");
                final String devPass = settings.getString("debugDevServerPass", "");

                if (devUser.length() == 0 || devPass.length() == 0)
                    throw new IOException("DEV: username or password may not be empty");

                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(devUser, devPass.toCharArray());
                    }
                });
            }

            c.setRequestProperty("User-Agent", "OSES for Android " + getVersion());
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);

            if (params != null && params.size() > 0) {

                c.setRequestMethod("POST");

                StringBuilder requestParams = new StringBuilder();

                c.setDoOutput(true); // true indicates POST request

                // creates the params string, encode them using URLEncoder
                for (String key : params.keySet()) {
                    String value = params.get(key);
                    requestParams.append(URLEncoder.encode(key, "UTF-8"));
                    requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
                    requestParams.append("&");
                }

                // sends POST data
                OutputStreamWriter writer = new OutputStreamWriter(c.getOutputStream());
                writer.write(requestParams.toString());
                writer.flush();
            } else {
                c.setRequestMethod("GET");
            }


            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    c.disconnect();
                    return sb.toString().trim();
            }

        } catch (IOException ex) {
            ex.getStackTrace();
        }

        return null;
    }

    public String getJSON(String url, int timeout) {

        return getJSON(url, null, timeout);

    }

    private boolean isAppInstalled(String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


}