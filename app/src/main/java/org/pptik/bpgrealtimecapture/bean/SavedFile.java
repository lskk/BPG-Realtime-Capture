package org.pptik.bpgrealtimecapture.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hynra on 8/22/16.
 */
public class SavedFile extends RealmObject {
    @PrimaryKey
    public int id;
    public String filename;
    public String path;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFilename(String _filename) {
        this.filename = _filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setPath(String _path) {
        this.path = _path;
    }

    public String getPath() {
        return path;
    }
}
