package de.stm.oses;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import de.stm.oses.helper.OSESBase;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
        //lol
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OSESBase OSES = new OSESBase(getActivity());

        addPreferencesFromResource(R.xml.preferences);

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

        copyright.setSummary("© " + String.valueOf(year) + " Steiner Media");

        if (OSES.getSession().getGroup() > 10) {
            PreferenceCategory devCat = ((PreferenceCategory) findPreference("debugCategory"));
            getPreferenceScreen().removePreference(devCat);
        } else {

            SwitchPreference devModeSwitch = (SwitchPreference) findPreference("debugUseDevServer");
            devModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (((boolean) newValue)) {
                        TextView devModeText = (TextView) getActivity().findViewById(R.id.devModeText);
                        devModeText.setVisibility(View.VISIBLE);
                    } else {
                        TextView devModeText = (TextView) getActivity().findViewById(R.id.devModeText);
                        devModeText.setVisibility(View.GONE);
                    }

                    return true;

                }
            });
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Einstellungen");

    }
}