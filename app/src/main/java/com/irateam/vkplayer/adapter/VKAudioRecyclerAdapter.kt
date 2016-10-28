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
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent.*
import com.irateam.vkplayer.adapter.event.VKAudioAdapterEvent.ItemRemovedFromCacheEvent
import com.irateam.vkplayer.event.DownloadFinishedEvent
import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.model.Header
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.HeaderViewHolder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.properties.Delegates.observable

/**
 * @author Artem Glugovsky
 */
class VKAudioRecyclerAdapter : BaseAudioRecyclerAdapter<VKAudio, RecyclerView.ViewHolder>() {

	override val searchDelegate = VKSearchDelegate(this)

	private var data = ArrayList<Any>()

	override var audios: List<VKAudio> by observable(emptyList()) {
		property, oldValue, newValue ->
		rebuildData()
	}

	var searchAudios: List<VKAudio> by observable(emptyList()) {
		property, oldValue, newValue ->
		rebuildData()
	}

	private fun rebuildData() {
		data = ArrayList<Any>().apply {
			addAll(audios)

			if (searchAudios.isNotEmpty()) {
				add(Header("Search result"))
				addAll(searchAudios)
			}
		}
	}

	override var checkedAudios: HashSet<VKAudio> = LinkedHashSet()

	override fun getItemViewType(position: Int) = when (data[position]) {
		is Header -> TYPE_HEADER
		is Audio  -> TYPE_AUDIO
		else      -> -1
	}

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (position) {
			TYPE_HEADER -> {
				val v = inflater.inflate(R.layout.item_header, parent, false)
				HeaderViewHolder(v)
			}

			TYPE_AUDIO  -> {
				val v = inflater.inflate(R.layout.item_audio, parent, false)
				AudioViewHolder(v)
			}

			else        -> throw IllegalStateException("Illegal view type")
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
								  position: Int,
								  payload: MutableList<Any>?) = when (holder) {

		is AudioViewHolder  -> bindAudioViewHolder(holder, position, payload)
		is HeaderViewHolder -> bindHeaderViewHolder(holder, position, payload)
		else                -> throw IllegalStateException("${holder.javaClass} is not supported!")
	}

	private fun bindAudioViewHolder(holder: AudioViewHolder,
									position: Int,
									payload: MutableList<Any>?) {

		val audio = data[position] as VKAudio
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

	private fun bindHeaderViewHolder(holder: HeaderViewHolder,
									 position: Int,
									 payload: MutableList<Any>?) {

		val header = data[position] as Header
		holder.setHeader(header)
	}

	override fun getItemCount(): Int {
		return data.size
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
							   audio: VKAudio,
							   events: Collection<Event>) {
		events.forEach {
			when (it) {
				is DownloadFinishedEvent  -> {
					audio.cachePath = if (it.audio is VKAudio) {
						it.audio.cachePath
					} else {
						it.audio.source
					}
					holder.setCached(cached = true, shouldAnimate = true)
				}

				ItemRemovedFromCacheEvent -> {
					holder.setCached(cached = false, shouldAnimate = true)
				}

				ItemUncheckedEvent        -> {
					holder.setChecked(checked = false, shouldAnimate = true)
				}

				SortModeStarted           -> {
					holder.setSorting(sorting = true, shouldAnimate = true)
					setupDragTouchListener(holder)
				}

				SortModeFinished          -> {
					holder.setSorting(sorting = false, shouldAnimate = true)
					setupCheckedClickListener(holder, audio)
				}
			}
		}
	}

	private fun configureAudio(holder: AudioViewHolder, audio: VKAudio) {
		holder.setAudio(audio)
		holder.setCached(audio.isCached)

		if (searchDelegate.isSearching) {
			holder.setQuery(searchDelegate.query)
		}

		holder.contentHolder.setOnClickListener {
			val queue = data.filterIsInstance<Audio>()
			Player.play(queue, audio)
		}
	}

	override fun clearChecked() {
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

	override fun removeChecked() {
		val forIterate = ArrayList(checkedAudios)
		forIterate.forEach {
			val index = data.indexOf(it)
			data.removeAt(index)
			checkedAudios.remove(it)
			notifyItemRemoved(index)
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onDownloadFinished(e: DownloadFinishedEvent) {
		val audio = e.audio
		data.filterIsInstance<Audio>()
				.filter { it.id == audio.id }
				.forEach { notifyItemChanged(data.indexOf(it), e) }
	}

	companion object {

		private val TYPE_HEADER = 1
		private val TYPE_AUDIO = 2
	}
}
