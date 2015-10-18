package com.irateam.vkplayer.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;

public class DownloadNotification {

    public static final int ID = 2;

    public static Notification create(Context context, Audio audio, int progress, boolean isSync) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setContentTitle(audio.artist + " - " + audio.title)
                .setSmallIcon(R.drawable.ic_statusbar_download_white_18dp)
                .setContentText(isSync ? context.getString(R.string.notification_sync) : context.getString(R.string.notification_download))
                .setProgress(100, progress, false);
        return builder.build();
    }

    public static void update(Context context, Audio audio, int progress, boolean isSync) {
        Notification notification = create(context, audio, progress, isSync);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ID, notification);
    }
}
