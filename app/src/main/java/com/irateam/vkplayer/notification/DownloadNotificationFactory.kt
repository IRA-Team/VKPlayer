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

package com.irateam.vkplayer.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import com.irateam.vkplayer.R
import com.irateam.vkplayer.service.DownloadService
import com.irateam.vkplayer.service.DownloadService.Session

class DownloadNotificationFactory {

    val context: Context

    constructor(context: Context) {
        this.context = context
    }

    fun getDownload(session: Session): Notification {
        val builder = NotificationCompat.Builder(context)
        with(builder) {
            val audio = session.audio
            setContentTitle(audio.artist + " - " + audio.title)

            if (session.isSync) {
                setSmallIcon(R.drawable.ic_notification_sync_white_24dp)
                setContentText(context.getString(R.string.notification_sync))
            } else {
                setSmallIcon(R.drawable.ic_statusbar_download_white_18dp)
                setContentText(context.getString(R.string.notification_download))
            }

            val left = session.audioCountLeft
            if (left > 0) {
                setContentInfo(context.getString(R.string.notification_audio_count_left) + left)
            }

            setProgress(100, session.progress, false)

            val stopIntent = DownloadService.stopDownloadIntent(context)
            val stopPending = PendingIntent.getService(context, 0, stopIntent, 0)
            addAction(R.drawable.ic_notification_cancel_white_24dp,
                    context.getString(R.string.notification_cancel),
                    stopPending)
        }
        return builder.build()
    }

    fun getSuccessful(session: Session): Notification {
        @StringRes val contentTitle: Int
        @StringRes val contentText: Int

        if (session.isSync) {
            contentTitle = R.string.notification_successful_sync
            contentText = R.string.notification_successful_sync_count
        } else {
            contentTitle = R.string.notification_successful_download
            contentText = R.string.notification_successful_download_count
        }

        return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_ab_done)
                .setContentTitle(context.getString(contentTitle))
                .setContentText(context.getString(contentText) + session.audioCount)
                .build()
    }

    fun getError(session: Session, cause: String = ""): Notification {
        @DrawableRes val smallIcon: Int
        @StringRes val contentTitle: Int
        @StringRes val contentText: Int

        if (session.isSync) {
            smallIcon = R.drawable.ic_notification_sync_problem_white_24dp
            contentTitle = R.string.notification_error_sync_title
            contentText = R.string.notification_error_sync_message
        } else {
            smallIcon = R.drawable.ic_error_white_24dp
            contentTitle = R.string.notification_error_download_title
            contentText = R.string.notification_error_download_message
        }

        return NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setContentTitle(context.getString(contentTitle))
                .setContentText(if (cause.isEmpty()) context.getString(contentText) else cause)
                .build()
    }

    fun getErrorNoWifiConnection(session: Session) : Notification {
        val cause = context.getString(R.string.error_no_wifi_connection)
        return getError(session, cause)
    }
}
