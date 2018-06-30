package com.alexbath.abod3ar;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.recklesscoding.abode.core.plan.Plan;
import com.recklesscoding.abode.core.plan.planelements.PlanElement;
import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import boofcv.abst.tracker.ConfigComaniciu2003;
import boofcv.abst.tracker.ConfigTld;
import boofcv.abst.tracker.MeanShiftLikelihoodType;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.tracker.sfot.SfotConfig;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Quadrilateral_F64;

import static com.alexbath.abod3ar.ObjectTrackerActivity.TrackerType.MEAN_SHIFT;

/**
 * Allow the user to select an object in the image and then track it
 *
 * @author Peter Abeles
 */
public class ObjectTrackerActivity extends Camera2Activity
        implements View.OnTouchListener
{

    private int mode = 0;

    // size of the minimum square which the user can select
    private final static int MINIMUM_MOTION = 20;

    private Point2D_I32 click0 = new Point2D_I32();
    private Point2D_I32 click1 = new Point2D_I32();
    private FrameLayout surfaceLayout = null;
    private Button connectToServerbutton = null;
    private Button loadPlanButton = null;
    private TextView statusTextView = null;
    private TextView serverTextView = null;
    private Button reset_button = null;
    private Button debugButton = null;
    private ConstraintLayout rootLayout = null;
    private ArrayList<ARPlanElement> drivesList = null;
    private ARPlanElement driveRoot = null;
    private boolean showARElements = false;
    private boolean showUI = false;
    private static final int SERVER_RESPONSE = 1;
    private static final int HIDE_ARPLANELEMENTS = 6;
    private static final int SHOW_ARPLANELEMENTS = 7;
    private Handler generalHandler = null;
    private String planName = null;
    private String serverIPAddress = null;
    private int serverPort;
    private ExecutorService networkExecutor = null;
    private NetworkTask networkTask = null;
    private ScheduledExecutorService serverPingerScheduler;
    private UIPlanTree.Node<ARPlanElement> root;
    private UIPlanTree uiPlanTree;
    private boolean highLevelView;

    public enum TrackerType { // TODO: add the others later
        CIRCULANT,MEAN_SHIFT_LIKELIHOOD,MEAN_SHIFT
    }

    public ObjectTrackerActivity() {
        super(Resolution.R1920x1080);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
        }

        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        planName = "plans/DiaPlan3.inst";
        serverIPAddress = "192.168.178.21";
        serverPort = 3001;
        highLevelView = true;

        createGeneralHandler();

        rootLayout = findViewById(R.id.root_layout);
        surfaceLayout = findViewById(R.id.camera_frame_layout);
        statusTextView = (TextView) findViewById(R.id.status_text);
        statusTextView.setMovementMethod(new ScrollingMovementMethod());
        serverTextView = (TextView) findViewById(R.id.server_response);
        serverTextView.setMovementMethod(new ScrollingMovementMethod());
        connectToServerbutton = findViewById(R.id.connect_server_button);
        loadPlanButton = findViewById(R.id.load_plan_button);
        debugButton = findViewById(R.id.debug_mode);

        statusTextView.append("\n Select the Robot first!");

        startCamera(surfaceLayout,null);
        displayView.setOnTouchListener(this);

        reset_button = findViewById(R.id.reset_button);
        reset_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetPressed();
            }
        });

        debugButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(showUI) {
                    showUI = false;
                    serverTextView.setVisibility(View.INVISIBLE);
                    connectToServerbutton.setVisibility(View.INVISIBLE);
                    loadPlanButton.setVisibility(View.INVISIBLE);
                    statusTextView.setVisibility(View.INVISIBLE);
                    serverTextView.setVisibility(View.INVISIBLE);
                    reset_button.setVisibility(View.INVISIBLE);
                }else{
                    showUI = true;
                    serverTextView.setVisibility(View.VISIBLE);
                    connectToServerbutton.setVisibility(View.VISIBLE);
                    loadPlanButton.setVisibility(View.VISIBLE);
                    statusTextView.setVisibility(View.VISIBLE);
                    serverTextView.setVisibility(View.VISIBLE);
                    reset_button.setVisibility(View.VISIBLE);
                }
            }
        });

        connectToServerbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(drivesList != null || !drivesList.isEmpty()) {

                    if(networkExecutor == null) {

                        //create executor and start that will get the server requests
                        networkExecutor = Executors.newSingleThreadExecutor();
                        serverPingerScheduler = Executors.newScheduledThreadPool(1);
                        networkTask = new NetworkTask(serverIPAddress, serverPort, generalHandler, serverPingerScheduler);
                        networkExecutor.execute(networkTask);

                    }else{
                        System.out.println("networkExecutor already exists!");
                    }

                }else {
                    statusTextView.append("\n Load a Plan first!");
                }
            }
        });

        loadPlanButton.setOnClickListener(v -> {

            if(driveRoot == null && drivesList == null) {

                String fileName = planName;
                List<DriveCollection> driveCollections = PlanLoader.loadPlanFile(fileName, getApplicationContext());

                //createTree
                uiPlanTree = new UIPlanTree(driveCollections,getApplicationContext());
                root = uiPlanTree.getRoot();
                uiPlanTree.addNodesToUI(rootLayout,root);

                drivesList = new ArrayList<>();

                driveRoot = new ARPlanElement(getApplicationContext(), null, Color.YELLOW);
                rootLayout.addView(driveRoot.getView());

                for (DriveCollection driveCollection : driveCollections) {

                    ARPlanElement arPlanElement = new ARPlanElement(getApplicationContext(), driveCollection.getNameOfElement(), Color.RED);

                    drivesList.add(arPlanElement);
                    rootLayout.addView(arPlanElement.getView());

                    arPlanElement.getView().setOnClickListener(new View.OnClickListener() {
                        ARPlanElement localArPlanElement = arPlanElement;
                        String triggeredElementName = null;

                        public void onClick(View v) {

//                            removeARPlanElements(rootLayout,driveRoot,drivesList);
//                            driveRoot = localArPlanElement;
//                            rootLayout.addView(driveRoot.getView());
//                            PlanElement triggeredElement = localArPlanElement.getDriveCollection().getTriggeredElement();
//
//                            if(triggeredElement != null) {
//                                ARPlanElement arPlanElement = new ARPlanElement(getApplicationContext(), driveCollection, Color.RED);
//                                arPlanElement.setUIName(driveCollection.getNameOfElement());
//                                drivesList.clear();
//                                drivesList.add(arPlanElement);
//                                //rootLayout.addView(arPlanElement.getView());
//                            }
//                            showARElements = false;
//                            statusTextView.append("\n Looking at Drive: " + localArPlanElement.getUIName());
//                            removeARPlanElements(rootLayout,driveRoot,drivesList);
//                            drivesList.clear();
//                            driveRoot = arPlanElement;
//                            rootLayout.addView(driveRoot.getView());
//                            drivesList.add(new ARPlanElement(getApplicationContext(), driveCollection.getTriggeredElement().getNameOfElement(), Color.RED));
//                            showARElements = true;
                            //TODO: change request
                            //TODO: start network calls
                        }
                    });
                }

                showARElements = true;
            }
        });

    }

    private void createGeneralHandler() {
        generalHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){

                switch (msg.what){
                    case SERVER_RESPONSE:

                        serverTextView.append("\n"+msg.obj);

                        updateARElementsVisuals(msg);

                        break;
                    case HIDE_ARPLANELEMENTS:

                            hideARPlanElements(driveRoot,drivesList);
                        break;
                    case SHOW_ARPLANELEMENTS:

                        if(driveRoot != null && (drivesList != null || !drivesList.isEmpty())) {

                            driveRoot.getView().setVisibility(View.VISIBLE);

                            for (ARPlanElement arPlanElement : drivesList) {
                                arPlanElement.getView().setVisibility(View.VISIBLE);
                            }
                        }
                        break;

                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    private void updateARElementsVisuals(Message msg) {
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
                            if(drive.getUIName().equals(planElementName)){
                                drive.setBackgroundColor(Color.parseColor("#0000ff"));
                            }else{
                                drive.setBackgroundColor(Color.parseColor("#2f4f4f"));
                            }
                        }
                    }else{
                        //System.out.println("NOT A DRIVE");
                    }
                }
            }
        }
    }

    @Override
    public void createNewProcessor() {
        startObjectTracking(setTrackerType(MEAN_SHIFT));
    }

    private void startObjectTracking(int pos) {
        TrackerObjectQuad tracker;

        switch (pos) {
            case 0:
                tracker = FactoryTrackerObjectQuad.circulant(null,GrayU8.class);
                break;

            case 1: {
                ImageType imageType = ImageType.pl(3, GrayU8.class);
                tracker = FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(false), imageType);
            }break;

            case 2: {
                ImageType imageType = ImageType.pl(3, GrayU8.class);
                tracker = FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(true), imageType);
            }break;

            case 3: {
                ImageType imageType = ImageType.pl(3, GrayU8.class);
                tracker = FactoryTrackerObjectQuad.meanShiftLikelihood(30, 5, 256, MeanShiftLikelihoodType.HISTOGRAM, imageType);
            }break;

            case 4:{
                SfotConfig config = new SfotConfig();
                config.numberOfSamples = 10;
                config.robustMaxError = 30;
                tracker = FactoryTrackerObjectQuad.sparseFlow(config,GrayU8.class,null);
            }break;

            case 5:
                tracker = FactoryTrackerObjectQuad.tld(new ConfigTld(false),GrayU8.class);
                break;

            default:
                throw new RuntimeException("Unknown tracker: "+pos);
        }
        setProcessing(new TrackingProcessing(tracker) );
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if( mode == 0 ) {
            if(MotionEvent.ACTION_DOWN == motionEvent.getActionMasked()) {
                click0.set((int) motionEvent.getX(), (int) motionEvent.getY());
                click1.set((int) motionEvent.getX(), (int) motionEvent.getY());
                mode = 1;
            }
        } else if( mode == 1 ) {
            if(MotionEvent.ACTION_MOVE == motionEvent.getActionMasked()) {
                click1.set((int)motionEvent.getX(),(int)motionEvent.getY());
            } else if(MotionEvent.ACTION_UP == motionEvent.getActionMasked()) {
                click1.set((int)motionEvent.getX(),(int)motionEvent.getY());
                mode = 2;
            }
        }
        return true;
    }

    private int setTrackerType(TrackerType type) {
        switch (type) {
            case CIRCULANT:
                return 0;
            case MEAN_SHIFT:
                return 1;
            case MEAN_SHIFT_LIKELIHOOD:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown");
        }
    }

    protected class TrackingProcessing extends DemoProcessingAbstract {

        TrackerObjectQuad tracker;
        boolean visible;

        Quadrilateral_F64 location = new Quadrilateral_F64();
        Point2D_F64 center = new Point2D_F64();

        Paint paintSelected = new Paint();
        Paint paintLine0 = new Paint();
        Paint paintLine1 = new Paint();
        Paint paintLine2 = new Paint();
        Paint paintLine3 = new Paint();
        private Paint textPaint = new Paint();

        int width,height;

        public TrackingProcessing(TrackerObjectQuad tracker ) {
            super(tracker.getImageType());
            mode = 0;
            this.tracker = tracker;

            paintSelected.setARGB(0xFF/2,0xFF,0xFF,0);
            paintSelected.setStyle(Paint.Style.FILL_AND_STROKE);

            paintLine0.setColor(Color.YELLOW);
            paintLine1.setColor(Color.YELLOW);
            paintLine2.setColor(Color.YELLOW);
            paintLine3.setColor(Color.WHITE);

            // Create out paint to use for drawing
            textPaint.setARGB(255, 200, 0, 0);
        }

        private void drawLine( Canvas canvas , Point2D_F64 a , Point2D_F64 b , Paint color ) {
            canvas.drawLine((float)a.x,(float)a.y,(float)b.x,(float)b.y,color);
        }

        private void drawCenter(Canvas canvas, Point2D_F64 center, Paint color ) {
            canvas.drawPoint((float)center.x,(float)center.y, color);
        }

        private void makeInBounds( Point2D_F64 p ) {
            if( p.x < 0 ) p.x = 0;
            else if( p.x >= width )
                p.x = width - 1;

            if( p.y < 0 ) p.y = 0;
            else if( p.y >= height )
                p.y = height - 1;
        }

        private boolean movedSignificantly( Point2D_F64 a , Point2D_F64 b ) {
            if( Math.abs(a.x-b.x) < MINIMUM_MOTION )
                return false;
            if( Math.abs(a.y-b.y) < MINIMUM_MOTION )
                return false;

            return true;
        }

        @Override
        public void initialize(int imageWidth, int imageHeight, int sensorOrientation) {
            this.width = imageWidth;
            this.height = imageHeight;

            float density = cameraToDisplayDensity;
            paintSelected.setStrokeWidth(5f*density);
            paintLine0.setStrokeWidth(5f*density);
            paintLine1.setStrokeWidth(5f*density);
            paintLine2.setStrokeWidth(5f*density);
            paintLine3.setStrokeWidth(2.5f*density);
            textPaint.setTextSize(60*density);
        }

        @Override
        public void onDraw(Canvas canvas, Matrix imageToView) {

            canvas.concat(imageToView);
            if( mode == 1 ) {
                Point2D_F64 a = new Point2D_F64();
                Point2D_F64 b = new Point2D_F64();

                applyToPoint(viewToImage, click0.x, click0.y, a);
                applyToPoint(viewToImage, click1.x, click1.y, b);

                double x0 = Math.min(a.x,b.x);
                double x1 = Math.max(a.x,b.x);
                double y0 = Math.min(a.y,b.y);
                double y1 = Math.max(a.y,b.y);

                canvas.drawRect((int) x0, (int) y0, (int) x1, (int) y1, paintSelected);
            } else if( mode == 2 ) {
                if (!imageToView.invert(viewToImage)) {
                    return;
                }
                applyToPoint(viewToImage,click0.x, click0.y, location.a);
                applyToPoint(viewToImage,click1.x, click1.y, location.c);

                // make sure the user selected a valid region
                makeInBounds(location.a);
                makeInBounds(location.c);

                if( movedSignificantly(location.a,location.c) ) {
                    // use the selected region and start the tracker
                    location.b.set(location.c.x, location.a.y);
                    location.d.set( location.a.x, location.c.y );

                    visible = true;
                    mode = 3;
                } else {
                    // the user screw up. Let them know what they did wrong
                    runOnUiThread(() -> Toast.makeText(ObjectTrackerActivity.this,
                            "Drag a larger region", Toast.LENGTH_SHORT).show());
                    mode = 0;
                }
            }

            if( mode >= 2 ) {
                if( visible ) {

                    center = updateCenter(location);
                    drawCenter(canvas,center,paintLine1);

//                    drawLine(canvas,q.a,q.b,paintLine0);
//                    drawLine(canvas,q.b,q.c,paintLine1);
//                    drawLine(canvas,q.c,q.d,paintLine2);
//                    drawLine(canvas,q.d,q.a,paintLine3);

                    if(showARElements && driveRoot !=null & (drivesList != null || !drivesList.isEmpty())){

                        uiPlanTree.renderPlan(canvas,root,center, highLevelView, paintLine3);


//                        driveRoot.getView().setX((float) (center.x - driveRoot.getView().getWidth()/2));
//                        driveRoot.getView().setY((float) (center.y - driveRoot.getView().getHeight()/2));
//
//                        for(int k = 0; k<drivesList.size(); k++){
//
//                            //TODO: 4 should be drivesList.size()!
//                            float xV = (float) (center.x + 290 * Math.cos(Math.PI / drivesList.size() * (2*k + 1)));
//                            float yV = (float) (center.y + 290 * Math.sin(Math.PI / drivesList.size() * (2*k + 1)));
//
//                            drivesList.get(k).getView().setX(Math.round(xV));
//                            drivesList.get(k).getView().setY(Math.round(yV));
//
//                            drawLine(canvas,new Point2D_F64(center.x,center.y),
//                                    new Point2D_F64(xV+ drivesList.get(k).getView().getWidth()/2,yV + drivesList.get(k).getView().getHeight()/2),paintLine3);
//
//                        }
                    }

                } else {
                    canvas.drawText("?",width/2,height/2,textPaint);
                }
            }
        }

        private Point2D_F64 updateCenter(Quadrilateral_F64 location) {
            center.x = (location.c.x + location.a.x)/2;
            center.y = (location.c.y + location.a.y)/2;

            return center;
        }

        @Override
        public void process(ImageBase input) {
            if( mode == 3 ) {
                tracker.initialize(input, location);
                visible = true;
                mode = 4;
            } else if( mode == 4 ) {
                //surfaceLayout.setVisibility(View.INVISIBLE);
                visible = tracker.process(input,location);
            }
        }
    }

    private boolean stopExecutorService(ExecutorService service) {

        if(service == null){
            return false;
        }else {
            service.shutdown();
            try {
                if (!service.awaitTermination(100, TimeUnit.MICROSECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.out.println("stopExecutorService() throwed " + e.getMessage());
                e.printStackTrace();
            }
        }

        if(service.isTerminated() && service.isShutdown()){
            Log.i("NETWORK_TASK", "SUCCESSFULL SHUTDOWN OF NETWORK_TASK");
            return true;
        }else{
            return false;
        }
    }

    public void resetPressed() {
        stopExecutorService(serverPingerScheduler);
        generalHandler.removeCallbacksAndMessages(null);
//        stopFlashingARElements(driveRoot,drivesList);
        removeARPlanElements(rootLayout,driveRoot,drivesList);
        showARElements = false;
        mode = 0;
    }
    @Override
    protected void onStop() {
        super.onStop();

        stopExecutorService(serverPingerScheduler);
        generalHandler.removeCallbacksAndMessages(null);
    }

    private void hideARPlanElements(ARPlanElement driveRoot, ArrayList<ARPlanElement> drivesList) {
        if(driveRoot != null && (drivesList != null || !drivesList.isEmpty())) {
            driveRoot.getView().setVisibility(View.INVISIBLE);

            for (ARPlanElement arPlanElement : drivesList) {
                arPlanElement.getView().setVisibility(View.INVISIBLE);
            }
        }
    }

    private void removeARPlanElements(ConstraintLayout rootLayout, ARPlanElement driveRoot, ArrayList<ARPlanElement> drivesList){
        if(driveRoot != null && (drivesList != null || !drivesList.isEmpty())) {
            rootLayout.removeView(driveRoot.getView());
            for (ARPlanElement arPlanElement : drivesList) {
                rootLayout.removeView(arPlanElement.getView());
            }
        }
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