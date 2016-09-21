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

package com.irateam.vkplayer.adapter

import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.service.VKSearchService
import com.irateam.vkplayer.models.VKAudio
import com.irateam.vkplayer.util.extension.execute

class VKSearchDelegate : SearchDelegate {

    private val searchService: VKSearchService
    private val adapter: VKAudioRecyclerAdapter

    private var original: List<VKAudio> = emptyList()
    private var lastQuery: Query<List<VKAudio>>? = null

    override var query = ""

    override val isSearching: Boolean
        get() = query.isNotEmpty()

    constructor(adapter: VKAudioRecyclerAdapter) {
        this.adapter = adapter
        this.searchService = VKSearchService()
    }

    override fun search(s: String) {
        if (query.isEmpty()) {
            original = adapter.audios
        }

        query = s.toLowerCase()
        if (s.isNotEmpty()) {
            adapter.audios = original.filter {
                query in it.title.toLowerCase() || query in it.artist.toLowerCase()
            }

            lastQuery?.cancel()
            lastQuery = searchService.search(query)
            lastQuery?.execute {
                onSuccess {
                    adapter.searchAudios = it
                }
            }
        } else {
            adapter.audios = original
            adapter.searchAudios = emptyList()
            lastQuery?.cancel()
        }


        adapter.notifyDataSetChanged()
    }

}