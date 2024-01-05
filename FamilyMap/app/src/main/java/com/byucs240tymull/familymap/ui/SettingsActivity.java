package com.byucs240tymull.familymap.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.byucs240tymull.familymap.R;
import com.byucs240tymull.familymap.data.DataCache;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    DataCache cache = DataCache.getInstance();
                    //will use this to redraw map when settings are changed
                    cache.setSettingsChanged(true);
                    return true;
                }
            };
            SwitchPreferenceCompat lifeStoryLinesPref = findPreference(getString(R.string.life_story_lines_key));
            lifeStoryLinesPref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat familyTreeLinesPref = findPreference(getString(R.string.family_tree_lines_key));
            familyTreeLinesPref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat spouseLinesPref = findPreference(getString(R.string.spouse_lines_key));
            spouseLinesPref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat fatherSidePref = findPreference(getString(R.string.father_side_key));
            fatherSidePref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat motherSidePref = findPreference(getString(R.string.mother_side_key));
            motherSidePref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat maleEventsPref = findPreference(getString(R.string.male_events_key));
            maleEventsPref.setOnPreferenceChangeListener(changeListener);
            SwitchPreferenceCompat femaleEventsPref = findPreference(getString(R.string.female_events_key));
            femaleEventsPref.setOnPreferenceChangeListener(changeListener);
            // logout button
            Preference logoutPref = findPreference(getString(R.string.logout_key));
            logoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }
}