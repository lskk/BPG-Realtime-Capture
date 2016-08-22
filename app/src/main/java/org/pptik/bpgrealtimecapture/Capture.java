package org.pptik.bpgrealtimecapture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.hardware.Camera;
import android.app.Activity;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.pptik.bpgrealtimecapture.setup.ApplicationConstants;

public class Capture extends Activity implements Runnable{
    private Camera camera;
    Button takePictureBtn;
    private String TAG = this.getClass().getSimpleName();
    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.content_capture);

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
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    jpgFile = new File(folder, System.currentTimeMillis()+".jpg");
                } else {
                    // TO-DO: Catch some crash
                }

                FileOutputStream outStream = new FileOutputStream(jpgFile);
                outStream.write(data);
                outStream.close();
                Log.i(TAG, "Image Path : "+ jpgFile.getAbsolutePath());
                Log.i(TAG, "is file exist : "+String.valueOf(jpgFile.exists()));
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