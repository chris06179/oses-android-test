package de.stm.oses.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import de.stm.oses.R;
import de.stm.oses.verwendung.VerwendungClass;

public class OSESSession {

    private final Context context;
    private String SessionUsername = "";
    private int SessionGroup = 0;
    private int SessionEst = 0;
    private String SessionEstText;
    private String SessionVorname = "";
    private String SessionNachname = "";
    private int SessionGB = 0;
    private String SessionGBText;
    private int SessionFunktion = 99;
    private String SessionIdentifier;
    private String SessionAufArt = "auto";
    private int SessionAufDb = 3;
    private int SessionAufDe = 0;
    private String SessionFcmInstanceId = "";
    private String SessionLastVerwendung;

    private SharedPreferences preferences;

    OSESSession(Context context) {
        this.context = context;

        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SessionUsername = settings.getString("SessionUsername", "");
        SessionGroup = settings.getInt("SessionGruppe", 0);
        SessionEst = settings.getInt("SessionEst", 0);
        SessionEstText = settings.getString("SessionEstText", "");
        SessionVorname = settings.getString("SessionVorname", "");
        SessionNachname = settings.getString("SessionNachname", "");
        SessionGB = settings.getInt("SessionGB", 0);
        SessionGBText = settings.getString("SessionGBText", "");
        SessionFunktion = settings.getInt("SessionFunktion", 99);
        SessionIdentifier = settings.getString("SessionIdentifier", "");
        SessionAufArt = settings.getString("SessionAufArt", "auto");
        SessionAufDb = settings.getInt("SessionAufDb", 3);
        SessionAufDe = settings.getInt("SessionAufDe", 0);
        SessionFcmInstanceId = settings.getString("SessionFcmInstanceId", "");
        SessionLastVerwendung = settings.getString("SessionLastVerwendung", null);

        if (SessionFcmInstanceId.isEmpty()) {

            String firebaseInstanceId = FirebaseInstanceId.getInstance().getToken();

            if (firebaseInstanceId != null && !firebaseInstanceId.isEmpty())    {

                SharedPreferences.Editor edit = settings.edit();
                edit.putString("SessionFcmInstanceId", firebaseInstanceId);
                edit.apply();

                SessionFcmInstanceId = firebaseInstanceId;
            }

        }

    }

    public String getUsername() {
        return SessionUsername;
    }
    public int getGroup() {
        return SessionGroup;
    }
    public int getEst() {
        return SessionEst;
    }
    public String getVorname() {
        return SessionVorname;
    }
    public String getNachname() {
        return SessionNachname;
    }
    public int getGB() {
        return SessionGB;
    }
    public int getFunktion() {
        return SessionFunktion;
    }
    public String getIdentifier() {
        return SessionIdentifier;
    }
    public String getGBText() {
        return SessionGBText;
    }
    public String getEstText() {
        return SessionEstText;
    }
    public Context getContext() {
        return context;
    }
    public String getSessionAufArt() {
        return SessionAufArt;
    }
    public int getSessionAufDb() {
        return SessionAufDb;
    }
    public int getSessionAufDe() {
        return SessionAufDe;
    }
    public String getSessionFcmInstanceId() {
            return SessionFcmInstanceId;
    }
    public SharedPreferences getPreferences() {
        return preferences;
    }
    public String getSessionLastVerwendung() {
        return SessionLastVerwendung;
    }
    public void setSessionLastVerwendung(String sessionLastVerwendung) {
        SessionLastVerwendung = sessionLastVerwendung;
        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SessionLastVerwendung", SessionLastVerwendung);
        editor.apply();
    }
}
