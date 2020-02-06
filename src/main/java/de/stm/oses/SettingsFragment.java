package de.stm.oses;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Calendar;

import de.stm.oses.helper.OSESBase;
import de.stm.oses.index.database.FileSystemDatabase;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PERMISSION_REQUEST_STORAGE_DILOC = 5600;

    public SettingsFragment() {
        // Required empty public constructor
        //lol
    }

    private void resetIndex() {
        new Thread(() -> {
           FileSystemDatabase.getInstance(requireContext()).clearAllTables();
        }).start();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final OSESBase OSES = new OSESBase(getActivity());
        addPreferencesFromResource(R.xml.preferences);

        String strVersion;
        int iVersionCode;

        PackageInfo packageInfo;
        try {
            packageInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            strVersion = packageInfo.versionName;
            iVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            strVersion = "?";
            iVersionCode = 0;
        }

        Preference notificationsv26 = findPreference("notificationsv26");

        if (notificationsv26 != null) {
            notificationsv26.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, "de.stm.oses");
                startActivity(intent);
                return true;
            });
        }

        Preference version = findPreference("version");
        version.setSummary(strVersion);

        Preference versioncode = findPreference("versioncode");
        versioncode.setSummary(String.valueOf(iVersionCode));

        Preference copyright = findPreference("copyright");

        Preference index = findPreference("index");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long count = FileSystemDatabase.getInstance(requireContext()).fileSystemEntryDao().getCount();
            if (count == 0) {
                index.setSummary("Derzeit sind keine Dateien indiziert!");
            } else {
                index.setSummary("Derzeit sind "+count+" Dateien mit "+FileSystemDatabase.getInstance(requireContext()).arbeitsauftragEntryDao().getCount()+" Arbeitsaufträgen indiziert");
            }
        }).start();


        index.setOnPreferenceClickListener(preference -> {
            resetIndex();
            index.setSummary("Derzeit sind keine Dateien indiziert!");
            return true;
        });


        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        copyright.setSummary("© " + year + " Steiner Media");

        PreferenceCategory aaCat = findPreference("aaCategory");
        Preference scanDiloc = aaCat.findPreference("scanDiloc");

        scanDiloc.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((boolean) newValue) && !OSES.hasStoragePermission()) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_DILOC);
            }
            resetIndex();
            index.setSummary("Derzeit sind keine Dateien indiziert!");
            return true;
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
            scanAgressive.setOnPreferenceClickListener(preference -> {
                resetIndex();
                index.setSummary("Derzeit sind keine Dateien indiziert!");
                return  true;
            });
            aaCat.removePreference(scanAgressive);
            PreferenceCategory devCat = findPreference("debugCategory");
            getPreferenceScreen().removePreference(devCat);
        } else {

            SwitchPreference devModeSwitch = findPreference("debugUseDevServer");
            devModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if (((boolean) newValue)) {
                    TextView devModeText =  requireActivity().findViewById(R.id.devModeText);
                    devModeText.setVisibility(View.VISIBLE);
                } else {
                    TextView devModeText = requireActivity().findViewById(R.id.devModeText);
                    devModeText.setVisibility(View.GONE);
                }

                return true;

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