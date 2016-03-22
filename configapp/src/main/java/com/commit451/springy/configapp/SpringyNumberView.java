package com.commit451.springy.configapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.commit451.springy.common.SpringyClockRenderer;

/**
 * The view which shows how the time will be rendered using the {@link com.commit451.springy.common.SpringyClockRenderer}
 */
public class SpringyNumberView extends View {

    SpringyClockRenderer mSpringyClockRenderer;

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
        mSpringyClockRenderer = new SpringyClockRenderer();
        setWillNotDraw(false);
    }

    public void update(Time time) {
        mSpringyClockRenderer.update(time);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mSpringyClockRenderer.onDraw(canvas);
        postInvalidate();
    }
}
