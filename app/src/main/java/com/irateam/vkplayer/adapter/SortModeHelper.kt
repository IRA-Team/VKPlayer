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

import android.support.v7.widget.RecyclerView
import com.irateam.vkplayer.adapter.event.ItemSortModeFinished
import com.irateam.vkplayer.adapter.event.ItemSortModeStarted
import java.util.*

class SortModeHelper<T> {

    private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>

    private var sortMode = false
    private var original = ArrayList<T>()
    private var data = ArrayList<T>()

    constructor(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>) {
        this.adapter = adapter
    }

    fun start(data: ArrayList<T>) {
        this.sortMode = true
        this.original = ArrayList(data)
        this.data = data
        adapter.notifyItemRangeChanged(0, data.size, ItemSortModeStarted)
    }

    fun sort(comparator: Comparator<in T>) {
        val sorted = ArrayList(original)
        Collections.sort(sorted, comparator)
        sorted.forEachIndexed { index, item ->
            val from = data.indexOf(item)
            data.removeAt(from)
            data.add(index, item)
            adapter.notifyItemMoved(from, index)
        }
    }

    fun move(from: Int, to: Int) {
        Collections.swap(data, from, to)
        adapter.notifyItemMoved(from, to)
    }

    fun commit() {
        this.sortMode = false
        adapter.notifyItemRangeChanged(0, data.size, ItemSortModeFinished)
    }

    fun revert() {
        sortMode = false
        original.forEachIndexed { index, item ->
            val from = data.indexOf(item)
            data.removeAt(from)
            data.add(index, item)
            adapter.notifyItemMoved(from, index)
            adapter.notifyItemChanged(from, ItemSortModeFinished)
            adapter.notifyItemChanged(index, ItemSortModeFinished)
        }
    }

    fun isSortMode(): Boolean {
        return sortMode
    }
}