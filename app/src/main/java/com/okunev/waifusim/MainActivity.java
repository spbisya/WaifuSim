package com.okunev.waifusim;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements Callback<WaifuMessages> {
    TextView textView, log;
    Response<WaifuMessages> response;
    int mNotificationId = 001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.tv1);
        textView.setText("Wait");
log = (TextView)findViewById(R.id.textView);
        log.setText("Log started!\n");
        getResp();
    }

    public void getResp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-52-38-11-210.us-west-2.compute.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // prepare call in Retrofit 2.0
        WaifuApi waifuApi = retrofit.create(WaifuApi.class);

        Call<WaifuMessages> call = waifuApi.loadMessages();
        //asynchronous call
        call.enqueue(this);
    }

    @Override
    public void onResponse(Response<WaifuMessages> response, Retrofit retrofit) {
        this.response = response;
        textView.setText("Click me!");
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
              log.append("Random is "+sise+" and text is "+MainActivity.this.response.body().items.get(sise).toString()+"\n");
// Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId++, notification);
            }
        });
    }

    @Override
    public void onFailure(Throwable t) {

    }
}
