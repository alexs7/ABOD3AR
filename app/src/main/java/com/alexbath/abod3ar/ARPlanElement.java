package com.alexbath.abod3ar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import georegression.struct.point.Point2D_F64;

class ARPlanElement {

    private final String name;
    private TextView element;
    private GradientDrawable drawableBackground;
//    private GradientDrawable drawableForeground;
    private String uIName;
    private int waitTime = 0;
    private static final int ARELEMENT_BACKGROUND_COLOR_CHANGE = 3;
    private boolean dragging = false;
    private boolean dragged = false;
    private Point2D_F64 newCoordinates;

    public ARPlanElement(Context applicationContext, int priority, String name, int borderColor){

        this.name = name;

        if(priority != 0){
            this.uIName = Integer.toString(priority)+": "+name;
        }else{
            this.uIName = name;
        }

        this.element = (TextView) View.inflate(applicationContext, R.layout.plan_element, null);
        this.element.setText(uIName);
        this.drawableBackground = (GradientDrawable) element.getBackground();
//        drawableForeground = (GradientDrawable) element.getForeground();
        this.drawableBackground.setStroke(2, borderColor);

        this.newCoordinates = new Point2D_F64(0,0);
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

    public String getName(){
        return name;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean getDragging() {
        return dragging;
    }

    public void setDragged(boolean dragged) {
        this.dragged = dragged;
    }

    public boolean getDragged() {
        return dragged;
    }

    public void setNewCoordinates(Point2D_F64 coordinates) {
        this.newCoordinates = coordinates;
    }

    public Point2D_F64 getNewCoordinates() {
        return newCoordinates;
    }

}
