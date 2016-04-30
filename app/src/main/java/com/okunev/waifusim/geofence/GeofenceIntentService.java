package com.okunev.waifusim.geofence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.okunev.waifusim.R;

public class GeofenceIntentService extends IntentService {
    private static final String TAG = "GeofenceIntentService";

    public static final String ACTION_SET_GEOFENCE = "GeofenceIntentService.Set";
    private static final int NOTIFICATION_GEOFENCE = 829;
    private static final int PENDING_INTENT_ID = 833;
    private static final String
            KEY_LAT = "lat",
            KEY_LON = "lon",
            KEY_RAD = "rad",
            GEOFENCE_UNIQUE_ID = "geofence";

    public static Intent getSetterIntent(@NonNull Context context, double lat, double lon, float rad) {
        return new Intent(context, GeofenceIntentService.class)
                .setAction(ACTION_SET_GEOFENCE)
                .putExtra(KEY_LAT, lat)
                .putExtra(KEY_LON, lon)
                .putExtra(KEY_RAD, rad);
    }

    private Handler mainThreadHandler;

    public GeofenceIntentService() {
        super(GeofenceIntentService.class.getSimpleName());
        mainThreadHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (ACTION_SET_GEOFENCE.equals(intent.getAction())) {
                try {
                    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .build();
                        if (googleApiClient.blockingConnect().isSuccess()) {
                            Log.d(TAG, "onHandleIntent: connected = success");

                            double lat = intent.getDoubleExtra(KEY_LON, 0);
                            double lon = intent.getDoubleExtra(KEY_LAT, 0);
                            float rad = intent.getFloatExtra(KEY_RAD, 0);

                            Log.d(TAG, "onHandleIntent: lat=" + lat + " lon=" + lon + " rad" + rad);

                            PendingIntent pendingIntent = PendingIntent.getService(this, PENDING_INTENT_ID,
                                    new Intent(this, GeofenceIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);

                            Geofence geofence = new Geofence.Builder()
                                    .setRequestId(GEOFENCE_UNIQUE_ID)
                                    .setCircularRegion(lat, lon, rad)
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build();

                            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                            builder.addGeofence(geofence);
                            GeofencingRequest geofencingRequest = builder.build();

                            if (lat == 0 && lon == 0 && rad == 0)
                                if (LocationServices.GeofencingApi.removeGeofences(
                                        googleApiClient,
                                        pendingIntent)
                                        .await().isSuccess())
                                    Log.d(TAG, "onHandleIntent: unset success");
                                else throw new RuntimeException("Failed to unset geofence");
                            else if (LocationServices.GeofencingApi.addGeofences(
                                    googleApiClient,
                                    geofencingRequest,
                                    pendingIntent)
                                    .await().isSuccess())
                                Log.d(TAG, "onHandleIntent: set success");
                            else throw new RuntimeException("Failed to set geofence");
                        } else throw new RuntimeException("No services!");
                    } else throw new RuntimeException("No services!");
                } catch (SecurityException ignored) {
                    Log.d(TAG, "onHandleIntent: security failed");
                    // No permission
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GeofenceIntentService.this, "SecurityException", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Throwable e) {
                    Log.d(TAG, "onHandleIntent: failed");
                    e.printStackTrace();
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GeofenceIntentService.this, "Не удалось установить геофенс", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
                if (geofencingEvent.hasError()) return;
                int geofenceTransition = geofencingEvent.getGeofenceTransition();
                switch (geofenceTransition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        setNot(this, "Окайри!");
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        setNot(this, "Иттерашай!");
                        break;
                }

            }
        }
    }

    // TODO Украл из WaifuBatteryReceiver, нужен единый класс нотификаций
    public void setNot(Context context, String text) {
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Your Waifu")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIFICATION_GEOFENCE, notification);
    }
}
