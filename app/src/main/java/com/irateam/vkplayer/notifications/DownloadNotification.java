package com.irateam.vkplayer.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.services.DownloadService;

public class DownloadNotification {

    public static final int ID = 2;

    public static Notification create(Context context, Audio audio, int progress, boolean isSync) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.STOP_DOWNLOADING);
        builder
                .setContentTitle(audio.artist + " - " + audio.title)
                .setSmallIcon(R.drawable.ic_statusbar_download_white_18dp)
                .setContentText(isSync ? context.getString(R.string.notification_sync) : context.getString(R.string.notification_download))
                .setProgress(100, progress, false)
                .addAction(R.drawable.ic_notification_cancel_white_24dp,
                        context.getString(R.string.notification_cancel),
                        PendingIntent.getService(context, 0, intent, 0));
        return builder.build();
    }

    public static void update(Context context, Audio audio, int progress, boolean isSync) {
        Notification notification = create(context, audio, progress, isSync);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ID, notification);
    }
}
