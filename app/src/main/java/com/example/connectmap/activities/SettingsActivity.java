package com.example.connectmap.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import com.example.connectmap.preferences.DatePreference;
import com.example.connectmap.preferences.DatePreferenceDialogFragment;
import com.example.connectmap.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            DatePreference datePreference = findPreference("date_preference");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(System.currentTimeMillis());
            datePreference.setDate(formatter.format(date));

            initSummary(getPreferenceScreen());


            // restore everything back to default
            Preference resetPreference = findPreference("reset_btn");
            resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    PreferenceManager.setDefaultValues(getActivity(), R.xml.root_preferences, true);
                    getPreferenceScreen().removeAll();
                    onCreatePreferences(null, null);
                    return true;
                }
            });

        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof DatePreference) {
                final DialogFragment f;
                f = DatePreferenceDialogFragment.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), null);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        private void initSummary(Preference p) {
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) p;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    initSummary(pGrp.getPreference(i));
                }
            } else {
                setPreferenceSummary(p);
            }
        }

        /**
         * Sets up summary providers for the preferences.
         *
         * @param p The preference to set up summary provider.
         */
        private void setPreferenceSummary(Preference p) {
            // No need to set up preference summaries for checkbox preferences because
            // they can be set up in xml using summaryOff and summary On
            if (p instanceof DatePreference) {
                p.setSummaryProvider(DatePreference.SimpleSummaryProvider.getInstance());
            } else if (p instanceof EditTextPreference) {
                p.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}