package com.irateam.vkplayer.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.vk.sdk.api.model.VKApiAudio;

public class DownloadNotification {

    public static final int ID = 2;

    public static Notification create(Context context, Audio audio, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setContentTitle(audio.artist + " " + audio.title)
                .setSmallIcon(R.drawable.ic_statusbar_download_white_18dp)
                .setContentText(audio.artist)
                .setProgress(100, progress, false);
        return builder.build();
    }

    public static void update(Context context, Audio audio, int progress) {
        Notification notification = create(context, audio, progress);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ID, notification);
    }
}
