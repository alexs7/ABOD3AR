package com.alexbath.abod3ar;

import android.view.View;
import android.widget.TextView;

public class ViewHolder {

    private TextView mTextView;

    public ViewHolder(View view) {
        mTextView = view.findViewById(R.id.textView);
    }

    public TextView getmTextView() {
        return mTextView;
    }


}
