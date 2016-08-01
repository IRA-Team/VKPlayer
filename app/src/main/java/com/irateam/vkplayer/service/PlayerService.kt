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

package com.irateam.vkplayer.service

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.*
import android.os.IBinder
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.service.MetadataService
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.event.MetadataLoadedEvent
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.notification.PlayerNotificationFactory
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.util.EventBus
import org.greenrobot.eventbus.Subscribe

class PlayerService : Service(), AudioManager.OnAudioFocusChangeListener {

    private val player = Player.getInstance()
    private val metadataService = MetadataService(this)
    private val headsetReceiver = HeadsetReceiver()

    private lateinit var settingsService: SettingsService
    private lateinit var notificationFactory: PlayerNotificationFactory
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    private var wasPlaying = false
    private var hasFocus = false

    override fun onCreate() {
        super.onCreate()
        this.settingsService = SettingsService(this)
        this.notificationFactory = PlayerNotificationFactory(this)

        player.repeatState = settingsService.loadRepeatState()
        player.randomState = settingsService.loadRandomState()

        EventBus.register(this)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.action?.let {
            when (it) {
                PREVIOUS -> player.previous()
                PAUSE -> player.pause()
                RESUME -> player.resume()
                NEXT -> player.next()
                STOP -> player.stop()
            }
        }
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.unregister(this)
        unregisterReceiver(headsetReceiver)
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Binding not supported. Use EventBus instead")
    }

    //Player callbacks
    @Subscribe
    fun onPlayEvent(e: PlayerPlayEvent) {
        val index = e.index
        val audio = e.audio

        startForeground(PLAYER_NOTIFICATION_ID, notificationFactory.get(e))
        requestFocus()

        metadataService.get(audio).execute(SimpleCallback
                .success {
                    audio.metadata = it
                    EventBus.post(MetadataLoadedEvent(audio, it))
                    updateNotification(index, audio)
                })
    }

    @Subscribe
    fun onPauseEvent(e: PlayerPauseEvent) {
        abandonFocus()
        if (e.shouldStopForeground) {
            stopForeground(true)
        } else {
            updateNotification(e)
        }
    }

    @Subscribe
    fun onResumeEvent(e: PlayerResumeEvent) {
        startForeground(PLAYER_NOTIFICATION_ID, notificationFactory.get(e))
        requestFocus()
    }

    @Subscribe
    fun onStopEvent(e: PlayerStopEvent) {
        abandonFocus()
        stopForeground(true)
    }

    @Subscribe
    fun onRepeatChangedEvent(e: PlayerRepeatChangedEvent) {
        settingsService.saveRepeatState(e.repeatState)
    }

    @Subscribe
    fun onRandomChangedEvent(e: PlayerRandomChangedEvent) {
        settingsService.saveRandomState(e.randomState)
    }

    fun updateNotification(index: Int, audio: Audio) {
        val notification = notificationFactory.get(index, audio)
        notificationManager.notify(PLAYER_NOTIFICATION_ID, notification)
    }

    fun updateNotification(e: PlayerEvent) {
        val notification = notificationFactory.get(e)
        notificationManager.notify(PLAYER_NOTIFICATION_ID, notification)
    }

    override fun onAudioFocusChange(focus: Int) {
        when (focus) {
            AUDIOFOCUS_LOSS -> if (player.isPlaying) {
                wasPlaying = true
                player.pause()
            } else {
                wasPlaying = false
            }

            AUDIOFOCUS_LOSS_TRANSIENT -> if (player.isPlaying) {
                wasPlaying = true
                player.pause()
            } else {
                wasPlaying = false
            }

            AUDIOFOCUS_GAIN -> if (wasPlaying) {
                player.resume()
            }
        }
    }

    private fun requestFocus() {
        val result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AUDIOFOCUS_GAIN)

        hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (hasFocus) {
            audioManager.abandonAudioFocus(this)
            hasFocus = false
        }
    }

    private inner class HeadsetReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_HEADSET_PLUG
                    && intent.getIntExtra("state", -1) == 0
                    && player.isPlaying) {

                player.pause()
            }
        }
    }

    companion object {

        val PLAYER_NOTIFICATION_ID = 1

        val PREVIOUS = "playerService.PREVIOUS"
        val PAUSE = "playerService.PAUSE"
        val RESUME = "playerService.RESUME"
        val NEXT = "playerService.NEXT"
        val STOP = "playerService.STOP"
    }
}