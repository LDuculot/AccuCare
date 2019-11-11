package com.example.managebattery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CustomDrawableView extends View {
    private ShapeDrawable drawable;
    private String TAGD = "TAGd";
    private String TAGE = "TAGe";

    public CustomDrawableView(Context context, AttributeSet attrs)  {
        super(context, attrs);

        int width = (this.getResources().getDisplayMetrics().widthPixels)*850/1080;
        int height = width;
        int x = (this.getResources().getDisplayMetrics().widthPixels)/2 - width/2;
        //int x1 = this.getResources().getDisplayMetrics().widthPixels;
        //int x = 0;
        //Log.d(TAGD, "la valeur de x est : " + x1);
        int y = (this.getResources().getDisplayMetrics().heightPixels)/2 - height/2 - (this.getResources().getDisplayMetrics().heightPixels)*100/1776;
        //int y1 = this.getResources().getDisplayMetrics().heightPixels;
        //int y = -150;
        //Log.d(TAGE, "la valeur de y est : " + y1);

        drawable = new ShapeDrawable(new OvalShape());
        // If the color isn't set, the shape uses black as the default.
        drawable.getPaint().setColor(0xff4BADC5);
        // If the bounds aren't set, the shape can't be drawn.
        drawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
        drawable.draw(canvas);
    }
}
