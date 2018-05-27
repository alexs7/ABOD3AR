package com.alexbath.abod3ar;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String OPENCVTAG = "OpenCVCamera";
    private static final String SERVERTAG = "SERVER";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat frame,frameHSV,thresh,mat4;
    private BaseLoaderCallback baseLoaderCallback;
    private TextView statusTextView;
    private TextView serverTextView;
    private Scalar lower;
    private Scalar upper;
    private Mat circles;
    private int iCannyUpperThreshold;
    private int iMinRadius;
    private int iMaxRadius;
    private int iAccumulator;
    private boolean drawCirclesDetection;
    private int robotIdx = 0;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        robotIdx = 1;

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.openCVCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
                super.onManagerConnected(status);
            }
        };


        // Example of a call to a native method
        statusTextView = (TextView) findViewById(R.id.status_text);
        serverTextView = (TextView) findViewById(R.id.server_response);
        //tv.setText(stringFromJNI());

        if(OpenCVLoader.initDebug()){
            statusTextView.setText("OpenCV Loaded!");
            Log.d(OPENCVTAG, "OpenCV Loaded!");
        }else{
            statusTextView.setText("OpenCV Error!");
            Log.d(OPENCVTAG, "OpenCV Failed!");
        }
    }

    @Override
    protected void onStart() {
        //new NetworkConnection().execute();
        
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onCameraViewStarted(int width, int height) {
        frame = new Mat(width,height, CvType.CV_8UC4);
        frameHSV = new Mat(width,height, CvType.CV_8UC4);
        thresh = new Mat(width,height, CvType.CV_8UC4);
        mat4 = new Mat(width,height, CvType.CV_8UC4);
        lower = new Scalar(29, 86, 6);
        upper = new Scalar(64, 255, 255);

        iCannyUpperThreshold = 100;
        iMinRadius = 50;
        iMaxRadius = 100;
        iAccumulator = 60;
        circles = new Mat();

        drawCirclesDetection = false;
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
        frameHSV.release();
        thresh.release();
        mat4.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        frame = inputFrame.rgba();

        Imgproc.cvtColor(frame,frameHSV,Imgproc.COLOR_BGR2HSV);
        Core.inRange(frameHSV,lower,upper,thresh);

        //returns single channel image!
        Imgproc.GaussianBlur( thresh, mat4, new Size(11, 11), 3, 3 );

        Imgproc.HoughCircles(mat4, circles, Imgproc.CV_HOUGH_GRADIENT,2.0,
                mat4.rows() / 8, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);

        for (int i = 0; i < circles.cols(); i++){

            //circlesDetails[0]=x, 1=y, 2=radius
            double circlesDetails[] = circles.get(0, i);

            double circleX = Math.round(circlesDetails[0]);
            double circleY = Math.round(circlesDetails[1]);
            int radius = (int) Math.round(circlesDetails[2]);

            Point center = new Point(circleX,circleY);

            if(drawCirclesDetection){
                Imgproc.circle(frame, center,2, new Scalar(0,0,255), -1, 8, 0 );
                Imgproc.circle( frame, center, radius, new Scalar(0,0,255), 3, 8, 0 );
            }

            serverTextView.setX((float) circleX);
            serverTextView.setY((float) circleY);
        }

        return frame;

    }

    @Override
    protected void onPause() {

        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
        super.onPause();

    }

    @Override
    protected void onResume() {

        if(!OpenCVLoader.initDebug()){
            Log.d(OPENCVTAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
            Toast.makeText(getApplicationContext(),"OpenCV problem!",Toast.LENGTH_LONG).show();
        }else{
            Log.d(OPENCVTAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {

        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
        super.onDestroy();
    }

//    private class NetworkConnection extends AsyncTask<String,String,String>{
//
//        private String response = null;
//
//        @Override
//        protected void onPreExecute() {
//            serverTextView.append("aman");
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//
//           // try {
//                //Socket socket = new Socket("192.168.178.21", 3001);
//                //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            boolean foo = true;
//            while(foo){
//
//
////                    try {
////                        Thread.sleep(150);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                    out.println("Request for Robot: "+robotIdx);
////                    //System.out.println("server says:" + br.readLine());
////                    response = br.readLine();
////                    Log.d(SERVERTAG, response);
//                    this.publishProgress("aman");
//                }
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//
//
//            TextView serverTextView2 = (TextView) findViewById(R.id.server_response);
//            serverTextView2.append(values[0]);
//            //Log.d(SERVERTAG, "onProgressUpdate" + response);
//
//
//        }
//    }
}
