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

class ARPlanElement {

    private TextView element;
    private GradientDrawable drawable;
    private String uIName;
    private Thread flasherThread;
    private int waitTime = 0;
    private static final int ARELEMENT_BACKGROUND_COLOR_CHANGE = 3;

    public ARPlanElement(Context applicationContext, String text, int borderColor){
        element = (TextView) View.inflate(applicationContext, R.layout.plan_element, null);
        element.setText(text);
        drawable = (GradientDrawable) element.getBackground();
        drawable.setStroke(2, borderColor);
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

    public Thread getFlasherThread() {
        return flasherThread;
    }

    public void createFlasherThread(Handler handler) {

        flasherThread = new Thread(new Runnable() {
            Message message = null;
            @Override
            public void run() {
                while(true){

                    if(waitTime == 0){

                        continue;

                    }else {
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

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
                    }
                }
            }
        });
    }

    public void increaseFlashFrequency(){
        waitTime -= 300;
        if (waitTime < 0) {
            waitTime = 0;
        }
    }

    public void decreaseFlashFrequency(){
        waitTime += 300;
        if (waitTime > 900) {
            waitTime = 900;
        }
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
