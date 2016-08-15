package cz.schrek.filemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;

import java.util.List;

/**
 * Created by ondra on 15. 8. 2016.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }


    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences SP;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SP = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final EditTextPreference pathPref = (EditTextPreference) findPreference("default_path");
            String savePath = SP.getString("default_path", "");
            pathPref.setSummary(savePath.toString().equals("") ? "-" : savePath);

            pathPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        pathPref.setSummary((String) newValue);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

        }


    }
}
