package com.okunev.waifusim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by gwa on 4/28/16.
 */
public class SplashScreenActivity extends Activity {

    private static int SPLASH_SCREEN_TIMEOUT = 1500;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        ImageView imageView = (ImageView)findViewById(R.id.imageView2);
        ObjectAnimator animationType2 = ObjectAnimator.ofFloat(imageView, "rotation", 0f,
                3.14f*2 * 180 / ((float) Math.PI));
        animationType2.setDuration((long) (1500));
        animationType2.setRepeatCount(0);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(imageView, "scaleX", 0);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(imageView, "scaleY", 0);
        scaleDownX.setDuration((long) (1500));
        scaleDownX.setRepeatCount(0);
        scaleDownY.setDuration((long) (1500));
        scaleDownY.setRepeatCount(0);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        scaleDown.play(scaleDownX).with(scaleDownY).with(animationType2);

        scaleDown.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
