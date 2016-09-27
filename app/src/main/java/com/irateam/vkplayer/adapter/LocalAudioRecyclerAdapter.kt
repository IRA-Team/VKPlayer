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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent.*
import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.LocalAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import java.util.*

/**
 * @author Artem Glugovsky
 */
class LocalAudioRecyclerAdapter : BaseAudioRecyclerAdapter<LocalAudio, AudioViewHolder>() {

    override val sortModeDelegate = LocalSortModeDelegate(this)
    override val searchDelegate = LocalSearchDelegate(this)

    var audios: List<LocalAudio> = emptyList()
    override var checkedAudios: HashSet<LocalAudio> = LinkedHashSet()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(v)
    }

    override fun onBindViewHolder(holder: AudioViewHolder,
                                  position: Int,
                                  payload: MutableList<Any>?) {
        val audio = audios[position]
        if (payload?.isEmpty() ?: true) {
            configureAudio(holder, audio)
            configurePlayingState(holder, audio)

            if (isSortMode()) {
                configureSortMode(holder)
            } else {
                configureCheckedState(holder, audio)
            }
        } else {
            payload?.let {
                val events = it.filterIsInstance<Event>()
                dispatchEvents(holder, audio, events)
            }
        }
    }

    override fun getItemCount(): Int {
        return audios.size
    }

    override fun onItemMove(from: Int, to: Int): Boolean {
        sortModeDelegate.move(from, to)
        return true
    }

    override fun onItemDismiss(position: Int) {
        audios -= audios[position]
        notifyItemRemoved(position)
    }

    private fun dispatchEvents(holder: AudioViewHolder,
                               audio: LocalAudio,
                               events: Collection<Event>) = events.forEach {
        when (it) {
            ItemUncheckedEvent -> {
                holder.setChecked(checked = false, shouldAnimate = true)
            }

            SortModeStarted -> {
                holder.setSorting(sorting = true, shouldAnimate = true)
                setupDragTouchListener(holder)
            }

            SortModeFinished -> {
                holder.setSorting(sorting = false, shouldAnimate = true)
                setupCheckedClickListener(holder, audio)
            }
        }
    }

    private fun configureAudio(holder: AudioViewHolder, audio: Audio) {
        holder.setAudio(audio)

        if (searchDelegate.isSearching) {
            holder.setQuery(searchDelegate.query)
        }

        holder.contentHolder.setOnClickListener {
            Player.play(audios, audio)
        }
    }

    override fun clearChecked() {
        checkedAudios.forEach {
            notifyItemChanged(audios.indexOf(it), ItemUncheckedEvent)
        }
        checkedAudios.clear()
    }

    fun removeAll(removed: Collection<Audio>) {
        removed.forEach {
            val index = audios.indexOf(it)
            audios -= audios[index]
            notifyItemRemoved(index)
        }
    }

    override fun removeChecked() {
        val layoutManager = recyclerView?.layoutManager

        checkedAudios.toList().forEach {
            val index = audios.indexOf(it)
            audios -= audios[index]

            layoutManager?.apply {
                val view = findViewByPosition(index)
                removeView(view)
                notifyItemRemoved(index)
            }

        }
        checkedAudios.clear()
    }

    fun setAudios(audios: Collection<LocalAudio>) {
        this.audios = audios.toList()
        notifyDataSetChanged()
    }

    fun addAudio(audio: LocalAudio) {
        audios += audio
        notifyItemInserted(audios.indexOf(audio))
    }

    companion object {

        val TAG = LocalAudioRecyclerAdapter::class.java.name
    }
}