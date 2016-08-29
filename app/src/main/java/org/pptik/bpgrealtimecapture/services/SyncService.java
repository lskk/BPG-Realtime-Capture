package org.pptik.bpgrealtimecapture.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.ftp.FtpHelper;
import org.pptik.bpgrealtimecapture.helper.RealmHelper;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;
import org.pptik.bpgrealtimecapture.utilities.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hynra on 8/29/16.
 */

public class SyncService extends Service implements Runnable {

    public static final String TAG = "Service Sender ";
    private Context context;
    private FtpHelper ftpHelper;
    private SharedPreferences sharedPreferences;
    private String workingdir;
    private int dataSize;
    private File fileUpload;
    private ArrayList<SavedFileModel> data;
    private RealmHelper realmHelper;
    private String pHost, pUser, pPass, pPort, pWorkingDir;
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.i(TAG, "service create");
        context = getApplicationContext();
        ftpHelper = new FtpHelper();
        realmHelper = new RealmHelper(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId){
       // super.onStart();
        setupFtp();
        Log.i(TAG, "service start");
        if(Tools.isNetworkConnected(this)) {
            Log.i(TAG, "Connect to internet");
            connectToFtp();

        }else {
            // not connect
        }
    }

    private void setupFtp(){
        pHost = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME,
                ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY);
        pUser = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME,
                ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY);
        pPass = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD,
                ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY);
        pPort = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PORT,
                ApplicationConstants.PREFS_PORT_DEFAULT_SUMMARY);
        pWorkingDir = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY,
                ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT);
    }


    private void connectToFtp() {
        Log.i(TAG, "Test connection to FTP");
        if(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_HOST_DEFAULT_SUMMARY)
                || sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME, ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_USER_DEFAULT_SUMMARY)
                || sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD, ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY).equals(ApplicationConstants.PREFS_PASS_DEFAULT_SUMMARY)
                ){
            Log.i(TAG, "Your FTP Setup is invalid, check again!");
        }else {
            final String host = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_HOST_NAME, "");
            final String username = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_USER_NAME, "");
            final String password = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PASSWORD, "");
            final int port = Integer.parseInt(sharedPreferences.getString(ApplicationConstants.PREFS_FTP_PORT, "21"));
            new Thread(new Runnable() {
                public void run() {
                    boolean status = false;
                    status = ftpHelper.ftpConnect(host, username, password, port);
                    if (status == true) {
                        Log.d(TAG, "Connection Success");
                        String dirToCheck = sharedPreferences.getString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY,
                                ApplicationConstants.PREFS_WORKING_DIRECTORY_DEFAULT);
                        boolean isWDExist = ftpHelper.ftpChangeDirectory(dirToCheck);
                        if(isWDExist){
                            Log.d(TAG, "Directory"+dirToCheck+" Exist, success change dir");
                            startTimerTask();
                        }else {
                            Log.d(TAG, "Directory not Exist, use default instead");
                            workingdir = ftpHelper.ftpGetCurrentWorkingDirectory();
                            Log.i(TAG, "Working dir : "+workingdir);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY, workingdir);
                            editor.commit();
                            startTimerTask();
                        }
                    } else {
                        Log.d(TAG, "Connection failed");
                    }
                }
            }).start();
        }
    }

    private void startTimerTask() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SyncService.this.run();
            }
        }, 30000, 30000);
    }

    @Override
    public void run() {
        postPicture();
    }

    private void postPicture() {
        final String[] pathFile = getFirstImagePath();
        if(dataSize > 0){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean status = false;
                        Log.i(TAG, "----------------------------------------------------------------------");
                        Log.i(TAG, "POSTING FILE");
                        Log.i(TAG, "----------------------------------------------------------------------");
                        Log.i(TAG, "start posting picture");
                        fileUpload = new File(pathFile[0]);
                        if(fileUpload.exists()) {
                            Log.i(TAG, "Uploading : " + pathFile[1]);
                            status = ftpHelper.ftpUpload(pathFile[0], pathFile[1]);
                        } else{
                            Log.d(TAG, "File not exist");
                        }

                        if (status == true) {
                            Log.d(TAG, "Upload success");
                            File file = new File(pathFile[0]);
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile[0]+" : "+deleted);
                            realmHelper = new RealmHelper(getApplicationContext());
                            realmHelper.deleteData(pathFile[0]);
                            ftpHelper.ftpDisconnect();
                        } else {
                            Log.d(TAG, "Upload failed");
                            File file = new File(pathFile[0]);
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile[0]+" : "+deleted);
                            realmHelper = new RealmHelper(getApplicationContext());
                            realmHelper.deleteData(pathFile[0]);
                            ftpHelper.ftpDisconnect();
                        }
                    } catch (Exception e) {
                    }

                }
            }).start();
        }
    }

    private String[] getFirstImagePath(){
        realmHelper = new RealmHelper(getApplicationContext());
        data = realmHelper.findAllArticle();
        String[] lastPath = {"0", "0"};
        dataSize = data.size();
        if (dataSize > 0){
            Log.i(TAG, "id : "+data.get(0).getId()+", Filename : "+data.get(0).getFilename()+", Path : "+data.get(0).getPath());
            lastPath[0] = data.get(0).getPath();
            lastPath[1] = data.get(0).getFilename();
        }
        return lastPath;
    }
}
