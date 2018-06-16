package com.alexbath.abod3ar;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.Image;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.struct.image.ImageBase;

public class CameraActivity extends VisualizeCamera2Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        super.targetResolution = 1920*1080;

        FrameLayout surfaceLayout = findViewById(R.id.camera_frame_layout);
        startCamera(surfaceLayout,null);

    }



    @Override
    protected void processImage(ImageBase image) {

    }

}
