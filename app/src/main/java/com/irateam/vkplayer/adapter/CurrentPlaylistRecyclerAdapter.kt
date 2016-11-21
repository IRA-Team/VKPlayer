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

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent
import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.player.PlaylistChangedEvent
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.util.extension.isNullOrEmpty
import org.greenrobot.eventbus.Subscribe
import java.util.*

class CurrentPlaylistRecyclerAdapter : BaseAudioRecyclerAdapter<Audio, AudioViewHolder> {

    override val searchDelegate: LocalSearchDelegate<Audio> = LocalSearchDelegate(this)
    override var audios: List<Audio> = emptyList()
    override var checkedAudios: HashSet<Audio> = HashSet()

    constructor() {
        this.audios = Player.playlist
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AudioViewHolder(inflater.inflate(R.layout.item_audio, parent, false))
    }

    override fun onBindViewHolder(holder: AudioViewHolder,
                                  position: Int,
                                  payload: MutableList<Any>?) {

        val audio = audios[position]
        if (payload.isNullOrEmpty()) {
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

    private fun dispatchEvents(holder: AudioViewHolder,
                               audio: Audio,
                               events: Collection<Event>) = events.forEach {
        when (it) {
            BaseAudioAdapterEvent.ItemUncheckedEvent -> {
                holder.setChecked(checked = false, shouldAnimate = true)
            }

            BaseAudioAdapterEvent.SortModeStarted -> {
                holder.setSorting(sorting = true, shouldAnimate = true)
                setupDragTouchListener(holder)
            }

            BaseAudioAdapterEvent.SortModeFinished -> {
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
            Player.play(audio)
        }
    }

    @Subscribe
    fun onPlaylistChangedEvent(e: PlaylistChangedEvent) {
        val newAudios = e.playlistAfter

        val pending = ArrayList(audios)
        audios = pending

        newAudios.forEachIndexed { index, item ->
            val from = pending.indexOf(item)
            pending.removeAt(from)
            pending.add(index, item)

            notifyItemMoved(from, index)
        }

        scrollToTop()
    }

    override fun getItemCount(): Int {
        return audios.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.post {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            val countVisible = manager.findFirstVisibleItemPosition() - manager.findLastVisibleItemPosition()
            val probPosition = Player.audioIndex - Math.round(countVisible / 2.0)
            val position = if (probPosition > 0) {
                probPosition
            } else {
                0
            }
            recyclerView.scrollToPosition(position.toInt())
        }
    }

    companion object {

        val TAG: String = CurrentPlaylistRecyclerAdapter::class.java.name

        private val TYPE_HEADER = 1
        private val TYPE_AUDIO = 2
    }
}