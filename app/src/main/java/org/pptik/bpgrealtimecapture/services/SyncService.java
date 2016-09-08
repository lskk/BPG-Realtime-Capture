package org.pptik.bpgrealtimecapture.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.pptik.bpgrealtimecapture.R;
import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.ftp.FtpHelper;
import org.pptik.bpgrealtimecapture.helper.FilesHelper;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;
import org.pptik.bpgrealtimecapture.utilities.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

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
    private String pHost, pUser, pPass, pPort, pWorkingDir;
    private Timer timer;
    private FilesHelper filesHelper;
    RequestParams params = new RequestParams();

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
        filesHelper = new FilesHelper();
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
        if(filesHelper.getCount(context) > 0){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean status = false;
                        Log.i(TAG, "----------------------------------------------------------------------");
                        Log.i(TAG, "POSTING FILE");
                        Log.i(TAG, "----------------------------------------------------------------------");
                        String pathFile = filesHelper.getLastPath(context);
                        final String filename = filesHelper.getLastFilename(context);
                        String _id = filesHelper.getLastId(context);
                        Log.i(TAG, "start posting picture, path : "+pathFile);
                        fileUpload = new File(pathFile);
                        if(fileUpload.exists()) {
                            Log.i(TAG, "Uploading : " + filename);
                            status = ftpHelper.ftpUpload(pathFile, filename);
                        } else{
                            Log.d(TAG, "File not exist");
                        }

                        if (status == true) {
                            Log.d(TAG, "Upload success");

                            File file = new File(pathFile);
                            final long fileSizeInKB = file.length() / 1024 ;
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile+" : "+deleted);
                            filesHelper.delete(context, _id);
                            new ExecuteTask().execute(filename,
                                    String.valueOf(fileSizeInKB), getApplicationContext().getResources().getString(R.string.main_path_stored_file)+filename);
                        } else {
                            Log.d(TAG, "Upload failed");
                            File file = new File(pathFile);
                            boolean deleted = file.delete();
                            Log.i(TAG, "delete file "+pathFile+" : "+deleted);
                            filesHelper.delete(context, _id);
                            ftpHelper.ftpDisconnect();
                            connectToFtp();
                        }
                    } catch (Exception e) {
                    }

                }
            }).start();
        }else {
            Log.i(TAG, "data < 0");
        }
    }


    //--------------------------------------------------------------- store path to db

    class ExecuteTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... params) {
            String res = PostData(params);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "HTTP RES : "+result);
        }
    }

    public String PostData(String[] values) {
        String s="";
        try
        {
            HttpClient httpClient=new DefaultHttpClient();
            HttpPost httpPost=new HttpPost(getApplicationContext().getResources().getString(R.string.url_store));
            List<NameValuePair> list=new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("file_name", values[0]));
            list.add(new BasicNameValuePair("file_size", values[1]));
            list.add(new BasicNameValuePair("file_path", values[2]));
            httpPost.setEntity(new UrlEncodedFormEntity(list));
            HttpResponse httpResponse=  httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            s = readResponse(httpResponse);

        }
        catch(Exception exception)  {
            exception.printStackTrace();
        }
        return s;
    }


    public String readResponse(HttpResponse res) {
        InputStream is = null;
        String return_text="";
        try {
            is=res.getEntity().getContent();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(is));
            String line="";
            StringBuffer sb=new StringBuffer();
            while ((line=bufferedReader.readLine())!=null)
            {
                sb.append(line);
            }
            return_text=sb.toString();
        } catch (Exception e)
        {

        }
        return return_text;

    }

}