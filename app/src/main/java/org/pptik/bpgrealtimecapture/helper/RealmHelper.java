package org.pptik.bpgrealtimecapture.helper;

import android.content.Context;
import android.util.Log;

import org.pptik.bpgrealtimecapture.bean.SavedFile;
import org.pptik.bpgrealtimecapture.bean.SavedFileModel;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by hynra on 8/22/16.
 */
public class RealmHelper {
    private static final String TAG = "REALM HELPER";
    private Realm realm;
    private RealmResults<SavedFile> realmResult;
    public Context context;


    public RealmHelper(Context context) {
        realm = Realm.getInstance(context);
        this.context = context;
    }

    public ArrayList<SavedFileModel> findAllArticle() {
        ArrayList<SavedFileModel> data = new ArrayList<>();
        realmResult = realm.where(SavedFile.class).findAll();
        realmResult.sort("id", Sort.DESCENDING);
        if (realmResult.size() > 0) {
            Log.i(TAG, "Size : " + realmResult.size());
            for (int i = 0; i < realmResult.size(); i++) {
                String filename, path;
                int id = realmResult.get(i).getId();
                filename = realmResult.get(i).getFilename();
                path = realmResult.get(i).getPath();
                data.add(new SavedFileModel(id, filename, path));

            }

        } else {
            Log.i(TAG, "EMPTY DB");
        }

        return data;
    }


    public void addFile(String filename, String path) {
        SavedFile file = new SavedFile();
        file.setId((int) (System.currentTimeMillis() / 1000));
        file.setFilename(filename);
        file.setPath(path);
        realm.beginTransaction();
        realm.copyToRealm(file);
        realm.commitTransaction();
        Log.i(TAG, "Added file : " + filename);
    }

    public void deleteData(int id) {
        RealmResults<SavedFile> dataResults = realm.where(SavedFile.class).equalTo("id", id).findAll();
        realm.beginTransaction();
        dataResults.remove(0);
        dataResults.removeLast();
        dataResults.clear();
        realm.commitTransaction();
        Log.i(TAG, "Deleted : "+id);
    }
}
