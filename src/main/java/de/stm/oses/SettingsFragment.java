package de.stm.oses;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import androidx.legacy.app.FragmentCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import de.stm.oses.helper.OSESBase;

public class SettingsFragment extends PreferenceFragment {

    private static final int PERMISSION_REQUEST_STORAGE_DILOC = 5600;

    public SettingsFragment() {
        // Required empty public constructor
        //lol
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final OSESBase OSES = new OSESBase(getActivity());

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

        Preference notificationsv26 = findPreference("notificationsv26");

        if (notificationsv26 != null) {
            notificationsv26.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                @SuppressLint("InlinedApi")
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, "de.stm.oses");
                    startActivity(intent);
                    return true;
                }
            });
        }

        Preference version = findPreference("version");
        version.setSummary(strVersion);

        Preference versioncode = findPreference("versioncode");
        versioncode.setSummary(String.valueOf(iVersionCode));

        Preference copyright = findPreference("copyright");

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        copyright.setSummary("© " + String.valueOf(year) + " Steiner Media");

        PreferenceCategory aaCat = ((PreferenceCategory) findPreference("aaCategory"));
        Preference scanDiloc = aaCat.findPreference("scanDiloc");

        scanDiloc.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (((boolean) newValue) && !OSES.hasStoragePermission()) {
                    FragmentCompat.requestPermissions(SettingsFragment.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_DILOC);
                }

                return true;
            }
        });

        if (OSES.getDilocStatus() == OSESBase.DILOC_STATUS_INSTALLED) {
            scanDiloc.setEnabled(true);
        } else {
            scanDiloc.setEnabled(false);
            scanDiloc.setSummary("Diloc|Sync wurde auf diesem Endgerät nicht gefunden. Funktion nicht verfügbar.");
        }

        if (OSES.getSession().getGroup() > 20) {
            Preference scanOSES = aaCat.findPreference("scanOSES");
            aaCat.removePreference(scanOSES);
        }

        if (OSES.getSession().getGroup() > 10) {
            Preference scanAgressive = aaCat.findPreference("scanAgressive");
            aaCat.removePreference(scanAgressive);
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