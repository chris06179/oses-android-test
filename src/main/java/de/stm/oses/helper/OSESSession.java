package de.stm.oses.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.stm.oses.R;

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
    private int SessionFunktion = 0;
    private String SessionIdentifier;
    private String SessionAufArt = "auto";
    private int SessionAufDb = 3;
    private int SessionAufDe = 0;

    private SharedPreferences preferences;

    public OSESSession(Context context) {
        this.context = context;

        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", 0);
        SessionUsername = settings.getString("SessionUsername", "");
        SessionGroup = settings.getInt("SessionGruppe", 0);
        SessionEst = settings.getInt("SessionEst", 0);
        SessionEstText = settings.getString("SessionEstText", "");
        SessionVorname = settings.getString("SessionVorname", "");
        SessionNachname = settings.getString("SessionNachname", "");
        SessionGB = settings.getInt("SessionGB", 0);
        SessionGBText = settings.getString("SessionGBText", "");
        SessionFunktion = settings.getInt("SessionFunktion", 0);
        SessionIdentifier = settings.getString("SessionIdentifier", "");
        SessionAufArt = settings.getString("SessionAufArt", "auto");
        SessionAufDb = settings.getInt("SessionAufDb", 3);
        SessionAufDe = settings.getInt("SessionAufDe", 0);

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
    public void setSessionAufArt(String sessionAufArt) {
        SessionAufArt = sessionAufArt;
    }
    public int getSessionAufDb() {
        return SessionAufDb;
    }
    public void setSessionAufDb(int sessionAufDb) {
        SessionAufDb = sessionAufDb;
    }
    public int getSessionAufDe() {
        return SessionAufDe;
    }
    public void setSessionAufDe(int sessionAufDe) {
        SessionAufDe = sessionAufDe;
    }
    public SharedPreferences getPreferences() {
        return preferences;
    }
}
