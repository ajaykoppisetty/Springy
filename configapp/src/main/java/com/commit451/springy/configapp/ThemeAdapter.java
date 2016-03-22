package com.commit451.springy.configapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.springy.shared.Theme;
import com.commit451.springy.shared.Themes;

/**
 * Adapter that holds themes
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeViewHolder> {

    public interface Listener {
        void onThemeClicked(Theme theme);
    }

    private Listener mListener;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Theme theme = (Theme) v.getTag();
            mListener.onThemeClicked(theme);
        }
    };

    public ThemeAdapter(Listener listener) {
        mListener = listener;
    }

    @Override
    public ThemeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThemeViewHolder holder = ThemeViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ThemeViewHolder holder, int position) {
        Theme theme = Themes.THEMES[position];
        holder.bind(theme);
        holder.itemView.setTag(theme);
    }

    @Override
    public int getItemCount() {
        return Themes.THEMES.length;
    }
}
