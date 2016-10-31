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
import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent
import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.model.Header
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.player.PlayerPlayEvent
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.HeaderViewHolder
import com.irateam.vkplayer.util.extension.isNullOrEmpty
import com.irateam.vkplayer.util.extension.v
import java.util.*

class CurrentPlaylistRecyclerAdapter : BaseAudioRecyclerAdapter<Audio, RecyclerView.ViewHolder> {

    override val searchDelegate: LocalSearchDelegate<Audio> = LocalSearchDelegate(this)
    override var audios: List<Audio> = emptyList()
    override var checkedAudios: HashSet<Audio> = HashSet()

    private var snapshot: PlaylistSnapshot
    private var data: List<Any>

    constructor() {
        this.snapshot = buildSnapshot()
        this.data = buildRecyclerData(snapshot)
    }

    private fun buildSnapshot(): PlaylistSnapshot {
        return PlaylistSnapshot(
                Player.playlist,
                Player.playNextList)
    }

    private fun buildRecyclerData(snapshot: PlaylistSnapshot): List<Any> {
        val data = ArrayList<Any>()

        val playNext = Player.playNextList
        if (playNext.isNotEmpty()) {
            data.add(Header("Play next"))
            data.addAll(playNext)
            data.add(Header("Playlist"))
        }

        val playlist = Player.playlist
        data.addAll(playlist)

        return data
    }

    private fun invalidateRecycler(before: PlaylistSnapshot, after: PlaylistSnapshot) {
        if (before != after) {

            //Invalidate playNext
            if (before.playNext.isEmpty() && after.playNext.isNotEmpty()) {
                notifyItemRangeInserted(0, after.playNext.size + 2) // +2 are headers
            } else if (before.playNext.isNotEmpty() && after.playNext.isEmpty()) {
                notifyItemRangeRemoved(0, before.playNext.size + 2) // +2 are headers
            }

        } else {
            v(TAG, "Snapshots are equal. No need to invalidate")
        }
    }

    override fun getItemViewType(position: Int) = when (data[position]) {
        is Header -> TYPE_HEADER
        is Audio -> TYPE_AUDIO
        else -> throw IllegalStateException("Unsupported data type")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
            }

            TYPE_AUDIO -> {
                AudioViewHolder(inflater.inflate(R.layout.item_audio, parent, false))
            }

            else -> throw IllegalStateException("Unsupported view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int,
                                  payload: MutableList<Any>?) {
        when (holder) {
            is AudioViewHolder -> {
                val audio = data[position] as Audio
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

            is HeaderViewHolder -> {
                val header = data[position] as Header
                holder.setHeader(header)
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


    override fun onPlayEvent(e: PlayerPlayEvent) {
        val before = snapshot
        val after = buildSnapshot()
        this.snapshot = after
        this.data = buildRecyclerData(after)
        invalidateRecycler(before, after)
    }

    private fun configureAudio(holder: AudioViewHolder, audio: Audio) {
        holder.setAudio(audio)

        if (searchDelegate.isSearching) {
            holder.setQuery(searchDelegate.query)
        }

        holder.contentHolder.setOnClickListener {
            val audios = if (searchDelegate.isSearching) {
                searchDelegate.original
            } else {
                audios
            }
            Player.play(audios, audio)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    data class PlaylistSnapshot(
            val playlist: List<Audio>,
            val playNext: List<Audio>)

    companion object {

        val TAG: String = CurrentPlaylistRecyclerAdapter::class.java.name

        private val TYPE_HEADER = 1
        private val TYPE_AUDIO = 2
    }
}