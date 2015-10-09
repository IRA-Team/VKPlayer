package com.irateam.vkplayer.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerNotification {

    public static final int ID = 1;

    public static Notification create(Context context, VKApiAudio audio) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_player_random_light_grey_18dp)
                .setContentTitle(audio.title)
                .setContentText(audio.artist)
                .build();

    }

    public static void update(Context context, VKApiAudio audio) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, create(context, audio));
    }
}
