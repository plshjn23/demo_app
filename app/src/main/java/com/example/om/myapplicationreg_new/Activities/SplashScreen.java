package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.om.myapplicationreg_new.R;

/**
 * Created by kshivang on 08/04/16.
 */
public class SplashScreen extends Activity {

    private static final int AUTO_HIDE_DELAY_MILLIS = 1000 * 2;

    private final Handler mHideHandler = new Handler();

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
            startActivity(new Intent(SplashScreen.this, Login.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        AutoStartActivity(AUTO_HIDE_DELAY_MILLIS);
    }

    private void AutoStartActivity(int autoHideDelayMillis) {
        mHideHandler.postDelayed(mHideRunnable, autoHideDelayMillis);
    }
}
