package de.stm.oses.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Calendar;
import java.util.Objects;

import de.stm.oses.R;
import de.stm.oses.application.OsesApplication;
import de.stm.oses.helper.OSESBase;

public class SettingsFragment extends PreferenceFragmentCompat {

    private long lastClick;
    private int clicks;


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

        Preference versioncode = findPreference("versioncode");
        versioncode.setSummary(String.valueOf(OSES.getVersionCode()));

        Preference copyright = findPreference("copyright");



        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        copyright.setSummary("© " + year + " Steiner Media");

        PreferenceCategory devCat = findPreference("debugCategory");

        if (OSES.getSession().getGroup() < 11 || OSES.getSession().getPreferences().getBoolean("useFileLogging", false)) {
            devCat.setVisible(true);
        } else {
            devCat.setVisible(false);
        }

        SwitchPreference devModeSwitch = findPreference("debugUseDevServer");
        devModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((boolean) newValue)) {
                TextView devModeText = requireActivity().findViewById(R.id.devModeText);
                devModeText.setVisibility(View.VISIBLE);
                devModeText.setText(((ListPreference) Objects.requireNonNull(findPreference("debugEnv"))).getValue());
            } else {
                TextView devModeText = requireActivity().findViewById(R.id.devModeText);
                devModeText.setVisibility(View.GONE);
            }
            return true;

        });

        ((ListPreference) findPreference("debugEnv")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                TextView devModeText = requireActivity().findViewById(R.id.devModeText);
                devModeText.setText((String) newValue);
                return true;
            }
        });

        findPreference("useFileLogging").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                OsesApplication.getInstance().getLogger().setUseFileLogging((boolean) newValue);
                return true;
            }
        });


        Preference version = findPreference("version");
        version.setSummary(OSES.getVersion());
        version.setOnPreferenceClickListener(preference -> {
            if (System.currentTimeMillis() - lastClick < 1000) {
                clicks++;
                if (clicks > 10) {
                    devCat.setVisible(true);
                    Toast.makeText(requireContext(), "Entwickleroptionen aktiviert!", Toast.LENGTH_SHORT).show();
                }
            } else {
                clicks = 0;
            }
            lastClick = System.currentTimeMillis();
            return true;
        });


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Einstellungen");

    }
}
