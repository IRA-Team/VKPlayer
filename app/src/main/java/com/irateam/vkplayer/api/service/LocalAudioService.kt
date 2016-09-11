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

package com.irateam.vkplayer.api.service

import android.content.Context
import android.os.Environment
import android.util.Log
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.database.AudioLocalIndexedDatabase
import com.irateam.vkplayer.models.LocalAudio
import com.mpatric.mp3agic.Mp3File
import java.io.File

class LocalAudioService {

    val database: AudioLocalIndexedDatabase

    constructor(context: Context) {
        database = AudioLocalIndexedDatabase(context)
    }

    fun getAll(): Query<List<LocalAudio>> {
        val root = Environment.getExternalStorageDirectory()
        return ScanAndIndexAudioQuery(root)
    }

    fun getAllIndexed(): Query<List<LocalAudio>> {
        return IndexedAudioQuery()
    }

    private inner class IndexedAudioQuery : AbstractQuery<List<LocalAudio>>() {

        override fun query() = database.getAll()
    }

    private inner class ScanAndIndexAudioQuery : AbstractQuery<List<LocalAudio>> {

        val root: File

        constructor(root: File) : super() {
            this.root = root
        }

        override fun query(): List<LocalAudio> {
            val indexed = database.getAll()
            val indexedPath = indexed.map { it.path }
            val list = root.walk()
                    .filter { !it.isDirectory }
                    .filter { it.name.endsWith(".mp3") }
                    .filterNot { it.path in indexedPath }
                    .map { Mp3File(it.path) }
                    .filter { it.id3v2Tag != null }
                    .map { mp3 ->
                        val audio = LocalAudio(mp3.id3v2Tag.artist,
                                mp3.id3v2Tag.title,
                                mp3.id3v2Tag.length,
                                mp3.filename)

                        Log.e(TAG, "Scanned $audio")
                        audio
                    }
                    .map {
                        try {
                            database.index(it)
                            Log.e(TAG, "Stored $it")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        it
                    }
                    .toList()

            return indexed + list
        }
    }

    companion object {
        val TAG = LocalAudioService::class.java.name
    }
}