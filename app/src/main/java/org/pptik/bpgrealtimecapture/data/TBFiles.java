package org.pptik.bpgrealtimecapture.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by hynra on 8/29/16.
 */
public class TBFiles {
    public static final String TAG = "Table Files";

    public static final String TABLE_LIST_FILES = "list_file";

    public static final String COL_ID = "id_";
    public static final String COL_FILE_NAME = "file_name";
    public static final String COL_FULL_PATH = "url_file";

    private SQLiteDatabase db;
    private DBLocalHelper dbHelper;

    public static final String INITIAL_CREATE = "create table " + TABLE_LIST_FILES
            + "(" + COL_ID + " integer primary key autoincrement,"
            + COL_FILE_NAME + " varchar(260) not null,"
            + COL_FULL_PATH + " varchar(30));";

    public TBFiles(Context context) {
        this.dbHelper = new DBLocalHelper(context);
    }

    public void open() {
        Log.w(TAG, "Open database connection...");
        this.db = dbHelper.getWritableDatabase();
    }

    public void close() {
        Log.w(TAG, "Close database connection...");
        this.dbHelper.close();
    }

    public long insert(ContentValues values) {
        Log.w(TAG, "inserting to table account");
        Long result = db.insert(TABLE_LIST_FILES, null, values);
        if (result != -1) {
            Log.i(TAG, "inserting to table account succeed");
        } else {
            Log.e(TAG, "inserting to table account failed");
        }

        return result;
    }

    public int delete(String id) {
        Log.i(TAG, "delete file id "+id);
        return db.delete(TABLE_LIST_FILES, COL_ID + "=?",
                new String[] { id });
    }

    public int update(String tID, ContentValues values) {
        Log.w(TAG, "updating to table account");
        if (tID == null) {
            Log.e(TAG, "updating to table account failed, no ID");
            return 0;
        }
        int result = db.update(TABLE_LIST_FILES, values, COL_ID + "=?",
                new String[]{tID});
        if (result > 0) {
            Log.i(TAG, "updating to "+TAG+" succeed");
        } else {
            Log.e(TAG, "updating to "+TAG+" failed");
        }
        return result;
    }

    public int delete() {
        Log.w(TAG, "Deleting " + TAG);

        int result = db.delete(TABLE_LIST_FILES, null, null);

        if (result > 0) {
            Log.i(TAG, "deleting " + TAG + " succeed");
        } else {
            Log.e(TAG, "deleting " + TAG + " failed");
        }

        return result;
    }

    public Cursor getRecordLast() {

        String[] allColumns = new String[] { TBFiles.COL_ID, TBFiles.COL_FILE_NAME,
                TBFiles.COL_FULL_PATH};

        Cursor c = db.query(TBFiles.TABLE_LIST_FILES, allColumns, null, null, null,
                null, null);
        if (c != null) {
            c.moveToLast();
        }
        return c;
    }

    public Cursor getAllData(){
        Log.i(TAG, "Get all data");
        String[] projection = new String[] {COL_ID, COL_FILE_NAME, COL_FULL_PATH};

        Cursor c = db.query(TABLE_LIST_FILES, projection, null, null, null, null, null);

        if (c.moveToFirst()) {
            Log.i(TAG, "Get all data list");
            return c;
        }

        Log.i(TAG, "No data found");
        return null;
    }
}
