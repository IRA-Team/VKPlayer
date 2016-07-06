/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v7.app.NotificationCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.activities.AudioActivity;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.player.PlayerEvent;
import com.irateam.vkplayer.player.PlayerPlayEvent;
import com.irateam.vkplayer.player.PlayerResumeEvent;
import com.irateam.vkplayer.services.PlayerService;

public class PlayerNotificationFactory {

    private final Context context;

    private final NotificationCompat.MediaStyle style;
    private final NotificationCompat.Builder builder;

    private final Action playerPreviousAction;
    private final Action playerNextAction;
    private final Action playerPauseAction;
    private final Action playerPlayAction;

    private final Bitmap defaultCover;

    public PlayerNotificationFactory(Context context) {
        this.context = context;

        //Initialize default cover
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_cover);
        defaultCover = scaleNotification(context, bitmap);

        //Initialize player actions
        playerPreviousAction = new Action.Builder(
                R.drawable.ic_notification_prev_white_24dp,
                context.getString(R.string.notification_previous),
                createPendingIntent(PlayerService.PREVIOUS)).build();

        playerNextAction = new Action.Builder(
                R.drawable.ic_notification_next_white_24dp,
                context.getString(R.string.notification_next),
                createPendingIntent(PlayerService.NEXT)).build();

        playerPlayAction = new Action.Builder(
                R.drawable.ic_notification_play_white_24dp,
                context.getString(R.string.notification_resume),
                createPendingIntent(PlayerService.RESUME)).build();

        playerPauseAction = new Action.Builder(
                R.drawable.ic_notification_pause_white_24dp,
                context.getString(R.string.notification_pause),
                createPendingIntent(PlayerService.PAUSE)).build();

        style = new NotificationCompat.MediaStyle()
                .setShowCancelButton(true)
                .setCancelButtonIntent(createPendingIntent(PlayerService.STOP))
                .setShowActionsInCompactView(1, 2);

        Intent contentIntent = new Intent(context, AudioActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        builder = new NotificationCompat.Builder(context);
        builder.setStyle(style)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))
                .setAutoCancel(true)
                .addAction(playerPreviousAction)
                .addAction(playerPlayAction)
                .addAction(playerNextAction);
    }

    private static Bitmap scaleNotification(Context context, Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap,
                (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height),
                (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width),
                false);
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private void postProcess(int index, Audio audio) {
        builder.setContentTitle(index + 1 + ". " + audio.getTitle())
                .setContentText(audio.getArtist())
                .setLargeIcon(audio.getAudioInfo().coverNotification != null ? audio.getAudioInfo().coverNotification : defaultCover);
    }

    private void postProcess(PlayerEvent e) {
        final int index = e.getIndex();
        final Audio audio = e.getAudio();

        postProcess(index, audio);

        if (e instanceof PlayerResumeEvent || e instanceof PlayerPlayEvent) {
            builder.setSmallIcon(R.drawable.ic_notification_play_white_18dp)
                    .mActions.set(1, playerPauseAction);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_pause_white_18dp)
                    .mActions.set(1, playerPlayAction);
        }
    }

    public Notification get(int index, Audio audio) {
        postProcess(index, audio);
        return builder.build();
    }

    public Notification get(PlayerEvent e) {
        postProcess(e);
        return builder.build();
    }
}
