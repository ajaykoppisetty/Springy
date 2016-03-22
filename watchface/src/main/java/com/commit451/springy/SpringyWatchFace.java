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

package com.commit451.springy;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;

import com.commit451.springy.common.SpringyClockRenderer;
import com.commit451.springy.common.config.ConfigHelper;
import com.commit451.springy.common.config.Themes;
import com.google.android.apps.muzei.api.MuzeiContract;

import java.util.TimeZone;

import static com.commit451.springy.common.MuzeiArtworkImageLoader.LoadedArtwork;
import static com.commit451.springy.common.config.Themes.MUZEI_THEME;
import static com.commit451.springy.common.config.Themes.Theme;

public class SpringyWatchFace extends CanvasWatchFaceService {

    private static final int UPDATE_THEME_ANIM_DURATION = 1000;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        Time mTime;
        private Paint mAmbientBackgroundPaint;
        private Paint mBackgroundPaint;

        private boolean mMute;
        private Rect mCardBounds = new Rect();
        private ValueAnimator mBottomBoundAnimator = new ValueAnimator();
        private ValueAnimator mSecondsAlphaAnimator = new ValueAnimator();
        private int mWidth = 0;
        private int mHeight = 0;
        private int mDisplayMetricsWidth = 0;
        private int mDisplayMetricsHeight = 0;

        private Handler mMainThreadHandler = new Handler();

        // For Muzei
        private WatchfaceArtworkImageLoader mMuzeiLoader;
        private Paint mMuzeiArtworkPaint;
        private LoadedArtwork mMuzeiLoadedArtwork;

        // FORM clock renderer specific stuff
        private SpringyClockRenderer mSpringyClockRenderer;
        private long mUpdateThemeStartAnimTimeMillis;

        private boolean mDrawMuzeiBitmap;
        private Theme mCurrentTheme;
        private Theme mAnimateFromTheme;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mMute = getInterruptionFilter() == WatchFaceService.INTERRUPTION_FILTER_NONE;
            handleConfigUpdated();

            initClockRenderers();

            registerSystemSettingsListener();
            registerSharedPrefsListener();
            registerTimeZoneReceiver();

