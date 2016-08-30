package org.pptik.bpgrealtimecapture.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

/**
 * Created by hynra on 8/29/16.
 */
public class DBLocalHelper extends SQLiteOpenHelper {
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(this.getClass().getSimpleName(), "Creating database " +ApplicationConstants.DB_NAME);

        Log.i(this.getClass().getSimpleName(), "Creating table " + TBFiles.TABLE_LIST_FILES);
        db.execSQL(TBFiles.INITIAL_CREATE);

    }

    public DBLocalHelper(Context context) {
        super(context, ApplicationConstants.DB_NAME, null, ApplicationConstants.DB_VER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
