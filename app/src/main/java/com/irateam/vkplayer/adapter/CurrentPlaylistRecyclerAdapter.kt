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
import com.irateam.vkplayer.player.PlaylistPlayNextEvent
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.HeaderViewHolder
import com.irateam.vkplayer.util.extension.isNullOrEmpty
import com.irateam.vkplayer.util.extension.v
import org.greenrobot.eventbus.Subscribe
import java.util.*

class CurrentPlaylistRecyclerAdapter : BaseAudioRecyclerAdapter<Audio, RecyclerView.ViewHolder> {

    override val searchDelegate: LocalSearchDelegate<Audio> = LocalSearchDelegate(this)
    override var audios: List<Audio> = emptyList()
    override var checkedAudios: HashSet<Audio> = HashSet()

    private var data: ArrayList<Any>

    constructor() {
        this.audios = Player.playlist
        this.data = buildRecyclerData()
    }

    private fun buildRecyclerData(): ArrayList<Any> {
        val data = ArrayList<Any>()

        val playNext = Player.playNext
        if (playNext.isNotEmpty()) {
            data.add(Header("Play next"))
            data.addAll(playNext)
            data.add(Header("Playlist"))
        }

        val playlist = Player.playlist
        data.addAll(playlist)

        return data
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


    private fun configureAudio(holder: AudioViewHolder, audio: Audio) {
        holder.setAudio(audio)

        if (searchDelegate.isSearching) {
            holder.setQuery(searchDelegate.query)
        }

        holder.contentHolder.setOnClickListener {
            Player.play(audios, audio)
        }
    }

    @Subscribe
    fun onPlaylistPlayNextEvent(e: PlaylistPlayNextEvent) {
        val from = e.playNextPosition + 1
        val playNext = data[from] // +1 cause header
        data.removeAt(from)
        val to = e.playNextSize + e.playlistPosition + 2
        data.add(to, playNext) // +2 cause of 2 headers
        notifyItemMoved(from, to)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    companion object {

        val TAG: String = CurrentPlaylistRecyclerAdapter::class.java.name

        private val TYPE_HEADER = 1
        private val TYPE_AUDIO = 2
    }
}