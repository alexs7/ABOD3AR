package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.concurrent.atomic.AtomicBoolean;

class ARPlanElement {

    private TextView element;
    private GradientDrawable drawableBackground;
//    private GradientDrawable drawableForeground;
    private String uIName;
    private int waitTime = 0;
    private static final int ARELEMENT_BACKGROUND_COLOR_CHANGE = 3;
    private final AtomicBoolean running;

    public ARPlanElement(Context applicationContext, String name, int borderColor){
        this.uIName = name;
        this.element = (TextView) View.inflate(applicationContext, R.layout.plan_element, null);
        this.element.setText(uIName);
        this.drawableBackground = (GradientDrawable) element.getBackground();
//        drawableForeground = (GradientDrawable) element.getForeground();
        this.drawableBackground.setStroke(2, borderColor);
        this.running = new AtomicBoolean(true);
    }

    public View getView() {
        return element;
    }

    public void setBackgroundColor(int color){
        drawableBackground.setColor(color);
    }

//    public void setForeGroundColor(int color){
//        drawableForeground.setColor(color);
//    }

    public String getUIName() {
        return uIName;
    }
}
