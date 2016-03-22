package com.commit451.springy.configapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.commit451.springy.common.*;
import com.commit451.springy.common.Number;

/**
 * The view which shows how the time will be rendered
 */
public class SpringyNumberView extends View {

    SpringyNumber mDigit1Hour;
    SpringyNumber mDigit2Hour;
    SpringyNumber mDigit1Minute;
    SpringyNumber mDigit2Minute;
    SpringyNumber mDigit1Second;
    SpringyNumber mDigit2Second;

    Paint mPaint;

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
        mDigit1Hour = new SpringyNumber(Number.ZERO);
        mDigit2Hour = new SpringyNumber(Number.ZERO);
        mDigit1Minute = new SpringyNumber(Number.ZERO);
        mDigit2Minute = new SpringyNumber(Number.ZERO);
        mDigit1Second = new SpringyNumber(Number.ZERO);
        mDigit2Second = new SpringyNumber(Number.ZERO);
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setAntiAlias(true);
    }

    public void update(Time time) {
        mDigit2Hour.animateTo(Number.VALUES[time.hour % 10]);
        mDigit1Hour.animateTo(Number.VALUES[time.hour / 10]);
        mDigit2Minute.animateTo(Number.VALUES[time.minute % 10]);
        mDigit1Minute.animateTo(Number.VALUES[time.minute/10]);
        mDigit2Second.animateTo(Number.VALUES[time.second % 10]);
        mDigit1Second.animateTo(Number.VALUES[time.second/10]);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int topWidth = canvas.getWidth()/4;
        int topHeight = (int) (canvas.getHeight() * 0.66666667f);
        int bottomWidth = canvas.getWidth()/8;
        int bottomHeight = (int) (canvas.getHeight() * 0.333333333f);
        mDigit1Hour.onDraw(canvas, topWidth, topHeight, 0, 0);
        mDigit2Hour.onDraw(canvas, topWidth, topHeight, topWidth, 0);
        mDigit1Minute.onDraw(canvas, topWidth, topHeight, 2*topWidth, 0);
        mDigit2Minute.onDraw(canvas, topWidth, topHeight, 3*topWidth, 0);
        mDigit1Second.onDraw(canvas, bottomWidth, bottomHeight, canvas.getWidth() - (2 * bottomWidth), topHeight);
        mDigit2Second.onDraw(canvas, bottomWidth, bottomHeight, canvas.getWidth() - bottomWidth, topHeight);
        postInvalidate();
    }
}
