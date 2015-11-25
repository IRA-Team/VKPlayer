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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.services.DownloadService;

public class DownloadNotification {

    public static final int ID = 2;
    public static final int FINAL_SYNC_NOTIFICATION_ID = 3;
    public static final int FINAL_DOWNLOAD_NOTIFICATION_ID = 4;

    public static Notification create(Context context, Audio audio, int progress, int audioLeftCount, boolean isSync) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.STOP_DOWNLOADING);
        builder
                .setContentTitle(audio.getArtist() + " - " + audio.getTitle())
                .setSmallIcon(isSync ? R.drawable.ic_notification_sync_white_24dp : R.drawable.ic_statusbar_download_white_18dp)
                .setContentText(isSync ? context.getString(R.string.notification_sync) : context.getString(R.string.notification_download));

        if (audioLeftCount > 0) {
            builder.setContentInfo(context.getString(R.string.notification_audio_count_left) + audioLeftCount);
        }

        builder.setProgress(100, progress, false)
                .addAction(R.drawable.ic_notification_cancel_white_24dp,
                        context.getString(R.string.notification_cancel),
                        PendingIntent.getService(context, 0, intent, 0));
        return builder.build();
    }

    public static void update(Context context, Audio audio, int progress, int audioLeftCount, boolean isSync) {
        Notification notification = create(context, audio, progress, audioLeftCount, isSync);
        NotificationManagerCompat.from(context).notify(ID, notification);
    }

    public static void error(Context context, boolean isSync) {
        error(context, "", isSync);
    }

    public static void error(Context context, String error, boolean isSync) {
        if (isSync) {
            errorSync(context, error);
        } else {
            errorDownload(context, error);
        }
    }

    public static void errorSync(Context context, String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_sync_problem_white_24dp)
                .setContentTitle(context.getString(R.string.notification_error_sync_title))
                .setContentText(error.isEmpty() ? context.getString(R.string.notification_error_sync_message) : error);
        NotificationManagerCompat.from(context).notify(FINAL_SYNC_NOTIFICATION_ID, builder.build());
    }

    public static void errorDownload(Context context, String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_error_white_24dp)
                .setContentTitle(context.getString(R.string.notification_error_download_title))
                .setContentText(error.isEmpty() ? context.getString(R.string.notification_error_download_message) : error);
        NotificationManagerCompat.from(context).notify(FINAL_DOWNLOAD_NOTIFICATION_ID, builder.build());
    }

    public static void successful(Context context, int audioCount, boolean isSync) {
        if (isSync) {
            successfulSync(context, audioCount);
        } else {
            successfulDownload(context, audioCount);
        }
    }

    public static void successfulSync(Context context, int audioCount) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_ab_done)
                .setContentTitle(context.getString(R.string.notification_successful_sync))
                .setContentText(context.getString(R.string.notification_successful_sync_count) + audioCount);
        NotificationManagerCompat.from(context).notify(FINAL_SYNC_NOTIFICATION_ID, builder.build());
    }

    public static void successfulDownload(Context context, int audioCount) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_ab_done)
                .setContentTitle(context.getString(R.string.notification_successful_download))
                .setContentText(context.getString(R.string.notification_successful_download_count) + audioCount);
        NotificationManagerCompat.from(context).notify(FINAL_DOWNLOAD_NOTIFICATION_ID, builder.build());
    }

}
