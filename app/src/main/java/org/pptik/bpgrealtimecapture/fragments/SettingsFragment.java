package org.pptik.bpgrealtimecapture.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.pptik.bpgrealtimecapture.R;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

/**
 * Created by hynra on 8/23/16.
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource

        addPreferencesFromResource(R.xml.app_settings);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register preference change listener
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // show values
        Preference editTextPref = findPreference(ApplicationConstants.PREFS_FTP_HOST_NAME);
        editTextPref.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, ""));
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setBackgroundColor(Color.WHITE);
        LinearLayout root = (LinearLayout)getView().findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().finish();
            }
        });
    }

    // change text or list values in PreferenceActivity ("Screen/Page")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        Log.d("APPLICATION SETTINGS", "key=" + key);
        if (key.equals(ApplicationConstants.PREFS_FTP_HOST_NAME)) {
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key, ""));
            // list value
        }

    }
}