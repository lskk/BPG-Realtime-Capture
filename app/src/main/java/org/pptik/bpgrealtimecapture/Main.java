package org.pptik.bpgrealtimecapture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.helper.RealmHelper;

import java.util.ArrayList;

public class Main extends AppCompatActivity {

    private RealmHelper helper;
    private ArrayList<SavedFileModel> data;
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new RealmHelper(this);
        data = helper.findAllArticle();
        for (int i = 0; i < data.size(); i++){
            Log.i(TAG, "id : "+data.get(i).getId()+", Filename : "+data.get(i).getFilename()+", Path : "+data.get(i).getPath());
        }

        // startActivity(new Intent(this, Capture.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
