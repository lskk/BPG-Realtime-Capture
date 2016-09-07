package org.pptik.bpgrealtimecapture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.pptik.bpgrealtimecapture.services.SyncService;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;


public class Main extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    private TextView tSetup, tStart;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        tSetup = (TextView)findViewById(R.id.setup);
        tStart = (TextView)findViewById(R.id.start);

        tSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, Settings.class));
            }
        });

        tStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getBoolean(ApplicationConstants.PREFS_EVERYTHING_OK, false) == false){
                    final Snackbar snackbar = Snackbar.make(v, "Setup your FTP Connection first!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }else {
                    startActivity(new Intent(Main.this, Capture.class));
                    startService(new Intent(Main.this, SyncService.class));
                }
            }
        });


    }

}
