/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commit451.springy.configapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.commit451.springy.common.MathUtil;
import com.commit451.springy.common.MuzeiArtworkImageLoader;
import com.commit451.springy.common.Theme;
import com.commit451.springy.common.Themes;
import com.commit451.springy.common.config.ConfigHelper;
import com.commit451.springy.common.config.UpdateConfigIntentService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CompanionWatchFaceConfigActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<MuzeiArtworkImageLoader.LoadedArtwork> {

    private static final int LOADER_MUZEI_ARTWORK = 1;

    private SharedPreferences mSharedPreferences;

    private ViewGroup mThemeItemContainer;
    private ArrayList<ThemeUiHolder> mThemeUiHolders = new ArrayList<>();
    private ThemeUiHolder mMuzeiThemeUiHolder;

    private ConfigComplicationsFragment mConfigComplicationsFragment;
    private ViewGroup mMainClockContainerView;
    private SpringyNumberView mMainClockView;
    private ViewGroup mAnimateClockContainerView;
    private SpringyNumberView mAnimateClockView;
    private Animator mCurrentRevealAnimator;
    private Theme mAnimatingTheme;

    private MuzeiArtworkImageLoader.LoadedArtwork mMuzeiLoadedArtwork;

    private long mInterval = TimeUnit.SECONDS.toMillis(1);
    private Handler mTimerHandler;
    Time mTime;

    private Runnable mTimeChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateTime();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mTimerHandler.postDelayed(mTimeChecker, mInterval);
            }
        }
    };

    private void updateTime() {
        mTime.setToNow();
        mMainClockView.update(mTime);
        mAnimateClockView.update(mTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.config_activity);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up general chrome
        ImageButton doneButton = (ImageButton) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ScrimInsetsFrameLayout scrimInsetsFrameLayout = (ScrimInsetsFrameLayout)
                findViewById(R.id.scrim_insets_frame_layout);
        scrimInsetsFrameLayout.setOnInsetsCallback(new ScrimInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                findViewById(R.id.chrome_container).setPadding(0, insets.top, 0, 0);
            }
        });

        // Set up theme list
        mMainClockContainerView = (ViewGroup) ((ViewGroup) findViewById(R.id.clock_container)).getChildAt(0);
        mMainClockView = (SpringyNumberView) mMainClockContainerView.findViewById(R.id.clock);

        mAnimateClockContainerView = (ViewGroup) ((ViewGroup) findViewById(R.id.clock_container)).getChildAt(1);
        mAnimateClockView = (SpringyNumberView) mAnimateClockContainerView.findViewById(R.id.clock);

        mAnimateClockContainerView.setVisibility(View.INVISIBLE);

        setupThemeList();
        String themeId = mSharedPreferences.getString(ConfigHelper.KEY_THEME, Themes.DEFAULT_THEME.id);
        updateUIToSelectedTheme(themeId, false);

        registerSharedPrefsListener();

        // Set up complications config fragment
        mConfigComplicationsFragment = (ConfigComplicationsFragment) getFragmentManager()
                .findFragmentById(R.id.config_complications_container);
        if (mConfigComplicationsFragment == null) {
            mConfigComplicationsFragment = new ConfigComplicationsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.config_complications_container, mConfigComplicationsFragment)
                    .commit();
        }

        // Set up tabs/pager
        final ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics()));

        SimplePagerHelper helper = new SimplePagerHelper(this, pager);
        helper.addPage(R.string.title_theme, R.id.config_theme_container);
        helper.addPage(R.string.title_complications, R.id.config_complications_container);

        TabLayout slidingTabLayout = (TabLayout) findViewById(R.id.tabs);
        slidingTabLayout.setupWithViewPager(pager);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                float translationX = -pager.getWidth();
                if (position == 0) {
                    translationX = -positionOffsetPixels;
                }
                mMainClockView.setTranslationX(translationX);
                mAnimateClockView.setTranslationX(translationX);
            }
        });

        mTime = new Time();
        mTimerHandler = new Handler();

        mTimeChecker.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSharedPrefsListener();
    }

    private void registerSharedPrefsListener() {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private void unregisterSharedPrefsListener() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (ConfigHelper.isConfigPrefKey(key)) {
                UpdateConfigIntentService.startConfigChangeService(
                        CompanionWatchFaceConfigActivity.this);

                if (mConfigComplicationsFragment != null) {
                    mConfigComplicationsFragment.update();
                }

                if (ConfigHelper.KEY_THEME.equals(key)) {
                    String themeId = mSharedPreferences
                            .getString(ConfigHelper.KEY_THEME, Themes.DEFAULT_THEME.id);
                    updateUIToSelectedTheme(themeId, true);
                }
            }
        }
    };

    private void setupThemeList() {
        mThemeUiHolders.clear();
        mThemeItemContainer = (ViewGroup) findViewById(R.id.theme_list);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (final Theme theme : Themes.THEMES) {
            ThemeUiHolder holder = new ThemeUiHolder();

            holder.theme = theme;
            holder.container = inflater.inflate(R.layout.config_theme_item, mThemeItemContainer, false);
            holder.button = (ImageButton) holder.container.findViewById(R.id.button);

            LayerDrawable bgDrawable = (LayerDrawable)
                    getResources().getDrawable(R.drawable.theme_item_bg).mutate();

            GradientDrawable gd = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.color);
            gd.setColor(theme.color);
            holder.button.setBackground(bgDrawable);

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateAndPersistTheme(theme);
                }
            });

            mThemeUiHolders.add(holder);
            mThemeItemContainer.addView(holder.container);
        }

        loadMuzei();
    }

    private void loadMuzei() {
        if (!MuzeiArtworkImageLoader.hasMuzeiArtwork(this)) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ThemeUiHolder holder = new ThemeUiHolder();

        final Theme theme = Themes.MUZEI_THEME;
        holder.theme = theme;
        holder.container = inflater.inflate(R.layout.config_theme_item, mThemeItemContainer, false);
        holder.button = (ImageButton) holder.container.findViewById(R.id.button);

        LayerDrawable bgDrawable = (LayerDrawable)
                getResources().getDrawable(R.drawable.theme_muzei_item_bg).mutate();
        holder.button.setBackground(bgDrawable);

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAndPersistTheme(theme);
            }
        });

        mThemeUiHolders.add(holder);
        mThemeItemContainer.addView(holder.container);
        mMuzeiThemeUiHolder = holder;

        // begin load using fragments
        getLoaderManager().initLoader(LOADER_MUZEI_ARTWORK, null, this);
    }

    private void updateAndPersistTheme(Theme theme) {
        mSharedPreferences.edit().putString(ConfigHelper.KEY_THEME, theme.id).apply();
    }

    @Override
    public Loader<MuzeiArtworkImageLoader.LoadedArtwork> onCreateLoader(int id, Bundle args) {
        return new MuzeiArtworkImageLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<MuzeiArtworkImageLoader.LoadedArtwork> loader, MuzeiArtworkImageLoader.LoadedArtwork data) {
        mMuzeiLoadedArtwork = data;
        if (mMuzeiThemeUiHolder.selected) {
            updatePreviewView(Themes.MUZEI_THEME, mMainClockContainerView);
        }
    }

    @Override
    public void onLoaderReset(Loader<MuzeiArtworkImageLoader.LoadedArtwork> loader) {
        mMuzeiLoadedArtwork = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateUIToSelectedTheme(final String themeId, final boolean animate) {
        for (final ThemeUiHolder holder : mThemeUiHolders) {
            boolean selected = holder.theme.id.equals(themeId);

            holder.button.setSelected(selected);

            if (holder.selected != selected && selected) {
                if (mCurrentRevealAnimator != null) {
                    mCurrentRevealAnimator.end();
                    updatePreviewView(mAnimatingTheme, mMainClockContainerView);
                }

                if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAnimatingTheme = holder.theme;
                    updatePreviewView(mAnimatingTheme, mAnimateClockContainerView);

                    Rect buttonRect = new Rect();
                    Rect clockContainerRect = new Rect();
                    holder.button.getGlobalVisibleRect(buttonRect);
                    mMainClockContainerView.getGlobalVisibleRect(clockContainerRect);

                    int cx = buttonRect.centerX() - clockContainerRect.left;
                    int cy = buttonRect.centerY() - clockContainerRect.top;
                    clockContainerRect.offsetTo(0, 0);

                    mCurrentRevealAnimator = ViewAnimationUtils.createCircularReveal(
                            mAnimateClockContainerView, cx, cy, 0,
                            MathUtil.maxDistanceToCorner(clockContainerRect, cx, cy));
                    mAnimateClockContainerView.setVisibility(View.VISIBLE);
                    mCurrentRevealAnimator.setDuration(300);
                    mCurrentRevealAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mCurrentRevealAnimator == animation) {
                                mAnimateClockContainerView.setVisibility(View.INVISIBLE);
                                updatePreviewView(holder.theme, mMainClockContainerView);
                            }
                        }
                    });

                    mAnimateClockView.postInvalidateOnAnimation();
                    mCurrentRevealAnimator.start();
                } else {
                    updatePreviewView(holder.theme, mMainClockContainerView);
                }
            }

            holder.selected = selected;
        }
    }

    private void updatePreviewView(Theme theme, ViewGroup clockContainerView) {
        if (theme == Themes.MUZEI_THEME) {
            if (mMuzeiLoadedArtwork != null) {
                ((ImageView) clockContainerView.findViewById(R.id.background_image))
                        .setImageBitmap(mMuzeiLoadedArtwork.bitmap);
            }
            clockContainerView.setBackgroundColor(Color.BLACK);
        } else {
            ((ImageView) clockContainerView.findViewById(R.id.background_image))
                    .setImageDrawable(null);
            clockContainerView.setBackgroundColor(theme.color);
        }
    }

    private static class ThemeUiHolder {
        Theme theme;
        View container;
        ImageButton button;
        boolean selected;
    }
}
