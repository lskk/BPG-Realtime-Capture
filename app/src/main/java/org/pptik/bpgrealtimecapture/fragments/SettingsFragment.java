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

import org.pptik.bpgrealtimecapture.R;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

/**
 * Created by hynra on 8/23/16.
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        Preference eHost = findPreference(ApplicationConstants.PREFS_FTP_HOST_NAME);
        Preference eUser = findPreference(ApplicationConstants.PREFS_FTP_USER_NAME);
        Preference ePass = findPreference(ApplicationConstants.PREFS_FTP_PASSWORD);

        eHost.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        eUser.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        ePass.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));
        if(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY)){

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setBackgroundColor(Color.WHITE);
        LinearLayout root = (LinearLayout)getView().findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().finish();
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        Log.d("APPLICATION SETTINGS", "key=" + key);
        if (key.equals(ApplicationConstants.PREFS_FTP_HOST_NAME)) {
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        }else if(key.equals(ApplicationConstants.PREFS_FTP_USER_NAME)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        }else if(key.equals(ApplicationConstants.PREFS_FTP_PASSWORD)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));
        }

    }
}