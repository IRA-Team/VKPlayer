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

import com.irateam.vkplayer.model.Audio

class LocalSearchDelegate<T : Audio> : SearchDelegate {

    private val adapter: BaseAudioRecyclerAdapter<T, *>

    var original: List<T> = emptyList()
        private set

    override var query: String = ""

    constructor(adapter: BaseAudioRecyclerAdapter<T, *>) {
        this.adapter = adapter
    }

    override fun search(s: String) {
        if (query.isEmpty()) {
            original = adapter.audios
        }

        query = s.toLowerCase()
        adapter.audios = if (s.isNotEmpty()) {
            original.filter {
                query in it.title.toLowerCase() || query in it.artist.toLowerCase()
            }
        } else {
            original
        }

        adapter.notifyDataSetChanged()
    }

}