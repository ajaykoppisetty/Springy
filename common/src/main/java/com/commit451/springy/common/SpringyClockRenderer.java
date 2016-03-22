package com.commit451.springy.common;

import android.graphics.Canvas;
import android.text.format.Time;

/**
 * Given a canvas, renders the clock
 */
public class SpringyClockRenderer {

    SpringyNumber mDigit1Hour;
    SpringyNumber mDigit2Hour;
    SpringyNumber mDigit1Minute;
    SpringyNumber mDigit2Minute;
    SpringyNumber mDigit1Second;
    SpringyNumber mDigit2Second;

    public SpringyClockRenderer() {
        mDigit1Hour = new SpringyNumber(Number.ZERO);
        mDigit2Hour = new SpringyNumber(Number.ZERO);
        mDigit1Minute = new SpringyNumber(Number.ZERO);
        mDigit2Minute = new SpringyNumber(Number.ZERO);
        mDigit1Second = new SpringyNumber(Number.ZERO);
        mDigit2Second = new SpringyNumber(Number.ZERO);
    }

    public void update(Time time) {
        mDigit2Hour.animateTo(Number.VALUES[time.hour % 10]);
        mDigit1Hour.animateTo(Number.VALUES[time.hour / 10]);
        mDigit2Minute.animateTo(Number.VALUES[time.minute % 10]);
        mDigit1Minute.animateTo(Number.VALUES[time.minute/10]);
        mDigit2Second.animateTo(Number.VALUES[time.second % 10]);
        mDigit1Second.animateTo(Number.VALUES[time.second/10]);
    }

    public void onDraw(Canvas canvas, boolean drawSeconds) {
        int topWidth = canvas.getWidth()/4;
        int topHeight = (int) (canvas.getHeight() * 0.66666667f);
        int bottomWidth = canvas.getWidth()/8;
        int bottomHeight = (int) (canvas.getHeight() * 0.333333333f);
        mDigit1Hour.onDraw(canvas, topWidth, topHeight, 0, 0);
        mDigit2Hour.onDraw(canvas, topWidth, topHeight, topWidth, 0);
        mDigit1Minute.onDraw(canvas, topWidth, topHeight, 2*topWidth, 0);
        mDigit2Minute.onDraw(canvas, topWidth, topHeight, 3*topWidth, 0);
        if (drawSeconds) {
            mDigit1Second.onDraw(canvas, bottomWidth, bottomHeight, canvas.getWidth() - (2 * bottomWidth), topHeight);
            mDigit2Second.onDraw(canvas, bottomWidth, bottomHeight, canvas.getWidth() - bottomWidth, topHeight);
        }
    }
}
