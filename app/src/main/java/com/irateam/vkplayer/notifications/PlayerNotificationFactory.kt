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

package com.irateam.vkplayer.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat.Action
import android.support.v7.app.NotificationCompat
import com.irateam.vkplayer.R
import com.irateam.vkplayer.activities.AudioActivity
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.PlayerEvent
import com.irateam.vkplayer.player.PlayerPlayEvent
import com.irateam.vkplayer.player.PlayerResumeEvent
import com.irateam.vkplayer.services.PlayerService

class PlayerNotificationFactory {

    private val context: Context

    private val style: NotificationCompat.MediaStyle
    private val builder: NotificationCompat.Builder

    private val playerPreviousAction: Action
    private val playerNextAction: Action
    private val playerPauseAction: Action
    private val playerPlayAction: Action

    private val defaultCover: Bitmap

    constructor(context: Context) {
        this.context = context

        //Initialize default cover
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player_cover)
        defaultCover = scaleNotification(context, bitmap)

        //Initialize player actions
        playerPreviousAction = Action.Builder(
                R.drawable.ic_notification_prev_white_24dp,
                context.getString(R.string.notification_previous),
                createPendingIntent(PlayerService.PREVIOUS)).build()

        playerNextAction = Action.Builder(
                R.drawable.ic_notification_next_white_24dp,
                context.getString(R.string.notification_next),
                createPendingIntent(PlayerService.NEXT)).build()

        playerPlayAction = Action.Builder(
                R.drawable.ic_notification_play_white_24dp,
                context.getString(R.string.notification_resume),
                createPendingIntent(PlayerService.RESUME)).build()

        playerPauseAction = Action.Builder(
                R.drawable.ic_notification_pause_white_24dp,
                context.getString(R.string.notification_pause),
                createPendingIntent(PlayerService.PAUSE)).build()

        style = NotificationCompat.MediaStyle()
                .setShowCancelButton(true)
                .setCancelButtonIntent(createPendingIntent(PlayerService.STOP))
                .setShowActionsInCompactView(1, 2)

        val contentIntent = Intent(context, AudioActivity::class.java)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        builder = NotificationCompat.Builder(context)
        builder.setStyle(style)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))
                .setAutoCancel(true)
                .addAction(playerPreviousAction)
                .addAction(playerPlayAction)
                .addAction(playerNextAction)
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = action
        return PendingIntent.getService(context, 0, intent, 0)
    }

    private fun postProcess(index: Int, audio: Audio) {
        val info = audio.audioInfo
        val position = (index + 1).toString()

        builder.setContentTitle(position + ". " + audio.title)
                .setContentText(audio.artist)
                .setLargeIcon(info?.coverNotification ?: defaultCover)
    }

    private fun postProcess(e: PlayerEvent) {
        val index = e.index
        val audio = e.audio

        postProcess(index, audio)

        if (e is PlayerResumeEvent || e is PlayerPlayEvent) {
            builder.setSmallIcon(R.drawable.ic_notification_play_white_18dp).mActions[1] = playerPauseAction
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_pause_white_18dp).mActions[1] = playerPlayAction
        }
    }

    operator fun get(index: Int, audio: Audio): Notification {
        postProcess(index, audio)
        return builder.build()
    }

    operator fun get(e: PlayerEvent): Notification {
        postProcess(e)
        return builder.build()
    }

    companion object {

        @JvmStatic
        fun scaleNotification(context: Context, bitmap: Bitmap): Bitmap {
            return Bitmap.createScaledBitmap(bitmap,
                    context.resources.getDimension(android.R.dimen.notification_large_icon_height).toInt(),
                    context.resources.getDimension(android.R.dimen.notification_large_icon_width).toInt(),
                    false)
        }
    }
}
