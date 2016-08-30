package org.pptik.bpgrealtimecapture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.hardware.Camera;
import android.app.Activity;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.pptik.bpgrealtimecapture.bean.SavedFileModel;
import org.pptik.bpgrealtimecapture.helper.FilesHelper;
import org.pptik.bpgrealtimecapture.helper.RealmHelper;
import org.pptik.bpgrealtimecapture.receivers.SendImageReceiver;
import org.pptik.bpgrealtimecapture.services.SyncService;
import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

public class Capture extends Activity implements Runnable{
    private Camera camera;
    private RealmHelper realmHelper;
    private FilesHelper filesHelper;
    private String TAG = this.getClass().getSimpleName();
    private Timer timer;
    private boolean isSavedSuccess = false;
    private ArrayList<SavedFileModel> datas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.content_capture);

    //    SendImageReceiver sendImageReceiver = new SendImageReceiver();
    //    sendImageReceiver.setAlarm(this);
        startService(new Intent(Capture.this, SyncService.class));

        realmHelper = new RealmHelper(Capture.this);
        filesHelper = new FilesHelper();
        initSurface();


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
        }, 30000, 30000);
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
                Log.i(TAG, "----------------------------------------------------------------------");
                Log.i(TAG, "SAVING FILE");

                isSavedSuccess = jpgFile.exists();
                Log.i(TAG, "Success save file : "+isSavedSuccess);
                Log.i(TAG, "Image Path : "+ jpgFile.getAbsolutePath());
                if(isSavedSuccess) {
                //    realmHelper.addFile(filename, jpgFile.getAbsolutePath());
                //    datas = realmHelper.findAllArticle();
                    filesHelper.insert(Capture.this, filename, jpgFile.getAbsolutePath());
                //    Log.i(TAG, "Total Size : "+datas.size());
                    Log.i(TAG, "Total size : "+filesHelper.getCount(Capture.this));
                    Log.i(TAG, "----------------------------------------------------------------------");
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