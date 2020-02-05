package de.stm.oses.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class OSESRequest extends AsyncTask<String, Integer, Object> {
    private OnRequestFinishedListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private String url;
    private Map<String, String> params = null;
    private int timeout = 60000;
    private String version;

    // TODO: 05.02.2020 Context aus Klasse entfernen -> genrell neuen Netzwerkcode verwenden

    public interface OnRequestFinishedListener {
        void onRequestFinished(String response);

        void onRequestException(Exception e);

        void onRequestUnknown(int status);

        void onIsNotConnected();
    }

    public void setOnRequestFinishedListener(OnRequestFinishedListener listener) {
        mListener = listener;
    }

    public OSESRequest(Context context) {
        this.context = context;
        initVersion();
    }

    private void initVersion() {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            this.version = "UNKNOWN VERSION";
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private String getVersion() {
        return version;
    }

    protected Object doInBackground(String... sUrl) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return null;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            return null;
        }

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
                    requestParams.append("=").append(
                            URLEncoder.encode(value, "UTF-8"));
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
                default:
                    return status;
            }

        } catch (Exception e) {
            return e;
        }

    }

    protected void onPostExecute(Object o) {

        if(isCancelled())
            return;

        if (o instanceof String) { // Erfolg
            if (mListener != null)
                mListener.onRequestFinished((String) o);
        }

        if (o instanceof Integer) { // Unbekannter Status
            if (mListener != null)
                mListener.onRequestUnknown((int) o);

            Crashlytics.log("URL: "+url+ " - Status: "+ o);
            Crashlytics.logException(new Exception("Unexpected HTTP-Status-Code"));
        }

        if (o instanceof Exception) { // Programmfehler
            if (mListener != null)
                mListener.onRequestException((Exception) o);

            Crashlytics.log("URL: "+url);
            Crashlytics.logException((Exception) o);
        }

        if (o == null) { // Keine Konnektivität
            if (mListener != null)
                mListener.onIsNotConnected();
        }

    }
}