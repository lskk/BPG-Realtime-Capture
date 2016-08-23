package org.pptik.bpgrealtimecapture;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.pptik.bpgrealtimecapture.fragments.SettingsFragment;

/**
 * Created by hynra on 8/23/16.
 */
public class Settings extends PreferenceActivity {
    SettingsFragment settingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsFragment = new SettingsFragment();


        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    public void connectFtp(View v){
        settingsFragment.connectToFTPAddress();
    }

}
