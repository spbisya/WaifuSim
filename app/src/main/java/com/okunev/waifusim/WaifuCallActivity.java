package com.okunev.waifusim;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by gwa on 4/10/16.
 */
public class WaifuCallActivity extends AppCompatActivity {
    Vibrator vibrator;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_card);
        getSupportActionBar().hide();

    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    long pattern[] = {0, 300, 200, 300, 500};
//start vibration with repeated count, use -1 if you don't want to repeat the vibration
    vibrator.vibrate(pattern, 0);
    player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_RING);
        try {
            player.setDataSource(this, Settings.System.DEFAULT_RINGTONE_URI);
            player.prepare();
        } catch (IOException e) {
            try {
                player.setDataSource(this, Uri.parse("android.resource://com.okunev.waifusim/" + R.raw.nyasha));
                player.prepare();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        player.start();


    final ImageButton accept = (ImageButton) findViewById(R.id.accept);
    final ImageButton decline = (ImageButton) findViewById(R.id.decline);
    //  decline.setVisibility(View.INVISIBLE);
    assert accept != null;
    accept.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent me) {
            v.setScaleX(1.2f);
            v.setScaleY(1.2f);
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                float oldXvalue = me.getX();
                float oldYvalue = me.getY();
                Log.i("Dre", "Action Down " + oldXvalue + "," + oldYvalue);
            } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                float screenSize = (float) (getWindowManager().getDefaultDisplay().getWidth());
                float raw = me.getRawX();
                float width = v.getWidth();
                float x = me.getRawX() - (v.getWidth() / 2);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                if ((int) x > 0) {
                    if (raw < screenSize - v.getWidth() / 2) {
                        if (raw < screenSize - v.getWidth()) {
                            float fullSize = screenSize - v.getWidth() * 2;
                            float alpha = (screenSize - v.getWidth() * 1.5f - raw) / (fullSize / 100) * 0.01f;
                            if (alpha >= 0.0 & alpha <= 1.0) decline.setAlpha(alpha);
                            Log.i("Dre", "Left Margin = " + x +
                                    ", Width = " + width + " Raw = " + raw + " Screen is = " +
                                    screenSize + " Alpha = " + alpha);
                            params.setMargins((int) x, 0, 0, 0);
                            v.setLayoutParams(params);
                        } else {

                            params.setMargins((int) x, 0, 0, 0);
                            Log.i("Dre", "Left Margin = " + x +
                                    ", Width = " + width + " Raw = " + raw + " Screen is = " +
                                    screenSize);
                            v.setLayoutParams(params);
                        }
                    }

                }
            } else if (me.getAction() == MotionEvent.ACTION_UP) {
                v.setScaleY(1.0f);
                v.setScaleX(1.0f);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                decline.setAlpha(1.0f);
                params.setMargins(0, 0, 0, 0);
                v.setLayoutParams(params);

                if (me.getRawX() - (v.getWidth() / 2) > v.getWidth()) {
                    player.stop();
                    vibrator.cancel();
                    finish();
                    Intent intent = new Intent(WaifuCallActivity.this, WaifuAnswerActivity.class);
                    startActivity(intent);
                }

            }
            return true;
        }
    });

    assert decline != null;
    decline.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent me) {
            v.setScaleX(1.2f);
            v.setScaleY(1.2f);
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                float oldXvalue = me.getX();
                float oldYvalue = me.getY();
                Log.i("Dre", "Action Down " + oldXvalue + ", " + oldYvalue);
            } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                float screenSize = (float) (getWindowManager().getDefaultDisplay().getWidth());
                float raw = me.getRawX();
                float width = v.getWidth();
                float x = screenSize - me.getRawX() - (v.getWidth() / 2);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                if ((int) x > 0) {
                    if (raw > width / 2) {
                        if (raw > width) {
                            float fullSize = screenSize - v.getWidth() * 2;
                            float alpha = Math.abs((v.getWidth() * 1.5f - raw) / (fullSize / 100) * 0.01f);
                            if (alpha >= 0.0 & alpha <= 1.0) {
                                accept.setAlpha(alpha);
                                if (alpha <= 0.01f) accept.setVisibility(View.INVISIBLE);
                            }
                            Log.i("Dre", "Left Margin = " + x +
                                    ", Width = " + width + " Raw = " + raw + " Screen is = " +
                                    screenSize + " Alpha = " + alpha);
                            params.setMargins(0, 0, (int) x, 0);
                            v.setLayoutParams(params);
                        } else {
                            params.setMargins(0, 0, (int) x, 0);
                            Log.i("Dre", "Left Margin = " + x +
                                    ", Width = " + width + " Raw = " + raw + " Screen is = " +
                                    screenSize);
                            v.setLayoutParams(params);
                        }

                    } else {
                        //Toast.makeText(WaifuCallActivity.this, "ACCEPTED", Toast.LENGTH_LONG).show();

                        params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        accept.setAlpha(1.0f);
                        accept.setVisibility(View.VISIBLE);
                        v.setScaleY(1.0f);
                        v.setScaleX(1.0f);
                        params.setMargins(0, 0, 0, 0);
                        v.setLayoutParams(params);
                        player.stop();
                        vibrator.cancel();
                        finish();
                        Notification notification = new Notification.Builder(WaifuCallActivity.this)
                                .setContentTitle("Your Waifu")
                                .setContentText("Я на тебя обижена!!!")
                                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                                .build();

                        // Sets an ID for the notification
                        int mNotificationId = 999;
// Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) WaifuCallActivity.this.getSystemService(WaifuCallActivity.this.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId++, notification);
                    }
                }
            } else if (me.getAction() == MotionEvent.ACTION_UP) {
                v.setScaleY(1.0f);
                v.setScaleX(1.0f);
                accept.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                accept.setAlpha(1.0f);
                params.setMargins(0, 0, 0, 0);
                v.setLayoutParams(params);
                if ((float) (getWindowManager().getDefaultDisplay().getWidth()) - me.getRawX() - (v.getWidth() / 2) > v.getWidth()) {
                    player.stop();
                    vibrator.cancel();
                    finish();
                    Notification notification = new Notification.Builder(WaifuCallActivity.this)
                            .setContentTitle("Your Waifu")
                            .setContentText("Я на тебя обижена!!!")
                            .setSmallIcon(R.drawable.ic_stat_1444249298867)
                            .build();

                    // Sets an ID for the notification
                    int mNotificationId = 999;
// Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) WaifuCallActivity.this.getSystemService(WaifuCallActivity.this.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                    mNotifyMgr.notify(mNotificationId++, notification);
                }

            }
            return true;
        }
    });

    }

    public void rejectMessage(View v) {
        player.stop();
        vibrator.cancel();
        finish();
        Notification notification = new Notification.Builder(WaifuCallActivity.this)
                .setContentTitle("Your Waifu")
                .setContentText("Я на тебя обижена!!!")
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();

        // Sets an ID for the notification
        int mNotificationId = 999;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) WaifuCallActivity.this.getSystemService(WaifuCallActivity.this.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId++, notification);
    }

}
