package com.commit451.springy.configapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.commit451.springy.shared.*;
import com.commit451.springy.shared.Number;

/**
 * The view
 * <br>
 * Copyright 2016 <a href="http://www.ovenbits.com">Oven Bits</a>
 *
 * @author Jawn.
 */
public class SpringyNumberView extends View {

    SpringyNumber mDigit1Second;
    SpringyNumber mDigit2Second;

    public SpringyNumberView(Context context) {
        super(context);
        init();
    }

    public SpringyNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpringyNumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public SpringyNumberView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDigit1Second = new SpringyNumber(Number.ZERO);
        mDigit2Second = new SpringyNumber(Number.ZERO);
        setWillNotDraw(false);
    }

    public void update(Time time) {
        mDigit2Second.animateTo(Number.VALUES[time.second % 10]);
        mDigit1Second.animateTo(Number.VALUES[time.second/10]);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth()/2;
        int height = canvas.getHeight()/2;
        mDigit1Second.onDraw(canvas, width, height, 0,0,0,0);
        mDigit2Second.onDraw(canvas, width, height, 0,0,0,0);
        postInvalidate();
    }
}
