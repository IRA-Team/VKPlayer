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

import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent.SortModeFinished
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent.SortModeStarted
import com.irateam.vkplayer.models.LocalAudio
import com.irateam.vkplayer.util.extension.swap
import java.util.*

class LocalSortModeDelegate : SortModeDelegate<LocalAudio> {

    private val adapter: LocalAudioRecyclerAdapter

    private var sortMode = false
    private var original: List<LocalAudio> = emptyList()

    constructor(adapter: LocalAudioRecyclerAdapter) {
        this.adapter = adapter
    }

    override fun start() {
        this.sortMode = true
        this.original = adapter.audios
        adapter.notifyItemRangeChanged(0, original.size, SortModeStarted)
    }

    override fun sort(comparator: Comparator<in LocalAudio>) {
        val toSort = adapter.audios
        val pending = ArrayList(toSort)
        val sorted = toSort.sortedWith(comparator)

        adapter.audios = sorted
        sorted.forEachIndexed { index, item ->
            val from = pending.indexOf(item)
            pending.removeAt(from)
            pending.add(index, item)
            adapter.audios = pending
            adapter.notifyItemMoved(from, index)
        }
    }

    override fun move(from: Int, to: Int) {
        adapter.audios = adapter.audios.swap(from, to)
        adapter.notifyItemMoved(from, to)
    }

    override fun commit() {
        this.sortMode = false
        adapter.notifyItemRangeChanged(0, adapter.audios.size, SortModeFinished)
    }

    override fun revert() {
        sortMode = false
        val pending = ArrayList(adapter.audios)
        original.forEachIndexed { index, item ->
            val from = pending.indexOf(item)
            pending.removeAt(from)
            pending.add(index, item)

            adapter.notifyItemMoved(from, index)
            adapter.notifyItemChanged(from, SortModeFinished)
            adapter.notifyItemChanged(index, SortModeFinished)
        }
    }

    override fun isSortMode(): Boolean {
        return sortMode
    }

}