package de.stm.oses.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Calendar;

import de.stm.oses.R;
import de.stm.oses.helper.OSESBase;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PERMISSION_REQUEST_STORAGE_DILOC = 5600;

    private SettingsViewModel model;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(this).get(SettingsViewModel.class);

        Preference index = findPreference("index");
        model.getFileSystemStatus().observe(getViewLifecycleOwner(), fileSystemStatus -> {
            if (fileSystemStatus.fileSystemCount == 0) {
                index.setEnabled(false);
                index.setSummary("Derzeit sind keine Dateien indiziert!");
            } else {
                index.setEnabled(true);
                index.setSummary("Derzeit sind "+fileSystemStatus.fileSystemCount+" Dateien mit "+fileSystemStatus.arbeitsauftragCount+" Arbeitsaufträgen indiziert");
            }
       });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final OSESBase OSES = new OSESBase(requireContext());
        addPreferencesFromResource(R.xml.preferences);



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
        version.setSummary(OSES.getVersion());

        Preference versioncode = findPreference("versioncode");
        versioncode.setSummary(String.valueOf(OSES.getVersionCode()));

        Preference copyright = findPreference("copyright");

        Preference index = findPreference("index");
        index.setOnPreferenceClickListener(preference -> {
            model.resetIndex();
            return true;
        });


        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        copyright.setSummary("© " + year + " Steiner Media");

        Preference scanDiloc = findPreference("scanDiloc");

        scanDiloc.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((boolean) newValue) && !OSES.hasStoragePermission()) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_DILOC);
            } else {
                model.resetIndex();
            }

            return true;
        });

        if (OSES.getDilocStatus() == OSESBase.STATUS_INSTALLED) {
            scanDiloc.setEnabled(true);
        } if (OSES.getDilocStatus() == OSESBase.STATUS_NOT_ALLOWED) {
            scanDiloc.setVisible(false);
        } else {
            scanDiloc.setEnabled(false);
            scanDiloc.setSummary("Diloc|Sync wurde auf diesem Endgerät nicht gefunden. Funktion nicht verfügbar.");
        }

        Preference scanFassi = findPreference("scanFassi");

        scanFassi.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((boolean) newValue) && !OSES.hasStoragePermission()) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_DILOC);
            } else {
                model.resetIndex();
            }
            return true;
        });

        if (OSES.getFassiStatus() == OSESBase.STATUS_INSTALLED) {
            scanFassi.setEnabled(true);
        } if (OSES.getFassiStatus() == OSESBase.STATUS_NOT_ALLOWED) {
            scanFassi.setVisible(false);
        } else {
            scanFassi.setEnabled(false);
            scanFassi.setSummary("FASSI-MOVE wurde auf diesem Endgerät nicht gefunden. Funktion nicht verfügbar.");
        }


        if (OSES.getSession().getGroup() > 10) {
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