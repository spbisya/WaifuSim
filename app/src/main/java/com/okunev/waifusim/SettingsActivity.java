package com.okunev.waifusim;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.okunev.waifusim.geofence.GeofenceSetupActivity;
import com.okunev.waifusim.network.WaifuApi;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gwa on 4/11/16.
 */
public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sPref;
    @Bind(R.id.getNewToken)
    Button getNewToken;
    @Bind(R.id.saveCurToken)
    Button save;
    @Bind(R.id.changeToken)
    Button changeToken;
    @Bind(R.id.curToken)
    EditText curToken;
    @Bind(R.id.textView5)
    TextView or;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        curToken.setText(sPref.getString("token", ""));
        curToken.setEnabled(false);
    }

    @OnClick(R.id.getNewToken)
    void onGetNewTokenClick() {
        createUser();
    }

    @OnClick(R.id.changeToken)
    void onChangeTokenClick() {
        changeToken.setVisibility(View.INVISIBLE);
        curToken.setEnabled(true);
        or.setVisibility(View.INVISIBLE);
        getNewToken.setVisibility(View.INVISIBLE);
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "Saved!", Toast.LENGTH_LONG).show();
                sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("token", curToken.getText().toString());
                ed.apply();
                finish();
                if (getIntent().getBooleanExtra("new", false)) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void createUser() {
        Call<Token> call = WaifuApi.api().user();
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Toast.makeText(SettingsActivity.this, "Token " + response.body().toString() + " generated!", Toast.LENGTH_LONG).show();
                curToken.setText(response.body().toString());
                sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("token", response.body().toString());
                ed.commit();
                finish();
                if (getIntent().getBooleanExtra("new", false)) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

            }
        });
    }


    private AccountHeader headerResult = null;
    private Drawer result = null;

    void createDrawer(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final IProfile profile = new ProfileDrawerItem()
                .withName("Mike Penz")
                .withEmail("mikepenz@gmail.com")
                .withIcon(getResources().getDrawable(R.drawable.yui))
                .withTextColor(Color.parseColor("#795548"))
                .withSelectedTextColor(Color.parseColor("#795548"));
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.background)
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
                .withDisplayBelowStatusBar(true)
                .withAccountHeader(headerResult)
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
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(SettingsActivity.this, MainActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(SettingsActivity.this, ModelActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                setAlarm(null);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(SettingsActivity.this, GeofenceSetupActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                Toast.makeText(SettingsActivity.this, "This feature is not ready yet!", Toast.LENGTH_LONG).show();
                            }

                            if (intent != null) {
                                SettingsActivity.this.startActivity(intent);
                            }
                        }
                        return false;

                    }
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(5)
                .build();
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
            DateTime nowTime = DateTime.now();
            DateTime alarmTime = DateTime.now().withTime(new LocalTime(hourOfDay, minute));

            if (alarmTime.compareTo(nowTime) <= 0) {
                // alarmTime раньше nowTime
                alarmTime = alarmTime.plusDays(1);
            }

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(SettingsActivity.this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(SettingsActivity.this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);
            else
                am.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);

            Duration duration = new Duration(nowTime, alarmTime);
            Toast.makeText(SettingsActivity.this, "Alarm set to " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
                    " in " + duration.getStandardDays() + " hours & " + duration.getStandardMinutes() + " minutes.", Toast.LENGTH_LONG).show();
            tpd = null;
        }
    };

    public void setAlarm(View v) {
        calendar = Calendar.getInstance();
        cal = Calendar.getInstance();
        showDialog(1);
    }
}
