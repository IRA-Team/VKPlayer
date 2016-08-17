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
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Callback
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.VKAudioQuery
import com.irateam.vkplayer.database.AudioDatabaseHelper
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.Player
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters

class AudioService {

    private val helper: AudioDatabaseHelper

    constructor(context: Context) {
        helper = AudioDatabaseHelper(context)
    }

    fun getCurrent(): Query<List<Audio>> {
        return CurrentAudioQuery()
    }

    fun getMy(): Query<List<Audio>> {
        val request = VKApi.audio().get()
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getMy(count: Int): Query<List<Audio>> {
        val params = VKParameters.from(VKApiConst.COUNT, count)
        val request = VKApi.audio().get(params)
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getRecommendation(): Query<List<Audio>> {
        val request = VKApi.audio().recommendations
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getPopular(): Query<List<Audio>> {
        val request = VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0))
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getCached(): Query<List<Audio>> {
        return CachedAudioQuery()
    }

    fun search(query: String): Query<List<Audio>> {
        val request = VKApi.audio().search(VKParameters.from(VKApiConst.Q, query))
        return VKAudioQuery(request)
    }

    fun removeFromCache(audios: Collection<Audio>): Query<List<Audio>> {
        return RemoveFromCacheQuery(audios)
    }

    fun removeAllCachedAudio(): Query<List<Audio>> {
        return RemoveAllFromCacheQuery()
    }

    private fun processCache(audios: List<Audio>): List<Audio> {
        val cached = helper.getAll()
        cached.forEach {
            val cachedAudio = it
            audios.filter { it.id == cachedAudio.id }
                    .forEach { it.cachePath = cachedAudio.cachePath }
        }
        return audios
    }

    //Queries
    private inner class CachedAudioQuery : AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> = helper.getAll().filter { it.isCached }
    }

    private inner class CurrentAudioQuery : AbstractQuery<List<Audio>>() {
        override fun query(): List<Audio> = Player.playlist
    }

    private inner class RemoveFromCacheQuery(val audios: Collection<Audio>) :
            AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> = audios.filter { it.isCached }
                .map { helper.delete(it); it }
                .map { it.removeFromCache(); it }
                .toList()
    }

    private inner class RemoveAllFromCacheQuery : AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> {
            val audios = helper.getAll()
            helper.removeAll()
            return audios.filter { it.isCached }
                    .map { it.removeFromCache(); it }
                    .toList()
        }
    }

    //Cache decorators
    private inner class CacheQueryDecorator : Query<List<Audio>> {

        val query: Query<List<Audio>>

        constructor(query: Query<List<Audio>>) {
            this.query = query
        }

        override fun execute(): List<Audio> {
            val audios = query.execute()
            return processCache(audios)
        }

        override fun execute(callback: Callback<List<Audio>>) {
            query.execute(CacheCallbackDecorator(callback))
        }

        override fun cancel() {
            query.cancel()
        }

    }

    private inner class CacheCallbackDecorator : Callback<List<Audio>> {

        val callback: Callback<List<Audio>>

        constructor(callback: Callback<List<Audio>>) {
            this.callback = callback
        }

        override fun onComplete(result: List<Audio>) {
            callback.onComplete(processCache(result))
        }

        override fun onError() {
            callback.onError()
        }

    }

    //Constants
    companion object {

        val GENRE_ID = "genre_id"
    }


}
