package com.letsmeet.letsmeetproject.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.letsmeet.letsmeetproject.MainActivity;
import com.letsmeet.letsmeetproject.R;

import java.util.HashSet;

public class FrequencyPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "FrequencyPreference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.frequency_setting);
        setHasOptionsMenu(true);

        bindPreferenceSummaryToValue(findPreference("frequency_accelerate"));
        bindPreferenceSummaryToValue(findPreference("frequency_magnetic"));
        bindPreferenceSummaryToValue(findPreference("frequency_orient"));
        bindPreferenceSummaryToValue(findPreference("frequency_gyroscope"));
        bindPreferenceSummaryToValue(findPreference("frequency_pressure"));
        bindPreferenceSummaryToValue(findPreference("frequency_light"));
        bindPreferenceSummaryToValue(findPreference("frequency_GPS"));
        bindPreferenceSummaryToValue(findPreference("frequency_satellite"));
        bindPreferenceSummaryToValue(findPreference("frequency_WiFi"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        if (preference instanceof MultiSelectListPreference){
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,sharedPreferences.getStringSet(preference.getKey(), null));
        }else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,sharedPreferences.getString(preference.getKey(), ""));
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private  Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            ListPreference frequencyL = (ListPreference) preference;
            int i = frequencyL.findIndexOfValue(stringValue);
            preference.setSummary(
                    i >= 0
                            ? frequencyL.getEntries()[i]
                            : null);
            return true;
        }
    };
}
