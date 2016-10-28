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
import com.irateam.vkplayer.database.AudioVKCacheDatabase
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.player.Player
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters

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
        val request = VKApi.audio().recommendations
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getPopular(): Query<List<VKAudio>> {
        val request = VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0))
        val query = VKAudioQuery(request)
        return CacheQueryDecorator(query)
    }

    fun getById(audios: Collection<String>): Query<List<VKAudio>> {
        val params = VKParameters.from("audios", audios.joinToString())
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

    //Queries
    private inner class CachedAudioQuery : AbstractQuery<List<VKAudio>>() {

        override fun query(): List<VKAudio> = helper.getAll().filter { it.isCached }
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

        val GENRE_ID = "genre_id"
    }


}
