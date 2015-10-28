package com.irateam.vkplayer.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.activities.AudioActivity;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.PlayerService;

public class PlayerNotification {

    public static final int ID = 1;
    private static Bitmap COVER;

    public static void init(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_cover);
        COVER = Bitmap.createScaledBitmap(bitmap,
                (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height),
                (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width),
                false);
    }

    private static PendingIntent createAction(Context context, String action) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    public static Notification create(Context context, int index, Audio audio, Player.PlayerEvent event) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.STOP);

        Intent contentIntent = new Intent(context, AudioActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
                .setShowCancelButton(true)
                .setCancelButtonIntent(PendingIntent.getService(context, 0, intent, 0));

        builder
                .setContentTitle(index + 1 + ". " + audio.title)
                .setContentText(audio.artist)
                .setLargeIcon(COVER)
                .setStyle(style)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))
                .addAction(R.drawable.ic_notification_prev_white_24dp,
                        context.getString(R.string.notification_previous),
                        createAction(context, PlayerService.PREVIOUS));

        if (event == Player.PlayerEvent.RESUME || event == Player.PlayerEvent.START) {
            builder.setSmallIcon(R.drawable.ic_notification_play_white_18dp)
                    .addAction(R.drawable.ic_notification_pause_white_24dp,
                            context.getString(R.string.notification_pause),
                            createAction(context, PlayerService.PAUSE));
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_pause_white_18dp)
                    .addAction(R.drawable.ic_notification_play_white_24dp,
                            context.getString(R.string.notification_resume),
                            createAction(context, PlayerService.RESUME));
        }

        builder.addAction(R.drawable.ic_notification_next_white_24dp,
                context.getString(R.string.notification_next),
                createAction(context, PlayerService.NEXT))
                .setAutoCancel(true);
        return builder.build();

    }

    public static void update(Context context, int index, Audio audio, Player.PlayerEvent event) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, create(context, index, audio, event));
    }

}
