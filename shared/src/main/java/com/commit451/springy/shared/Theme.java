package com.commit451.springy.shared;

import android.support.annotation.ColorInt;

/**
 * A class to hold a color configuration
 */
public class Theme {

    public String name;
    public int color;

    public Theme(String name, @ColorInt int color) {
        this.name = name;
        this.color = color;
    }

}
