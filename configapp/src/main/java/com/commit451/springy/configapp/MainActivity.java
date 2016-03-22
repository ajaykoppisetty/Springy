package com.commit451.springy.configapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.View;

import com.commit451.adapterlayout.AdapterLinearLayout;
import com.commit451.springy.shared.Theme;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.springy)
    SpringyNumberView mSpringyNumberView;
    @Bind(R.id.list)
    AdapterLinearLayout mList;
    @Bind(R.id.background)
    View mBackground;

    ThemeAdapter mThemeAdapter;

    Time mTime;

    private long mInterval = TimeUnit.SECONDS.toMillis(1);
    private Handler mTimerHandler;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mToolbar.setNavigationIcon(R.drawable.ic_done_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mThemeAdapter = new ThemeAdapter(new ThemeAdapter.Listener() {
            @Override
            public void onThemeClicked(Theme theme) {
                mBackground.setBackgroundColor(theme.color);
            }
        });
        mList.setAdapter(mThemeAdapter);

        mTime = new Time();
        mTimerHandler = new Handler();

        mTimeChecker.run();
    }

    private void updateTime() {
        mTime.setToNow();
        mSpringyNumberView.update(mTime);
    }
}
