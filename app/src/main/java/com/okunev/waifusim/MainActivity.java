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

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements Callback<WaifuMessages> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView)findViewById(R.id.tv1);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
getResp();
            }
        });

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Your Waifu")
                .setContentText("Сходи пропукайся, на улице солнышко!")
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();

        String jsonData = loadJSONFromAsset();
        try {
            JSONObject games = new JSONObject(jsonData);
            JSONArray items = games.getJSONArray("items");
            Toast.makeText(this, items.getJSONObject(0).getString("message"), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notification);

    }

    public void getResp(){
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

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("lotteries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onResponse(Response<WaifuMessages> response, Retrofit retrofit) {
        Toast.makeText(this, response.body().items.get(1).toString(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(Throwable t) {

    }
}
