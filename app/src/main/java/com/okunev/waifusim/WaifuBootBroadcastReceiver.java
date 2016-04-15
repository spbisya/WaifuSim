package com.okunev.waifusim;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WaifuBootBroadcastReceiver extends BroadcastReceiver {
    public WaifuBootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Notification notification = new Notification.Builder(context)
                .setContentTitle("Your Waifu")
                .setContentText("Привет, антош!")
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();

        // Sets an ID for the notification
        int mNotificationId = 999;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notification);
        // Toast.makeText(context, "Привет, Антош!", Toast.LENGTH_LONG).show();
        // throw new UnsupportedOperationException("Not yet implemented");
    }
}
