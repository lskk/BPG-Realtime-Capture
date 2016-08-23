package org.pptik.bpgrealtimecapture.fragments;

import android.content.Context;
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
import org.pptik.bpgrealtimecapture.ftp.FtpHelper;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

/**
 * Created by hynra on 8/23/16.
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private String TAG = this.getClass().getSimpleName();
    private FtpHelper ftpHelper;
    private Context context;
    private SharedPreferences sharedPreferences;
    Preference button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
    }

    @Override
    public void onStart() {
        super.onStart();

        context = getActivity();
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        ftpHelper = new FtpHelper();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        Preference eHost = findPreference(ApplicationConstants.PREFS_FTP_HOST_NAME);
        Preference eUser = findPreference(ApplicationConstants.PREFS_FTP_USER_NAME);
        Preference ePass = findPreference(ApplicationConstants.PREFS_FTP_PASSWORD);

        Log.i(TAG, "curr host : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        Log.i(TAG, "curr username : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        Log.i(TAG, "curr Pass : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));

        eHost.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        eUser.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        ePass.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));

        if(!sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY)){
            ePass.setSummary("******");
        }

        button = findPreference(ApplicationConstants.PREFS_CONNECT_BUTTON);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
             //   connectToFTPAddress();
                return false;
            }
        });
    }

    public void connectToFTPAddress() {
        Log.i(TAG, "START CHECK");
        final String host = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, "");
        final String username = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME, "");
        final String password = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD, "");
        new Thread(new Runnable() {
            public void run() {
                boolean status = false;
                status = ftpHelper.ftpConnect(host, username, password, 21);
                if (status == true) {
                    Log.d(TAG, "Connection Success");
                } else {
                    Log.d(TAG, "Connection failed");
                }
            }
        }).start();

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
        Log.d(TAG, "key=" + key);
        if (key.equals(ApplicationConstants.PREFS_FTP_HOST_NAME)) {
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        }else if(key.equals(ApplicationConstants.PREFS_FTP_USER_NAME)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        }else if(key.equals(ApplicationConstants.PREFS_FTP_PASSWORD)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary("******");

        }

    }
}