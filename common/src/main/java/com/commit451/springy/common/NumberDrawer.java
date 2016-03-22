/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commit451.springy.common;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

/**
 * The class that draws the number
 */
public final class NumberDrawer {
    private static final Paint paint = new Paint();
    private static final Path path = new Path();
    private static final Matrix matrix = new Matrix();
    private static float od;
    @ColorInt
    private static int strokeColor = Color.parseColor("#ffffff");

    public static void draw(Canvas canvas, int width, int height, int dx, int dy, PathParser.PathDataNode[] nodes) {
        //I think these are the defaults? not well documented...
        float ow = 132f;
        float oh = 132f;

        od = (width / ow < height / oh) ? width / ow : height / oh;

        r();
        canvas.save();
        canvas.translate((width - od * ow) / 2f + dx, (height - od * oh) / 2f + dy);

        matrix.reset();
        matrix.setScale(od, od);

        canvas.save();
        paint.setColor(Color.argb(0, 0, 0, 0));
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeMiter(4.0f * od);
        canvas.scale(1.0f, 1.0f);
        canvas.save();
        paint.setColor(strokeColor);
        paint.setStrokeWidth(5.0f * od);
        paint.setStrokeJoin(Paint.Join.ROUND);
        path.reset();

        for (PathParser.PathDataNode node : nodes) {
            if (node.type == 'M') {
                path.moveTo(node.params[0], node.params[1]);
            } else if (node.type == 'L') {
                path.lineTo(node.params[0], node.params[1]);
            } else if (node.type == 'C') {
                path.cubicTo(node.params[0], node.params[1], node.params[2], node.params[3], node.params[4],
                        node.params[5]);
            }
        }

        path.transform(matrix);
        canvas.drawPath(path, paint);
        canvas.restore();
        r(3, 2, 0, 1);
        paint.setColor(Color.parseColor("#e35444"));
        paint.setStrokeWidth(5.0f * od);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.restore();
        r();

        canvas.restore();
    }

    public static Drawable getDrawable(Number number, int size) {
        return new NumberDrawable(number, size);
    }

    public static Drawable getTintedDrawable(Number number, int size, int color) {
        return new NumberDrawable(number, size, color);
    }

    public static class NumberDrawable extends Drawable {
        private int size = 0;
        private ColorFilter colorFilter = null;

        private Number number;

        public NumberDrawable(Number number, int size) {
            this.number = number;
            this.size = size;
            setBounds(0, 0, size, size);
            invalidateSelf();
        }

        public NumberDrawable(Number number, int size, int color) {
            this(number, size);
            colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        @Override
        public int getIntrinsicHeight() {
            return size;
        }

        @Override
        public int getIntrinsicWidth() {
            return size;
        }

        @Override
        public void draw(Canvas c) {
            Rect b = getBounds();
            NumberDrawer.draw(c, b.width(), b.height(), b.left, b.top, Number.getNodes(number));
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(ColorFilter c) {
            colorFilter = c;
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    private static void r(Integer... o) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        for (Integer i : o) {
            switch (i) {
                case 0:
                    paint.setStrokeJoin(Paint.Join.MITER);
                    break;
                case 1:
                    paint.setStrokeMiter(4.0f * od);
                    break;
                case 2:
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    break;
                case 3:
                    paint.setColor(Color.argb(0, 0, 0, 0));
                    break;
            }
        }
    }
}
