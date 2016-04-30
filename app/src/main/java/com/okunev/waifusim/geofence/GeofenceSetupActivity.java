package com.okunev.waifusim.geofence;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.okunev.waifusim.R;

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

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
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

    @OnCheckedChanged(R.id.enableCheckBox)
    void enableCheckBoxChanged(boolean value) {
        if (value)
            if (map != null) updateCircle(seekBar.getProgress(), map.getCameraPosition().target);
        else {
            sharedPreferences.edit()
                    .putString(PREF_LAT, null)
                    .putString(PREF_LON, null)
                    .apply();
            startService(GeofenceIntentService.getSetterIntent(GeofenceSetupActivity.this, 0, 0, 0));
        }
    }

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

        if (checkBox.isChecked()) {
            sharedPreferences.edit()
                    .putString(PREF_LAT, String.format(Locale.US, "%f", latLng.latitude))
                    .putString(PREF_LON, String.format(Locale.US, "%f", latLng.longitude))
                    .putInt(PREF_RAD, seekBarValue)
                    .apply();

            startService(GeofenceIntentService.getSetterIntent(GeofenceSetupActivity.this,
                    latLng.latitude, latLng.longitude, rad));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);

        String lat = sharedPreferences.getString(PREF_LAT, null);
        String lon = sharedPreferences.getString(PREF_LON, null);

        LatLng latLng = new LatLng(0, 0);
        if (lat != null && lon != null) {
            try {
                double latD = Double.parseDouble(lat);
                double lonD = Double.parseDouble(lon);
                latLng = new LatLng(latD, lonD);
            } catch (Exception ignored) {
            }
        }

        marker = googleMap.addMarker(new MarkerOptions().position(latLng));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng newLocation = cameraPosition.target;
                marker.setPosition(newLocation);
                updateCircle(seekBar.getProgress(), newLocation);
            }
        });
    }
}
