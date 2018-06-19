package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class ARPlanElement {

    private TextView element;
    private GradientDrawable drawable;
    private String uIName;
    private Thread flasherThread;
    private int waitTime = 0;
    private static final int ARELEMENT_BACKGROUND_COLOR_CHANGE = 3;
    private final AtomicBoolean running;

    public ARPlanElement(Context applicationContext, String text, int borderColor){
        element = (TextView) View.inflate(applicationContext, R.layout.plan_element, null);
        element.setText(text);
        drawable = (GradientDrawable) element.getBackground();
        drawable.setStroke(2, borderColor);
        this.running = new AtomicBoolean(true);
    }

    public View getView() {
        return element;
    }

    public void setBackgroundColor(int color){
        drawable.setColor(color);
    }

    public void setUIName(String UIName) {
        this.uIName = UIName;
    }

    public String getUIName() {
        return uIName;
    }

    public void startFlasherThread() {
        flasherThread.start();
    }

    public void stopFlasherThread() {
        running.set(false);
    }

    public void createFlasherThread(Handler handler) {

        flasherThread = new Thread(new Runnable() {

            Message message = null;
            String opacity = ""; // Reference: https://stackoverflow.com/questions/15852122/hex-transparency-in-colors
            String color = "";

            @Override
            public void run() {
                while(running.get()){

                    if(waitTime > 0) {
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//
//                        if(waitTime < 900){
//                            opacity = "FF";
//                        }
//
//                        if(waitTime > 900){
//                            opacity = "A6";
//                        }
//
//                        if(waitTime > 1400){
//                            opacity = "A6";
//                            color = "0000ff";
//                        }else{
//                            color = "2f4f4f";
//                        }

                        message = new Message();
                        message.what = ARELEMENT_BACKGROUND_COLOR_CHANGE;
                        message.obj = getUIName() + ":" + "#0000ff";
                        handler.sendMessage(message);

                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        message = new Message();
                        message.what = ARELEMENT_BACKGROUND_COLOR_CHANGE;
                        message.obj = getUIName() + ":" + "#2f4f4f";
                        handler.sendMessage(message);
                    }else{ //TODO: Do I need this sleep ?
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
    }

    public void increaseFlashFrequency(){
        waitTime -= 300;
        if (waitTime < 0) {
            waitTime = 100;
        }
    }

    public void decreaseFlashFrequency(){
        waitTime += 150;
        if (waitTime > 1500) {
            waitTime = 1500;
        }
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
