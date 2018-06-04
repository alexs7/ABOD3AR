package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

class TVElement {

    TextView txtv = null;

    public TVElement(Context applicationContext, String string, Integer color) {
        txtv = new TextView(applicationContext);
        txtv.setText(string);
        txtv.setTextSize(16);
        txtv.setTextColor(color);
        txtv.setBackgroundColor(android.graphics.Color.rgb(47,79,79));
    }

    public TextView getTxtv() {
        return txtv;
    }

}
