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

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.event.VKAudioAdapterEvent.ItemUncheckedEvent
import com.irateam.vkplayer.adapter.event.VKAudioAdapterEvent.ItemRemovedFromCacheEvent
import com.irateam.vkplayer.event.DownloadFinishedEvent
import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.Header
import com.irateam.vkplayer.models.VKAudio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.ui.ItemTouchHelperAdapter
import com.irateam.vkplayer.ui.SimpleItemTouchHelperCallback
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder.State.*
import com.irateam.vkplayer.ui.viewholder.HeaderViewHolder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author Artem Glugovsky
 */
class VKAudioRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        ItemTouchHelperAdapter {


    private val itemTouchHelper: ItemTouchHelper

    private var sortMode = false
    private var data = ArrayList<Any>()
    private var audios: List<VKAudio> = ArrayList()

    private var searchQuery: String? = null

    var checkedAudios: HashSet<VKAudio> = LinkedHashSet()

    //Listeners
    var checkedListener: CheckedListener? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Header -> TYPE_HEADER
        is Audio -> TYPE_AUDIO
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (position) {
            TYPE_HEADER -> {
                val v = inflater.inflate(R.layout.item_header, parent, false)
                return HeaderViewHolder(v)
            }
            TYPE_AUDIO -> {
                val v = inflater.inflate(R.layout.item_audio, parent, false)
                return AudioViewHolder(v)
            }
            else -> throw IllegalStateException("Illegal view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int,
                                  payload: MutableList<Any>?) = when (holder) {

        is AudioViewHolder -> bindAudioViewHolder(holder, position, payload)
        is HeaderViewHolder -> bindHeaderViewHolder(holder, position, payload)
        else -> throw IllegalStateException("${holder.javaClass} is not supported!")
    }

    private fun bindAudioViewHolder(holder: AudioViewHolder,
                                    position: Int,
                                    payload: MutableList<Any>?) {

        if (payload?.isEmpty() ?: true) {
            val audio = data[position] as VKAudio
            configureAudio(holder, audio)
            configurePlayingState(holder, audio)

            if (sortMode) {
                configureSortMode(holder, audio)

            } else {
                configureCheckedState(holder, audio)
            }
        } else {
            payload?.let {
                val events = it.filterIsInstance<Event>()
                dispatchEvents(holder, position, events)
            }
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder,
                                     position: Int,
                                     payload: MutableList<Any>?) {

        val header = data[position] as Header
        holder.setHeader(header)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onItemMove(from: Int, to: Int): Boolean {
        Collections.swap(data, from, to)
        notifyItemMoved(from, to)
        return true
    }

    override fun onItemDismiss(position: Int) {
        data.remove(position)
        notifyItemRemoved(position)
    }

    private fun dispatchEvents(holder: AudioViewHolder,
                               position: Int,
                               events: Collection<Event>) {
        events.forEach {
            when (it) {
                is DownloadFinishedEvent -> {
                    data[position] = it.audio
                    holder.setCached(cached = true, shouldAnimate = true)
                }
                is ItemRemovedFromCacheEvent -> {
                    holder.setCached(cached = false, shouldAnimate = true)
                }
                is ItemUncheckedEvent -> {
                    holder.setChecked(checked = false, shouldAnimate = true)
                }
            }
        }
    }

    private fun configureAudio(holder: AudioViewHolder, audio: VKAudio) {
        holder.setAudio(audio)
        holder.setCached(audio.isCached)
        val searchQuery = searchQuery
        if (searchQuery != null) holder.setQuery(searchQuery)
        holder.contentHolder.setOnClickListener {
            val queue = data.filterIsInstance<Audio>()
            Player.play(queue, audio)
        }
    }

    private fun configurePlayingState(holder: AudioViewHolder, audio: Audio) {
        val playingAudio = Player.audio
        if (audio.id == playingAudio?.id) {
            val state = when {
                !Player.isReady -> PREPARE
                Player.isReady && Player.isPlaying -> PLAY
                Player.isReady && !Player.isPlaying -> PAUSE
                else -> NONE
            }
            holder.setPlayingState(state)
        }
    }

    private fun configureCheckedState(holder: AudioViewHolder, audio: VKAudio) {
        holder.setChecked(audio in checkedAudios)
        holder.coverHolder.setOnClickListener {
            holder.toggleChecked(true)
            if (holder.isChecked()) checkedAudios.add(audio) else checkedAudios.remove(audio)
            checkedListener?.onChanged(audio, checkedAudios)
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
        checkedAudios.forEach {
            notifyItemChanged(data.indexOf(it), ItemUncheckedEvent)
        }
        checkedAudios.clear()
    }

    fun removeFromCache(audios: Collection<Audio>) {
        audios.forEach {
            val index = data.indexOf(it)
            notifyItemChanged(index, ItemRemovedFromCacheEvent)
        }
    }

    fun removeChecked() {
        val forIterate = ArrayList(checkedAudios)
        forIterate.forEach {
            val index = data.indexOf(it)
            data.removeAt(index)
            checkedAudios.remove(it)
            notifyItemRemoved(index)
        }
    }

    fun setSortMode(enabled: Boolean) {
        sortMode = enabled
        notifyDataSetChanged()
    }

    fun isSortMode(): Boolean {
        return sortMode
    }

    fun setAudios(audios: List<VKAudio>) {
        data.clear()
        data.addAll(audios)
        this.audios = audios
        notifyDataSetChanged()
    }

    fun setSearchQuery(query: String) {
        val lowerQuery = query.toLowerCase()
        searchQuery = query

        val filtered = audios.filter {
            lowerQuery in it.title.toLowerCase() || lowerQuery in it.artist.toLowerCase()
        }

        data.clear()
        data.addAll(filtered)
        notifyDataSetChanged()
    }

    fun setSearchAudios(searchAudios: List<Audio>) {
        data.add(Header("Search result"))
        data.addAll(searchAudios)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFinished(e: DownloadFinishedEvent) {
        val audio = e.audio
        data.filter { it is Audio }
                .map { it as Audio }
                .filter { it.id == audio.id }
                .forEach {
                    val index = data.indexOf(it)
                    notifyItemChanged(index, e)
                }
    }

    private fun notifyEvent(e: PlayerEvent) {
        notifyDataSetChanged()
    }

    interface CheckedListener {

        fun onChanged(audio: VKAudio, checked: HashSet<VKAudio>)
    }

    companion object {

        private val TYPE_HEADER = 1
        private val TYPE_AUDIO = 2
    }
}
