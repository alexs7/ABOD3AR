package com.alexbath.abod3ar;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.recklesscoding.abode.core.plan.Plan;
import com.recklesscoding.abode.core.plan.planelements.PlanElement;
import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String OPENCVTAG = "OpenCVCamera";
    private static final String SERVERTAG = "SERVER";
    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat frame,frameHSV,thresh, eroded, dilated,blurred;
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
    private Thread networkEnquirerThread;
    private Handler networkEnquirerResponseHandler;
    private Button connectToServerbutton;
    private Button loadPlanButton;
    private static final int START_NETWORK_THREAD = 0;
    private static final int SERVER_POLLING = 1;
    private Point center;
    Mat element = null;
    private ARPlanElement driveRoot = null;
    private ArrayList<ARPlanElement> drivesList = null;
    private boolean showElements = false;
    private int nodeRadialOffset = 320;

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
        statusTextView = (TextView) findViewById(R.id.status_text);
        serverTextView = (TextView) findViewById(R.id.server_response);
        serverTextView.setMovementMethod(new ScrollingMovementMethod());
        connectToServerbutton = findViewById(R.id.connect_server_button);
        loadPlanButton  = findViewById(R.id.load_plan_button);

        networkEnquirerThread = new Thread(new Runnable() {

            @Override
            public void run() {

                Socket socket = null;
                PrintWriter out = null;
                BufferedReader br = null;
                String response = null;

                try {
                    System.out.println("ANDROID connecting to server...");
                    socket = new Socket("138.38.99.31", 3001);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while(true){
                        try {
                            Thread.sleep(50); //network is might be slow too!
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        out.println("Request for Robot: "+robotIdx);
                        response = br.readLine();
                        Message message = new Message();
                        message.what = SERVER_POLLING;
                        message.obj = response;
                        networkEnquirerResponseHandler.sendMessage(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        connectToServerbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                networkEnquirerResponseHandler.sendEmptyMessage(START_NETWORK_THREAD);
            }
        });

        loadPlanButton.setOnClickListener(v -> {
            String fileName = "plans/DiaPlan3.inst";
            List<DriveCollection> driveCollections = PlanLoader.loadPlanFile(fileName, getApplicationContext());

            ConstraintLayout cl = findViewById(R.id.coordinatorLayout);
            drivesList = new ArrayList<>();

            driveRoot = new ARPlanElement(getApplicationContext(), "Drives", Color.YELLOW);

            for (DriveCollection driveCollection : driveCollections){
                ARPlanElement arPlanElement = new ARPlanElement(getApplicationContext(), driveCollection.getNameOfElement(), Color.RED);
                arPlanElement.setUIName(driveCollection.getNameOfElement());
                drivesList.add(arPlanElement);
                cl.addView(arPlanElement.getView());
            }

            cl.addView(driveRoot.getView());
            showElements = true;

        });

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.openCVCameraView);
        //cameraBridgeViewBase.setMaxFrameSize(1600,900); // this will improve performance!!
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
        //tv.setText(stringFromJNI());

        if(OpenCVLoader.initDebug()){
            statusTextView.setText("OpenCV Loaded!");
            Log.d(OPENCVTAG, "OpenCV Loaded!");
        }else{
            statusTextView.setText("OpenCV Error!");
            Log.d(OPENCVTAG, "OpenCV Failed!");
        }

        element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * 7 + 1, 2 * 7 + 1),
                new Point(7, 7));
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
        eroded = new Mat(width,height, CvType.CV_8UC4);
        dilated = new Mat(width,height, CvType.CV_8UC4);
        blurred = new Mat(width,height, CvType.CV_8UC4);
        lower = new Scalar(29, 86, 6);
        upper = new Scalar(64, 255, 255);

        iCannyUpperThreshold = 100;
        iMinRadius = 10;
        iMaxRadius = 120;
        iAccumulator = 60;
        circles = new Mat();

        drawCirclesDetection = false;
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
        frameHSV.release();
        thresh.release();
        eroded.release();
        dilated.release();
        blurred.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        frame = inputFrame.rgba();

        Imgproc.cvtColor(frame,frameHSV,Imgproc.COLOR_BGR2HSV);
        Core.inRange(frameHSV,lower,upper,thresh);

        Imgproc.erode(thresh, eroded,element);
        Imgproc.dilate(eroded, dilated,element);

        //returns single channel image!
        Imgproc.GaussianBlur(dilated, blurred, new Size(7, 7), 3, 3 );

        Imgproc.HoughCircles(blurred, circles, Imgproc.CV_HOUGH_GRADIENT,2.0,
                blurred.rows() / 8, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);

        if(circles.cols() == 1 ){

            int i = 0;
            //circlesDetails[0]=x, 1=y, 2=radius
            double circlesDetails[] = circles.get(0, 0);

            double circleX = Math.round(circlesDetails[0]);
            double circleY = Math.round(circlesDetails[1]);
            int radius = (int) Math.round(circlesDetails[2]);

            center = new Point(circleX,circleY);

            if(drawCirclesDetection){
                Imgproc.circle(frame, center,2, new Scalar(0,0,255), -1, 8, 0 );
                Imgproc.circle( frame, center, radius, new Scalar(0,0,255), 3, 8, 0 );
            }

            if(showElements){

                driveRoot.getView().setX((float) (circleX - driveRoot.getView().getWidth()/2));
                driveRoot.getView().setY((float) (circleY - driveRoot.getView().getHeight()/2));

                for(int k = 0; k<drivesList.size(); k++){

                    //TODO: 4 should be drivesList.size()!
                    float xV = (float) (circleX + nodeRadialOffset * Math.cos(Math.PI / 4 * (k + 1)));
                    float yV = (float) (circleY + nodeRadialOffset * Math.sin(Math.PI / 4 * (k + 1)));

                    drivesList.get(k).getView().setX(Math.round(xV));
                    drivesList.get(k).getView().setY(Math.round(yV));

                    Imgproc.line(frame, center,
                                 new Point(xV + drivesList.get(k).getView().getWidth()/2,yV + drivesList.get(k).getView().getHeight()/2),
                                 new Scalar(255,255,255),3);
                }
            }
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

        networkEnquirerResponseHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        networkEnquirerThread.start();
                    break;
                    case 1:
                        serverTextView.append("\n"+msg.obj);

                        String[] splittedLine = ((String) msg.obj).split(" ");
                        PlanElement planElement = null;
                        String typeOfPlanElement;
                        String planElementName = splittedLine[3];

                        if (isValidLine(splittedLine)) {
                            typeOfPlanElement = splittedLine[2];
                            if (!isActionPatternElement(typeOfPlanElement)) { //We ignore ActionPatternELements as they are instinct only
                                planElement = getPlanElement(typeOfPlanElement, planElement, planElementName);
                                if (planElement != null) {

                                    if(typeOfPlanElement.equals("D")){
                                        for (ARPlanElement drive : drivesList){
//                                            if(drive.getUIName().equals(planElementName)){
//                                                //increase flash/blink freq
//                                                drive.setBackgroundColor(Color.BLUE);
//                                            }else{
//                                                //decrease flash/blink freq
//                                                drive.setBackgroundColor(Color.parseColor("#2f4f4f"));
//                                            }
                                        }
                                    }
                                    //planElement.setToUpdate();
                                }
                            }
                        }

                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

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

    private boolean isValidLine(String[] splittedLine) {
        return !splittedLine[0].startsWith("*") && splittedLine.length >= 4;
    }

    private boolean isActionPatternElement(String typeOfPlanElement) {
        return typeOfPlanElement.startsWith("APE");
    }

    private boolean isActionPattern(String typeOfPlanElement) {
        return typeOfPlanElement.equals("AP");
    }

    private boolean isAction(String typeOfPlanElement) {
        return typeOfPlanElement.equals("A");
    }

    private boolean isCompetence(String typeOfPlanElement) {
        return typeOfPlanElement.equals("C");
    }

    private boolean isCompetenceElement(String typeOfPlanElement) {
        return typeOfPlanElement.equals("CE");
    }

    private boolean isDrive(String typeOfPlanElement) {
        return typeOfPlanElement.equals("D");
    }

    private PlanElement getPlanElement(String typeOfPlanElement, PlanElement planElement, String planElementName) {
        if (isAction(typeOfPlanElement)) {
            planElement = Plan.getInstance().findAction(planElementName);
            if (planElement == null) {
                planElement = new ActionEvent(planElementName);
            }
        } else if (isActionPattern(typeOfPlanElement)) {
            planElement = Plan.getInstance().findActionPattern(planElementName);
            if (planElement == null) {
                planElement = new ActionEvent(planElementName);
            }
        } else if (isCompetence(typeOfPlanElement)) {
            planElement = Plan.getInstance().findCompetence(planElementName);
        } else if (isCompetenceElement(typeOfPlanElement)) {
            planElement = Plan.getInstance().findCompetenceElement(planElementName);
        } else if (isDrive(typeOfPlanElement)) {
            planElement = Plan.getInstance().findDriveCollection(planElementName);
        }
        return planElement;
    }

}
