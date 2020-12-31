package com.minhui.networkcapture.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/9.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class SmartTextView extends TextView {


    public SmartTextView(Context context) {
        super(context);
    }

    public SmartTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        }catch (Exception e){

        }

    }
}
