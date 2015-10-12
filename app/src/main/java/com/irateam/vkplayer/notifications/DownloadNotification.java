package com.irateam.vkplayer.notifications;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.vk.sdk.api.model.VKApiAudio;

public class DownloadNotification {

    public static final int ID = 1;

    public static Notification create(Context context, VKApiAudio audio) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setContentTitle(audio.artist + " " + audio.title)
                .setSmallIcon(R.drawable.ic_statusbar_download_white_18dp)
                .setContentText(audio.artist);
        return builder.build();
    }
}
