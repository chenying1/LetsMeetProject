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

public class MyPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "MyPreferenceFragment";
    String fre = "";

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

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        preference.setOnPreferenceClickListener(clickListener);

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
            Log.e("PreFra",stringValue);
            String key = preference.getKey();
            switch (key){
                case "dataCollectSwitch":  //采集数据开关
                    Log.e(TAG,"dataCollectSwitch"+value.toString());
                    break;
                case "parameter":   //采集参数设置
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
                case "stepLength":    //步长设置
                    Log.e(TAG,"stepLength"+value.toString());
                    preference.setSummary(stringValue);
                    fre = stringValue;
                    break;
                case "frequency":      //采样频率设置
                    ListPreference frequencyL = (ListPreference) preference;
                    int index = frequencyL.findIndexOfValue(stringValue);
                    preference.setSummary(
                            index >= 0
                                    ? frequencyL.getEntries()[index]
                                    : null);
                    break;
            }
            return true;
        }
    };

    private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            switch (key){
                case "frequency":
                    Log.e("MyPreferenceFragment","点击了采样频率");
                    Intent intent = new Intent(getActivity(),FrequencySettingActivity.class);
                    startActivityForResult(intent,1);
                    break;
            }
            return true;
        }
    };
}
