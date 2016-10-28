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
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.VkConstants
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.model.VKAudio
import java.io.File

class VKExternalAudioService {

    private val vkAudioService: VKAudioService
    private val audioConverterService: AudioConverterService

    constructor(context: Context) {
        this.vkAudioService = VKAudioService(context)
        this.audioConverterService = AudioConverterService(context)
    }

    fun getExternal(): Query<List<VKAudio>> {
        return VKExternalAudioQuery()
    }

    fun isAudiosExist(): Boolean {
        return VkConstants.POSSIBLE_AUDIO_DIRECTORIES
                .map(::File)
                .map(File::listFiles)
                .filterNotNull()
                .flatMap { it.toList() }
                .filter { it.extension.isNotEmpty() }
                .isNotEmpty()
    }

    fun removeFromFilesystem(audios: Collection<LocalAudio>): Query<List<LocalAudio>> {
        TODO()
    }

    private inner class VKExternalAudioQuery : AbstractQuery<List<VKAudio>>() {

        override fun query(): List<VKAudio> {
            val audioMap = VkConstants.POSSIBLE_AUDIO_DIRECTORIES
                    .map(::File)
                    .map { it.walk() }
                    .flatMap { it.toList() }
                    .filter { !it.isDirectory }
                    .filter { it.extension.isEmpty() }
                    .map { it.name.to(it) }
                    .toMap()

            val vkAudios = vkAudioService.getById(audioMap.keys).execute()
            return vkAudios
                    .map { buildAudioIdPair(it) }
                    .map {
                        it.second.cachePath = audioMap[it.first]?.absolutePath
                        it.second
                    }
        }

        private fun buildAudioIdPair(audio: VKAudio): Pair<String, VKAudio> = with(audio) {
            "${ownerId}_${id}".to(this)
        }
    }
}