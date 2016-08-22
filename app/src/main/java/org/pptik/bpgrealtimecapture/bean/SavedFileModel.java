package org.pptik.bpgrealtimecapture.bean;

/**
 * Created by hynra on 8/22/16.
 */
public class SavedFileModel {
    private int id;
    private String filename;
    private String path;


    public SavedFileModel(int id, String title, String description) {
        this.id = id;
        this.filename = title;
        this.path = description;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getFilename() {
        return filename;
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }
}
