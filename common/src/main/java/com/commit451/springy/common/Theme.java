package com.commit451.springy.common;

import android.support.annotation.ColorInt;

/**
 * A class to hold a color configuration
 */
public class Theme {

    public String id;
    public int color;

    public Theme(String id, @ColorInt int color) {
        this.id = id;
        this.color = color;
    }

}
