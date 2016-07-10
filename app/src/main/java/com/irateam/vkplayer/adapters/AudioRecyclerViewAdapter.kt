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

package com.irateam.vkplayer.adapters

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.ui.ItemTouchHelperAdapter
import com.irateam.vkplayer.ui.SimpleItemTouchHelperCallback
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder.State.*
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @author Artem Glugovsky
 */
class AudioRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        ItemTouchHelperAdapter {

    private val TYPE_HEADER = 1
    private val TYPE_AUDIO = 2
    private val player = Player.getInstance()

    private var sortMode = false
    private var searchQuery: String? = null

    private val itemTouchHelper: ItemTouchHelper

    private var data = ArrayList<Any>()
    private var audios: List<Audio> = ArrayList()

    var checkedAudios: HashSet<Audio> = LinkedHashSet()

    //Listeners
    var checkedListener: ((audio: Audio, checked: HashSet<Audio>) -> Unit)? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Audio -> TYPE_AUDIO
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            else -> {
                val v = inflater.inflate(R.layout.item_audio, parent, false)
                return AudioViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AudioViewHolder -> {
                val audio = data[position] as Audio

                configureAudio(holder, audio)
                configurePlayingState(holder, audio)
                configureCheckedState(holder, audio)
                configureSortMode(holder, audio)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        data.remove(position)
        notifyItemRemoved(position)
    }

    private fun configureAudio(holder: AudioViewHolder, audio: Audio) {
        holder.setAudio(audio)
        val searchQuery = searchQuery
        if (searchQuery != null) holder.setQuery(searchQuery)
        holder.itemView.setOnClickListener {
            player.queue = audios
            player.play(audios.indexOf(audio))
        }
    }

    private fun configurePlayingState(holder: AudioViewHolder, audio: Audio) {
        val playingAudio = player.playingAudio
        if (audio.id == playingAudio?.id) {
            val state = when {
                !player.isReady -> PREPARE
                player.isReady && player.isPlaying -> PLAY
                player.isReady && !player.isPlaying -> PAUSE
                else -> NONE
            }
            holder.setPlayingState(state)
        }
    }

    private fun configureCheckedState(holder: AudioViewHolder, audio: Audio) {
        holder.setChecked(checkedAudios.contains(audio))
        holder.cover.setOnClickListener {
            holder.toggleChecked()
            if (holder.isChecked()) checkedAudios.add(audio) else checkedAudios.remove(audio)
            checkedListener?.invoke(audio, checkedAudios)
        }
    }

    private fun configureSortMode(holder: AudioViewHolder, audio: Audio) {
        holder.setSorting(sortMode)
        if (sortMode) {
            holder.coverHolder.setOnTouchListener { v, e ->
                if (MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                false
            }
        }
    }

    fun clearChecked() {
        checkedAudios.clear()
        notifyDataSetChanged()
    }

    fun setSortMode(enabled: Boolean) {
        sortMode = enabled
        notifyDataSetChanged()
    }

    fun isSortMode(): Boolean {
        return sortMode
    }

    fun setAudios(audios: List<Audio>) {
        data.clear()
        data.addAll(audios)
        this.audios = audios
        notifyDataSetChanged()
    }

    fun setSearchQuery(query: String) {
        val lowerQuery = query.toLowerCase()
        searchQuery = query

        val filtered = audios.filter {
            it.title.toLowerCase().contains(lowerQuery)
                    || it.artist.toLowerCase().contains(lowerQuery)
        }

        data.clear()
        data.addAll(filtered)
        notifyDataSetChanged()
    }

    fun clearSearchQuery() {
        searchQuery = null
        data.clear()
        data.addAll(audios)
        notifyDataSetChanged()
    }

    @Subscribe
    fun onStartEvent(e: PlayerStartEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onPlayEvent(e: PlayerPlayEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onResumeEvent(e: PlayerResumeEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onPauseEvent(e: PlayerPauseEvent) {
        notifyEvent(e)
    }

    private fun notifyEvent(e: PlayerEvent) {
        notifyDataSetChanged()
    }
}
