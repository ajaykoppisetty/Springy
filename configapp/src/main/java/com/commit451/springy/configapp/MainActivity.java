package com.commit451.springy.configapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;

import com.commit451.springy.shared.GoogleIO2016NumberView;
import com.commit451.springy.shared.Number;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.second0)
    GoogleIO2016NumberView mSecond0;
    @Bind(R.id.second1)
    GoogleIO2016NumberView mSecond1;
    @Bind(R.id.springy)
    SpringyNumberView mSpringyNumberView;

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

        mTime = new Time();
        mTimerHandler = new Handler();

        mTimeChecker.run();
    }

    private void updateTime() {
        mTime.setToNow();

        mSecond0.animateTo(Number.VALUES[mTime.second / 10]);
        mSecond1.animateTo(Number.VALUES[mTime.second%10]);
        mSpringyNumberView.update(mTime);
    }
}
