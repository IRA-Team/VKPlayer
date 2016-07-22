/*
 * Copyright (C) 2016 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.utils

import android.content.Context
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.event.DownloadErrorEvent
import com.irateam.vkplayer.event.DownloadFinishedEvent
import com.irateam.vkplayer.event.DownloadProgressChangedEvent
import com.irateam.vkplayer.event.DownloadTerminatedEvent
import com.irateam.vkplayer.models.Audio
import org.greenrobot.eventbus.EventBus
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors

class AudioDownloader {

    private val context: Context
    private val settings: SettingsService
    private val executor = Executors.newSingleThreadExecutor()
    private val eventBus = EventBus.getDefault()

    constructor(context: Context) {
        this.context = context
        this.settings = SettingsService.getInstance(context)
    }

    fun download(audio: Audio) {
        val runnable = prepareRunnable(audio)
        executor.execute(runnable)
    }

    fun stop() {
        executor.shutdown()
    }

    private fun prepareRunnable(audio: Audio) = Runnable {
        try {
            val source = URL(audio.url).openConnection()
            val input = BufferedInputStream(source.inputStream)

            val destination = File(settings.audioCacheDir, audio.id.toString())
            val output = FileOutputStream(destination)

            val size = source.contentLength
            val buffer = ByteArray(1024)

            var currentProgress: Int
            var totalProgress = 0
            var totalBytes = 0

            var currentBytes = input.read(buffer, 0, 1024)
            while (currentBytes != -1) {
                if (Thread.interrupted()) {
                    notifyTerminated(audio)
                    return@Runnable
                }

                output.write(buffer, 0, currentBytes)
                totalBytes += currentBytes
                currentProgress = (totalBytes.toDouble() / size * 100).toInt()

                if (currentProgress - totalProgress > 3) {
                    totalProgress = currentProgress
                    notifyDownloadProgressChanged(audio, totalProgress)
                }
                currentBytes = input.read(buffer, 0, 1024)
            }
            audio.cachePath = destination.path
            notifyDownloadFinished(audio)
        } catch (e: IOException) {
            notifyDownloadError(audio, e)
        }
    }

    private fun notifyTerminated(audio: Audio) {
        eventBus.post(DownloadTerminatedEvent(audio))
    }

    private fun notifyDownloadFinished(audio: Audio) {
        eventBus.post(DownloadFinishedEvent(audio))
    }

    private fun notifyDownloadError(audio: Audio, cause: Throwable) {
        eventBus.post(DownloadErrorEvent(audio, cause))
    }

    private fun notifyDownloadProgressChanged(audio: Audio, progress: Int) {
        eventBus.post(DownloadProgressChangedEvent(audio, progress))
    }

}