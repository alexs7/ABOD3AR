package com.alexbath.abod3ar;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String OPENCVTAG = "OpenCVCamera";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat frame,frameHSV,thresh,mat4;
    private BaseLoaderCallback baseLoaderCallback;
    private TextView status;
    private Scalar lower;
    private Scalar upper;
    private Mat circles;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private int iCannyUpperThreshold;
    private int iMinRadius;
    private int iMaxRadius;
    private int iAccumulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        //new NetworkConnection().execute();

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
        status = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        if(OpenCVLoader.initDebug()){
            status.setText("OpenCV Loaded!");
            Log.d(OPENCVTAG, "OpenCV Loaded!");
        }else{
            status.setText("OpenCV Error!");
            Log.d(OPENCVTAG, "OpenCV Failed!");
        }
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
        iMinRadius = 30;
        iMaxRadius = 100;
        iAccumulator = 30;
        circles = new Mat();
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

        //Imgproc.cvtColor(thresh,mat4,Imgproc.COLOR_HSV2BGR);

        Imgproc.GaussianBlur( thresh, mat4, new Size(11, 11), 3, 3 );

        //System.out.println(">>>>"+mat4.channels());

        Imgproc.HoughCircles(mat4, circles, Imgproc.CV_HOUGH_GRADIENT,2.0,
                mat4.rows() / 8, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);

        for (int x = 0; x < circles.cols(); x++){

            System.out.println("Found Circle ?"+circles.cols());

            double vCircle[]=circles.get(0,x);

            Point center=new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);
            // draw the circle center
            Imgproc.circle(frame, center, 3,new Scalar(0,0,255), -1, 8, 0 );
            // draw the circle outline
            Imgproc.circle( frame, center, radius, new Scalar(0,0,255), 3, 8, 0 );

        }

        return frame;

//        Imgproc.HoughCircles(thresh, circles, Imgproc.CV_HOUGH_GRADIENT,2.0,
//                thresh.rows() / 8, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);
//
//        for (int x = 0; x < circles.cols(); x++){
//
//            System.out.println(circles.cols());
//
//            double vCircle[]=circles.get(0,x);
//
//            Point center=new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
//            int radius = (int)Math.round(vCircle[2]);
//            // draw the circle center
//            Imgproc.circle(frame, center, 3,new Scalar(0,0,255), -1, 8, 0 );
//            // draw the circle outline
//            Imgproc.circle( frame, center, radius, new Scalar(0,0,255), 3, 8, 0 );
//
//        }
//
//         Imgproc.circle (
//                frame,                 //Matrix obj of the image
//                new Point(230, 160),    //Center of the circle
//                100,                    //Radius
//                new Scalar(0, 255, 0),  //Scalar object for color
//                10                      //Thickness of the circle
//        );
//
//        return frame;

        //mat1 = inputFrame.rgba();
//
//        mat4 = inputFrame.rgba();
//
//        Scalar lower = new Scalar(29, 86, 6);
//        Scalar upper = new Scalar(64, 255, 255);
//
//        Imgproc.cvtColor(mat4,mat1,Imgproc.COLOR_BGR2HSV);
//        Core.inRange(mat1,lower,upper,mat2);
//
//        Imgproc.dilate(mat1, mat3, new Mat(), new Point(-1, -1), 2);
//        Imgproc.dilate(mat3, mat2, new Mat(), new Point(-1, -1), 2);
//
////        Mat hierarchy;
////        List<MatOfPoint> contours = ;
////        Imgproc.findContours ( mat2, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE );
////
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//
//        Imgproc.findContours(mat2, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Imgproc.drawContours(mat3, contours, -1, new Scalar(200, 255, 255, 0), 3);

        //Imgproc.cvtColor(mat3,mat4,Imgproc.COLOR_HSV2BGR);

//        Imgproc.circle (
//                mat2,                 //Matrix obj of the image
//                new Point(230, 160),    //Center of the circle
//                100,                    //Radius
//                new Scalar(0, 255, 0),  //Scalar object for color
//                10                      //Thickness of the circle
//        );

        //Imgproc.cvtColor(mat3,mat4,Imgproc.COLOR_HSV2RGB);

//        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
//        {
//            // for each contour, display it in blue
//            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//            {
//                Imgproc.drawContours(mat3, contours, idx, new Scalar(0, 255, 0));
//            }
//        }

//        return mat2;

        //Imgproc.blur(mat2, mat1, new Size(5, 5));
        //Imgproc.GaussianBlur( mat2, mat1, new Size(9, 9), 2, 2 );
        //Imgproc.Canny(mat1, mat3, 40, 40 * 3, 3, false);

//        Imgproc.findContours(mat2,contours,hierarchy,Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
//        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
//        {
//            // for each contour, display it in blue
//            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//            {
//                Imgproc.drawContours(mat3, contours, idx, new Scalar(250, 0, 0));
//            }
//        }

//        Imgproc.HoughCircles(mat1,circles,Imgproc.CV_HOUGH_GRADIENT, 2, mat3.rows()/4, 120, 10, 10, 18);
//
//        if(circles.cols() > 0){
//            status.append("Found: "+circles.cols()+" circles");
//        }

        //reset circles ?

        //rotate frame
        //Core.transpose(mat1,mat2);
        //Imgproc.resize(mat2,mat3,mat3.size(),0,0,Imgproc.INTER_LANCZOS4);
        //Core.flip(mat2,mat1,1);

//        return thresh;
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

    private class NetworkConnection extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {

            try {
                Socket socket = new Socket("10.0.2.2", 3001);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while(true) {
                    out.println(">>> Android Client: I want information!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
