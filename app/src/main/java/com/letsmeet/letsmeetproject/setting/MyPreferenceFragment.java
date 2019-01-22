package com.letsmeet.letsmeetproject.setting;

import android.content.Intent;
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

public class MyPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "MyPreferenceFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        setHasOptionsMenu(true);

        bindPreferenceSummaryToValue(findPreference("parameter"));
        bindPreferenceSummaryToValue(findPreference("stepLength"));
        bindPreferenceSummaryToValue(findPreference("frequency"));

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

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof MultiSelectListPreference){
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getStringSet(preference.getKey(), null));
        }else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Log.e("PreFra",stringValue);

            String key = preference.getKey();
            switch (key){
                case "dataCollectSwitch":
                    Log.e(TAG,"dataCollectSwitch"+value.toString());
                    break;
                case "parameter":
                    MultiSelectListPreference parameterP = (MultiSelectListPreference) preference;
                    Log.e(TAG,"parameter"+value.toString());
                    HashSet<String> parametersValue = (HashSet<String>) value;
                    HashSet<String> parameter = new HashSet<>();
                    for (String s:parametersValue){
                        int index = parameterP.findIndexOfValue(s);
                        parameter.add(parameterP.getEntries()[index].toString());
                    }
//                    parameterP.setSummary(parameter.toString());
                    break;
                case "stepLength":
                    Log.e(TAG,"stepLength"+value.toString());
                    preference.setSummary(stringValue);
                    break;
                case "frequency":
                    ListPreference frequencyL = (ListPreference) preference;
                    int index = frequencyL.findIndexOfValue(stringValue);
                    preference.setSummary(
                            index >= 0
                                    ? frequencyL.getEntries()[index]
                                    : null);
                    break;
            }

//            if (preference instanceof ListPreference) {
//                // For list preferences, look up the correct display value in
//                // the preference's 'entries' list.
//                ListPreference listPreference = (ListPreference) preference;
//                int index = listPreference.findIndexOfValue(stringValue);
//
//                // Set the summary to reflect the new value.
//                preference.setSummary(
//                        index >= 0
//                                ? listPreference.getEntries()[index]
//                                : null);
//
//            }  else {
//                // For all other preferences, set the summary to the value's
//                // simple string representation.
//                preference.setSummary(stringValue);
//            }
            return true;
        }
    };
}
