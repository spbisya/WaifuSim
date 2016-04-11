package com.okunev.waifusim;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    TextView textView, log, tokenView;
    Response<WaifuMessages> response;
    int mNotificationId = 999;
    SharedPreferences sPref;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        sPref = PreferenceManager.getDefaultSharedPreferences(this);;
        textView = (TextView) findViewById(R.id.tv1);
        tokenView = (TextView) findViewById(R.id.token);
        textView.setText("Wait");
        log = (TextView) findViewById(R.id.textView);
        log.setText("Log started!\n");
        Button makeCall = (Button) findViewById(R.id.call);
        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WaifuCallActivity.class);
                startActivity(intent);
            }
        });

        token = sPref.getString("token", "-1");
     //   tokenView.setText("token = " + token);
        if (token.equals("-1")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Seems you have no generated tokens... Set one?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            intent.putExtra("new", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog

                        }
                    });
            AlertDialog d = builder.create();
            d.setTitle("No tokens");
            d.show();


        } else
            getMessages(token);
    }

    public void getMessages(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-52-38-11-210.us-west-2.compute.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WaifuApi waifuApi = retrofit.create(WaifuApi.class);
        Call<WaifuMessages> call = waifuApi.loadMessages(token);
        call.enqueue(new Callback<WaifuMessages>() {
            @Override
            public void onResponse(Call<WaifuMessages> call, Response<WaifuMessages> response) {
                MainActivity.this.response = response;
                Toast.makeText(MainActivity.this, "" + MainActivity.this.response.body().items.size(), Toast.LENGTH_LONG).show();
                textView.setText("Click me!");
              //  tokenView.setText("token = " + MainActivity.this.token);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Random random = new Random();
                        int sise = random.nextInt(MainActivity.this.response.body().items.size());

                        //  Toast.makeText(MainActivity.this, MainActivity.this.response.body().items.get(sise).toString(), Toast.LENGTH_LONG).show();

                        Notification notification = new Notification.Builder(getApplicationContext())
                                .setContentTitle("Your Waifu")
                                .setContentText(MainActivity.this.response.body().items.get(sise).toString())
                                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                                .build();

                        // Sets an ID for the notification
                        log.append("Random is " + sise + " and text is " + MainActivity.this.response.body().items.get(sise).toString() + "\n");
// Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, notification);
                    }
                });
            }

            @Override
            public void onFailure(Call<WaifuMessages> call, Throwable t) {

            }
        });

    }


    public void notReady(View v) {
        Toast.makeText(MainActivity.this, "This feature is not ready yet!", Toast.LENGTH_LONG).show();
    }

    public void goSettings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}
