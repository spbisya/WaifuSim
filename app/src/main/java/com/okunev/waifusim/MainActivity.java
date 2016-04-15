package com.okunev.waifusim;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.okunev.waifusim.network.WaifuApi;

import java.util.Calendar;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.tv1)
    TextView textView;
    @Bind(R.id.textView)
    TextView log;
    @Bind(R.id.token)
    TextView tokenView;

    Response<WaifuMessages> response;
    int mNotificationId = 999;
    SharedPreferences sPref;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportActionBar().hide();
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        textView.setText("Wait");
        log.setText("Log started!\n");

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

    @OnClick(R.id.call)
    void onCallClick() {
        Intent intent = new Intent(MainActivity.this, WaifuCallActivity.class);
        startActivity(intent);
    }

    public void getMessages(String token) {
        Call<WaifuMessages> call = WaifuApi.api().loadMessages(token);
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

    public void goSample(View v) {
        Intent intent = new Intent(this, SampleActivity.class);
        startActivity(intent);
    }

    TimePickerDialog tpd = null;
    Calendar cal;
    Calendar calendar;
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            tpd = new TimePickerDialog(this, myCallBack, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            return tpd;
        }
        return super.onCreateDialog(id);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            int addHour, addMin;
            int curHour = cal.get(Calendar.HOUR_OF_DAY), curMinute = cal.get(Calendar.MINUTE);
          //  Toast.makeText(MainActivity.this,""+hourOfDay+":"+minute,Toast.LENGTH_LONG).show();

            if (hourOfDay < curHour & minute > curMinute) {
                addHour = 24 - (curHour - hourOfDay);
                addMin = minute - curMinute;
            } else if (hourOfDay <= curHour & minute < curMinute) {
                addHour = 23 - (curHour - hourOfDay);
                addMin = 60 - (curMinute - minute);
            } else {
                addHour = hourOfDay - curHour;
                addMin = minute - curMinute;
            }
            calendar.add(Calendar.HOUR_OF_DAY, addHour);
            calendar.add(Calendar.MINUTE, addMin);
            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(MainActivity.this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    pendingIntent);
            Toast.makeText(MainActivity.this, "Alarm set to " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
                    " in " + addHour + " hours & " + addMin + " minutes.", Toast.LENGTH_LONG).show();
            tpd=null;
            // input.setText("" + ((hourOfDay<10)?"0"+hourOfDay:hourOfDay) + ":" + ((minute<10)?"0"+minute:minute));

            //22:11
            //21:06
            //22h 54 min
        }
    };

    public void setAlarm(View v) {
        calendar = Calendar.getInstance();
        cal = Calendar.getInstance();
        showDialog(1);
    }


}
