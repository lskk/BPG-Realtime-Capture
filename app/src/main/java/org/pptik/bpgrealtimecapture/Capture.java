package org.pptik.bpgrealtimecapture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.app.Activity;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.ftp.FtpHelper;
import org.pptik.bpgrealtimecapture.helper.RealmHelper;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;
import org.pptik.bpgrealtimecapture.utilities.WarningDialog;

public class Capture extends Activity implements Runnable{
    private Camera camera;
    private RealmHelper realmHelper;
    private String TAG = this.getClass().getSimpleName();
    private Timer timer;
    private boolean isSavedSuccess = false;

    private SharedPreferences sharedPreferences;
    private String pHost, pUser, pPass, pPort, pWorkingDir;
    private ProgressDialog dialog;
    private String workingdir;
    private ArrayList<SavedFileModel> data;
    private int dataSize = 0;
    private File fileUpload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.content_capture);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        dialog.setMessage("Connecting to FTP Server...");
        setupFtp();

        realmHelper = new RealmHelper(Capture.this);
        initSurface();


    }




    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            if (msg.what == 0) {
                dialog.dismiss();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ApplicationConstants.PREFS_FTP_WORKING_DIRECTORY, workingdir);
                editor.commit();
            } else if (msg.what == 1) {
                // connect success
                dialog.dismiss();

            } else if (msg.what == 2) {
                // file not exist

            } else if (msg.what == 3) {
                // success upload



            }else if (msg.what == 4) {
                // failed upload

            }else {

            }

        }

    };

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

    private void initSurface(){
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(176, 144);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceCallback());


        //--- BUILD TIMER ---//
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Capture.this.run();
            }
        }, 5000, 5000);
    }

    public void takepicture(){
        camera.takePicture(null, null, new MyPictureCallback());
    }

    private final class MyPictureCallback implements PictureCallback{
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                File folder = new File(Environment.getExternalStorageDirectory() + ApplicationConstants.DIRECTORY_FILE_NAME);
                File jpgFile = null;
                String filename = "";
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    filename = System.currentTimeMillis()+".jpg";
                    jpgFile = new File(folder, filename);
                } else {
                    // TO-DO: Catch some crash
                }

                FileOutputStream outStream = new FileOutputStream(jpgFile);
                outStream.write(data);
                outStream.close();
                isSavedSuccess = jpgFile.exists();
                Log.i(TAG, "Success save file : "+isSavedSuccess);
                Log.i(TAG, "Image Path : "+ jpgFile.getAbsolutePath());
                if(isSavedSuccess) {
                    realmHelper.addFile(filename, jpgFile.getAbsolutePath());
                }
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private final class SurfaceCallback implements Callback{
        public void surfaceCreated(SurfaceHolder holder) {
            try{
                camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                Log.i(TAG, parameters.flatten());
                parameters.setPreviewSize(800, 480);
                parameters.setPreviewFrameRate(5);
                List<Camera.Size> ss = parameters.getSupportedPictureSizes();
                Camera.Size s = ss.get(0);
                parameters.setPictureSize(s.width, s.height);
                parameters.setJpegQuality(100);
                parameters.setRotation(0);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera!=null){
                camera.release();
                camera = null;
            }
        }

    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }


    @Override
    public void run() {
        takepicture();
    }


}