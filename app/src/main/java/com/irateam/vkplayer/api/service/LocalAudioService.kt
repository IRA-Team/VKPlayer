/*
 * Copyright (C)r 2016 IRA-Team
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

package com.irateam.vkplayer.api.service

import android.content.Context
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.ProgressableAbstractQuery
import com.irateam.vkplayer.api.ProgressableQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.database.AudioLocalIndexedDatabase
import com.irateam.vkplayer.event.AudioScannedEvent
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.i
import com.irateam.vkplayer.util.extension.splitToPartitions
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class LocalAudioService {

    private val context: Context
    private val database: AudioLocalIndexedDatabase
    private val converter: AudioConverterService


    constructor(context: Context) {
        this.context = context
        this.database = AudioLocalIndexedDatabase(context)
        this.converter = AudioConverterService(context)
    }

    fun scan(): ProgressableQuery<List<LocalAudio>, AudioScannedEvent> {
        return ScanAndIndexAudioQuery()
    }

    fun getAllIndexed(): Query<List<LocalAudio>> {
        return IndexedAudioQuery()
    }

    fun removeFromFilesystem(audios: Collection<LocalAudio>): Query<Collection<LocalAudio>> {
        return RemoveFromFilesystemQuery(audios)
    }

    private inner class IndexedAudioQuery : AbstractQuery<List<LocalAudio>>() {

        override fun query() = database.getAll()
    }

    private inner class ScanAndIndexAudioQuery :
            ProgressableAbstractQuery<List<LocalAudio>, AudioScannedEvent>() {

        override fun query(): List<LocalAudio> {
            val audioFiles = POSSIBLE_DIRECTORIES
                    .map(::File)
                    .map { it.walk().toList() }
                    .flatten()
                    .filter { !it.isDirectory }
                    .filter { it.name.endsWith(".mp3") }

            val counter = AtomicInteger()
            val tasks = audioFiles
                    .splitToPartitions(THREADS_COUNT)
                    .map { partition ->
                        ConverterCallable(partition) { audio ->
                            notifyAudioScanned(audio, counter, audioFiles.size)
                        }
                    }
            val audios = CONVERTER_EXECUTOR
                    .invokeAll(tasks)
                    .map { it.get() }
                    .flatten()

            database.bulkIndex(audios)
            return audios
        }

        private fun notifyAudioScanned(audio: LocalAudio, counter: AtomicInteger, total: Int) {
            notifyProgress(AudioScannedEvent(audio, counter.incrementAndGet(), total))
        }

        private inner class ConverterCallable : Callable<List<LocalAudio>> {

            val files: List<File>
            val onAudioScannedListener: (LocalAudio) -> Unit

            constructor(mp3Files: List<File>, onAudioScannedListener: (LocalAudio) -> Unit) {
                this.files = mp3Files
                this.onAudioScannedListener = onAudioScannedListener
            }

            override fun call(): List<LocalAudio> = files
                    .map {
                        val audio = converter.toLocalAudioFromFile(it)
                        if (audio != null) {
                            i(TAG, "Stored $audio")
                            onAudioScannedListener.invoke(audio)
                        }
                        audio
                    }
                    .filterNotNull()
                    .toList()
        }
    }

    private inner class RemoveFromFilesystemQuery : AbstractQuery<Collection<LocalAudio>> {

        val audios: Collection<LocalAudio>

        /**
         * Always make copy
         */
        constructor(audios: Collection<LocalAudio>) : super() {
            this.audios = audios.toList()
        }

        override fun query(): Collection<LocalAudio> {
            val removed = ArrayList<LocalAudio>()
            audios.forEach {
                val file = File(it.path)
                if (!file.exists() || file.delete()) {
                    database.delete(it)
                    removed.add(it)
                } else {
                    throw AccessDeniedException(
                            file = file,
                            reason = "The error occurred during deleting file.")
                }
            }
            return removed
        }
    }

    companion object {
        val TAG: String = LocalAudioService::class.java.name
        val POSSIBLE_DIRECTORIES = listOf(
                "/sdcard"
        )

        val THREADS_COUNT = Runtime.getRuntime().availableProcessors() * 4
        val CONVERTER_EXECUTOR: ExecutorService = Executors.newFixedThreadPool(THREADS_COUNT)
    }
}