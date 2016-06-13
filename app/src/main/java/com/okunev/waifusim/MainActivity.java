package com.okunev.waifusim;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.okunev.waifusim.geofence.GeofenceSetupActivity;
import com.okunev.waifusim.network.WaifuApi;
import com.okunev.waifusim.utils.FileManager;
import com.okunev.waifusim.utils.MessageAdapter;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.live2d.Live2D;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.tv1)
    TextView textView;
    @Bind(R.id.checkBox)
    CheckBox checkBox;
    @Bind(R.id.floatingActionButton)
    FloatingActionButton fab;
    @Bind(R.id.listview)
    ListView lv;
    @Bind(R.id.date)
    TextView date;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    Response<WaifuMessages> response;
    int mNotificationId = 999;
    SharedPreferences sPref;
    String token;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    ArrayList<MessageItem> items = new ArrayList<>();

    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show this activity when device is locked
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);
        //createDrawer(savedInstanceState);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        textView.setText("Wait");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPlayServices()) {
                    Log.d("DRE", "InstanceID token: " + FirebaseInstanceId.getInstance().getToken());
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WaifuCallActivity.class);
                startActivity(intent);
            }
        });
        items = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        date.setText(c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR));
        items.add(new MessageItem("Ой, всё!", c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE)));
        items.add(new MessageItem("Я обиделась!", c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE)));
        items.add(new MessageItem("Ты не зарабатываешь 50000 рублей!", c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE)));

        lv.setAdapter(new MessageAdapter(this, items));


        checkBox.setChecked(sPref.getBoolean("sound", false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) sPref.edit().putBoolean("sound", true).commit();
                else sPref.edit().putBoolean("sound", false).commit();
            }
        });
        token = sPref.getString("token", "-1");
        //   tokenView.setText("token = " + token);
        if (token.equals("-1")) {
            try {
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
            } catch (Exception l) {
                Toast.makeText(this, "Покажи это сообщение Изе Питерскому: \n" + l.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else
            getMessages(token);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean("sentTokenToServer", false);
                if (sentToken) {
                    Toast.makeText(MainActivity.this, "send", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();
        createDrawer(savedInstanceState);
    }

//    protected void setStatusBarTranslucent(boolean makeTranslucent) {
//        if (makeTranslucent) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        } else {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
//    }



    void createDrawer(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (Exception l){

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

//setStatusBarTranslucent(true);
//        // create our manager instance after the content view is set
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//// enable status bar tint
//        tintManager.setStatusBarTintEnabled(true);
//// enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);
//// set the transparent color of the status bar, 20% darker
//        tintManager.setTintColor(Color.parseColor("#20000000"));
        final IProfile profile = new ProfileDrawerItem()
                .withName("Mike Penz")
                .withEmail("mikepenz@gmail.com")
                .withIcon(getResources().getDrawable(R.drawable.yui))
                .withTextColor(Color.parseColor("#795548"))
                .withSelectedTextColor(Color.parseColor("#795548"));
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withTextColor(Color.parseColor("#795548"))
                .withHeaderBackground(R.drawable.background)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER)
                .addProfiles(
                        profile
                )
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult)
                .withDisplayBelowStatusBar(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Главная").withIcon(FontAwesome.Icon.faw_user).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName("Мои вайфу").withIcon(FontAwesome.Icon.faw_bars).withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName("Место жительства").withIcon(FontAwesome.Icon.faw_map_marker).withIdentifier(3).withSelectable(false),
                        new PrimaryDrawerItem().withName("Мои будильники").withIcon(FontAwesome.Icon.faw_clock_o).withIdentifier(4).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Настройки").withIcon(FontAwesome.Icon.faw_cog).withIdentifier(5).withSelectable(false),
                        new PrimaryDrawerItem().withName("О программе").withIcon(FontAwesome.Icon.faw_info_circle).withIdentifier(6).withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, ModelActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(MainActivity.this, GeofenceSetupActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                setAlarm(null);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                Toast.makeText(MainActivity.this, "This feature is not ready yet!", Toast.LENGTH_LONG).show();
                            }

                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }
                        return false;

                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter("registrationComplete"));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    public void getMessages(String token) {
        Call<WaifuMessages> call = WaifuApi.api().loadMessages(token);
        call.enqueue(new Callback<WaifuMessages>() {
            @Override
            public void onResponse(Call<WaifuMessages> call, Response<WaifuMessages> response) {
                MainActivity.this.response = response;
              //  Toast.makeText(MainActivity.this, "" + MainActivity.this.response.body().items.size(), Toast.LENGTH_LONG).show();
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

                        Calendar c = Calendar.getInstance();

                        items.add(new MessageItem(MainActivity.this.response.body().items.get(sise).toString(), c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE)));
                        lv.setAdapter(new MessageAdapter(MainActivity.this, items));
                        lv.setSelection(items.size() - 1);
                        // Sets an ID for the notification
                        //log.append("Random is " + sise + " and text is " + MainActivity.this.response.body().items.get(sise).toString() + "\n");
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


    TimePickerDialog tpd = null;
    Calendar cal;
    Calendar calendar;

    protected Dialog onCreateDialog(int id) {
        if (id == 1543) {
            tpd = new TimePickerDialog(this, myCallBack, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            return tpd;
        }
        return super.onCreateDialog(id);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            DateTime nowTime = DateTime.now();
            DateTime alarmTime = DateTime.now().withTime(new LocalTime(hourOfDay, minute));

            if (alarmTime.compareTo(nowTime) <= 0) {
                // alarmTime раньше nowTime
                alarmTime = alarmTime.plusDays(1);
            }

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(MainActivity.this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);
            else
                am.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);

            Duration duration = new Duration(nowTime, alarmTime);
            Toast.makeText(MainActivity.this, "Alarm set to " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
                    " in " + duration.getStandardDays() + " hours & " + duration.getStandardMinutes() + " minutes.", Toast.LENGTH_LONG).show();
            tpd = null;
        }
    };

    public void setAlarm(View v) {
        calendar = Calendar.getInstance();
        cal = Calendar.getInstance();
        showDialog(1543);
    }

}
