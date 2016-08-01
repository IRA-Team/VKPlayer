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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.service.AudioService
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.database.AudioDatabaseHelper
import com.irateam.vkplayer.event.DownloadErrorEvent
import com.irateam.vkplayer.event.DownloadFinishedEvent
import com.irateam.vkplayer.event.DownloadProgressChangedEvent
import com.irateam.vkplayer.event.DownloadTerminatedEvent
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.notification.DownloadNotificationFactory
import com.irateam.vkplayer.util.AudioDownloader
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.isNetworkAvailable
import com.irateam.vkplayer.util.extension.isWifiNetworkAvailable
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DownloadService : Service(), AudioDownloader.Listener {

    private val audioService = AudioService(this)
    private val database = AudioDatabaseHelper(this)
    private val settings = SettingsService.getInstance(this)
    private val notificationFactory = DownloadNotificationFactory(this)
    private val downloadQueue = ConcurrentLinkedQueue<Audio>()
    private val syncQueue = ConcurrentLinkedQueue<Audio>()

    private var currentSession: Session? = null

    private lateinit var downloader: AudioDownloader
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        downloader = AudioDownloader(this)
        downloader.listener = this
        notificationManager = NotificationManagerCompat.from(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.action?.let {
            when (intent.action) {
                START_DOWNLOADING -> {
                    val audios = intent.getParcelableArrayListExtra<Audio>(AUDIO_LIST)
                    downloadQueue.addAll(audios)
                    startDownloadIfNeeded()
                }
                START_SYNC -> if (intent.extras.getBoolean(USER_SYNC, false)) {
                    startManualSync()
                } else {
                    startScheduledSync()
                }
                STOP_DOWNLOADING -> {
                    stopDownloading()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startManualSync() = if (isNetworkAvailable()) {
        prepareToSync()
    } else {
        Toast.makeText(this, R.string.error_no_internet_connection, Toast.LENGTH_LONG).show()
    }

    private fun startScheduledSync() {
        val isWifiOnly = settings.isWifiSync
        if ((isWifiOnly && isWifiNetworkAvailable()) || (!isWifiOnly && isNetworkAvailable())) {
            prepareToSync()
        } else {
            notifyNoWifiConnection()
        }
    }

    private fun prepareToSync() {
        val count = settings.syncCount
        audioService.getMy(count).execute(SimpleCallback
                .success<List<Audio>> {
                    val vkList = it
                    val cachedIds = database.getAll().map { it.id }
                    val nonCached = vkList.filter { cachedIds.contains(it.id) }.asReversed()

                    syncQueue.clear()
                    syncQueue.addAll(nonCached)

                    if (!downloader.isDownloading()) {
                        pollAndDownload()
                    }
                }
                .error {
                    notifyError()
                })
    }

    private fun clearCurrentSession() {
        currentSession = null
    }

    private fun getPreferredQueue() = if (downloadQueue.isEmpty()) {
        syncQueue
    } else {
        downloadQueue
    }

    private fun hasAudios(): Boolean {
        val queue = getPreferredQueue()
        return !queue.isEmpty()
    }

    private fun startDownloadIfNeeded() {
        if (!downloader.isDownloading()) {
            pollAndDownload()
        }
    }

    fun stopDownloading() {
        downloader.stop()
    }

    fun pollAndDownload() {
        val queue = getPreferredQueue()
        val audio = queue.poll()

        if (audio != null) {
            val session = Session(audio = audio,
                    progress = 0,
                    audioCount = currentSession?.audioCount ?: 0,
                    queue = queue,
                    isSync = syncQueue === queue)

            currentSession = session

            downloader.download(audio)
            val notification = notificationFactory.getDownload(session)
            startForeground(NOTIFICATION_ID_DOWNLOADING, notification)
        } else {
            clearCurrentSession()
        }
    }

    override fun onDownloadProgressChanged(audio: Audio, progress: Int) {
        EventBus.post(DownloadProgressChangedEvent(audio, progress))
        currentSession?.let {
            currentSession = it.copy(progress = progress)
            notifyDownloading()
        }
    }

    override fun onDownloadFinished(audio: Audio) {
        database.cache(audio)
        EventBus.post(DownloadFinishedEvent(audio))
        currentSession?.let {
            val newSession = it.copy(audioCount = it.audioCount + 1)
            currentSession = newSession
        }

        if (hasAudios()) {
            pollAndDownload()
        } else {
            notifyFinished()
            stopForeground(true)
        }
    }

    override fun onDownloadError(audio: Audio, cause: Throwable) {
        EventBus.post(DownloadErrorEvent(audio, cause))
        stopForeground(true)
        clearCurrentSession()
    }

    override fun onDownloadTerminated(audio: Audio) {
        EventBus.post(DownloadTerminatedEvent(audio))
        stopForeground(true)
        clearCurrentSession()
    }

    fun notifyDownloading() = currentSession?.let {
        val notification = notificationFactory.getDownload(it)
        notificationManager.notify(NOTIFICATION_ID_DOWNLOADING, notification)
    }

    fun notifyFinished() = currentSession?.let {
        val notification = notificationFactory.getSuccessful(it)
        notificationManager.notify(NOTIFICATION_ID_FINISHED, notification)
    }

    fun notifyError() = currentSession?.let {
        val notification = notificationFactory.getError(it)
        notificationManager.notify(NOTIFICATION_ID_FINISHED, notification)
    }

    fun notifyNoWifiConnection() = currentSession?.let {
        val notification = notificationFactory.getErrorNoWifiConnection(it)
        notificationManager.notify(NOTIFICATION_ID_FINISHED, notification)
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException()
    }

    data class Session(val audio: Audio,
                       val progress: Int,
                       val audioCount: Int,
                       private val queue: Queue<Audio>,
                       val isSync: Boolean) {

        val audioCountLeft: Int
            get() = queue.size
    }

    companion object {

        val NOTIFICATION_ID_DOWNLOADING = 2
        val NOTIFICATION_ID_FINISHED = 3

        val AUDIO_LIST = "audio_list"
        val START_SYNC = "start_sync"
        val STOP_DOWNLOADING = "stop_downloading"
        val START_DOWNLOADING = "start_downloading"
        val USER_SYNC = "user_sync"

        @JvmStatic
        fun download(context: Context, audios: Collection<Audio>) {
            val intent = startDownloadIntent(context, audios)
            context.startService(intent)
        }

        @JvmStatic
        fun stop(context: Context) {
            val intent = stopDownloadIntent(context)
            context.stopService(intent)
        }

        @JvmStatic
        fun startDownloadIntent(context: Context, audios: Collection<Audio>): Intent {
            return Intent(context, DownloadService::class.java)
                    .setAction(START_DOWNLOADING)
                    .putParcelableArrayListExtra(AUDIO_LIST, ArrayList(audios))
        }

        @JvmStatic
        fun stopDownloadIntent(context: Context): Intent {
            return Intent(context, DownloadService::class.java)
                    .setAction(STOP_DOWNLOADING)
        }

        @JvmStatic
        fun startSyncIntent(context: Context, userSync: Boolean = false): Intent {
            return Intent(context, DownloadService::class.java)
                    .setAction(DownloadService.START_SYNC)
                    .putExtra(DownloadService.USER_SYNC, userSync)
        }

    }

}
