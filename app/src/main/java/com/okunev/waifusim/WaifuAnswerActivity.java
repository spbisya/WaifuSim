package com.okunev.waifusim;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by gwa on 4/10/16.
 */
public class WaifuAnswerActivity extends AppCompatActivity {
    int seconds = 0;
    int minutes = 0;
    MediaPlayer player;
    AudioManager audiomgr;

    @Bind(R.id.time)
    TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.answer_card);
        ButterKnife.bind(this);
        getSupportActionBar().hide();

        player = new MediaPlayer();
        try {
            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setDataSource(this, Uri.parse("android.resource://com.okunev.waifusim/" + R.raw.nyasha));
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finish();
                }
            });
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setSpeakerphoneOn(false);
            am.setMode(AudioManager.MODE_IN_CALL);

        } catch (IOException e) {
            //   Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
        }
        //
        //  player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        final Timer timer = new Timer();
        final TimerTask task = new TimerTask() {
            public void run() {
                if (seconds < 60)
                    seconds++;
                else {
                    seconds = 0;
                    minutes++;
                }
                setText();
            }
        };
        timer.schedule(task, 0, 1000);
    }

    @OnClick(R.id.decline)
    void onDeclineClick(View v) {
        player.release();
        ObjectAnimator animationType2 = ObjectAnimator.ofFloat(v, "rotation", 0f,
                (6.25f * 180) / ((float) Math.PI));
        animationType2.setDuration(700);
        animationType2.start();
        CountDownTimer countDownTimer = new CountDownTimer(700, 700) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                finish();
            }
        }.start();
    }

    private void setText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeTextView.setText("" + ((minutes < 10) ? "0" + minutes : "" + minutes) + ":" + ((seconds < 10) ? "0" + seconds : "" + seconds));
            }
        });
    }


}
