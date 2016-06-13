package com.okunev.waifusim;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.okunev.waifusim.utils.FileManager;
import com.okunev.waifusim.utils.LAppDefine;
import com.okunev.waifusim.utils.LAppLive2DManager;
import com.okunev.waifusim.utils.LAppView;
import com.okunev.waifusim.utils.SoundManager;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.live2d.Live2D;

/**
 * Created by gwa on 5/3/16.
 */
public class ModelActivity extends AppCompatActivity {
    @Bind(R.id.layout)
    RelativeLayout relativeLayout;
    @Bind(R.id.imageButton8)
    ImageButton iBtn;
    private LAppLive2DManager live2DMgr;
    static private AppCompatActivity instance;
    private AccountHeader headerResult = null;
    private Drawer result = null;

    public ModelActivity() {
        instance = this;
        if (LAppDefine.DEBUG_LOG) {
            Log.d("", "==============================================\n");
            Log.d("", "   Live2D Sample  \n");
            Log.d("", "==============================================\n");
        }

        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //   requestWindowFeature(Window.FEATURE_NO_TITLE);
        setupGUI(savedInstanceState);
        Toast.makeText(this, "Остальные вайфы выпилены для экономии трафика \n(у Изи нет интернета нормального сейчас)", Toast.LENGTH_SHORT).show();
        FileManager.init(this.getApplicationContext());

    }

    void setupGUI(Bundle savedInstanceState) {
        setContentView(R.layout.activity_waifu);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        LAppView view = live2DMgr.createView(this);

        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        relativeLayout.addView(view);

        ClickListener listener = new ClickListener();
        iBtn.setOnClickListener(listener);
    }

    void createDrawer(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final IProfile profile = new ProfileDrawerItem()
                .withName("Mike Penz")
                .withEmail("mikepenz@gmail.com")
                .withTextColor(Color.parseColor("#795548"))
                .withIcon(getResources().getDrawable(R.drawable.yui))
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
                                intent = new Intent(ModelActivity.this, MainActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(ModelActivity.this, GeofenceSetupActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                setAlarm(null);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(ModelActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                Toast.makeText(ModelActivity.this, "This feature is not ready yet!", Toast.LENGTH_LONG).show();
                            }

                            if (intent != null) {
                                ModelActivity.this.startActivity(intent);
                            }
                        }
                        return false;

                    }
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(2)
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
            Intent intent = new Intent(ModelActivity.this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ModelActivity.this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);
            else
                am.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);

            Duration duration = new Duration(nowTime, alarmTime);
            Toast.makeText(ModelActivity.this, "Alarm set to " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
                    " in " + duration.getStandardDays() + " hours & " + duration.getStandardMinutes() + " minutes.", Toast.LENGTH_LONG).show();
            tpd = null;
        }
    };

    public void setAlarm(View v) {
        calendar = Calendar.getInstance();
        cal = Calendar.getInstance();
        showDialog(1);
    }









    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "change model", Toast.LENGTH_SHORT).show();
            live2DMgr.changeModel();//Live2D Event
        }
    }


    @Override
    protected void onResume() {
        //live2DMgr.onResume() ;
        super.onResume();
    }


    @Override
    protected void onPause() {
        live2DMgr.onPause();
        super.onPause();
    }


}
