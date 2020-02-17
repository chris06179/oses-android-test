package de.stm.oses.helper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import de.stm.oses.R;

public class OSESSession {

    private final Context context;
    private String SessionUsername = "";
    private int SessionGroup = 90;
    private int SessionEst = 0;
    private String SessionEstText;
    private String SessionVorname = "";
    private String SessionNachname = "";
    private int SessionGB = 0;
    private String SessionGBText;
    private int SessionFunktion = 0;
    private String SessionIdentifier;
    private String SessionFcmInstanceId = "";
    private String SessionLastVerwendung;
    private boolean SessionDilocReminder;
    private boolean SessionIndexReminder;

    private SharedPreferences preferences;

    OSESSession(Context context) {
        this.context = context;

        // Fix für API < 20
        try {
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SessionUsername = settings.getString("SessionUsername", "");
        SessionGroup = settings.getInt("SessionGruppe", 90);
        SessionEst = settings.getInt("SessionEst", 0);
        SessionEstText = settings.getString("SessionEstText", "");
        SessionVorname = settings.getString("SessionVorname", "");
        SessionNachname = settings.getString("SessionNachname", "");
        SessionGB = settings.getInt("SessionGB", 0);
        SessionGBText = settings.getString("SessionGBText", "");
        SessionFunktion = settings.getInt("SessionFunktion", 0);
        SessionIdentifier = settings.getString("SessionIdentifier", "");
        SessionFcmInstanceId = settings.getString("SessionFcmInstanceId", "");
        SessionLastVerwendung = settings.getString("SessionLastVerwendung", null);
        SessionDilocReminder = settings.getBoolean("SessionDilocReminder", false);
        SessionIndexReminder = settings.getBoolean("SessionIndexReminder", false);

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
        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SessionLastVerwendung", SessionLastVerwendung);
        editor.apply();
    }
    public boolean getSessionDilocReminder() {
        return SessionDilocReminder;
    }
    public void setSessionDilocReminder(boolean sessionDilocReminder) {
        SessionDilocReminder = sessionDilocReminder;
        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("SessionDilocReminder", SessionDilocReminder);
        editor.apply();
    }

    public boolean getSessionIndexReminder() {
        return SessionIndexReminder;
    }
    public void setSessionIndexReminder(boolean sessionIndexReminder) {
        SessionIndexReminder = sessionIndexReminder;
        SharedPreferences settings = context.getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("SessionIndexReminder", SessionIndexReminder);
        editor.apply();
    }
}
