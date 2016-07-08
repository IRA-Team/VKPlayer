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
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.VKAudioQuery
import com.irateam.vkplayer.database.AudioDatabaseHelper
import com.irateam.vkplayer.models.Audio
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters

class AudioService {

    private val helper: AudioDatabaseHelper

    constructor(context: Context) {
        helper = AudioDatabaseHelper(context)
    }

    fun getCurrent(): Query<List<Audio>> {
        val request = VKApi.audio().get()
        return VKAudioQuery(request)
    }

    fun getMy(): Query<List<Audio>> {
        val request = VKApi.audio().get()
        return VKAudioQuery(request)
    }

    fun getRecommendation(): Query<List<Audio>> {
        val request = VKApi.audio().recommendations
        return VKAudioQuery(request)
    }

    fun getPopular(): Query<List<Audio>> {
        val request = VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0))
        return VKAudioQuery(request)
    }

    fun getCached(): Query<List<Audio>> {
        return CachedAudioQuery();
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

    //Queries
    private inner class CachedAudioQuery : AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> = helper.all.filter { !it.isCached }
    }

    private inner class RemoveFromCacheQuery(val audios: Collection<Audio>) :
            AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> = audios.filter { it.isCached }
                .map { helper.delete(it); it }
                .map { it.removeCacheFile(); it }
                .toList();
    }

    private inner class RemoveAllFromCacheQuery : AbstractQuery<List<Audio>>() {

        override fun query(): List<Audio> {
            val audios = helper.all
            helper.removeAll();
            return audios.filter { it.isCached }
                    .map { it.removeCacheFile(); it }
                    .toList();
        }
    }

    //Constants
    companion object {

        val GENRE_ID = "genre_id"
    }


}