            initMuzei();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterSystemSettingsListener();
            unregisterSharedPrefsListener();
            unregisterTimeZoneReceiver();
            destroyMuzei();
        }

        private void initClockRenderers() {
            // Init paints
            mAmbientBackgroundPaint = new Paint();
            mAmbientBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint = new Paint();

            mSpringyClockRenderer = new SpringyClockRenderer();
        }

        private void handleConfigUpdated() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SpringyWatchFace.this);
            String themeId = sp.getString(ConfigHelper.KEY_THEME, Themes.DEFAULT_THEME.id);
            Theme newCurrentTheme = Themes.getThemeById(themeId);
            if (newCurrentTheme != mCurrentTheme) {
                mAnimateFromTheme = mCurrentTheme;
                mCurrentTheme = newCurrentTheme;
                mUpdateThemeStartAnimTimeMillis = System.currentTimeMillis() + 200;
            }

            updateWatchFaceStyle();
            postInvalidate();
        }

        private void updateWatchFaceStyle() {
            setWatchFaceStyle(new WatchFaceStyle.Builder(SpringyWatchFace.this)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                    .setStatusBarGravity(Gravity.TOP | Gravity.CENTER)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER)
                    .build());
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;

            DisplayMetrics dm = getResources().getDisplayMetrics();
            mDisplayMetricsWidth = dm.widthPixels;
            mDisplayMetricsHeight = dm.heightPixels;

            mBottomBoundAnimator.cancel();
            mBottomBoundAnimator.setFloatValues(mHeight, mHeight);
            mBottomBoundAnimator.setInterpolator(new DecelerateInterpolator(3));
            mBottomBoundAnimator.setDuration(0);
            mBottomBoundAnimator.start();

            mSecondsAlphaAnimator.cancel();
            mSecondsAlphaAnimator.setFloatValues(1f, 1f);
            mSecondsAlphaAnimator.setDuration(0);
            mSecondsAlphaAnimator.start();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            updateWatchFaceStyle();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                postInvalidate();
                registerTimeZoneReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterTimeZoneReceiver();
            }
        }

        private void initMuzei() {
            mMuzeiArtworkPaint = new Paint();
            mMuzeiArtworkPaint.setAlpha(102);
            mMuzeiLoader = new WatchfaceArtworkImageLoader(SpringyWatchFace.this);
            mMuzeiLoader.registerListener(0, mMuzeiLoadCompleteListener);
            mMuzeiLoader.startLoading();

            // Watch for artwork changes
            IntentFilter artworkChangedIntent = new IntentFilter();
            artworkChangedIntent.addAction(MuzeiContract.Artwork.ACTION_ARTWORK_CHANGED);
            registerReceiver(mMuzeiArtworkChangedReceiver, artworkChangedIntent);
        }

        private BroadcastReceiver mMuzeiArtworkChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mMuzeiLoader.startLoading();
            }
        };

        private void destroyMuzei() {
            unregisterReceiver(mMuzeiArtworkChangedReceiver);
            if (mMuzeiLoader != null) {
                mMuzeiLoader.unregisterListener(mMuzeiLoadCompleteListener);
                mMuzeiLoader.reset();
                mMuzeiLoader = null;
            }
        }

        private Loader.OnLoadCompleteListener<LoadedArtwork> mMuzeiLoadCompleteListener
                = new Loader.OnLoadCompleteListener<LoadedArtwork>() {
            public void onLoadComplete(Loader<LoadedArtwork> loader, LoadedArtwork data) {
                if (data != null) {
                    mMuzeiLoadedArtwork = data;
                } else {
                    mMuzeiLoadedArtwork = null;
                }
                postInvalidate();
            }
        };

        private void registerSystemSettingsListener() {
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.TIME_12_24),
                    false, mSystemSettingsObserver);
        }

        private void unregisterSystemSettingsListener() {
            getContentResolver().unregisterContentObserver(mSystemSettingsObserver);
        }

        private ContentObserver mSystemSettingsObserver = new ContentObserver(mMainThreadHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                initClockRenderers();
                postInvalidate();
            }
        };

        private void registerSharedPrefsListener() {
            PreferenceManager.getDefaultSharedPreferences(SpringyWatchFace.this)
                    .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }

        private void unregisterSharedPrefsListener() {
            PreferenceManager.getDefaultSharedPreferences(SpringyWatchFace.this)
                    .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }

        private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (ConfigHelper.isConfigPrefKey(key)) {
                    handleConfigUpdated();
                }
            }
        };

        private void registerTimeZoneReceiver() {
            IntentFilter timeZoneIntentFilter = new IntentFilter();
            timeZoneIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(mTimeZoneReceiver, timeZoneIntentFilter);
        }

        private void unregisterTimeZoneReceiver() {
            unregisterReceiver(mTimeZoneReceiver);
        }

        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    initClockRenderers();
                    postInvalidate();
                }
            }
        };

        @Override
        public void onPeekCardPositionUpdate(Rect bounds) {
            super.onPeekCardPositionUpdate(bounds);
            if (!bounds.equals(mCardBounds)) {
                mCardBounds.set(bounds);

                mBottomBoundAnimator.cancel();
                mBottomBoundAnimator.setFloatValues(
                        (Float) mBottomBoundAnimator.getAnimatedValue(),
                        mCardBounds.top > 0 ? mCardBounds.top : mHeight);
                mBottomBoundAnimator.setDuration(200);
                mBottomBoundAnimator.start();

                mSecondsAlphaAnimator.cancel();
                mSecondsAlphaAnimator.setFloatValues(
                        (Float) mSecondsAlphaAnimator.getAnimatedValue(),
                        mCardBounds.top > 0 ? 0f : 1f);
                mSecondsAlphaAnimator.setDuration(200);
                mSecondsAlphaAnimator.start();

                postInvalidate();
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            postInvalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            postInvalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;

            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                updateWatchFaceStyle();
                postInvalidate();
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            boolean ambientMode = isInAmbientMode();

            updatePaintsForTheme(mCurrentTheme);

            if (ambientMode) {
                canvas.drawRect(0, 0, mWidth, mHeight, mAmbientBackgroundPaint);
            } else if (mDrawMuzeiBitmap && mMuzeiLoadedArtwork != null) {
                canvas.drawRect(0, 0, mWidth, mHeight, mAmbientBackgroundPaint);
                canvas.drawBitmap(mMuzeiLoadedArtwork.bitmap,
                        (mDisplayMetricsWidth - mMuzeiLoadedArtwork.bitmap.getWidth()) / 2,
                        (mDisplayMetricsHeight - mMuzeiLoadedArtwork.bitmap.getHeight()) / 2,
                        mMuzeiArtworkPaint);
            } else {
                canvas.drawRect(0, 0, mWidth, mHeight, mBackgroundPaint);
            }

            mTime.setToNow();
            mSpringyClockRenderer.update(mTime);
            mSpringyClockRenderer.onDraw(canvas, !ambientMode);

//            if (isAnimatingThemeChange()) {
//                // show a reveal animation
//                updatePaintsForTheme(mAnimateFromTheme);
//                drawClock(canvas);
//
//                sc = canvas.save(Canvas.CLIP_SAVE_FLAG);
//
//                mUpdateThemeClipPath.reset();
//                float cx = mWidth / 2;
//                float bottom = (Float) mBottomBoundAnimator.getAnimatedValue();
//                float cy = bottom / 2;
//                float maxRadius = MathUtil.maxDistanceToCorner(0, 0, mWidth, mHeight, cx, cy);
//                float radius = interpolate(
//                        decelerate3(constrain(
//                                (currentTimeMillis - mUpdateThemeStartAnimTimeMillis)
//                                        * 1f / UPDATE_THEME_ANIM_DURATION,
//                                0 , 1)),
//                        0 , maxRadius);
//
//                mTempRectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
//                mUpdateThemeClipPath.addOval(mTempRectF, Path.Direction.CW);
//                canvas.clipPath(mUpdateThemeClipPath);
//            }
            if (!ambientMode) {
                postInvalidate();
            }
        }

        private boolean isAnimatingThemeChange() {
            return mAnimateFromTheme != null
                    && System.currentTimeMillis() - mUpdateThemeStartAnimTimeMillis
                    < UPDATE_THEME_ANIM_DURATION;
        }

        private void updatePaintsForTheme(Theme theme) {
            if (theme == MUZEI_THEME) {
                mBackgroundPaint.setColor(Color.BLACK);
                mDrawMuzeiBitmap = true;
            } else {
                mBackgroundPaint.setColor(getResources().getColor(theme.color));
                mDrawMuzeiBitmap = false;
            }
        }
    }
}
