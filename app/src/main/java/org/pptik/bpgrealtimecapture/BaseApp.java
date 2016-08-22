package org.pptik.bpgrealtimecapture;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by hynraon 8/22/16.
 */
public class BaseApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(0)
                .migration(new DataMigration())
                .build();
        Realm.setDefaultConfiguration(config);
    }

    private class DataMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 0) {
                schema.create("SavedFile")
                        .addField("id", int.class)
                        .addField("filename", String.class)
                        .addField("path", String.class);
                oldVersion++;
            }

        }
    }
}