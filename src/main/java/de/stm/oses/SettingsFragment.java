package de.stm.oses;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();


        String strVersion;
        int iVersionCode;

        PackageInfo packageInfo;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            strVersion = packageInfo.versionName;
            iVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            strVersion = "?";
            iVersionCode = 0;
        }

        Preference version = findPreference("version");
        version.setSummary(strVersion);

        Preference versioncode = findPreference("versioncode");
        versioncode.setSummary(String.valueOf(iVersionCode));

        Preference copyright = findPreference("copyright");

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        copyright.setSummary("© "+String.valueOf(year)+" Steiner Media");
                
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Einstellungen");
    }
}