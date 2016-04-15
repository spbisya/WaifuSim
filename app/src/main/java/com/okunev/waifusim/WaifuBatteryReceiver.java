package com.okunev.waifusim;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class WaifuBatteryReceiver extends BroadcastReceiver {
    public WaifuBatteryReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = (level / (float) scale) * 100;
        if (batteryPct < 15) {
            setNot(context, "С-сенпай, не дай мне умереть!", 999);
            // do your task here.
        } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            setNot(context, "Нямка!!!", 999);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED) & batteryPct < 80) {
            setNot(context, "Я не наелась!", 999);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED) & batteryPct > 80) {
            setNot(context, "ВКУШНО!!!!", 999);
        }

    }

    public void setNot(Context context, String text, int id) {
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Your Waifu")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_1444249298867)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;

        int mNotificationId = id;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, notification);
    }
}
