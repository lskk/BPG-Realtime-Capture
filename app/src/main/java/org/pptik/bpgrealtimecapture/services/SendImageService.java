package org.pptik.bpgrealtimecapture.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.pptik.bpgrealtimecapture.Capture;
import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.ftp.FtpHelper;
import org.pptik.bpgrealtimecapture.helper.RealmHelper;
import org.pptik.bpgrealtimecapture.receivers.SendImageReceiver;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;
import org.pptik.bpgrealtimecapture.utilities.Tools;
import org.pptik.bpgrealtimecapture.utilities.WarningDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hynra on 8/25/16.
 */

public class SendImageService extends IntentService {

    public static final String TAG = "Service Sender ";
    private Context context;
    private FtpHelper ftpHelper;
    private SharedPreferences sharedPreferences;
    private String workingdir;
    private int dataSize;
    private File fileUpload;
    private ArrayList<SavedFileModel> data;
    private RealmHelper realmHelper;


    public SendImageService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        context = getApplicationContext();
        ftpHelper = new FtpHelper();
        realmHelper = new RealmHelper(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(Tools.isNetworkConnected(this)) {
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    connectToFtp();
                }
            });

        }
        SendImageReceiver.completeWakefulIntent(intent);
    }

    private void connectToFtp() {
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
                            postPicture();
                        }else {
                            Log.d(TAG, "Directory not Exist");
                            workingdir = ftpHelper.ftpGetCurrentWorkingDirectory();
                            Log.i(TAG, "Working dir : "+workingdir);
                            postPicture();
                        }
                    } else {
                        Log.d(TAG, "Connection failed");
                    }
                }
            }).start();
        }
    }

    private void postPicture() {
        final String[] pathFile = getFirstImagePath();
        if(dataSize > 0){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean status = false;
                        fileUpload = new File(pathFile[0]);
                        if(fileUpload.exists())
                            status = ftpHelper.ftpUpload(pathFile[0], pathFile[1]);
                        else{
                            Log.d(TAG, "File not exist");
                        }

                        if (status == true) {
                            Log.d(TAG, "Upload success");
                            File file = new File(pathFile[0]);
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile[0]+" : "+deleted);
                            realmHelper.deleteData(pathFile[0]);
                            ftpHelper.ftpDisconnect();
                        } else {
                            Log.d(TAG, "Upload failed");
                            File file = new File(pathFile[0]);
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile[0]+" : "+deleted);
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
