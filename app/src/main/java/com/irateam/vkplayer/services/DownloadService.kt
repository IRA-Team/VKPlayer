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

package com.irateam.vkplayer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.service.AudioService
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.database.AudioDatabaseHelper
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.notifications.DownloadNotification
import com.irateam.vkplayer.receivers.DownloadFinishedReceiver
import com.irateam.vkplayer.utils.isNetworkAvailable
import com.irateam.vkplayer.utils.isWifiNetworkAvailable
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DownloadService : Service() {

    private val audioService = AudioService(this)
    private val database = AudioDatabaseHelper(this)
    private val settings = SettingsService.getInstance(this)

    private val downloadQueue = ConcurrentLinkedQueue<Audio>()
    private val syncQueue = ConcurrentLinkedQueue<Audio>()

    private var currentThread: Thread? = null

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException()
    }

    @Suppress("unchecked")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                START_DOWNLOADING -> {
                    val list = intent.getParcelableArrayListExtra<Audio>(AUDIO_LIST)
                    downloadQueue.addAll(list)
                    if (!isDownloading) {
                        download()
                    }
                }

                STOP_DOWNLOADING -> {
                    stopDownloading()
                }

                START_SYNC -> if (intent.extras.getBoolean(USER_SYNC, false)) {
                    startManualSync()
                } else {
                    startScheduledSync()
                }
            }
        }
        return Service.START_NOT_STICKY
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
            DownloadNotification.errorSync(this, getString(R.string.error_no_wifi_connection))
        }
    }

    private fun prepareToSync() {
        val count = settings.syncCount
        audioService.getMy(count).execute(SimpleCallback
                .success<List<Audio>> {
                    val vkList = it
                    val cachedIds = database.all.map { it.id }
                    val nonCached = vkList.filter { cachedIds.contains(it.id) }.asReversed()

                    syncQueue.clear()
                    syncQueue.addAll(nonCached)

                    if (!isDownloading) {
                        download()
                    }
                }
                .error {
                    DownloadNotification.error(this, true)
                })
    }

    private fun getPreferredQueue(): Queue<Audio> = if (downloadQueue.isEmpty()) {
        syncQueue
    } else {
        downloadQueue
    }

    //TODO: Refactor this hell!
    fun download() {
        currentThread = Thread {
            var audioTotalCount = 0
            var audio: Audio?
            do {
                val queue = getPreferredQueue()
                val audioLeftCount = queue.size
                val isSynchronization = syncQueue === queue
                audio = queue.poll()

                if (audio != null) {
                    startForeground(DownloadNotification.ID, DownloadNotification.create(this, audio, 0, audioLeftCount - 1, isSynchronization))



                    audio.cachePath = file.path
                    database.cache(audio)

                    val intent = Intent(DOWNLOAD_FINISHED)
                    intent.putExtra(DownloadFinishedReceiver.AUDIO_ID, audio)
                    sendBroadcast(intent)

                    audioTotalCount++
                    if (queue.isEmpty()) {
                        DownloadNotification.successful(this, audioTotalCount, isSynchronization)
                        audioTotalCount = 0
                    }
                }
            } while (audio != null)
            stopForeground(true)
        }
        currentThread!!.start()
    }

    fun stopDownloading() {
        val currentThread = currentThread
        if (currentThread != null && currentThread.isInterrupted) {
            currentThread.interrupt()
        }
    }

    val isDownloading: Boolean
        get() = currentThread != null && currentThread?.isAlive ?: false


    companion object {

        val AUDIO_LIST = "audio_list"
        val DOWNLOAD_FINISHED = "download_service.download_finished"

        val START_SYNC = "start_sync"
        val STOP_DOWNLOADING = "stop_downloading"
        val START_DOWNLOADING = "start_downloading"
        val USER_SYNC = "user_sync"

        @JvmStatic
        fun download(context: Context, audios: Collection<Audio>) {
            val intent = Intent(context, DownloadService::class.java)
                    .setAction(START_DOWNLOADING)
                    .putParcelableArrayListExtra(AUDIO_LIST, ArrayList(audios))

            context.startService(intent)
        }
    }

}
