package com.commit451.springy.configapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.springy.shared.Theme;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the theme
 */
public class ThemeViewHolder extends RecyclerView.ViewHolder{

    public static ThemeViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @Bind(R.id.color)
    public View color;

    private ThemeViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Theme theme) {
        color.setBackgroundColor(theme.color);
    }
}
