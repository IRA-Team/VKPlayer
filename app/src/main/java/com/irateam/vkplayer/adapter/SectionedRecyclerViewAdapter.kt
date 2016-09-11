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
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup
import java.util.*

abstract class SectionedRecyclerViewAdapter : RecyclerView.Adapter<ViewHolder>() {

    protected val sectionMap: Map<String, Section<ViewHolder>> = LinkedHashMap()

    override fun getItemCount(): Int = sectionMap.values
            .map { it.getItemCount() }
            .reduce { count1, count2 -> count1 + count2 }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return getSectionForPosition(position)
                .onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    private fun getSectionForPosition(position: Int): Section<ViewHolder> {
        var index = 0
        val iterator = sectionMap.iterator()
        while (index < position && iterator.hasNext()) {
            val section = iterator.next().value
            val nextIndex = index + section.getRealItemCount()

            if (position >= index && position < nextIndex) {
                return section
            } else {
                index = nextIndex
            }
        }
        throw IllegalArgumentException("No section for such position.")
    }


    interface Section<V : ViewHolder> {

        fun getItemCount(): Int

        fun onCreateViewHolder(parent: ViewGroup): V

        fun onBindViewHolder(holder: V, relative: Int, absolute: Int)

        fun getTitle() = ""

        fun shouldShowTitle() = false
    }

    private fun <V : ViewHolder> Section<V>.getRealItemCount() = if (shouldShowTitle()) {
        getItemCount() + 1
    } else {
        getItemCount()
    }
}