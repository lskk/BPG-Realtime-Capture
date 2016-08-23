package org.pptik.bpgrealtimecapture.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
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
    private String workingdir;
    private SharedPreferences sharedPreferences;
    Preference button;
    Preference eHost;
    Preference eUser;
    Preference ePass;
    Preference ePort;
    Preference eWD;

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
        eHost = findPreference(ApplicationConstants.PREFS_FTP_HOST_NAME);
        eUser = findPreference(ApplicationConstants.PREFS_FTP_USER_NAME);
        ePass = findPreference(ApplicationConstants.PREFS_FTP_PASSWORD);
        ePort = findPreference(ApplicationConstants.PREFS_FTP_PORT);
        eWD = findPreference(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY);

        Log.i(TAG, "curr host : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        Log.i(TAG, "curr username : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        Log.i(TAG, "curr Pass : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));
        Log.i(TAG, "curr port : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PORT,
                ApplicationConstants.PREFS_PORT_DEFAULT_SUMMARY));
        Log.i(TAG, "curr dir : "+sharedPreferences.getString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY,
                ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT));

        eHost.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY));
        eUser.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY));
        ePass.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY));
        ePort.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PORT,
                ApplicationConstants.PREFS_PORT_DEFAULT_SUMMARY));
        eWD.setSummary(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY,
                ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT));

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
        if(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY)
                || sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME, ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY)
                || sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD, ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY)
                ){
            Snackbar.make(getView(), "Your FTP Setup is invalid, check again!", Snackbar.LENGTH_LONG).show();
        }else {
            final String host = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, "");
            final String username = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME, "");
            final String password = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD, "");
            final int port = Integer.parseInt(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PORT, "21"));
            new Thread(new Runnable() {
                public void run() {
                    boolean status = false;
                    status = ftpHelper.ftpConnect(host, username, password, port);
                    if (status == true) {
                        Log.d(TAG, "Connection Success");
                        String dirToCheck = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY,
                                ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT);
                        boolean isWDExist = ftpHelper.ftpChangeDirectory(dirToCheck);
                        if(isWDExist){
                            Log.d(TAG, "Directory"+dirToCheck+" Exist, success change dir");
                            final Snackbar snackbar = Snackbar.make(getView(), "Connected to host "+host+" with working directory "+dirToCheck, Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }else {
                            Log.d(TAG, "Directory not Exist");
                            workingdir = ftpHelper.ftpGetCurrentWorkingDirectory();
                            Log.i(TAG, "Working dir : "+workingdir);
                            final Snackbar snackbar = Snackbar.make(getView(), "Connected to host "+host+", but directory doesn't exist. Use "+workingdir+" directory instead."
                                    , Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                            handler.sendEmptyMessage(0);
                        }
                    } else {
                        Log.d(TAG, "Connection failed");
                    }
                }
            }).start();
        }
    }


    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            if (msg.what == 0) {
                eWD.setKey(workingdir);
                eWD.setSummary(workingdir);
            //    SharedPreferences.Editor editor = sharedPreferences.edit();
            //    editor.putString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY, workingdir);
            //    editor.commit();
            } else if (msg.what == 2) {


            } else if (msg.what == 3) {

            } else if (msg.what == 4) {

            }else {

            }

        }

    };

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        ftpHelper.ftpDisconnect();
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
        }else if(key.equals(ApplicationConstants.PREFS_FTP_PORT)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_PORT_DEFAULT_SUMMARY));
        }else if(key.equals(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY)){
            Preference editTextPref = findPreference(key);
            editTextPref.setSummary(sharedPref.getString(key,  ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT));
        }

    }
}