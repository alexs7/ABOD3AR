package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

class ARPlanElement {

    private TextView element;
    private GradientDrawable drawable;
    private DriveCollection driveCollection;

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
        drawable.setColor(Color.WHITE);
    }

    public void setDriveCollection(DriveCollection driveCollection) {
        this.driveCollection = driveCollection;
    }
}
