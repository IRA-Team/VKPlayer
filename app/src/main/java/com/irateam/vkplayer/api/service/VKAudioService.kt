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

package com.irateam.vkplayer.api.service

import android.content.Context
import com.irateam.vkplayer.api.*
import com.irateam.vkplayer.database.AudioVKCacheDatabase
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.util.extension.e
import com.irateam.vkplayer.util.extension.process
import com.irateam.vkplayer.util.extension.v
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters
import java.io.File

class VKAudioService {

    private val helper: AudioVKCacheDatabase

    constructor(context: Context) {
        helper = AudioVKCacheDatabase(context)
    }

    fun getCurrent(): Query<List<VKAudio>> {
        return CurrentAudioQuery()
    }

    fun getMy(): Query<List<VKAudio>> {
        val request = VKApi.audio().get()
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getMy(count: Int): Query<List<VKAudio>> {
        val params = VKParameters.from(VKApiConst.COUNT, count)
        val request = VKApi.audio().get(params)
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getRecommendation(): Query<List<VKAudio>> {
        val request = VKApi.audio().getRecommendations()
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getPopular(): Query<List<VKAudio>> {
        val request = VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0))
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getById(audios: Collection<String>): Query<List<VKAudio>> {
        val params = VKParameters.from(AUDIOS, audios.joinToString())
        val request = VKApi.audio().getById(params)
        return VKAudioQuery(request)
    }

    fun getCached(): Query<List<VKAudio>> {
        return CachedAudioQuery()
    }

    fun removeFromCache(audios: Collection<VKAudio>): Query<List<VKAudio>> {
        return RemoveFromCacheQuery(audios)
    }

    fun removeAllCachedAudio(): Query<List<VKAudio>> {
        return RemoveAllFromCacheQuery()
    }

    private fun processCache(audios: List<VKAudio>): List<VKAudio> {
        val cached = helper.getAll()
        cached.forEach {
            val cachedAudio = it
            audios.filter { it.id == cachedAudio.id }
                    .forEach { it.cachePath = cachedAudio.cachePath }
        }
        return audios
    }

    /**
     * Returns a map that contains names of found external audio files and this files.
     * Name of files have format {OWNER_ID}_{AUDIO_ID}
     */
    private fun getExternalAudioFileMap(): Map<String, File> {
        return VkConstants.POSSIBLE_AUDIO_DIRECTORIES
                .map(::File)
                .map { it.walk().toList() }
                .flatten()
                .filter { !it.isDirectory }
                .filter { it.extension.isEmpty() }
                .map { it.name.to(it) }
                .toMap()
    }

    //Queries
    private inner class CachedAudioQuery : AbstractQuery<List<VKAudio>>() {

        override fun query(): List<VKAudio> {
            val cached = helper.getAll().filter { it.isCached }
            val cachedIds = cached.map { it.id }.toSet()
            val nonIndexedExternal = getExternalAudioFileMap()
                    .filter { !cachedIds.contains(it.key) }

            v(TAG, "External non indexed audios: ${nonIndexedExternal.keys}")
            val externalIndexed = try {
                if (nonIndexedExternal.isNotEmpty()) {
                    getById(nonIndexedExternal.keys)
                            .execute()
                            .process { it.cachePath = nonIndexedExternal[it.id]?.absolutePath }
                            .process { helper.cache(it) }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e(TAG, "An error occurred during indexing")
                e.printStackTrace()
                emptyList<VKAudio>()
            }

            return cached + externalIndexed
        }
    }

    private inner class CurrentAudioQuery : AbstractQuery<List<VKAudio>>() {
        override fun query(): List<VKAudio> = Player.playlist
                .filterIsInstance(VKAudio::class.java)
    }

    private inner class RemoveFromCacheQuery(val audios: Collection<VKAudio>) :
            AbstractQuery<List<VKAudio>>() {

        override fun query(): List<VKAudio> = audios.filter { it.isCached }
                .map { helper.delete(it); it }
                .map { it.removeFromCache(); it }
                .toList()
    }

    private inner class RemoveAllFromCacheQuery : AbstractQuery<List<VKAudio>>() {

        override fun query(): List<VKAudio> {
            val audios = helper.getAll()
            helper.removeAll()
            return audios.filter { it.isCached }
                    .map { it.removeFromCache(); it }
                    .toList()
        }
    }

    //Cache decorators
    private inner class CacheQueryDecorator : Query<List<VKAudio>> {

        val query: Query<List<VKAudio>>

        constructor(query: Query<List<VKAudio>>) {
            this.query = query
        }

        override fun execute(): List<VKAudio> {
            val audios = query.execute()
            return processCache(audios)
        }

        override fun execute(callback: Callback<List<VKAudio>>) {
            query.execute(CacheCallbackDecorator(callback))
        }

        override fun cancel() {
            query.cancel()
        }

    }

    private inner class CacheCallbackDecorator : Callback<List<VKAudio>> {

        val callback: Callback<List<VKAudio>>

        constructor(callback: Callback<List<VKAudio>>) {
            this.callback = callback
        }

        override fun onComplete(result: List<VKAudio>) {
            callback.onComplete(processCache(result))
        }

        override fun onError() {
            callback.onError()
        }

    }

    //Constants
    companion object {

        val TAG: String = VKAudioService::class.java.name

        val AUDIOS = "audios"
        val GENRE_ID = "genre_id"
    }


}
