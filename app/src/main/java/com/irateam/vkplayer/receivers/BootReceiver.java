package com.irateam.vkplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.irateam.vkplayer.models.Settings;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Settings.getInstance(context).isSyncEnabled()) {
            Settings.setSyncAlarm(context);
        }
    }
}
