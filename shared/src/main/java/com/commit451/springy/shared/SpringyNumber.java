package com.commit451.springy.shared;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.util.Pair;
import android.view.InflateException;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Springy number drawing
 * @author Jawn.
 */
public class SpringyNumber {

    private Number mNumber;

    private PathParser.PathDataNode[] mNodes;

    private PathParser.PathDataNode[] nodesFrom;
    private PathParser.PathDataNode[] nodesTo;
    private TypeEvaluator evaluator;
    private ValueAnimator animator;

    public SpringyNumber(Number number) {
        mNumber = number;
        reset();
    }

    public void animateTo(final Number number) {
        if (number == mNumber) {
            return;
        }

        Pair<String, String> pathValues = Number.alignNumbers(new Pair<>(mNumber, number));
        nodesFrom = PathParser.createNodesFromPathData(pathValues.first);
        nodesTo = PathParser.createNodesFromPathData(pathValues.second);

        PropertyValuesHolder valuesHolder;
        evaluator = new AnimationUtil.PathDataEvaluator(PathParser.deepCopyNodes(nodesFrom));
        if (!PathParser.canMorph(nodesFrom, nodesTo)) {
            throw new InflateException(" Can't morph from " + pathValues.first + " to " +
                    pathValues.second);
        }

        valuesHolder = PropertyValuesHolder.ofObject("pathData", evaluator, nodesFrom, nodesTo);
        animator = ValueAnimator.ofPropertyValuesHolder(valuesHolder);
        if (animator != null) {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mNodes = (PathParser.PathDataNode[]) animation.getAnimatedValue();
                    //invalidate();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    mNumber = number;
                    reset();
                }
            });
            animator.start();
        }
    }

    public void reset() {
        mNodes = Number.getNodes(mNumber);
    }

    public void onDraw(Canvas canvas, int width, int height, int dx, int dy) {
        NumberDrawer.draw(canvas, width, height, dx, dy, mNodes);
    }
}
