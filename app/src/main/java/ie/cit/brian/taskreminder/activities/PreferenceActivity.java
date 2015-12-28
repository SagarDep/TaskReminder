package ie.cit.brian.taskreminder.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

import ie.cit.brian.taskreminder.R;

/**
 * Created by briancoveney on 12/21/15.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(
                android.R.id.content, new MyPreferenceFragment()).commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }


        //Configure CheckboxPreferences to work like RadioButtons
        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

            String key = preference.getKey();

            if (key.equals("day_preference")) {
                Toast.makeText(getActivity(), "Day", Toast.LENGTH_SHORT).show();
                CheckBoxPreference wkPref = (CheckBoxPreference) findPreference("week_preference");
                wkPref.setChecked(false);

            } else if (key.equals("week_preference")) {
                Toast.makeText(getActivity(), "Week", Toast.LENGTH_SHORT).show();
                CheckBoxPreference dayPref = (CheckBoxPreference) findPreference("day_preference");
                dayPref.setChecked(false);
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

    }
}