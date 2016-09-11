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

import android.os.Environment
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.models.LocalAudio
import com.mpatric.mp3agic.Mp3File
import java.io.File

class LocalAudioService {

    fun getAll(): Query<List<LocalAudio>> {
        val root = Environment.getExternalStorageDirectory()
        return LocalAudioQuery(root)
    }

    private class LocalAudioQuery : AbstractQuery<List<LocalAudio>> {

        val root: File

        constructor(root: File) : super() {
            this.root = root
        }

        override fun query(): List<LocalAudio> {
            val list = root.walk()
                    .filter { !it.isDirectory }
                    .filter { it.name.endsWith(".mp3") }
                    .map { Mp3File(it.path) }
                    .mapIndexed { index, mp3 ->
                        LocalAudio(mp3.id3v2Tag.artist,
                                mp3.id3v2Tag.title,
                                mp3.id3v2Tag.length,
                                mp3.filename)
                    }.toList()
            return list
        }
    }

    companion object {
        val TAG = LocalAudioService::class.java.name
    }
}