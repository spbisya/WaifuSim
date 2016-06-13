package com.okunev.waifusim.geofence;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.okunev.waifusim.AlarmReceiverActivity;
import com.okunev.waifusim.MainActivity;
import com.okunev.waifusim.ModelActivity;
import com.okunev.waifusim.R;
import com.okunev.waifusim.SettingsActivity;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class GeofenceSetupActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int MAX_RADIUS = 500, MIN_RADIUS = 25;
    private static final String PREF_LAT = "GeofenceLat", PREF_LON = "GeofenceLon", PREF_RAD = "GeofenceRad";
    private static final int RC_PERMISSION = 25;

    @Bind(R.id.radiusSeekBar)
    SeekBar seekBar;
    @Bind(R.id.enableCheckBox)
    CheckBox checkBox;

    @BindColor(R.color.geofence_marker_fill)
    int fillColor;
    @BindColor(R.color.geofence_marker_stroke)
    int strokeColor;

    private SharedPreferences sharedPreferences;

    private GoogleMap map;
    private Circle currentCircle;
    private Marker marker;
    private float radius;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Нет плей сервисов или не могу к ним подлючиться", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_geofence_setup);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (marker != null) updateCircle(progress, marker.getPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setProgress(sharedPreferences.getInt(PREF_RAD, MAX_RADIUS));

        checkBox.setChecked(sharedPreferences.getString(PREF_LON, null) != null
                && sharedPreferences.getString(PREF_LAT, null) != null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Аната бака?", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

//    @OnCheckedChanged(R.id.enableCheckBox)
//    void enableCheckBoxChanged(boolean value) {
//        if (value)
//            if (map != null) updateCircle(seekBar.getProgress(), map.getCameraPosition().target);
//        else {
//
//            startService(GeofenceIntentService.getSetterIntent(GeofenceSetupActivity.this, 0, 0, 0));
//        }
//    }

    private void updateCircle(int seekBarValue, LatLng latLng) {
        if (map == null) return;

        float rad = (MAX_RADIUS - MIN_RADIUS) * seekBarValue / 100f + MIN_RADIUS;
        if (currentCircle == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .radius(rad);
            currentCircle = map.addCircle(circleOptions);
        } else {
            currentCircle.setCenter(latLng);
            currentCircle.setRadius(rad);
        }

        //  if (checkBox.isChecked()) {
        radius = rad;
        sharedPreferences.edit()
                .putString(PREF_LAT, String.format(Locale.US, "%f", latLng.latitude))
                .putString(PREF_LON, String.format(Locale.US, "%f", latLng.longitude))
                .putInt(PREF_RAD, seekBarValue)
                .apply();

        startService(GeofenceIntentService.getSetterIntent(GeofenceSetupActivity.this,
                latLng.latitude, latLng.longitude, rad));
        //  }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);

        String lat = sharedPreferences.getString(PREF_LAT, null);
        String lon = sharedPreferences.getString(PREF_LON, null);

        LatLng latLng = new LatLng(37.630693, 55.758026);
        if (lat != null && lon != null) {
            try {
                double latD = Double.parseDouble(lat);
                double lonD = Double.parseDouble(lon);
                latLng = new LatLng(latD, lonD);
            } catch (Exception ignored) {
            }
        }

        marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .title("Geo: " + latLng.latitude + ", " + latLng.longitude));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13f);
        googleMap.moveCamera(cameraUpdate);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);
                updateCircle(seekBar.getProgress(), latLng);
            }
        });
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Some permissions are blocked, fix it!", Toast.LENGTH_SHORT).show();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

    }

    @Override
    public void onPause() {
        super.onPause();
        if(marker.getPosition()!=null)
        sharedPreferences.edit()
                .putString(PREF_LAT, "" + marker.getPosition().latitude)
                .putString(PREF_LON, "" + marker.getPosition().longitude)
                .commit();
        startService(GeofenceIntentService.getSetterIntent(GeofenceSetupActivity.this,
                marker.getPosition().latitude, marker.getPosition().longitude, radius));
        finish();

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
                                intent = new Intent(GeofenceSetupActivity.this, MainActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(GeofenceSetupActivity.this, ModelActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                setAlarm(null);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(GeofenceSetupActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                Toast.makeText(GeofenceSetupActivity.this, "This feature is not ready yet!", Toast.LENGTH_LONG).show();
                            }

                            if (intent != null) {
                                GeofenceSetupActivity.this.startActivity(intent);
                            }
                        }
                        return false;

                    }
                })
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(3)
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
            Intent intent = new Intent(GeofenceSetupActivity.this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(GeofenceSetupActivity.this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);
            else
                am.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), pendingIntent);

            Duration duration = new Duration(nowTime, alarmTime);
            Toast.makeText(GeofenceSetupActivity.this, "Alarm set to " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
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
