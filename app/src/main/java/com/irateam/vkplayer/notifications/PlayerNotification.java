package com.irateam.vkplayer.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.PlayerService;
import com.irateam.vkplayer.utils.AlbumCoverUtils;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerNotification {

    public static final int ID = 1;

    public static Notification create(Context context, int index, VKApiAudio audio, Player.PlayerEvent event) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder
                .setContentTitle(index + 1 + ". " + audio.title)
                .setContentText(audio.artist)
                .setLargeIcon(AlbumCoverUtils.createBitmapFromAudio(audio))
                .setShowWhen(false)
                .addAction(R.drawable.ic_player_previous_grey_18dp,
                        context.getString(R.string.notification_previous),
                        createAction(context, PlayerService.PREVIOUS));

        if (event == Player.PlayerEvent.RESUME || event == Player.PlayerEvent.PLAY) {
            builder.setSmallIcon(R.drawable.ic_notification_play_white_18dp)
                    .addAction(R.drawable.ic_player_pause_grey_18dp,
                            context.getString(R.string.notification_pause),
                            createAction(context, PlayerService.PAUSE));
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_pause_white_18dp)
                    .addAction(R.drawable.ic_player_play_grey_18dp,
                            context.getString(R.string.notification_resume),
                            createAction(context, PlayerService.RESUME));
        }

        builder.addAction(R.drawable.ic_player_next_grey_18dp,
                context.getString(R.string.notification_next),
                createAction(context, PlayerService.NEXT))
                .setAutoCancel(true);
        return builder.build();

    }

    public static void update(Context context, int index, VKApiAudio audio, Player.PlayerEvent event) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, create(context, index, audio, event));
    }

    private static PendingIntent createAction(Context context, String action) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }
}
