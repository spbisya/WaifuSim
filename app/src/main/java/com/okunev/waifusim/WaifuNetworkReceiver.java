package com.okunev.waifusim;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WaifuNetworkReceiver extends BroadcastReceiver {
    public WaifuNetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = wifi != null && wifi.isConnectedOrConnecting() ||
                mobile != null && mobile.isConnectedOrConnecting();
        if (isConnected) {
            setNot(context, "Ты в сети, поздравляю!", 007);
        } else {
            setNot(context, "Молодец, показал характер! ВЕРНИСЬ!", 007);
        }


    }

    public void setNot(Context context, String text, int id) {
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Your Waifu")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();


        int mNotificationId = id;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId++, notification);
    }
}
