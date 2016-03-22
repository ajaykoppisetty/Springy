package com.commit451.springy.common.config;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import java.util.HashMap;
import java.util.Map;

/**
 * All the themes
 */
public class Themes {

    private Themes() {
    }

    private static Map<String, Theme> THEMES_BY_ID = new HashMap<>();

    public static final Theme[] THEMES = new Theme[] {
            new Theme("blue", Color.parseColor("#2A36B1")),
            new Theme("teal", Color.parseColor("#00695C")),
            new Theme("red", Color.parseColor("#A52714")),
            new Theme("yellow", Color.parseColor("#F09300")),
            new Theme("gray", Color.parseColor("#424242"))
    };

    public static Theme MUZEI_THEME = new Theme("muzei", 0);

    public static final Theme DEFAULT_THEME = THEMES[0];

    static {
        for (Theme theme : THEMES) {
            THEMES_BY_ID.put(theme.id, theme);
        }
    }

    public static Theme getThemeById(String id) {
        if ("muzei".equals(id)) {
            return MUZEI_THEME;
        }

        return THEMES_BY_ID.get(id);
    }

    /**
     * A class to hold a color configuration
     */
    public static class Theme {

        public String id;
        public int color;

        public Theme(String id, @ColorInt int color) {
            this.id = id;
            this.color = color;
        }

    }
}
