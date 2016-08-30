package org.pptik.bpgrealtimecapture.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.pptik.bpgrealtimecapture.data.TBFiles;

/**
 * Created by hynra on 8/29/16.
 */
public class FilesHelper {

    public void insert(Context context, String filename, String filePath) {
        TBFiles tbFiles = new TBFiles(context);
        tbFiles.open();
        ContentValues values = new ContentValues();
        values.put(TBFiles.COL_FILE_NAME, filename);
        values.put(TBFiles.COL_FULL_PATH, filePath);
        tbFiles.insert(values);
        tbFiles.close();
    }


    public int delete(Context context, String id){
        int state;
        TBFiles tb = new TBFiles(context);
        tb.open();
        state = tb.delete(id);
        tb.close();
        return state;
    }


    public String getLastId(Context context){
        String lastId = "0";

        TBFiles tbFiles = new TBFiles(context);
        tbFiles.open();
        Cursor cursor = tbFiles.getRecordLast();
        if(cursor != null){
            lastId = cursor.getString(0);
            cursor.close();
        }
        tbFiles.close();

        return lastId;
    }

    public String getLastFilename(Context context){
        String lastPath = "0";
        TBFiles tbFiles = new TBFiles(context);
        tbFiles.open();
        Cursor cursor = tbFiles.getRecordLast();
        if(cursor != null){
            lastPath = cursor.getString(1);
            cursor.close();
        }
        tbFiles.close();
        return lastPath;
    }

    public String getLastPath(Context context){
        String lastPath = "0";
        TBFiles tbFiles = new TBFiles(context);
        tbFiles.open();
        Cursor cursor = tbFiles.getRecordLast();
        if(cursor != null){
            lastPath = cursor.getString(2);
            cursor.close();
        }
        tbFiles.close();
        return lastPath;
    }

    public int getCount(Context context){
        int count = 0;
        TBFiles tbFiles = new TBFiles(context);
        tbFiles.open();
        Cursor cursor = tbFiles.getRecordLast();
        if(cursor != null){
            count = cursor.getCount();
            cursor.close();
        }
        tbFiles.close();
        return count;
    }
}
